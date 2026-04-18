package com.example.a212062_rimaniza_lab1

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    onMenuClick: () -> Unit,
    plannerEvents: List<PlannerEvent>,
    allFoodItems: List<FoodItemData>,
    onAddEvent: (PlannerEvent) -> Unit,
    onUpdateEvent: (PlannerEvent) -> Unit,
    onDeleteEvent: (PlannerEvent) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<PlannerEvent?>(null) }
    var eventToDelete by remember { mutableStateOf<PlannerEvent?>(null) }

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
                text = "Planner",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = AppPink,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (plannerEvents.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No meals planned yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(plannerEvents) { event ->
                        PlannerEventItem(
                            event = event,
                            onEdit = { eventToEdit = event },
                            onDelete = { eventToDelete = event }
                        )
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
                Icon(Icons.Default.Add, contentDescription = "Plan Meal")
            }
        }
    }

    if (showAddDialog || eventToEdit != null) {
        PlanMealDialog(
            event = eventToEdit,
            allFoodItems = allFoodItems,
            onDismiss = {
                showAddDialog = false
                eventToEdit = null
            },
            onConfirm = { updatedEvent ->
                if (eventToEdit != null) {
                    onUpdateEvent(updatedEvent)
                } else {
                    onAddEvent(updatedEvent)
                }
                showAddDialog = false
                eventToEdit = null
            }
        )
    }

    if (eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Delete Event") },
            text = { Text("Are you sure you want to delete '${eventToDelete?.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEvent(eventToDelete!!)
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanMealDialog(
    event: PlannerEvent?,
    allFoodItems: List<FoodItemData>,
    onDismiss: () -> Unit,
    onConfirm: (PlannerEvent) -> Unit
) {
    var title by remember { mutableStateOf(event?.title ?: "") }
    var selectedFood by remember { mutableStateOf(event?.foodItem) }
    var date by remember { mutableStateOf(event?.date ?: "Select Date") }
    var time by remember { mutableStateOf(event?.time ?: "Select Time") }
    var repeat by remember { mutableStateOf(event?.repeat ?: "Never") }
    var stopRepeatingDate by remember { mutableStateOf(event?.stopRepeatingDate ?: "Select End Date") }
    var alarmEnabled by remember { mutableStateOf(event?.alarmEnabled ?: false) }
    
    var foodSearchQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStopDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var foodExpanded by remember { mutableStateOf(false) }
    var repeatExpanded by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = event?.let {
            try {
                val parts = it.date.split("/")
                val cal = Calendar.getInstance()
                cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                cal.timeInMillis
            } catch (e: Exception) { null }
        }
    )
    val stopDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = event?.stopRepeatingDate?.let {
            try {
                val parts = it.split("/")
                val cal = Calendar.getInstance()
                cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                cal.timeInMillis
            } catch (e: Exception) { null }
        }
    )
    val timePickerState = rememberTimePickerState(
        initialHour = event?.let {
            try {
                val parts = it.time.split(":")
                val hour = parts[0].toInt()
                if (it.time.contains("PM") && hour < 12) hour + 12
                else if (it.time.contains("AM") && hour == 12) 0
                else hour
            } catch (e: Exception) { 12 }
        } ?: 12,
        initialMinute = event?.let {
            try {
                it.time.split(":")[1].split(" ")[0].toInt()
            } catch (e: Exception) { 0 }
        } ?: 0,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (event == null) "Plan Meal" else "Edit Meal") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Meal Title (e.g. Lunch)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Food Selection
                ExposedDropdownMenuBox(
                    expanded = foodExpanded,
                    onExpandedChange = { foodExpanded = !foodExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFood?.name ?: "No Food Selected",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Food (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = foodExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = foodExpanded,
                        onDismissRequest = { foodExpanded = false }
                    ) {
                        OutlinedTextField(
                            value = foodSearchQuery,
                            onValueChange = { foodSearchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            placeholder = { Text("Search Food...") },
                            trailingIcon = {
                                if (foodSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { foodSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, "Clear")
                                    }
                                }
                            },
                            singleLine = true
                        )
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedFood = null
                                foodExpanded = false
                                foodSearchQuery = ""
                            }
                        )
                        val filteredFood = allFoodItems.filter { 
                            it.name.contains(foodSearchQuery, ignoreCase = true) 
                        }
                        filteredFood.forEach { food ->
                            DropdownMenuItem(
                                text = { Text(food.name) },
                                onClick = {
                                    selectedFood = food
                                    foodExpanded = false
                                    foodSearchQuery = ""
                                }
                            )
                        }
                        if (filteredFood.isEmpty() && foodSearchQuery.isNotEmpty()) {
                            Text(
                                "No matches found",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Date Picker
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Time Picker
                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Time") },
                    trailingIcon = { Icon(Icons.Default.AccessTime, null) },
                    modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Repeat Selection
                ExposedDropdownMenuBox(
                    expanded = repeatExpanded,
                    onExpandedChange = { repeatExpanded = !repeatExpanded }
                ) {
                    OutlinedTextField(
                        value = repeat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = repeatExpanded,
                        onDismissRequest = { repeatExpanded = false }
                    ) {
                        listOf("Never", "Daily", "Weekly", "Monthly").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    repeat = option
                                    repeatExpanded = false
                                }
                            )
                        }
                    }
                }

                // Stop Repeating Date Picker
                if (repeat != "Never") {
                    OutlinedTextField(
                        value = stopRepeatingDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Stop repeating after") },
                        trailingIcon = { Icon(Icons.Default.EventBusy, null) },
                        modifier = Modifier.fillMaxWidth().clickable { showStopDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Alarm Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Enable Alarm Reminder", modifier = Modifier.weight(1f))
                    Switch(
                        checked = alarmEnabled,
                        onCheckedChange = { alarmEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AppPink)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            PlannerEvent(
                                id = event?.id ?: UUID.randomUUID().toString(),
                                title = title,
                                foodItem = selectedFood,
                                date = date,
                                time = time,
                                repeat = repeat,
                                stopRepeatingDate = if (repeat == "Never") null else stopRepeatingDate,
                                alarmEnabled = alarmEnabled
                            )
                        )
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        date = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStopDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStopDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    stopDatePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        stopRepeatingDate = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
                    }
                    showStopDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStopDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = stopDatePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val amPm = if (timePickerState.hour < 12) "AM" else "PM"
                    val hour = if (timePickerState.hour % 12 == 0) 12 else timePickerState.hour % 12
                    time = String.format("%02d:%02d %s", hour, timePickerState.minute, amPm)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
fun PlannerEventItem(
    event: PlannerEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppPink,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit", tint = AppPink, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = AppPink)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${event.date} at ${event.time}", style = MaterialTheme.typography.bodyMedium)
            }
            
            if (event.foodItem != null) {
                Text(
                    text = "Recipe: ${event.foodItem.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (event.repeat != "Never") {
                Text(
                    text = "Repeats ${event.repeat}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppPink,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (event.stopRepeatingDate != null && event.stopRepeatingDate != "Select End Date") {
                    Text(
                        text = "Until ${event.stopRepeatingDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
