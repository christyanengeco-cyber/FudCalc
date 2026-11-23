package com.fudcalc.data

import com.fudcalc.core.database.DiaryDao
import com.fudcalc.core.database.DiaryEntryEntity
import com.fudcalc.core.database.toColumns
import com.fudcalc.core.database.toDomain
import com.fudcalc.domain.food.Food
import com.fudcalc.domain.meals.DiaryEntry
import com.fudcalc.domain.meals.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

class DiaryRepository(private val diaryDao: DiaryDao) {

    fun entries(date: LocalDate): Flow<List<DiaryEntry>> =
        diaryDao.forDay(date.toEpochDay()).map { rows -> rows.map { it.toDomain() } }

    suspend fun add(food: Food, grams: Double, meal: MealType, date: LocalDate) {
        require(grams > 0) { "A quantidade em gramas deve ser positiva." }
        require(food.referenceGrams > 0) { "Alimento sem quantidade de referência válida." }
        diaryDao.insert(
            DiaryEntryEntity(
                id = UUID.randomUUID().toString(),
                epochDay = date.toEpochDay(),
                meal = meal.name,
                foodId = food.id,
                foodName = food.displayName,
                grams = grams,
                referenceGrams = food.referenceGrams,
                nutrients = food.facts.toColumns(),
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun delete(entryId: String) = diaryDao.delete(entryId)
}
