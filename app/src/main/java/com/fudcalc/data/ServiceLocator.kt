package com.fudcalc.data

import android.content.Context
import com.fudcalc.core.database.AppDatabase
import com.fudcalc.data.packages.FoodPackageImporter

/** Instanciação única durante a vida do processo. */
object ServiceLocator {
    private const val PACKAGE_ASSET = "foods/taco.fudpkg"

    @Volatile
    private var database: AppDatabase? = null

    fun foodRepository(context: Context): FoodRepository {
        val db = db(context)
        val assets = context.applicationContext.assets
        return FoodRepository(db.foodDao(), FoodPackageImporter(db) {
            assets.open(PACKAGE_ASSET).bufferedReader().use { it.readText() }
        })
    }

    fun diaryRepository(context: Context): DiaryRepository = DiaryRepository(db(context).diaryDao())

    private fun db(context: Context): AppDatabase =
        database ?: synchronized(this) {
            database ?: AppDatabase.build(context.applicationContext).also { database = it }
        }
}
