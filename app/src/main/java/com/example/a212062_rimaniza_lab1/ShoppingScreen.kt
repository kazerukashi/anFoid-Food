package com.example.a212062_rimaniza_lab1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a212062_rimaniza_lab1.ui.theme.AppPink

@Composable
fun ShoppingScreen(
    onMenuClick: () -> Unit,
    shoppingItems: List<ShoppingItem>,
    onAddItem: (ShoppingItem) -> Unit,
    onUpdateItem: (ShoppingItem) -> Unit,
    onDeleteItem: (ShoppingItem) -> Unit,
    onDeleteIngredient: (String) -> Unit,
    onCheckedChange: (String, Boolean) -> Unit,
    onItemCheckedChange: (String, Boolean) -> Unit
) {
    var displayMode by remember { mutableStateOf("Ungrouped") }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ShoppingItem?>(null) }
    var itemToDelete by remember { mutableStateOf<ShoppingItem?>(null) }
    var ingredientToDelete by remember { mutableStateOf<String?>(null) }

    var expandedGroups by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = AppPink)
            }
            Text(
                text = "Shopping List",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = AppPink,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryItem(
                icon = Icons.Default.ShoppingCart,
                label = "Ungrouped",
                isSelected = displayMode == "Ungrouped",
                onClick = { displayMode = "Ungrouped" }
            )
            CategoryItem(
                icon = Icons.Default.Flatware,
                label = "By Food",
                isSelected = displayMode == "By Food",
                onClick = { displayMode = "By Food" }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (shoppingItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Your shopping list is empty",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (displayMode == "Ungrouped") {
                        val groupedItems = shoppingItems.groupBy { it.ingredient }
                        items(groupedItems.toList()) { (ingredient, items) ->
                            ShoppingListItem(
                                ingredient = ingredient,
                                amount = items.joinToString(", ") { it.amount },
                                foodName = null,
                                isChecked = items.all { it.isChecked },
                                onCheckedChange = { checked ->
                                    onCheckedChange(ingredient, checked)
                                },
                                onEdit = {
                                    itemToEdit = items.first()
                                },
                                onDelete = {
                                    ingredientToDelete = ingredient
                                }
                            )
                        }
                    } else {
                        // Group items, treating null/blank foodName as "Ungrouped"
                        val itemsByFood = shoppingItems.groupBy { it.foodName?.takeIf { name -> name.isNotBlank() } ?: "Ungrouped" }
                        
                        // Sort so "Ungrouped" is at the top
                        val sortedFoodNames = itemsByFood.keys.sortedWith { a, b ->
                            when {
                                a == "Ungrouped" -> -1
                                b == "Ungrouped" -> 1
                                else -> a.compareTo(b)
                            }
                        }

                        sortedFoodNames.forEach { foodName ->
                            val items = itemsByFood[foodName] ?: emptyList()
                            val isExpanded = expandedGroups.contains(foodName)
                            
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            expandedGroups = if (isExpanded) expandedGroups - foodName else expandedGroups + foodName
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = foodName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AppPink,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = AppPink
                                    )
                                }
                            }
                            
                            if (isExpanded) {
                                items(items) { item ->
                                    ShoppingListItem(
                                        ingredient = item.ingredient,
                                        amount = item.amount,
                                        foodName = null,
                                        isChecked = item.isChecked,
                                        onCheckedChange = { checked ->
                                            onItemCheckedChange(item.id, checked)
                                        },
                                        onEdit = {
                                            itemToEdit = item
                                        },
                                        onDelete = {
                                            itemToDelete = item
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AppPink,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    }

    if (showAddDialog || itemToEdit != null) {
        var foodName by remember { mutableStateOf(itemToEdit?.foodName ?: "") }
        var ingredient by remember { mutableStateOf(itemToEdit?.ingredient ?: "") }
        var amount by remember { mutableStateOf(itemToEdit?.amount ?: "") }

        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                itemToEdit = null
            },
            title = { Text(if (itemToEdit == null) "Add Item" else "Edit Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Food Name (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ingredient,
                        onValueChange = { ingredient = it },
                        label = { Text("Ingredient") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (e.g. 200g, 2 pieces)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ingredient.isNotBlank()) {
                            if (itemToEdit != null) {
                                onUpdateItem(itemToEdit!!.copy(
                                    foodName = foodName,
                                    ingredient = ingredient,
                                    amount = amount
                                ))
                            } else {
                                onAddItem(ShoppingItem(
                                    foodName = foodName,
                                    ingredient = ingredient,
                                    amount = amount
                                ))
                            }
                            showAddDialog = false
                            itemToEdit = null
                        }
                    }
                ) {
                    Text(if (itemToEdit == null) "Add" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    itemToEdit = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (itemToDelete != null || ingredientToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                itemToDelete = null
                ingredientToDelete = null
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (itemToDelete != null) {
                            onDeleteItem(itemToDelete!!)
                        } else if (ingredientToDelete != null) {
                            onDeleteIngredient(ingredientToDelete!!)
                        }
                        itemToDelete = null
                        ingredientToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    itemToDelete = null
                    ingredientToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ShoppingListItem(
    ingredient: String,
    amount: String,
    foodName: String?,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = AppPink)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                if (foodName != null) {
                    Text(
                        text = foodName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isChecked) AppPink.copy(alpha = 0.6f) else AppPink,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = AppPink,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
