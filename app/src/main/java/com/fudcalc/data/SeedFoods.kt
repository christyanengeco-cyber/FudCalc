package com.fudcalc.data

import com.fudcalc.core.database.FoodEntity
import com.fudcalc.core.database.FoodPortionEntity
import com.fudcalc.core.database.NutrientColumns
import com.fudcalc.domain.nutrition.NutrientValue

/**
 * Semente mínima da Fase 1, com poucos itens da TACO 4ª ed. para o app ser usável offline.
 * O pacote TACO/TBCA completo e verificado entra na Fase 2. Valores por 100 g do alimento
 * no estado de preparo indicado; `null` significa que a fonte não foi consultada para o campo.
 */
object SeedFoods {

    private const val SOURCE = "TACO"
    private const val VERSION = "4ª ed. (2011), subconjunto de semente"

    data class SeededFood(val food: FoodEntity, val portions: List<FoodPortionEntity>)

    val foods: List<SeededFood> = listOf(
        seed("taco:arroz-branco-cozido", "Arroz, branco", "cozido", 128.0, 2.5, 28.1, 0.2, 1.6, portions = listOf("colher de sopa" to 25.0)),
        seed("taco:feijao-carioca-cozido", "Feijão, carioca", "cozido", 76.0, 4.8, 13.6, 0.5, 8.5, portions = listOf("concha" to 100.0)),
        seed("taco:frango-peito-grelhado", "Frango, peito, sem pele", "grelhado", 159.0, 32.0, 0.0, 2.5, 0.0, portions = listOf("filé" to 100.0)),
        seed("taco:ovo-cozido", "Ovo, de galinha", "cozido", 146.0, 13.3, 0.6, 9.5, 0.0, portions = listOf("unidade" to 50.0)),
        seed("taco:banana-prata", "Banana, prata", "crua", 98.0, 1.3, 26.0, 0.1, 2.0, portions = listOf("unidade" to 70.0)),
        seed("taco:pao-frances", "Pão, francês", null, 300.0, 8.0, 58.6, 3.1, 2.3, portions = listOf("unidade" to 50.0)),
        seed("taco:leite-integral", "Leite, de vaca, integral", null, 61.0, 2.9, 4.3, 3.2, 0.0, portions = listOf("copo" to 200.0)),
        seed("taco:batata-inglesa-cozida", "Batata, inglesa", "cozida", 52.0, 1.2, 11.9, 0.0, 1.3),
        seed("taco:azeite-oliva", "Azeite, de oliva, extra virgem", null, 884.0, 0.0, 0.0, 100.0, 0.0, portions = listOf("colher de sopa" to 13.0)),
        seed("taco:tomate-cru", "Tomate, cru", null, 15.0, 1.1, 3.1, 0.2, 1.2),
        seed("taco:acucar-cristal", "Açúcar, cristal", null, 387.0, 0.3, 99.9, 0.0, 0.0),
    )

    private fun seed(
        id: String,
        name: String,
        preparation: String?,
        kcal: Double?,
        protein: Double?,
        carbs: Double?,
        fat: Double?,
        fiber: Double?,
        sodium: Double? = null,
        portions: List<Pair<String, Double>> = emptyList(),
    ): SeededFood {
        val food = FoodEntity(
            id = id,
            name = name,
            searchName = FoodRepository.normalizeForSearch("$name ${preparation.orEmpty()}"),
            preparation = preparation,
            source = SOURCE,
            sourceVersion = VERSION,
            referenceGrams = 100.0,
            nutrients = NutrientColumns(
                energyKcal = value(kcal),
                proteinG = value(protein),
                carbsG = value(carbs),
                fatG = value(fat),
                fiberG = value(fiber),
                sodiumMg = value(sodium),
            ),
            updatedAt = null,
        )
        return SeededFood(
            food = food,
            portions = portions.map { (label, grams) ->
                FoodPortionEntity(foodId = id, label = label, grams = grams)
            },
        )
    }

    private fun value(amount: Double?): NutrientValue =
        if (amount == null) NutrientValue.Unknown else NutrientValue.Value(amount)
}
