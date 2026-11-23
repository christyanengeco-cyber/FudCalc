package com.fudcalc.data.packages

import androidx.room.withTransaction
import com.fudcalc.core.database.AppDatabase
import com.fudcalc.core.database.FoodEntity
import com.fudcalc.core.database.FoodPackageEntity
import com.fudcalc.core.database.FoodPortionEntity
import com.fudcalc.data.normalizeForSearch

/**
 * Importa um pacote nutricional de forma idempotente: mesmo checksum não reescreve nada.
 * Alimento que saiu do pacote fica indisponível (o histórico do diário é snapshot e não muda).
 */
class FoodPackageImporter(
    private val db: AppDatabase,
    private val loadPackage: suspend () -> String,
) {

    sealed interface Result {
        data class Imported(val packageName: String, val version: String, val itemCount: Int) : Result
        data class Unchanged(val version: String) : Result
    }

    suspend fun importIfNeeded(): Result {
        val package_ = FoodPackage.parse(loadPackage())
        if (db.foodDao().packageById(package_.id)?.checksum == package_.checksum) {
            return Result.Unchanged(package_.version)
        }
        val ids = package_.items.map { it.id }
        db.withTransaction {
            db.foodDao().upsertPackage(
                FoodPackageEntity(
                    id = package_.id,
                    name = package_.name,
                    version = package_.version,
                    source = package_.source,
                    license = package_.license,
                    checksum = package_.checksum,
                    importedAt = System.currentTimeMillis(),
                ),
            )
            db.foodDao().upsertFoods(package_.items.map { it.toEntity(package_) })
            // Substitui as porções só quando o conteúdo do pacote mudou.
            db.foodDao().deletePortionsFor(ids)
            db.foodDao().insertPortions(
                package_.items.flatMap { item ->
                    item.portions.map { FoodPortionEntity(foodId = item.id, label = it.label, grams = it.grams) }
                },
            )
            db.foodDao().markUnavailableExcept(package_.id, ids)
        }
        return Result.Imported(package_.name, package_.version, package_.items.size)
    }

    private fun PackageItem.toEntity(package_: FoodPackage) = FoodEntity(
        id = id,
        name = name,
        searchName = normalizeForSearch("$name ${preparation.orEmpty()}"),
        preparation = preparation,
        source = package_.source,
        sourceVersion = package_.version,
        referenceGrams = 100.0,
        nutrients = nutrients,
        updatedAt = null,
        packageId = package_.id,
        available = true,
    )
}
