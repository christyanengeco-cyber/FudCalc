package com.fudcalc.domain.meals

import com.fudcalc.domain.nutrition.NutritionFacts
import java.time.LocalDate

enum class MealType(val label: String, val shortLabel: String) {
    BREAKFAST("Café da manhã", "Café"),
    LUNCH("Almoço", "Almoço"),
    DINNER("Jantar", "Jantar"),
    SNACK("Lanches", "Lanches"),
}

/**
 * Registro do diário. [facts] é o snapshot calculado (porção já escalada), preservado no momento
 * do registro para que atualizações futuras das bases não reescrevam o histórico.
 */
data class DiaryEntry(
    val id: String,
    val date: LocalDate,
    val meal: MealType,
    val foodName: String,
    val grams: Double,
    val facts: NutritionFacts,
)
