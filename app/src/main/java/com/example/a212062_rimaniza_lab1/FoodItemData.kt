package com.example.a212062_rimaniza_lab1

data class RecipeSection(
    val sectionName: String,
    val ingredients: List<String>,
    val instructions: List<String>
)

data class FoodItemData(
    val id: String,
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
            
            // Origin-based tags
            if (origin == "Malay") list.add("Halal")
            if (origin == "Italian") list.add("Dairy")
            
            // Ingredient-based tags
            if (allIngredients.any { it.contains("Chicken", ignoreCase = true) }) list.add("Chicken-based")
            if (allIngredients.any { it.contains("Beef", ignoreCase = true) || it.contains("Pork", ignoreCase = true) || it.contains("Duck", ignoreCase = true) }) list.add("Protein")
            if (allIngredients.any { it.contains("Rice", ignoreCase = true) || it.contains("Noodles", ignoreCase = true) || it.contains("Pasta", ignoreCase = true) || it.contains("Dough", ignoreCase = true) || it.contains("Potato", ignoreCase = true) }) list.add("Carbs")
            if (allIngredients.any { it.contains("Egg", ignoreCase = true) || it.contains("Cheese", ignoreCase = true) || it.contains("Butter", ignoreCase = true) || it.contains("Cream", ignoreCase = true) || it.contains("Yogurt", ignoreCase = true) }) list.add("Dairy")
            
            val nonVeganIngredients = listOf("Chicken", "Beef", "Pork", "Duck", "Egg", "Cheese", "Butter", "Cream", "Yogurt", "Honey", "Milk", "Meat", "Fish", "Shrimp", "Anchovies")
            if (!allIngredients.any { ing -> nonVeganIngredients.any { it.contains(ing, ignoreCase = true) || ing.contains(it, ignoreCase = true) } }) {
                list.add("Vegan")
            }
            
            // Generic Type categorization
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

data class ShoppingItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val foodName: String?, // Null if added ungrouped or generic
    val ingredient: String,
    val amount: String,
    var isChecked: Boolean = false
)

data class PlannerEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val foodItem: FoodItemData?,
    val date: String,
    val time: String,
    val repeat: String = "Never",
    val stopRepeatingDate: String? = null,
    val reminders: List<String> = emptyList(),
    val alarmEnabled: Boolean = false
)
