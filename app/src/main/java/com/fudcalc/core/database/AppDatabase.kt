package com.fudcalc.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FoodEntity::class,
        FoodPortionEntity::class,
        DiaryEntryEntity::class,
        FoodPackageEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(NutrientValueConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    abstract fun diaryDao(): DiaryDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "fudcalc.db")
                .addMigrations(MIGRATION_1_2)
                .build()

        /** v1 → v2: pacotes nutricionais versionados; alimentos antigos ficam disponíveis. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `food_packages` (`id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, `version` TEXT NOT NULL, `source` TEXT NOT NULL, " +
                        "`license` TEXT NOT NULL, `checksum` TEXT NOT NULL, `importedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))",
                )
                db.execSQL("ALTER TABLE `foods` ADD COLUMN `packageId` TEXT")
                db.execSQL("ALTER TABLE `foods` ADD COLUMN `available` INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
