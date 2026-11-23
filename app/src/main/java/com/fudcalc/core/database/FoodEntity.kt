package com.fudcalc.core.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fudcalc.domain.food.Food
import com.fudcalc.domain.food.Portion

@Entity(tableName = "foods", indices = [Index(value = ["searchName"])])
data class FoodEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** Nome normalizado (minúsculo, sem acentos) usado na busca. */
    val searchName: String,
    val preparation: String?,
    val source: String,
    val sourceVersion: String?,
    val referenceGrams: Double,
    @Embedded val nutrients: NutrientColumns,
    val updatedAt: Long?,
)

@Entity(
    tableName = "food_portions",
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("foodId")],
)
data class FoodPortionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodId: String,
    val label: String,
    val grams: Double,
)

data class FoodWithPortions(
    @Embedded val food: FoodEntity,
    @Relation(parentColumn = "id", entityColumn = "foodId")
    val portions: List<FoodPortionEntity>,
)

fun FoodWithPortions.toDomain(): Food = Food(
    id = food.id,
    name = food.name,
    preparation = food.preparation,
    source = food.source,
    sourceVersion = food.sourceVersion,
    referenceGrams = food.referenceGrams,
    facts = food.nutrients.toFacts(),
    portions = portions.map { Portion(label = it.label, grams = it.grams) },
)
