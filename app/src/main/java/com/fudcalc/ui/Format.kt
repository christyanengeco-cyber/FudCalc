package com.fudcalc.ui

import com.fudcalc.domain.nutrition.NutrientValue
import com.fudcalc.domain.nutrition.roundTo
import java.util.Locale

fun Double.format(decimals: Int = 1, unit: String = ""): String {
    val rounded = roundTo(decimals)
    val text = if (decimals == 0) {
        rounded.toLong().toString()
    } else {
        String.format(Locale.ROOT, "%.${decimals}f", rounded).trimEnd('0').trimEnd('.')
    }
    return if (unit.isEmpty()) text else "$text $unit"
}

/** "—" = fonte sem o dado; "n/i" = campo vazio na fonte; nunca zero no lugar de ambos. */
fun NutrientValue.format(decimals: Int = 1, unit: String = ""): String = when (this) {
    is NutrientValue.Value -> amount.format(decimals, unit)
    NutrientValue.Unknown -> "—"
    NutrientValue.NotInformed -> "n/i"
}
