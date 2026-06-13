package com.example.a212062_rimaniza_project2

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FoodRepository(
            database.foodDao(),
            database.shoppingDao(),
            database.plannerDao(),
            database.recentDao(),
            database.settingsDao()
        )

        // Initialize database if empty
        viewModelScope.launch {
            repository.allFoodItems.collectLatest { items ->
                if (items.isEmpty()) {
                    repository.initializeFoodItems()
                }
            }
        }

        // Load Settings
        viewModelScope.launch {
            repository.getSetting("isDarkTheme")?.let { isDarkTheme = it.toBoolean() }
            repository.getSetting("maxRecentItems")?.let { maxRecentItems = it.toInt() }
        }
    }

    // --- UI State (Shared across screens) ---
    var searchQuery by mutableStateOf("")
    var isSearchActive by mutableStateOf(false)
    var selectedCategory by mutableStateOf("Origin")
    
    // Persistent settings
    var maxRecentItems by mutableIntStateOf(30)
        private set
    var isDarkTheme by mutableStateOf(true)
        private set

    // --- Required Component 5: ViewModel exposing StateFlow to UI ---
    
    val allFoodItems: StateFlow<List<FoodItemData>> = repository.allFoodItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentNames: StateFlow<List<String>> = repository.recentItems
        .map { items -> items.map { it.foodName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingItems: StateFlow<List<ShoppingItem>> = repository.shoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plannerEvents: StateFlow<List<PlannerEvent>> = repository.plannerEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Process / Business Logic (Calling Repository) ---

    fun updateTheme(dark: Boolean) {
        isDarkTheme = dark
        viewModelScope.launch {
            repository.saveSetting("isDarkTheme", dark.toString())
        }
    }

    fun updateMaxRecentItems(limit: Int) {
        maxRecentItems = limit
        viewModelScope.launch {
            repository.saveSetting("maxRecentItems", limit.toString())
        }
    }
    
    fun toggleFavourite(foodId: String) {
        viewModelScope.launch {
            allFoodItems.value.find { it.id == foodId }?.let {
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
        val existing = plannerEvents.value.find { it.id == event.id }
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

    // Notification helpers
    private fun scheduleNotification(context: Context, event: PlannerEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.a212062_rimaniza_project2.ALARM_ACTION"
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
            action = "com.example.a212062_rimaniza_project2.ALARM_ACTION"
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
        
        val existing = shoppingItems.value.find { 
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
        shoppingItems.value.filter { it.ingredient == ingredient }.forEach { item ->
            viewModelScope.launch { repository.updateShoppingItem(item.copy(isChecked = isChecked)) }
        }
    }

    fun toggleShoppingItemCheckedById(id: String, isChecked: Boolean) {
        shoppingItems.value.find { it.id == id }?.let { item ->
            viewModelScope.launch { repository.updateShoppingItem(item.copy(isChecked = isChecked)) }
        }
    }
}
