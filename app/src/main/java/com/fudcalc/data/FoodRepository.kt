package com.fudcalc.data

import com.fudcalc.core.database.FoodDao
import com.fudcalc.core.database.toDomain
import com.fudcalc.data.packages.FoodPackageImporter
import com.fudcalc.domain.food.Food
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.Normalizer
import java.util.Locale

class FoodRepository(
    private val foodDao: FoodDao,
    private val importer: FoodPackageImporter,
) {

    /** Pacote instalado, para exibição em "Sobre". */
    data class PackageInfo(val source: String, val version: String, val itemCount: Int)

    fun search(query: String): Flow<List<Food>> =
        foodDao.search(normalizeForSearch(query)).map { rows -> rows.map { it.toDomain() } }

    val packageInfo: Flow<PackageInfo?> = foodDao.latestPackage().map { row ->
        row?.let { PackageInfo(it.packageInfo.source, it.packageInfo.version, it.itemCount) }
    }

    suspend fun byId(id: String): Food? = foodDao.byId(id)?.toDomain()

    /** Importa o pacote embutido; não faz nada se a mesma versão já foi importada. */
    suspend fun ensureImported(): FoodPackageImporter.Result = importer.importIfNeeded()
}

/** Minúsculas e sem acentos, para que "acucar" encontre "Açúcar". */
fun normalizeForSearch(raw: String): String =
    Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
