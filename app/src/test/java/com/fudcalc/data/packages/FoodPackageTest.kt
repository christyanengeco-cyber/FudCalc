package com.fudcalc.data.packages

import com.fudcalc.domain.nutrition.NutrientValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodPackageTest {

    @Test
    fun `le cabecalho itens e porcoes`() {
        val pkg = FoodPackage.parse(testPackage("v1", listOf(testRow(), testRow(id = "taco:feijao", name = "Feijão"))))
        assertEquals("taco", pkg.id)
        assertEquals("v1", pkg.version)
        assertEquals(2, pkg.items.size)
        assertEquals(5.0, (pkg.items[0].nutrients.proteinG as NutrientValue.Value).amount, 0.0)
        assertEquals("unidade", pkg.items[0].portions[0].label)
        assertEquals(50.0, pkg.items[0].portions[0].grams, 0.0)
    }

    @Test
    fun `campo vazio e desconhecido e zero e zero`() {
        val item = FoodPackage.parse(testPackage("v1", listOf(testRow(energy = "0")))).items[0]
        assertEquals(NutrientValue.Unknown, item.nutrients.sugarsG)
        assertEquals(0.0, (item.nutrients.energyKcal as NutrientValue.Value).amount, 0.0)
    }

    @Test
    fun `nao informado preservado`() {
        val row = testRow().replace(";5;;;1;", ";5;;ni;1;")
        val item = FoodPackage.parse(testPackage("v1", listOf(row))).items[0]
        assertEquals(NutrientValue.NotInformed, item.nutrients.sugarsG)
    }

    @Test
    fun `aceita quebras de linha CRLF`() {
        val text = testPackage("v1", listOf(testRow())).replace("\n", "\r\n")
        assertEquals(1, FoodPackage.parse(text).items.size)
    }

    @Test
    fun `checksum divergente recusa o pacote`() {
        val text = testPackage("v1", listOf(testRow())).replace("Arroz", "Arrooz")
        assertRejected(text, "Checksum")
    }

    @Test
    fun `formato desconhecido e recusado`() {
        assertRejected("qualquer coisa", "Formato")
    }

    @Test
    fun `campo faltando indica a linha`() {
        val text = testPackage("v1", listOf(testRow(), "taco:quebrado;Queijo"))
        assertRejected(text, "Linha")
    }

    @Test
    fun `id repetido e recusado`() {
        assertRejected(testPackage("v1", listOf(testRow(), testRow())), "repetido")
    }

    @Test
    fun `valor negativo e recusado`() {
        assertRejected(testPackage("v1", listOf(testRow(energy = "-1"))), "negativo")
    }

    @Test
    fun `energia obrigatoria`() {
        assertRejected(testPackage("v1", listOf(testRow(energy = ""))), "energia")
    }

    @Test
    fun `porcao invalida e recusada`() {
        assertRejected(testPackage("v1", listOf(testRow(portions = "unidade=0"))), "porção")
    }

    @Test
    fun `pacote sem alimentos e recusado`() {
        assertRejected(testPackage("v1", listOf("# só comentário")), "sem alimentos")
    }

    private fun assertRejected(text: String, expectedInMessage: String) {
        val error = runCatching { FoodPackage.parse(text) }.exceptionOrNull()
        assertTrue("esperava PackageFormatException, veio $error", error is PackageFormatException)
        assertTrue(error!!.message.orEmpty().contains(expectedInMessage))
    }
}
