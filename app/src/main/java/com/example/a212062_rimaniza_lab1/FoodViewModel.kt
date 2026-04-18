package com.example.a212062_rimaniza_lab1

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class FoodViewModel : ViewModel() {
    // UI State
    var searchQuery by mutableStateOf("")
    var isSearchActive by mutableStateOf(false)
    var selectedCategory by mutableStateOf("Origin")
    var maxRecentItems by mutableIntStateOf(30)
    var isDarkTheme by mutableStateOf(true)

    // Data State
    val allFoodItems = mutableStateListOf(
        FoodItemData(
            id = "1",
            imageRes = R.drawable.nasmak,
            name = "Nasi Lemak",
            origin = "Malay",
            sections = listOf(
                RecipeSection(
                    sectionName = "Rice",
                    ingredients = listOf("2 cups rice", "1 cup coconut milk", "1 cup water", "1 tsp salt", "2 pandan leaves"),
                    instructions = listOf("Wash rice.", "Add coconut milk, water, salt and pandan leaves.", "Cook in a rice cooker.")
                ),
                RecipeSection(
                    sectionName = "Sambal",
                    ingredients = listOf("10 dried chilies", "1 onion", "3 garlic cloves", "1 tbsp tamarind juice", "Sugar and salt to taste", "1/2 cup dried anchovies"),
                    instructions = listOf("Blend chilies, onion, and garlic.", "Sauté the blended paste until fragrant.", "Add tamarind juice, sugar, and salt.", "Stir in the fried anchovies.")
                ),
                RecipeSection(
                    sectionName = "Accompaniments",
                    ingredients = listOf("2 boiled eggs", "1 cucumber, sliced", "1/2 cup roasted peanuts", "1/2 cup fried anchovies"),
                    instructions = listOf("Serve everything together with the rice and sambal.")
                )
            ),
            isFavourite = true
        ),
        FoodItemData(
            id = "2",
            imageRes = R.drawable.margherita_pizza,
            name = "Margherita Pizza",
            origin = "Italian",
            sections = listOf(
                RecipeSection(
                    sectionName = "Pizza Dough",
                    ingredients = listOf("500g flour", "325ml water", "7g yeast", "1 tsp salt", "1 tbsp olive oil"),
                    instructions = listOf("Mix flour, water, yeast, salt and oil.", "Knead for 10 minutes.", "Let rise for 1 hour.", "Roll out into a pizza shape.")
                ),
                RecipeSection(
                    sectionName = "Pizza Sauce",
                    ingredients = listOf("1 can peeled tomatoes", "1 clove garlic, minced", "1 tbsp olive oil", "Handful of fresh basil", "Salt to taste"),
                    instructions = listOf("Crush tomatoes.", "Sauté garlic in olive oil.", "Add tomatoes, basil and salt.", "Simmer for 15 minutes.")
                ),
                RecipeSection(
                    sectionName = "Toppings",
                    ingredients = listOf("200g mozzarella cheese", "Fresh basil leaves", "Extra virgin olive oil"),
                    instructions = listOf("Spread sauce on dough.", "Top with cheese and basil.", "Bake at 250°C for 10-12 minutes.", "Drizzle with olive oil.")
                )
            ),
            isFavourite = true
        ),
        FoodItemData(
            id = "3",
            imageRes = R.drawable.beefrendang,
            name = "Beef Rendang",
            origin = "Malay",
            sections = listOf(
                RecipeSection(
                    sectionName = "Rendang",
                    ingredients = listOf("1kg beef, cubed", "2 cups coconut milk", "1 tbsp tamarind paste", "2 turmeric leaves, sliced", "Salt and sugar to taste"),
                    instructions = listOf("Simmer beef with coconut milk and spices.", "Cook until the liquid is absorbed and beef is tender.")
                )
            )
        ),
        FoodItemData(
            id = "4",
            imageRes = R.drawable.satay,
            name = "Satay",
            origin = "Malay",
            sections = listOf(
                RecipeSection(
                    sectionName = "Chicken Skewers",
                    ingredients = listOf("500g chicken breast", "1 tsp turmeric", "1 tbsp lemongrass paste", "Sugar and salt"),
                    instructions = listOf("Marinate chicken.", "Thread onto skewers.", "Grill until cooked.")
                )
            )
        ),
        FoodItemData(
            id = "5",
            imageRes = R.drawable.pekingduck,
            name = "Peking Duck",
            origin = "Chinese",
            sections = listOf(
                RecipeSection(
                    sectionName = "Duck",
                    ingredients = listOf("1 whole duck", "Honey", "Spices"),
                    instructions = listOf("Roast the duck until skin is crispy.")
                )
            )
        ),
        FoodItemData(
            id = "6",
            imageRes = R.drawable.xiaolongbao,
            name = "Xiaolongbao",
            origin = "Chinese",
            sections = listOf(
                RecipeSection(
                    sectionName = "Filling",
                    ingredients = listOf("Pork", "Ginger", "Soup gelatin"),
                    instructions = listOf("Wrap filling in thin dough.", "Steam for 8 minutes.")
                )
            ),
            isFavourite = true
        ),
        FoodItemData(
            id = "7",
            imageRes = R.drawable.chowmein,
            name = "Chow Mein",
            origin = "Chinese",
            sections = listOf(
                RecipeSection(
                    sectionName = "Noodles",
                    ingredients = listOf("Egg noodles", "Soy sauce", "Cabbage", "Pork"),
                    instructions = listOf("Stir fry everything together.")
                )
            )
        ),
        FoodItemData(
            id = "8",
            imageRes = R.drawable.butterchicken,
            name = "Butter Chicken",
            origin = "Indian",
            sections = listOf(
                RecipeSection(
                    sectionName = "Chicken & Sauce",
                    ingredients = listOf("Chicken", "Butter", "Cream", "Tomato", "Garam Masala"),
                    instructions = listOf("Cook chicken.", "Simmer in creamy tomato sauce.")
                )
            ),
            isFavourite = true
        ),
        FoodItemData(
            id = "9",
            imageRes = R.drawable.biryani,
            name = "Biryani",
            origin = "Indian",
            sections = listOf(
                RecipeSection(
                    sectionName = "Rice & Chicken",
                    ingredients = listOf("Basmati rice", "Chicken", "Yogurt", "Saffron"),
                    instructions = listOf("Layer rice and chicken curry.", "Cook together on low heat.")
                )
            )
        ),
        FoodItemData(
            id = "10",
            imageRes = R.drawable.samosa,
            name = "Samosa",
            origin = "Indian",
            sections = listOf(
                RecipeSection(
                    sectionName = "Filling & Pastry",
                    ingredients = listOf("Potato", "Peas", "Spices", "Wheat flour"),
                    instructions = listOf("Fill pastry with spiced potatoes.", "Deep fry until golden.")
                )
            )
        ),
        FoodItemData(
            id = "11",
            imageRes = R.drawable.lasagna,
            name = "Lasagna",
            origin = "Italian",
            sections = listOf(
                RecipeSection(
                    sectionName = "Layers",
                    ingredients = listOf("Pasta sheets", "Minced beef", "Tomato sauce", "Béchamel", "Cheese"),
                    instructions = listOf("Layer everything.", "Bake until cheese is bubbly.")
                )
            )
        ),
        FoodItemData(
            id = "12",
            imageRes = R.drawable.spaghetti_bolognese,
            name = "Spaghetti Bolognese",
            origin = "Italian",
            sections = listOf(
                RecipeSection(
                    sectionName = "Pasta & Sauce",
                    ingredients = listOf("Spaghetti", "Beef", "Tomato", "Onion"),
                    instructions = listOf("Cook spaghetti.", "Prepare sauce and mix.")
                )
            )
        )
    )

    val recentNames = mutableStateListOf<String>()
    val shoppingItems = mutableStateListOf<ShoppingItem>()
    val plannerEvents = mutableStateListOf<PlannerEvent>()

    // Actions
    fun toggleFavourite(foodId: String) {
        val index = allFoodItems.indexOfFirst { it.id == foodId }
        if (index != -1) {
            allFoodItems[index] = allFoodItems[index].copy(
                isFavourite = !allFoodItems[index].isFavourite
            )
        }
    }

    fun addToRecent(food: FoodItemData) {
        recentNames.removeAll { it == food.name }
        recentNames.add(0, food.name)
        if (recentNames.size > maxRecentItems) {
            recentNames.removeAt(recentNames.size - 1)
        }
    }

    fun addIngredientsToShoppingList(food: FoodItemData) {
        food.sections.forEach { section ->
            section.ingredients.forEach { ingredient ->
                shoppingItems.add(
                    ShoppingItem(
                        foodName = food.name,
                        ingredient = ingredient,
                        amount = ""
                    )
                )
            }
        }
    }

    // Planner Actions
    fun addPlannerEvent(context: Context, event: PlannerEvent) {
        plannerEvents.add(event)
        scheduleNotification(context, event)
    }

    fun updatePlannerEvent(context: Context, event: PlannerEvent) {
        val index = plannerEvents.indexOfFirst { it.id == event.id }
        if (index != -1) {
            cancelNotification(context, plannerEvents[index])
            plannerEvents[index] = event
            scheduleNotification(context, event)
        }
    }

    fun deletePlannerEvent(context: Context, event: PlannerEvent) {
        cancelNotification(context, event)
        plannerEvents.removeIf { it.id == event.id }
    }

    private fun scheduleNotification(context: Context, event: PlannerEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.a212062_rimaniza_lab1.ALARM_ACTION"
            putExtra("EXTRA_TITLE", "Meal Reminder: ${event.title}")
            putExtra("EXTRA_MESSAGE", "Time for your scheduled ${event.title}!")
            putExtra("EXTRA_ID", event.id.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        try {
            val date = sdf.parse("${event.date} ${event.time}")
            if (date != null && date.after(Date())) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    date.time,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelNotification(context: Context, event: PlannerEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.a212062_rimaniza_lab1.ALARM_ACTION"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    // Shopping List Actions
    fun addShoppingItem(item: ShoppingItem) {
        shoppingItems.add(item)
    }

    fun updateShoppingItem(item: ShoppingItem) {
        val index = shoppingItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            shoppingItems[index] = item
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        shoppingItems.removeIf { it.id == item.id }
    }

    fun deleteShoppingItemsByIngredient(ingredient: String) {
        shoppingItems.removeIf { it.ingredient == ingredient }
    }

    fun toggleShoppingItemChecked(ingredient: String, isChecked: Boolean) {
        shoppingItems.forEachIndexed { index, item ->
            if (item.ingredient == ingredient) {
                shoppingItems[index] = item.copy(isChecked = isChecked)
            }
        }
    }

    fun toggleShoppingItemCheckedById(id: String, isChecked: Boolean) {
        val index = shoppingItems.indexOfFirst { it.id == id }
        if (index != -1) {
            shoppingItems[index] = shoppingItems[index].copy(isChecked = isChecked)
        }
    }
}
