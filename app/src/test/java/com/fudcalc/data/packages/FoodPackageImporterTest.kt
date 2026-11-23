package com.fudcalc.data.packages

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fudcalc.core.database.AppDatabase
import com.fudcalc.core.database.DiaryEntryEntity
import com.fudcalc.core.database.NutrientColumns
import com.fudcalc.domain.nutrition.NutrientValue
import com.fudcalc.domain.nutrition.numberOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FoodPackageImporterTest {

    private lateinit var db: AppDatabase
    private var payload = ""

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun importer() = FoodPackageImporter(db) { payload }

    @Test
    fun `importa uma vez e nao repete`() = runBlocking {
        payload = testPackage("v1", listOf(testRow(), testRow(id = "taco:feijao", name = "Feijão")))
        val first = importer().importIfNeeded()
        assertTrue(first is FoodPackageImporter.Result.Imported)
        assertEquals(2, db.foodDao().count())
        assertTrue(importer().importIfNeeded() is FoodPackageImporter.Result.Unchanged)
        assertEquals(2, db.foodDao().count())
        assertEquals(2, db.foodDao().latestPackage().first()!!.itemCount)
    }

    @Test
    fun `atualiza valores adiciona e remove sem apagar o diario`() = runBlocking {
        payload = testPackage("v1", listOf(testRow(), testRow(id = "taco:sai", name = "Sai")))
        importer().importIfNeeded()

        db.diaryDao().insert(
            DiaryEntryEntity(
                id = "e1",
                epochDay = 0,
                meal = "LUNCH",
                foodId = "taco:arroz",
                foodName = "Arroz",
                grams = 100.0,
                referenceGrams = 100.0,
                nutrients = NutrientColumns(energyKcal = NutrientValue.Value(100.0)),
                createdAt = 1,
            ),
        )

        payload = testPackage("v2", listOf(testRow(energy = "120"), testRow(id = "taco:novo", name = "Novo")))
        assertTrue(importer().importIfNeeded() is FoodPackageImporter.Result.Imported)

        val arroz = db.foodDao().byId("taco:arroz")!!
        assertEquals(120.0, arroz.food.nutrients.energyKcal.numberOrNull()!!, 0.0)
        assertEquals("v2", arroz.food.sourceVersion)
        assertEquals(1, arroz.portions.size)
        assertEquals(50.0, arroz.portions[0].grams, 0.0)

        assertFalse(db.foodDao().byId("taco:sai")!!.food.available)
        assertTrue(db.foodDao().byId("taco:novo")!!.food.available)
        assertEquals(setOf("taco:arroz", "taco:novo"), db.foodDao().search("").first().map { it.food.id }.toSet())

        val entry = db.diaryDao().forDay(0).first().single()
        assertEquals(100.0, entry.nutrients.energyKcal.numberOrNull()!!, 0.0)
    }

    @Test
    fun `pacote embutido importa com acentos corretos`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        payload = context.assets.open("foods/taco.fudpkg").bufferedReader().use { it.readText() }
        assertTrue(importer().importIfNeeded() is FoodPackageImporter.Result.Imported)
        assertEquals(11, db.foodDao().count())
        assertEquals("Feijão, carioca", db.foodDao().byId("taco:feijao-carioca-cozido")!!.food.name)
    }

    @Test
    fun `pacote invalido nao altera o banco`() = runBlocking {
        payload = testPackage("v1", listOf(testRow()))
        importer().importIfNeeded()
        payload = "lixo"
        assertTrue(runCatching { importer().importIfNeeded() }.isFailure)
        assertEquals(1, db.foodDao().count())
        assertEquals(100.0, db.foodDao().byId("taco:arroz")!!.food.nutrients.energyKcal.numberOrNull()!!, 0.0)
    }
}
