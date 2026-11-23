package com.fudcalc.domain.nutrition

/** Valores nutricionais por uma quantidade de referência (normalmente 100 g). */
data class NutritionFacts(
    val energyKcal: NutrientValue = NutrientValue.Unknown,
    val proteinG: NutrientValue = NutrientValue.Unknown,
    val carbsG: NutrientValue = NutrientValue.Unknown,
    val sugarsG: NutrientValue = NutrientValue.Unknown,
    val fatG: NutrientValue = NutrientValue.Unknown,
    val saturatedFatG: NutrientValue = NutrientValue.Unknown,
    val transFatG: NutrientValue = NutrientValue.Unknown,
    val fiberG: NutrientValue = NutrientValue.Unknown,
    val sodiumMg: NutrientValue = NutrientValue.Unknown,
) {
    fun scaledBy(factor: Double): NutritionFacts = NutritionFacts(
        energyKcal = energyKcal.scaledBy(factor),
        proteinG = proteinG.scaledBy(factor),
        carbsG = carbsG.scaledBy(factor),
        sugarsG = sugarsG.scaledBy(factor),
        fatG = fatG.scaledBy(factor),
        saturatedFatG = saturatedFatG.scaledBy(factor),
        transFatG = transFatG.scaledBy(factor),
        fiberG = fiberG.scaledBy(factor),
        sodiumMg = sodiumMg.scaledBy(factor),
    )
}

/**
 * Soma de valores. Se qualquer parcela for desconhecida, o total é desconhecido:
 * um total parcial apresentado como total seria informação enganosa.
 */
fun Iterable<NutritionFacts>.sumFacts(): NutritionFacts {
    val list = toList()
    if (list.isEmpty()) return NutritionFacts()
    return NutritionFacts(
        energyKcal = list.map { it.energyKcal }.sumNutrient(),
        proteinG = list.map { it.proteinG }.sumNutrient(),
        carbsG = list.map { it.carbsG }.sumNutrient(),
        sugarsG = list.map { it.sugarsG }.sumNutrient(),
        fatG = list.map { it.fatG }.sumNutrient(),
        saturatedFatG = list.map { it.saturatedFatG }.sumNutrient(),
        transFatG = list.map { it.transFatG }.sumNutrient(),
        fiberG = list.map { it.fiberG }.sumNutrient(),
        sodiumMg = list.map { it.sodiumMg }.sumNutrient(),
    )
}

private fun List<NutrientValue>.sumNutrient(): NutrientValue {
    if (any { it.numberOrNull() == null }) return NutrientValue.Unknown
    val total = sumOf { it.numberOrNull()!! }
    return NutrientValue.Value(total, Origin.Calculated)
}

/** Fatos de uma porção em gramas a partir de valores por 100 g. */
fun factsForGrams(per100: NutritionFacts, grams: Double): NutritionFacts {
    require(grams > 0) { "A quantidade em gramas deve ser positiva." }
    return per100.scaledBy(grams / 100.0)
}

/** Fatos de uma porção descrita por medida caseira, a partir dos gramas da medida. */
fun factsForPortion(per100: NutritionFacts, gramsPerMeasure: Double, measures: Double = 1.0): NutritionFacts {
    require(measures > 0) { "A quantidade de medidas deve ser positiva." }
    return factsForGrams(per100, gramsPerMeasure * measures)
}
