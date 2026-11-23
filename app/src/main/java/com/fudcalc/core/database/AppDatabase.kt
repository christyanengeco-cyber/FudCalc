package com.fudcalc.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FoodEntity::class, FoodPortionEntity::class, DiaryEntryEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(NutrientValueConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    abstract fun diaryDao(): DiaryDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "fudcalc.db").build()
    }
}
