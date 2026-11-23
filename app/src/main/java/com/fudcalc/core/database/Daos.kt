package com.fudcalc.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT COUNT(*) FROM foods")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortions(portions: List<FoodPortionEntity>)

    @Upsert
    suspend fun upsertFoods(foods: List<FoodEntity>)

    @Query("DELETE FROM food_portions WHERE foodId IN (:foodIds)")
    suspend fun deletePortionsFor(foodIds: List<String>)

    @Upsert
    suspend fun upsertPackage(packageInfo: FoodPackageEntity)

    @Query("SELECT * FROM food_packages WHERE id = :id")
    suspend fun packageById(id: String): FoodPackageEntity?

    @Query(
        """
        SELECT p.*, (SELECT COUNT(*) FROM foods f WHERE f.packageId = p.id AND f.available = 1) AS itemCount
        FROM food_packages p ORDER BY p.importedAt DESC LIMIT 1
        """,
    )
    fun latestPackage(): Flow<PackageWithCount?>

    @Query("UPDATE foods SET available = 0 WHERE packageId = :packageId AND id NOT IN (:keepIds)")
    suspend fun markUnavailableExcept(packageId: String, keepIds: List<String>)

    @Transaction
    @Query("SELECT * FROM foods WHERE searchName LIKE '%' || :normalizedQuery || '%' AND available = 1 ORDER BY name LIMIT 100")
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
