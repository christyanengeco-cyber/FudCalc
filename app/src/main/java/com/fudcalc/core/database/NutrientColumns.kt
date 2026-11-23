package com.fudcalc.core.database

import androidx.room.TypeConverter
import com.fudcalc.domain.nutrition.NutrientValue
import com.fudcalc.domain.nutrition.NutritionFacts
import com.fudcalc.domain.nutrition.Origin

/**
 * Colunas nutricionais compartilhadas por alimentos e registros do diário.
 * Valores ausentes são preservados como tal (Unknown / NotInformed), nunca como zero.
 */
data class NutrientColumns(
    val energyKcal: NutrientValue = NutrientValue.Unknown,
    val proteinG: NutrientValue = NutrientValue.Unknown,
    val carbsG: NutrientValue = NutrientValue.Unknown,
    val sugarsG: NutrientValue = NutrientValue.Unknown,
    val fatG: NutrientValue = NutrientValue.Unknown,
    val saturatedFatG: NutrientValue = NutrientValue.Unknown,
    val transFatG: NutrientValue = NutrientValue.Unknown,
    val fiberG: NutrientValue = NutrientValue.Unknown,
    val sodiumMg: NutrientValue = NutrientValue.Unknown,
)

fun NutritionFacts.toColumns(): NutrientColumns = NutrientColumns(
    energyKcal = energyKcal,
    proteinG = proteinG,
    carbsG = carbsG,
    sugarsG = sugarsG,
    fatG = fatG,
    saturatedFatG = saturatedFatG,
    transFatG = transFatG,
    fiberG = fiberG,
    sodiumMg = sodiumMg,
)

fun NutrientColumns.toFacts(): NutritionFacts = NutritionFacts(
    energyKcal = energyKcal,
    proteinG = proteinG,
    carbsG = carbsG,
    sugarsG = sugarsG,
    fatG = fatG,
    saturatedFatG = saturatedFatG,
    transFatG = transFatG,
    fiberG = fiberG,
    sodiumMg = sodiumMg,
)

class NutrientValueConverter {
    @TypeConverter
    fun toText(value: NutrientValue): String = when (value) {
        is NutrientValue.Value -> when (value.origin) {
            Origin.Measured -> "m:${value.amount}"
            Origin.Estimated -> "e:${value.amount}"
            Origin.Calculated -> "c:${value.amount}"
        }
        NutrientValue.Unknown -> "u"
        NutrientValue.NotInformed -> "ni"
    }

    @TypeConverter
    fun fromText(raw: String): NutrientValue {
        if (raw == "u") return NutrientValue.Unknown
        if (raw == "ni") return NutrientValue.NotInformed
        val sep = raw.indexOf(':')
        require(sep > 0) { "Valor nutricional inválido: $raw" }
        val origin = when (raw.substring(0, sep)) {
            "m" -> Origin.Measured
            "e" -> Origin.Estimated
            "c" -> Origin.Calculated
            else -> error("Origem nutricional inválida: $raw")
        }
        return NutrientValue.Value(raw.substring(sep + 1).toDouble(), origin)
    }
}
