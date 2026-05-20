package com.example.a212062_rimaniza_lab1

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodRepository

    // --- UI State ---
    var searchQuery by mutableStateOf("")
    var isSearchActive by mutableStateOf(false)
    var selectedCategory by mutableStateOf("Origin")
    var maxRecentItems by mutableIntStateOf(30)
    var isDarkTheme by mutableStateOf(true)

    // --- App Data (Observing Database) ---
    val allFoodItems = mutableStateListOf<FoodItemData>()
    val recentNames = mutableStateListOf<String>()
    val shoppingItems = mutableStateListOf<ShoppingItem>()
    val plannerEvents = mutableStateListOf<PlannerEvent>()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FoodRepository(
            database.foodDao(),
            database.shoppingDao(),
            database.plannerDao(),
            database.recentDao()
        )

        // Sync local lists with Database
        viewModelScope.launch {
            repository.allFoodItems.collectLatest { items ->
                if (items.isEmpty()) {
                    repository.initializeFoodItems()
                } else {
                    allFoodItems.clear()
                    allFoodItems.addAll(items)
                }
            }
        }

        viewModelScope.launch {
            repository.recentItems.collectLatest { items ->
                recentNames.clear()
                recentNames.addAll(items.map { it.foodName })
            }
        }

        viewModelScope.launch {
            repository.shoppingItems.collectLatest { items ->
                shoppingItems.clear()
                shoppingItems.addAll(items)
            }
        }

        viewModelScope.launch {
            repository.plannerEvents.collectLatest { events ->
                plannerEvents.clear()
                plannerEvents.addAll(events)
            }
        }
    }

    // --- Business Logic / Processes (DB Operations) ---
    
    fun toggleFavourite(foodId: String) {
        val food = allFoodItems.find { it.id == foodId }
        food?.let {
            viewModelScope.launch {
                repository.updateFoodItem(it.copy(isFavourite = !it.isFavourite))
            }
        }
    }

    fun addToRecent(food: FoodItemData) {
        viewModelScope.launch {
            repository.addToRecent(food.name, maxRecentItems)
        }
    }

    fun addIngredientsToShoppingList(food: FoodItemData) {
        food.sections.forEach { section ->
            section.ingredients.forEach { rawIngredient ->
                val trimmed = rawIngredient.trim()
                val parts = trimmed.split(" ")
                
                val (amt, ing) = when {
                    parts.size == 1 -> "" to trimmed
                    else -> {
                        val amountPart = parts.dropLast(1).joinToString(" ")
                        val ingredientPart = parts.last()
                        amountPart to ingredientPart
                    }
                }

                addShoppingItem(
                    ShoppingItem(
                        foodName = food.name,
                        ingredient = ing,
                        amount = amt
                    )
                )
            }
        }
    }

    // --- Planner Processes ---
    fun addPlannerEvent(context: Context, event: PlannerEvent) {
        viewModelScope.launch {
            repository.addPlannerEvent(event)
            scheduleNotification(context, event)
        }
    }

    fun updatePlannerEvent(context: Context, event: PlannerEvent) {
        val existing = plannerEvents.find { it.id == event.id }
        viewModelScope.launch {
            existing?.let { cancelNotification(context, it) }
            repository.updatePlannerEvent(event)
            scheduleNotification(context, event)
        }
    }

    fun deletePlannerEvent(context: Context, event: PlannerEvent) {
        viewModelScope.launch {
            cancelNotification(context, event)
            repository.deletePlannerEvent(event)
        }
    }

    // Notification helpers (same as before)
    private fun scheduleNotification(context: Context, event: PlannerEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.a212062_rimaniza_lab1.ALARM_ACTION"
            putExtra("EXTRA_TITLE", "Meal Reminder: ${event.title}")
            putExtra("EXTRA_MESSAGE", "Time for your scheduled ${event.title}!")
            putExtra("EXTRA_ID", event.id.hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, event.id.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        try {
            val date = sdf.parse("${event.date} ${event.time}")
            if (date != null && date.after(Date())) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date.time, pendingIntent)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun cancelNotification(context: Context, event: PlannerEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.a212062_rimaniza_lab1.ALARM_ACTION"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, event.id.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    // --- Shopping List Processes ---
    fun addShoppingItem(newItem: ShoppingItem) {
        val trimmedIngredient = newItem.ingredient.trim()
        val trimmedFoodName = newItem.foodName?.trim()?.takeIf { it.isNotBlank() } ?: "Ungrouped"
        
        val existing = shoppingItems.find { 
            it.ingredient.trim().equals(trimmedIngredient, ignoreCase = true) &&
            (it.foodName?.trim() ?: "Ungrouped").equals(trimmedFoodName, ignoreCase = true)
        }

        viewModelScope.launch {
            if (existing != null) {
                repository.updateShoppingItem(existing.copy(
                    amount = mergeAmounts(existing.amount, newItem.amount),
                    isChecked = false
                ))
            } else {
                repository.addShoppingItem(newItem.copy(
                    ingredient = trimmedIngredient,
                    foodName = trimmedFoodName
                ))
            }
        }
    }

    private fun mergeAmounts(amount1: String, amount2: String): String {
        if (amount1.isBlank()) return amount2
        if (amount2.isBlank()) return amount1
        val regex = """^(\d+\.?\d*)\s*(.*)$""".toRegex()
        val match1 = regex.find(amount1.trim()); val match2 = regex.find(amount2.trim())
        if (match1 != null && match2 != null) {
            val val1 = match1.groupValues[1].toDoubleOrNull() ?: 0.0
            val unit1 = match1.groupValues[2].trim()
            val val2 = match2.groupValues[1].toDoubleOrNull() ?: 0.0
            val unit2 = match2.groupValues[2].trim()
            fun normalize(u: String) = u.lowercase().removeSuffix("es").removeSuffix("s")
            if (normalize(unit1) == normalize(unit2)) {
                val total = val1 + val2
                val displayTotal = if (total % 1.0 == 0.0) total.toInt().toString() else total.toString()
                val finalUnit = if (total > 1) (if (unit1.length > unit2.length) unit1 else unit2) else (if (unit1.length < unit2.length) unit1 else unit2)
                return if (amount1.trim().contains(" ") || amount2.trim().contains(" ")) "$displayTotal $finalUnit" else "$displayTotal$finalUnit"
            }
        }
        return if (amount1.contains(amount2)) amount1 else "$amount1, $amount2"
    }

    fun updateShoppingItem(item: ShoppingItem) {
        viewModelScope.launch { repository.updateShoppingItem(item) }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch { repository.deleteShoppingItem(item) }
    }

    fun deleteShoppingItemsByIngredient(ingredient: String) {
        viewModelScope.launch { repository.deleteShoppingItemsByIngredient(ingredient) }
    }

    fun toggleShoppingItemChecked(ingredient: String, isChecked: Boolean) {
        shoppingItems.filter { it.ingredient == ingredient }.forEach { item ->
            viewModelScope.launch { repository.updateShoppingItem(item.copy(isChecked = isChecked)) }
        }
    }

    fun toggleShoppingItemCheckedById(id: String, isChecked: Boolean) {
        shoppingItems.find { it.id == id }?.let { item ->
            viewModelScope.launch { repository.updateShoppingItem(item.copy(isChecked = isChecked)) }
        }
    }
}
