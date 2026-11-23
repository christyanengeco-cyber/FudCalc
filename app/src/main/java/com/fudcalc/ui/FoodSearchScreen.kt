package com.fudcalc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fudcalc.domain.food.Food
import com.fudcalc.domain.meals.MealType
import com.fudcalc.domain.nutrition.factsForGrams
import java.util.Locale

@Composable
fun FoodSearchScreen(
    query: String,
    results: List<Food>,
    onQueryChange: (String) -> Unit,
    onAdd: (Food, Double, MealType) -> Unit,
) {
    var selected by remember { mutableStateOf<Food?>(null) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Buscar alimento") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        if (results.isEmpty()) {
            Text(
                text = if (query.isBlank()) {
                    "Digite para buscar na base local. Funciona sem internet."
                } else {
                    "Nenhum alimento encontrado."
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 24.dp),
            )
        }

        LazyColumn(Modifier.weight(1f)) {
            items(results, key = { it.id }) { food ->
                ListItem(
                    headlineContent = { Text(food.displayName) },
                    supportingContent = {
                        Text(
                            "${food.facts.energyKcal.format(0, "kcal")} / ${food.referenceGrams.format(0, "g")} · ${food.source}",
                        )
                    },
                    modifier = Modifier.clickable { selected = food },
                )
                HorizontalDivider()
            }
        }
    }

    selected?.let { food ->
        AddFoodDialog(
            food = food,
            onDismiss = { selected = null },
            onConfirm = { grams, meal ->
                onAdd(food, grams, meal)
                selected = null
            },
        )
    }
}

@Composable
private fun AddFoodDialog(
    food: Food,
    onDismiss: () -> Unit,
    onConfirm: (Double, MealType) -> Unit,
) {
    var gramsText by remember { mutableStateOf("100") }
    var meal by remember { mutableStateOf(MealType.LUNCH) }
    val grams = gramsText.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }
    val preview = grams?.let { factsForGrams(food.facts, it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.displayName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${food.source}${food.sourceVersion?.let { " · $it" }.orEmpty()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = gramsText,
                    onValueChange = { gramsText = it },
                    label = { Text("Quantidade (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = grams == null,
                )

                if (food.portions.isNotEmpty()) {
                    Text(
                        "Medidas caseiras aproximadas",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        food.portions.forEach { portion ->
                            AssistChip(
                                onClick = { gramsText = formatGrams(portion.grams) },
                                label = { Text("${portion.label} (${formatGrams(portion.grams)} g)") },
                            )
                        }
                    }
                }

                Text("Refeição", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealType.entries.take(2).forEach { option ->
                        FilterChip(
                            selected = meal == option,
                            onClick = { meal = option },
                            label = { Text(option.shortLabel) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealType.entries.drop(2).forEach { option ->
                        FilterChip(
                            selected = meal == option,
                            onClick = { meal = option },
                            label = { Text(option.shortLabel) },
                        )
                    }
                }

                if (preview != null) {
                    Text(
                        "${preview.energyKcal.format(0, "kcal")} · P ${preview.proteinG.format(1, "g")} · " +
                            "C ${preview.carbsG.format(1, "g")} · G ${preview.fatG.format(1, "g")}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { grams?.let { onConfirm(it, meal) } },
                enabled = grams != null,
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

private fun formatGrams(grams: Double): String =
    if (grams % 1.0 == 0.0) grams.toLong().toString()
    else String.format(Locale.ROOT, "%.1f", grams)
