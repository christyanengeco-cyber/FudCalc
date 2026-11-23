package com.fudcalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fudcalc.domain.meals.DiaryEntry
import com.fudcalc.domain.meals.MealType
import com.fudcalc.domain.nutrition.sumFacts
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, dd 'de' MMMM", Locale.forLanguageTag("pt-BR"))

@Composable
fun DiaryScreen(
    date: LocalDate,
    entries: List<DiaryEntry>,
    isToday: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onDelete: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val totals = remember(entries) { entries.map { it.facts }.sumFacts() }
    var pendingDelete by remember { mutableStateOf<DiaryEntry?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPreviousDay) { Text("◀") }
            Text(
                text = date.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            TextButton(onClick = onNextDay) { Text("▶") }
            if (!isToday) TextButton(onClick = onToday) { Text("Hoje") }
        }

        TotalsCard(totals, Modifier.padding(horizontal = 16.dp))

        Text(
            text = "Valores calculados a partir dos dados da fonte; não são medições.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MealType.entries.forEach { meal ->
                val mealEntries = entries.filter { it.meal == meal }
                item(key = "header-${meal.name}") {
                    MealHeader(meal, mealEntries)
                }
                if (mealEntries.isEmpty()) {
                    item(key = "empty-${meal.name}") {
                        Text(
                            "Nada registrado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }
                items(mealEntries, key = { it.id }) { entry ->
                    EntryRow(entry = entry, onDelete = { pendingDelete = entry })
                }
            }
        }

        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Text("Adicionar alimento")
        }
    }

    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Excluir registro?") },
            text = { Text("${entry.foodName} — ${entry.grams.format(1, "g")}") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(entry.id)
                    pendingDelete = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun TotalsCard(totals: com.fudcalc.domain.nutrition.NutritionFacts, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Energia", style = MaterialTheme.typography.labelMedium)
            Text(
                totals.energyKcal.format(0, "kcal"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Macro("Proteínas", totals.proteinG.format(1, "g"), Modifier.weight(1f))
                Macro("Carboidratos", totals.carbsG.format(1, "g"), Modifier.weight(1f))
                Macro("Gorduras", totals.fatG.format(1, "g"), Modifier.weight(1f))
            }
            Text(
                "Fibras ${totals.fiberG.format(1, "g")} · Sódio ${totals.sodiumMg.format(0, "mg")}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun Macro(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MealHeader(meal: MealType, entries: List<DiaryEntry>) {
    val subtotal = remember(entries) { entries.map { it.facts }.sumFacts() }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            meal.label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (entries.isNotEmpty()) {
            Text(
                subtotal.energyKcal.format(0, "kcal"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EntryRow(entry: DiaryEntry, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(entry.foodName, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${entry.grams.format(1, "g")} · ${entry.facts.energyKcal.format(0, "kcal")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir ${entry.foodName}")
            }
        }
    }
}
