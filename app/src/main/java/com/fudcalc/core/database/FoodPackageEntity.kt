package com.fudcalc.core.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Registro do pacote nutricional importado: versão, procedência e checksum do conteúdo. */
@Entity(tableName = "food_packages")
data class FoodPackageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val source: String,
    val license: String,
    val checksum: String,
    val importedAt: Long,
)

data class PackageWithCount(
    @Embedded val packageInfo: FoodPackageEntity,
    val itemCount: Int,
)
