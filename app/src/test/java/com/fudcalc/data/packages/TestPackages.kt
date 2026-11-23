package com.fudcalc.data.packages

/** Monta pacotes de teste com checksum correto. */
internal fun testPackage(
    version: String,
    rows: List<String>,
    packageId: String = "taco",
): String {
    val body = rows.joinToString("\n") + "\n"
    val header = listOf(
        "fudcalc-package 1",
        "id=$packageId",
        "name=Tabela de teste",
        "version=$version",
        "source=Fonte de teste",
        "license=MIT",
        "sha256=${sha256(body)}",
        "---",
    )
    return header.joinToString("\n") + "\n" + body
}

/** Linha válida: energia 100 kcal, proteína 5 g, fibra 2 g, porção unidade=50 g. */
internal fun testRow(
    id: String = "taco:arroz",
    name: String = "Arroz",
    energy: String = "100",
    portions: String = "unidade=50",
): String = "$id;$name;cozido;$energy;5;;;1;;;2;;$portions"
