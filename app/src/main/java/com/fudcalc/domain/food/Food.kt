package com.fudcalc.domain.food

import com.fudcalc.domain.nutrition.NutritionFacts

/** Alimento ou produto com proveniência explícita. [referenceGrams] é a base dos [facts]. */
data class Food(
    val id: String,
    val name: String,
    val preparation: String?,
    val source: String,
    val sourceVersion: String?,
    val referenceGrams: Double,
    val facts: NutritionFacts,
    val portions: List<Portion> = emptyList(),
) {
    val displayName: String
        get() = if (preparation.isNullOrBlank()) name else "$name, $preparation"
}

/** Medida caseira aproximada. Os gramas variam por utensílio e modo de servir. */
data class Portion(
    val label: String,
    val grams: Double,
)
