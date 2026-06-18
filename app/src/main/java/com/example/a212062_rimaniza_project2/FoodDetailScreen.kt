package com.example.a212062_rimaniza_project2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.a212062_rimaniza_project2.ui.theme.AppPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodItem: FoodItemData,
    onBackClick: () -> Unit,
    onFavouriteToggle: () -> Unit,
    onAddToShoppingList: () -> Unit,
    onAddToPlanner: () -> Unit,
    isAdmin: Boolean = false,
    onEditFood: () -> Unit = {},
    onDeleteFood: () -> Unit = {},
    viewModel: FoodViewModel? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = foodItem.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = AppPink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = AppPink,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    if (!foodItem.imageUrl.isNullOrEmpty()) {
                        val bitmap = remember(foodItem.imageUrl) {
                            if (foodItem.imageUrl.startsWith("data:image")) {
                                viewModel?.decodeImage(foodItem.imageUrl.substringAfter("base64,"))
                            } else null
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = foodItem.name,
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = foodItem.imageUrl,
                                contentDescription = foodItem.name,
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.default_pic),
                                error = painterResource(id = R.drawable.default_pic)
                            )
                        }
                    } else {
                        Image(
                            painter = painterResource(id = getImageResource(context, foodItem.imageResName)),
                            contentDescription = foodItem.name,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Shopping Button
                        ActionButton(
                            icon = Icons.Default.ShoppingCart,
                            label = "Shopping List",
                            onClick = onAddToShoppingList
                        )
                        
                        // Planner Button
                        ActionButton(
                            icon = Icons.AutoMirrored.Filled.EventNote,
                            label = "Planner",
                            onClick = onAddToPlanner
                        )
                        
                        // Favourite Button
                        ActionButton(
                            icon = if (foodItem.isFavourite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            label = "Favourite",
                            iconColor = if (foodItem.isFavourite) Color.Red else AppPink,
                            onClick = onFavouriteToggle
                        )

                        if (isAdmin && !foodItem.isCommunity) {
                            // Edit Button
                            ActionButton(
                                icon = Icons.Default.Edit,
                                label = "Edit Food",
                                onClick = onEditFood
                            )

                            // Delete Button
                            ActionButton(
                                icon = Icons.Default.Delete,
                                label = "Delete Food",
                                iconColor = MaterialTheme.colorScheme.error,
                                onClick = { showDeleteDialog = true }
                            )
                        }
                    }
                }
            }

            items(foodItem.sections) { section ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = section.sectionName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppPink,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    section.ingredients.forEach { ingredient ->
                        Text(
                            text = "• $ingredient",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Recipe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    section.instructions.forEachIndexed { index, instruction ->
                        Text(
                            text = "${index + 1}. $instruction",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Food Item") },
            text = { Text("Are you sure you want to delete '${foodItem.name}' from the database? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteFood()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    iconColor: Color = AppPink
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}
