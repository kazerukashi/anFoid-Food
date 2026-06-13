package com.example.a212062_rimaniza_project2

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class RecipeSection(
    val sectionName: String,
    val ingredients: List<String>,
    val instructions: List<String>
)

@Entity(tableName = "food_items")
data class FoodItemData(
    @PrimaryKey val id: String,
    val imageRes: Int,
    val name: String,
    val origin: String,
    val sections: List<RecipeSection>,
    var isFavourite: Boolean = false
) {
    val ingredients: List<String> get() = sections.flatMap { it.ingredients }
    
    val tags: List<String>
        get() {
            val list = mutableListOf<String>()
            val allIngredients = ingredients
            
            if (origin == "Malay") list.add("Halal")
            if (origin == "Italian") list.add("Dairy")
            
            if (allIngredients.any { it.contains("Chicken", ignoreCase = true) }) list.add("Chicken-based")
            if (allIngredients.any { it.contains("Beef", ignoreCase = true) || it.contains("Pork", ignoreCase = true) || it.contains("Duck", ignoreCase = true) }) list.add("Protein")
            if (allIngredients.any { it.contains("Rice", ignoreCase = true) || it.contains("Noodles", ignoreCase = true) || it.contains("Pasta", ignoreCase = true) || it.contains("Dough", ignoreCase = true) || it.contains("Potato", ignoreCase = true) }) list.add("Carbs")
            if (allIngredients.any { it.contains("Egg", ignoreCase = true) || it.contains("Cheese", ignoreCase = true) || it.contains("Butter", ignoreCase = true) || it.contains("Cream", ignoreCase = true) || it.contains("Yogurt", ignoreCase = true) }) list.add("Dairy")
            
            val nonVeganIngredients = listOf("Chicken", "Beef", "Pork", "Duck", "Egg", "Cheese", "Butter", "Cream", "Yogurt", "Honey", "Milk", "Meat", "Fish", "Shrimp", "Anchovies")
            if (!allIngredients.any { ing -> nonVeganIngredients.any { it.contains(ing, ignoreCase = true) || ing.contains(it, ignoreCase = true) } }) {
                list.add("Vegan")
            }
            
            if (allIngredients.any { it.contains("Rice", ignoreCase = true) || it.contains("Noodles", ignoreCase = true) || it.contains("Pasta", ignoreCase = true) || it.contains("Beef", ignoreCase = true) || it.contains("Chicken", ignoreCase = true) }) {
                list.add("Main Course")
            } else {
                list.add("Appetizer")
            }

            if (!list.contains("Dairy")) list.add("Non-Dairy")
            if (!list.contains("Halal") && !allIngredients.any { it.contains("Pork", ignoreCase = true) }) list.add("Halal")
            if (allIngredients.any { it.contains("Pork", ignoreCase = true) }) list.add("Non-Halal")

            return list.distinct()
        }
}

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val foodName: String?,
    val ingredient: String,
    val amount: String,
    var isChecked: Boolean = false
)

@Entity(tableName = "planner_events")
data class PlannerEvent(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val foodItem: FoodItemData?,
    val date: String,
    val time: String,
    val repeat: String = "Never",
    val stopRepeatingDate: String? = null,
    val reminders: List<String> = emptyList(),
    val alarmEnabled: Boolean = false
)

@Entity(tableName = "recent_items")
data class RecentItem(
    @PrimaryKey val foodName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromRecipeSectionList(value: List<RecipeSection>?): String = gson.toJson(value)

    @TypeConverter
    fun toRecipeSectionList(value: String): List<RecipeSection>? {
        val type = object : TypeToken<List<RecipeSection>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromFoodItemData(value: FoodItemData?): String = gson.toJson(value)

    @TypeConverter
    fun toFoodItemData(value: String): FoodItemData? = gson.fromJson(value, FoodItemData::class.java)

    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}
