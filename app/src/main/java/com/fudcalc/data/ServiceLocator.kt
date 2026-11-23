package com.fudcalc.data

import android.content.Context
import com.fudcalc.core.database.AppDatabase

/** Instanciação única durante a vida do processo. */
object ServiceLocator {
    @Volatile
    private var database: AppDatabase? = null

    fun foodRepository(context: Context): FoodRepository = FoodRepository(db(context).foodDao())

    fun diaryRepository(context: Context): DiaryRepository = DiaryRepository(db(context).diaryDao())

    private fun db(context: Context): AppDatabase =
        database ?: synchronized(this) {
            database ?: AppDatabase.build(context.applicationContext).also { database = it }
        }
}
