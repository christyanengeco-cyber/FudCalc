package com.fudcalc.data

import com.fudcalc.core.database.FoodDao
import com.fudcalc.core.database.toDomain
import com.fudcalc.domain.food.Food
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.Normalizer
import java.util.Locale

class FoodRepository(private val foodDao: FoodDao) {

    fun search(query: String): Flow<List<Food>> =
        foodDao.search(normalizeForSearch(query)).map { rows -> rows.map { it.toDomain() } }

    suspend fun byId(id: String): Food? = foodDao.byId(id)?.toDomain()

    /** Insere a semente apenas se o banco ainda não tiver alimentos. */
    suspend fun ensureSeeded() {
        if (foodDao.count() > 0) return
        foodDao.insertFoods(SeedFoods.foods.map { it.food })
        foodDao.insertPortions(SeedFoods.foods.flatMap { it.portions })
    }

    companion object {
        /** Minúsculas e sem acentos, para que "acucar" encontre "Açúcar". */
        fun normalizeForSearch(raw: String): String =
            Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
                .lowercase(Locale.ROOT)
    }
}
