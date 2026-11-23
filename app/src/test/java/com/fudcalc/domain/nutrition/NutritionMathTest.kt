package com.fudcalc.domain.nutrition

import com.fudcalc.domain.food.Portion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NutritionMathTest {

    private val rice = NutritionFacts(
        energyKcal = NutrientValue.Value(130.0),
        proteinG = NutrientValue.Value(2.5),
        carbsG = NutrientValue.Value(28.1),
        fatG = NutrientValue.Value(0.2),
        fiberG = NutrientValue.Value(1.6),
    )

    @Test
    fun `150 g de 130 kcal por 100 g da 195 kcal`() {
        val portion = factsForGrams(rice, 150.0)
        assertEquals(195.0, portion.energyKcal.numberOrNull()!!, 1e-9)
        assertEquals(3.75, portion.proteinG.numberOrNull()!!, 1e-9)
    }

    @Test
    fun `zero real permanece zero e nao vira desconhecido`() {
        val scaled = NutrientValue.Value(0.0).scaledBy(1.5)
        assertEquals(0.0, scaled.numberOrNull()!!, 0.0)
        assertNull(NutrientValue.Unknown.numberOrNull())
        assertNull(NutrientValue.NotInformed.numberOrNull())
    }

    @Test
    fun `valor desconhecido continua desconhecido ao escalar`() {
        val facts = NutritionFacts(energyKcal = NutrientValue.Value(100.0), proteinG = NutrientValue.Unknown)
        val scaled = factsForGrams(facts, 200.0)
        assertEquals(200.0, scaled.energyKcal.numberOrNull()!!, 1e-9)
        assertEquals(NutrientValue.Unknown, scaled.proteinG)
    }

    @Test
    fun `escala de valor medido passa a ser calculada`() {
        val scaled = NutrientValue.Value(10.0).scaledBy(2.0)
        assertEquals(Origin.Calculated, (scaled as NutrientValue.Value).origin)
        assertEquals(NutrientValue.Value(10.0, Origin.Estimated).scaledBy(2.0), NutrientValue.Value(20.0, Origin.Estimated))
    }

    @Test
    fun `soma de nutrientes conhecidos e calculada`() {
        val total = listOf(factsForGrams(rice, 100.0), factsForGrams(rice, 200.0)).sumFacts()
        assertEquals(390.0, total.energyKcal.numberOrNull()!!, 1e-9)
        assertEquals(Origin.Calculated, (total.energyKcal as NutrientValue.Value).origin)
    }

    @Test
    fun `soma com dado desconhecido resulta desconhecida`() {
        val withUnknown = NutritionFacts(energyKcal = NutrientValue.Value(50.0), fatG = NutrientValue.Unknown)
        val total = listOf(NutritionFacts(energyKcal = NutrientValue.Value(10.0), fatG = NutrientValue.Value(1.0)), withUnknown).sumFacts()
        assertEquals(60.0, total.energyKcal.numberOrNull()!!, 1e-9)
        assertEquals(NutrientValue.Unknown, total.fatG)
    }

    @Test
    fun `soma vazia nao inventa zero`() {
        assertEquals(NutrientValue.Unknown, emptyList<NutritionFacts>().sumFacts().energyKcal)
    }

    @Test
    fun `arredondamento usa meio para cima`() {
        assertEquals(0.1, 0.05.roundTo(1), 0.0)
        assertEquals(2.35, 2.345.roundTo(2), 0.0)
        assertEquals(1.0, 0.95.roundTo(1), 0.0)
    }

    @Test
    fun `medida caseira converte para gramas antes de calcular`() {
        val unity = Portion(label = "unidade", grams = 50.0)
        val facts = factsForPortion(rice, unity.grams, measures = 2.0)
        assertEquals(130.0, facts.energyKcal.numberOrNull()!!, 1e-9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `quantidade nao positiva e rejeitada`() {
        factsForGrams(rice, 0.0)
    }
}
