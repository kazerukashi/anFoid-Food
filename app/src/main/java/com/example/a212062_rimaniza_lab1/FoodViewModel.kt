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
    val allFoodItems = mutableStateListOf<FoodItemData>().apply {
        addAll(FoodRepository.getInitialFoodItems())
    }

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
            section.ingredients.forEach { rawIngredient ->
                val trimmed = rawIngredient.trim()
                val parts = trimmed.split(" ")
                
                val (amt, ing) = when {
                    parts.size == 1 -> "" to trimmed
                    else -> {
                        // The last word is the ingredient, everything before it is the amount
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
    private fun mergeAmounts(amount1: String, amount2: String): String {
        if (amount1.isBlank()) return amount2
        if (amount2.isBlank()) return amount1

        // Regex to match a number and everything else as the unit
        val regex = """^(\d+\.?\d*)\s*(.*)$""".toRegex()
        val match1 = regex.find(amount1.trim())
        val match2 = regex.find(amount2.trim())

        if (match1 != null && match2 != null) {
            val val1 = match1.groupValues[1].toDoubleOrNull() ?: 0.0
            val unit1 = match1.groupValues[2].trim()
            val val2 = match2.groupValues[1].toDoubleOrNull() ?: 0.0
            val unit2 = match2.groupValues[2].trim()

            // Normalize units by lowercasing and removing plural suffixes for comparison
            fun normalize(u: String) = u.lowercase().removeSuffix("es").removeSuffix("s")

            if (normalize(unit1) == normalize(unit2)) {
                val total = val1 + val2
                val displayTotal = if (total % 1.0 == 0.0) total.toInt().toString() else total.toString()
                
                // Pick the more appropriate unit name (plural if total > 1)
                val finalUnit = if (total > 1) {
                    if (unit1.length > unit2.length) unit1 else unit2
                } else {
                    if (unit1.length < unit2.length) unit1 else unit2
                }
                
                // Keep the formatting (space vs no space) based on inputs
                val hasSpace = amount1.trim().contains(" ") || amount2.trim().contains(" ")
                return if (hasSpace) "$displayTotal $finalUnit" else "$displayTotal$finalUnit"
            }
        }
        
        // Fallback: if they don't match, join them
        return if (amount1.contains(amount2)) amount1 else "$amount1, $amount2"
    }

    fun addShoppingItem(newItem: ShoppingItem) {
        val trimmedIngredient = newItem.ingredient.trim()
        val trimmedFoodName = newItem.foodName?.trim()?.takeIf { it.isNotBlank() } ?: "Ungrouped"
        
        // Merge only if both ingredient name AND food name match
        val existingIndex = shoppingItems.indexOfFirst { 
            it.ingredient.trim().equals(trimmedIngredient, ignoreCase = true) &&
            (it.foodName?.trim() ?: "Ungrouped").equals(trimmedFoodName, ignoreCase = true)
        }

        if (existingIndex != -1) {
            val existingItem = shoppingItems[existingIndex]
            shoppingItems[existingIndex] = existingItem.copy(
                amount = mergeAmounts(existingItem.amount, newItem.amount),
                isChecked = false
            )
        } else {
            shoppingItems.add(newItem.copy(
                ingredient = trimmedIngredient,
                foodName = trimmedFoodName
            ))
        }
    }

    fun toggleIngredientGroup(ingredientName: String, isChecked: Boolean) {
        shoppingItems.indices.forEach { i ->
            if (shoppingItems[i].ingredient.equals(ingredientName, ignoreCase = true)) {
                shoppingItems[i] = shoppingItems[i].copy(isChecked = isChecked)
            }
        }
    }

    fun deleteIngredientGroup(ingredientName: String) {
        shoppingItems.removeAll { it.ingredient.equals(ingredientName, ignoreCase = true) }
    }

    fun updateShoppingItem(item: ShoppingItem) {
        val index = shoppingItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            shoppingItems[index] = item.copy(
                ingredient = item.ingredient.trim(),
                foodName = item.foodName?.trim()?.takeIf { it.isNotBlank() }
            )
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
