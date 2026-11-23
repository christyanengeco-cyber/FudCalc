package com.fudcalc.domain.nutrition

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Valor de um nutriente. Ausência de dado nunca é convertida em zero.
 *
 * - [Value] com [Origin.Measured]: valor medido/declarado pela fonte (zero real é Value(0.0)).
 * - [Value] com [Origin.Estimated]: estimativa (ex.: análise por fotografia).
 * - [Value] com [Origin.Calculated]: derivado por escala ou soma (ex.: porção de 150 g).
 * - [Unknown]: a fonte não possui o dado.
 * - [NotInformed]: o campo existe na fonte, mas está vazio.
 */
sealed interface NutrientValue {
    data class Value(val amount: Double, val origin: Origin = Origin.Measured) : NutrientValue

    data object Unknown : NutrientValue

    data object NotInformed : NutrientValue
}

enum class Origin { Measured, Estimated, Calculated }

fun NutrientValue.numberOrNull(): Double? = when (this) {
    is NutrientValue.Value -> amount
    NutrientValue.Unknown, NutrientValue.NotInformed -> null
}

/** Escala proporcional (ex.: 150 g de valores por 100 g). Nunca inventa valor para dado ausente. */
fun NutrientValue.scaledBy(factor: Double): NutrientValue = when (this) {
    is NutrientValue.Value -> when (origin) {
        Origin.Measured -> NutrientValue.Value(amount * factor, Origin.Calculated)
        else -> copy(amount = amount * factor)
    }
    NutrientValue.Unknown, NutrientValue.NotInformed -> this
}

fun Double.roundTo(decimals: Int): Double =
    BigDecimal.valueOf(this).setScale(decimals, RoundingMode.HALF_UP).toDouble()
