package com.fudcalc.core.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fudcalc.domain.meals.DiaryEntry
import com.fudcalc.domain.meals.MealType
import java.time.LocalDate

/**
 * Snapshot do alimento no momento do registro: atualizar uma base de dados não pode
 * reescrever o histórico do usuário. [nutrients] fica na base de [referenceGrams].
 */
@Entity(tableName = "diary_entries", indices = [Index("epochDay")])
data class DiaryEntryEntity(
    @PrimaryKey val id: String,
    val epochDay: Long,
    val meal: String,
    val foodId: String?,
    val foodName: String,
    val grams: Double,
    val referenceGrams: Double,
    @Embedded val nutrients: NutrientColumns,
    val createdAt: Long,
)

fun DiaryEntryEntity.toDomain(): DiaryEntry = DiaryEntry(
    id = id,
    date = LocalDate.ofEpochDay(epochDay),
    meal = MealType.entries.firstOrNull { it.name == meal } ?: MealType.SNACK,
    foodName = foodName,
    grams = grams,
    facts = nutrients.toFacts().scaledBy(grams / referenceGrams),
)
