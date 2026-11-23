package com.fudcalc.data.packages

import com.fudcalc.core.database.NutrientColumns
import com.fudcalc.domain.food.Portion
import com.fudcalc.domain.nutrition.NutrientValue
import java.security.MessageDigest

class PackageFormatException(message: String) : Exception(message)

/**
 * Pacote nutricional versionado e verificável.
 *
 * Formato em texto: cabeçalho `chave=valor` até a linha `---`, depois uma linha por alimento,
 * com 13 campos separados por `;` (id;nome;preparo;energia;proteína;carboidrato;açúcar;
 * gordura;gordura sat.;gordura trans;fibra;sódio;porções). Campo vazio = desconhecido,
 * `ni` = não informado pela fonte. Porções no formato `rótulo=gramas|rótulo=gramas`.
 * `sha256` cobre o corpo (tudo após `---`) com quebras de linha normalizadas.
 */
data class FoodPackage(
    val id: String,
    val name: String,
    val version: String,
    val source: String,
    val license: String,
    val checksum: String,
    val items: List<PackageItem>,
) {
    companion object {
        private const val MAGIC = "fudcalc-package 1"
        private const val SEPARATOR = "---"
        private const val COLUMNS = 13
        private val REQUIRED_HEADER = listOf("id", "name", "version", "source", "license", "sha256")

        fun parse(raw: String): FoodPackage {
            val lines = raw.replace("\r\n", "\n").lines()
            if (lines.firstOrNull() != MAGIC) {
                throw PackageFormatException("Formato não reconhecido: esperava \"$MAGIC\" na primeira linha.")
            }
            val separator = lines.indexOf(SEPARATOR)
            if (separator < 0) throw PackageFormatException("Cabeçalho sem a linha \"$SEPARATOR\".")

            val header = HashMap<String, String>()
            for (line in lines.take(separator)) {
                if (line.isBlank() || line == MAGIC || line.startsWith("#")) continue
                val eq = line.indexOf('=')
                if (eq <= 0) throw PackageFormatException("Cabeçalho inválido: \"$line\".")
                header[line.substring(0, eq).trim()] = line.substring(eq + 1).trim()
            }
            val missing = REQUIRED_HEADER.filter { header[it].isNullOrBlank() }
            if (missing.isNotEmpty()) {
                throw PackageFormatException("Cabeçalho sem: ${missing.joinToString(", ")}.")
            }
            val packageId = header.getValue("id")
            val body = lines.drop(separator + 1).joinToString("\n")
            if (!sha256(body).equals(header.getValue("sha256"), ignoreCase = true)) {
                throw PackageFormatException("Checksum não confere com o conteúdo do pacote.")
            }

            val items = ArrayList<PackageItem>()
            val seen = HashSet<String>()
            for ((offset, line) in lines.drop(separator + 1).withIndex()) {
                if (line.isBlank() || line.startsWith("#")) continue
                val lineNumber = separator + offset + 2
                val cells = line.split(";")
                if (cells.size != COLUMNS) {
                    throw PackageFormatException("Linha $lineNumber: esperava $COLUMNS campos, achou ${cells.size}.")
                }
                val id = cells[0].trim()
                if (!id.startsWith("$packageId:")) {
                    throw PackageFormatException("Linha $lineNumber: id \"$id\" deve começar com \"$packageId:\".")
                }
                if (!seen.add(id)) throw PackageFormatException("Linha $lineNumber: id repetido \"$id\".")
                val name = cells[1].trim()
                if (name.isEmpty()) throw PackageFormatException("Linha $lineNumber: nome vazio.")
                val energy = nutrient(cells[3], lineNumber, "energia")
                if (energy !is NutrientValue.Value) {
                    throw PackageFormatException("Linha $lineNumber: energia é obrigatória.")
                }
                items += PackageItem(
                    id = id,
                    name = name,
                    preparation = cells[2].trim().ifEmpty { null },
                    nutrients = NutrientColumns(
                        energyKcal = energy,
                        proteinG = nutrient(cells[4], lineNumber, "proteína"),
                        carbsG = nutrient(cells[5], lineNumber, "carboidrato"),
                        sugarsG = nutrient(cells[6], lineNumber, "açúcar"),
                        fatG = nutrient(cells[7], lineNumber, "gordura"),
                        saturatedFatG = nutrient(cells[8], lineNumber, "gordura saturada"),
                        transFatG = nutrient(cells[9], lineNumber, "gordura trans"),
                        fiberG = nutrient(cells[10], lineNumber, "fibra"),
                        sodiumMg = nutrient(cells[11], lineNumber, "sódio"),
                    ),
                    portions = portions(cells[12], lineNumber),
                )
            }
            if (items.isEmpty()) throw PackageFormatException("Pacote sem alimentos.")

            return FoodPackage(
                id = packageId,
                name = header.getValue("name"),
                version = header.getValue("version"),
                source = header.getValue("source"),
                license = header.getValue("license"),
                checksum = header.getValue("sha256").lowercase(),
                items = items,
            )
        }

        private fun nutrient(cell: String, lineNumber: Int, column: String): NutrientValue {
            val raw = cell.trim()
            if (raw.isEmpty()) return NutrientValue.Unknown
            if (raw.equals("ni", ignoreCase = true)) return NutrientValue.NotInformed
            val amount = raw.toDoubleOrNull()
                ?: throw PackageFormatException("Linha $lineNumber: $column inválido (\"$raw\").")
            if (amount < 0) throw PackageFormatException("Linha $lineNumber: $column negativo (\"$raw\").")
            return NutrientValue.Value(amount)
        }

        private fun portions(cell: String, lineNumber: Int): List<Portion> =
            cell.split('|').filter { it.isNotBlank() }.map { entry ->
                val eq = entry.indexOf('=')
                val label = if (eq > 0) entry.substring(0, eq).trim() else ""
                val grams = if (eq > 0) entry.substring(eq + 1).trim().toDoubleOrNull() else null
                if (label.isEmpty() || grams == null || grams <= 0) {
                    throw PackageFormatException("Linha $lineNumber: porção inválida (\"$entry\").")
                }
                Portion(label, grams)
            }
    }
}

/** Alimento lido do pacote, com valores por 100 g. */
data class PackageItem(
    val id: String,
    val name: String,
    val preparation: String?,
    val nutrients: NutrientColumns,
    val portions: List<Portion>,
)

internal fun sha256(text: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(text.toByteArray(Charsets.UTF_8))
        .joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
