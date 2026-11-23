package com.fudcalc.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT COUNT(*) FROM foods")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortions(portions: List<FoodPortionEntity>)

    @Transaction
    @Query("SELECT * FROM foods WHERE searchName LIKE '%' || :normalizedQuery || '%' ORDER BY name LIMIT 100")
    fun search(normalizedQuery: String): Flow<List<FoodWithPortions>>

    @Transaction
    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun byId(id: String): FoodWithPortions?
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE epochDay = :epochDay ORDER BY createdAt")
    fun forDay(epochDay: Long): Flow<List<DiaryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun delete(id: String)
}
