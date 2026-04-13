package com.example.a212062_rimaniza_lab1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a212062_rimaniza_lab1.ui.theme.A212062_Rimaniza_Lab1Theme
import kotlinx.coroutines.launch

// Data class for easier item management
data class FoodItemData(
    val imageRes: Int, 
    val name: String, 
    val origin: String,
    val ingredients: List<String>,
    val isFavourite: Boolean = false
) {
    // Automatic Labeling System: Tags are derived from ingredients
    val tags: List<String> get() {
        val result = mutableListOf<String>()
        val lowerIngredients = ingredients.map { it.lowercase() }
        
        // Category detection
        if (lowerIngredients.any { it.contains("chicken") || it.contains("beef") || it.contains("lamb") || it.contains("fish") || it.contains("shrimp") }) result.add("Protein")
        if (lowerIngredients.any { it.contains("chicken") }) result.add("Chicken-based")
        if (lowerIngredients.any { it.contains("rice") || it.contains("noodle") || it.contains("flour") || it.contains("dough") || it.contains("pasta") }) result.add("Carbs")
        
        // Dietary restrictions
        val nonVegan = listOf("meat", "chicken", "beef", "fish", "egg", "milk", "cheese", "cream", "butter")
        if (lowerIngredients.none { ing -> nonVegan.any { nv -> ing.contains(nv) } }) result.add("Vegan")
        
        val dairyIng = listOf("milk", "cheese", "cream", "butter", "yogurt")
        if (lowerIngredients.any { ing -> dairyIng.any { d -> ing.contains(d) } }) result.add("Dairy")
        else result.add("Non-Dairy")

        // Halal status (simplified heuristic for demo)
        val nonHalal = listOf("pork", "lard", "wine", "alcohol", "ham", "bacon")
        if (lowerIngredients.none { ing -> nonHalal.any { nh -> ing.contains(nh) } }) result.add("Halal")
        else result.add("Non-Halal")

        return result
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            A212062_Rimaniza_Lab1Theme(darkTheme = isDarkTheme) {
                var searchQuery by remember { mutableStateOf("") }
                var isSearchActive by remember { mutableStateOf(false) }
                var selectedCategory by remember { mutableStateOf("Origin") }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var selectedDrawerItem by remember { mutableStateOf("Home") }
                val pinkColor = Color(0xFFFF69B4)

                // Master list of all food items with Ingredients for Automatic Labeling
                val allFoodItems = remember {
                    mutableStateListOf(
                        FoodItemData(R.drawable.nasmak, "Nasi Lemak", "Malay", listOf("Rice", "Coconut Milk", "Anchovies", "Peanuts", "Egg", "Sambal"), isFavourite = true),
                        FoodItemData(R.drawable.beefrendang, "Beef Rendang", "Malay", listOf("Beef", "Coconut Milk", "Lemongrass", "Galangal", "Spices")),
                        FoodItemData(R.drawable.satay, "Satay", "Malay", listOf("Chicken", "Turmeric", "Lemongrass", "Peanut Sauce")),
                        FoodItemData(R.drawable.pekingduck, "Peking Duck", "Chinese", listOf("Duck", "Hoisin Sauce", "Honey", "Cucumber", "Scallion")),
                        FoodItemData(R.drawable.xiaolongbao, "Xiaolongbao", "Chinese", listOf("Minced Pork", "Wheat Flour", "Soup Broth", "Ginger"), isFavourite = true),
                        FoodItemData(R.drawable.chowmein, "Chow Mein", "Chinese", listOf("Egg Noodles", "Soy Sauce", "Cabbage", "Carrot", "Pork")),
                        FoodItemData(R.drawable.butterchicken, "Butter Chicken", "Indian", listOf("Chicken", "Butter", "Cream", "Tomato", "Garam Masala"), isFavourite = true),
                        FoodItemData(R.drawable.biryani, "Biryani", "Indian", listOf("Basmati Rice", "Chicken", "Yogurt", "Saffron", "Spices")),
                        FoodItemData(R.drawable.samosa, "Samosa", "Indian", listOf("Potato", "Peas", "Wheat Flour", "Spices")),
                        FoodItemData(R.drawable.margherita_pizza, "Margherita Pizza", "Italian", listOf("Dough", "Tomato", "Mozzarella Cheese", "Basil", "Olive Oil"), isFavourite = true),
                        FoodItemData(R.drawable.lasagna, "Lasagna", "Italian", listOf("Pasta", "Minced Beef", "Tomato Sauce", "Béchamel", "Cheese")),
                        FoodItemData(R.drawable.spaghetti_bolognese, "Spaghetti Bolognese", "Italian", listOf("Spaghetti", "Beef", "Tomato", "Onion", "Garlic"))
                    )
                }

                // State for recent items (storing names to keep synced with master list).
                val recentNames = remember { mutableStateListOf<String>() }
                // Limit for recent items. Easy to edit later.
                val MAX_RECENT_ITEMS = 30

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerState = drawerState,
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                            drawerContentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Spacer(Modifier.height(48.dp))
                            
                            // Side Menu Logo
                            Text(
                                text = "anFoid Food",
                                color = pinkColor,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 28.dp)
                                    .padding(top = 16.dp, bottom = 24.dp),
                                textAlign = TextAlign.Start
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                                label = { Text("Home") },
                                selected = selectedDrawerItem == "Home",
                                onClick = {
                                    selectedDrawerItem = "Home"
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = pinkColor.copy(alpha = 0.2f),
                                    selectedIconColor = pinkColor,
                                    selectedTextColor = pinkColor,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = pinkColor,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                label = { Text("Profile") },
                                selected = selectedDrawerItem == "Profile",
                                onClick = {
                                    selectedDrawerItem = "Profile"
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = pinkColor.copy(alpha = 0.2f),
                                    selectedIconColor = pinkColor,
                                    selectedTextColor = pinkColor,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = pinkColor,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                label = { Text("Settings") },
                                selected = selectedDrawerItem == "Settings",
                                onClick = {
                                    selectedDrawerItem = "Settings"
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = pinkColor.copy(alpha = 0.2f),
                                    selectedIconColor = pinkColor,
                                    selectedTextColor = pinkColor,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = pinkColor,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Dark Mode Toggle
                            NavigationDrawerItem(
                                icon = { 
                                    Icon(
                                        if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode, 
                                        contentDescription = null 
                                    ) 
                                },
                                label = { Text("Dark Mode") },
                                selected = false,
                                badge = {
                                    Switch(
                                        checked = isDarkTheme,
                                        onCheckedChange = { isDarkTheme = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = pinkColor,
                                            checkedTrackColor = pinkColor.copy(alpha = 0.5f)
                                        )
                                    )
                                },
                                onClick = { isDarkTheme = !isDarkTheme },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = pinkColor,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val lazyListState = rememberLazyListState()
                        val showButton by remember {
                            derivedStateOf {
                                lazyListState.firstVisibleItemIndex > 2
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            Spacer(modifier = Modifier.size(24.dp))
                            TopBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                isSearchActive = isSearchActive,
                                onSearchToggle = { isSearchActive = it },
                                onMenuClick = { scope.launch { drawerState.open() } }
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                // Optimized: Using LazyColumn instead of Column + verticalScroll
                                LazyColumn(
                                    state = lazyListState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    item {
                                        Category(
                                            selectedCategory = selectedCategory,
                                            onCategoryClick = { selectedCategory = it }
                                        )
                                    }
                                    
                                    // Food Categories as Lazy Items
                                    item {
                                        FoodCategory(
                                            searchQuery = searchQuery,
                                            selectedCategory = selectedCategory,
                                            allFoodItems = allFoodItems,
                                            recentNames = recentNames,
                                            onFoodClick = { clickedItem ->
                                                recentNames.removeAll { it == clickedItem.name }
                                                recentNames.add(0, clickedItem.name)
                                                if (recentNames.size > MAX_RECENT_ITEMS) {
                                                    recentNames.removeAt(recentNames.size - 1)
                                                }
                                            },
                                            onFavouriteToggle = { toggledItem ->
                                                val index = allFoodItems.indexOfFirst { it.name == toggledItem.name }
                                                if (index != -1) {
                                                    allFoodItems[index] = allFoodItems[index].copy(
                                                        isFavourite = !allFoodItems[index].isFavourite
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }

                                // Scroll to Top Button
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showButton,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            scope.launch {
                                                lazyListState.animateScrollToItem(0)
                                            }
                                        },
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        shape = CircleShape,
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.KeyboardArrowUp,
                                            contentDescription = "Scroll to top"
                                        )
                                    }
                                }
                            }

                            NavBar(modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchToggle: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pinkColor = Color(0xFFFF69B4)
    val focusManager = LocalFocusManager.current

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(25.dp))
            .padding(horizontal = 4.dp)
            .height(56.dp)
    ) {
        if (isSearchActive) {
            IconButton(onClick = { 
                onSearchToggle(false)
                onQueryChange("") 
                focusManager.clearFocus()
            }) {
                Icon(Icons.Filled.Menu, contentDescription = "Back", tint = pinkColor)
            }
            TextField(
                singleLine = true,
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search food...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = pinkColor.copy(alpha = 0.7f))
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(onClick = { focusManager.clearFocus() }) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = pinkColor)
            }
        } else {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = pinkColor)
            }
            
            Text(
                text = "anFoid Food",
                color = pinkColor,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = { onSearchToggle(true) }) {
                Icon(Icons.Filled.Search, contentDescription = "Open Search", tint = pinkColor)
            }
        }
    }
}

@Composable
fun Category(
    selectedCategory: String,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoryItem(
                icon = Icons.Filled.Favorite,
                label = "Favourite",
                modifier = Modifier.weight(1f),
                isSelected = selectedCategory == "Favourite",
                onClick = { onCategoryClick("Favourite") }
            )
            CategoryItem(
                icon = Icons.Filled.AccessTime,
                label = "Recent",
                modifier = Modifier.weight(1f),
                isSelected = selectedCategory == "Recent",
                onClick = { onCategoryClick("Recent") }
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoryItem(
                icon = Icons.Filled.Public,
                label = "Origin",
                modifier = Modifier.weight(1f),
                isSelected = selectedCategory == "Origin",
                onClick = { onCategoryClick("Origin") }
            )
            CategoryItem(
                icon = Icons.Filled.Flatware,
                label = "Type",
                modifier = Modifier.weight(1f),
                isSelected = selectedCategory == "Type",
                onClick = { onCategoryClick("Type") }
            )
        }
    }
}

@Composable
fun CategoryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val pinkColor = Color(0xFFFF69B4)
    val backgroundColor = if (isSelected) pinkColor else MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor = if (isSelected) Color.White else pinkColor
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            textAlign = TextAlign.Left,
            color = textColor,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun FoodCategory(
    searchQuery: String,
    selectedCategory: String,
    allFoodItems: List<FoodItemData>,
    recentNames: List<String>,
    onFoodClick: (FoodItemData) -> Unit,
    onFavouriteToggle: (FoodItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    // Optimization: Use derivedStateOf to automatically track state changes in allFoodItems and recentNames.
    // This recalculates only when relevant data changes.
    val filteredCategories by remember(searchQuery, selectedCategory, allFoodItems, recentNames) {
        derivedStateOf {
            val baseList = when (selectedCategory) {
                "Favourite" -> allFoodItems.filter { it.isFavourite }
                "Recent" -> recentNames.mapNotNull { name -> allFoodItems.find { it.name == name } }
                else -> allFoodItems
            }

            val itemsWithSearch = baseList.filter { it.name.contains(searchQuery, ignoreCase = true) }

            if (selectedCategory == "Favourite" || selectedCategory == "Recent") {
                if (itemsWithSearch.isEmpty()) emptyMap()
                else mapOf((if (selectedCategory == "Favourite") "Your Favourites" else "Recently Viewed") to itemsWithSearch)
            } else {
                val groups = mutableMapOf<String, List<FoodItemData>>()
                if (selectedCategory == "Origin") {
                    itemsWithSearch.forEach { item ->
                        groups[item.origin] = (groups[item.origin] ?: emptyList()) + item
                    }
                } else { // "Type"
                    val typeTags = listOf("Main Course", "Appetizer", "Chicken-based", "Protein", "Carbs", "Vegan", "Halal", "Non-Halal", "Dairy", "Non-Dairy")
                    typeTags.forEach { tag ->
                        val itemsForTag = itemsWithSearch.filter { it.tags.contains(tag) }
                        if (itemsForTag.isNotEmpty()) {
                            groups[tag] = itemsForTag
                        }
                    }
                }
                groups
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filteredCategories.isEmpty()) {
            Text(
                text = "No results found for \"$searchQuery\"",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                textAlign = TextAlign.Center
            )
        } else {
            filteredCategories.forEach { (categoryName, items) ->
                FoodSectionHeader(categoryName, onClick = { /* Action for category */ })
                
                if (selectedCategory == "Favourite" || selectedCategory == "Recent") {
                    FoodGridRow(items, onFoodClick = onFoodClick, onFavouriteToggle = onFavouriteToggle)
                } else {
                    FoodRow(items, onFoodClick = onFoodClick, onFavouriteToggle = onFavouriteToggle)
                }
            }
        }
    }
}

@Composable
fun FoodSectionHeader(title: String, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, 
            contentDescription = "More", 
            tint = Color(0xFFFF69B4),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun FoodRow(items: List<FoodItemData>, onFoodClick: (FoodItemData) -> Unit, onFavouriteToggle: (FoodItemData) -> Unit) {
    // Horizontal row for Origin and Type categories (uses fixed width)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        items(items, key = { it.name }) { item ->
            FoodItem(
                item = item, 
                onClick = { onFoodClick(item) },
                onFavouriteToggle = { onFavouriteToggle(item) },
                modifier = Modifier.width(140.dp) // Fixed width restored for horizontal scrolling
            )
        }
    }
}

@Composable
fun FoodGridRow(items: List<FoodItemData>, onFoodClick: (FoodItemData) -> Unit, onFavouriteToggle: (FoodItemData) -> Unit) {
    // Logic updated to arrange items as a 2-column vertical grid:
    // 6 5
    // 4 3
    // 2 1
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    FoodItem(
                        item = item, 
                        onClick = { onFoodClick(item) },
                        onFavouriteToggle = { onFavouriteToggle(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // If there's only one item in the last row, add a spacer to keep alignment
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FoodItem(
    item: FoodItemData, 
    modifier: Modifier = Modifier, 
    onClick: () -> Unit = {},
    onFavouriteToggle: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(item.imageRes),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                
                // Optimized toggleable heart icon with backdrop
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                        .clickable { onFavouriteToggle() }
                ) {
                    Icon(
                        imageVector = if (item.isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (item.isFavourite) Color(0xFFFF69B4) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxSize()
                    )
                }
            }
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun NavBar(
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Recipe", "Shopping", "Planner", "Community")
    val icons = listOf(Icons.AutoMirrored.Filled.MenuBook, Icons.Filled.ShoppingCart, Icons.Filled.CalendarMonth, Icons.Filled.Groups)
    val pinkColor = Color(0xFFFF69B4)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedItem == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) pinkColor else Color.Transparent)
                        .clickable { selectedItem = index },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            icons[index],
                            contentDescription = item,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            item,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodAppPreview() {
    A212062_Rimaniza_Lab1Theme {
        var searchQuery by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }
        var selectedCategory by remember { mutableStateOf("Origin") }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var selectedDrawerItem by remember { mutableStateOf("Home") }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true, // Enable gestures for preview to avoid accidental opening
            drawerContent = {
                // If the drawer is closed, we don't render anything here to keep the preview clean
                if (drawerState.isOpen) {
                    ModalDrawerSheet(
                        drawerState = drawerState,
                        drawerContainerColor = Color(0xFF242424),
                        drawerContentColor = Color.White
                    ) {
                        Spacer(Modifier.height(48.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                            label = { Text("Home") },
                            selected = selectedDrawerItem == "Home",
                            onClick = { selectedDrawerItem = "Home" },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFFFF69B4),
                                selectedIconColor = Color(0xFF242424),
                                selectedTextColor = Color(0xFF242424),
                                unselectedContainerColor = Color.Transparent,
                                unselectedIconColor = Color(0xFFFF69B4),
                                unselectedTextColor = Color.White
                            )
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            label = { Text("Profile") },
                            selected = selectedDrawerItem == "Profile",
                            onClick = { selectedDrawerItem = "Profile" },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFFFF69B4),
                                selectedIconColor = Color(0xFF242424),
                                selectedTextColor = Color(0xFF242424),
                                unselectedContainerColor = Color.Transparent,
                                unselectedIconColor = Color(0xFFFF69B4),
                                unselectedTextColor = Color.White
                            )
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            label = { Text("Settings") },
                            selected = selectedDrawerItem == "Settings",
                            onClick = { selectedDrawerItem = "Settings" },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFFFF69B4),
                                selectedIconColor = Color(0xFF242424),
                                selectedTextColor = Color(0xFF242424),
                                unselectedContainerColor = Color.Transparent,
                                unselectedIconColor = Color(0xFFFF69B4),
                                unselectedTextColor = Color.White
                            )
                        )
                    }
                }
            }
        ) {
            Surface(color = Color.Black) {
                val scrollState = rememberScrollState()
                val showButton by remember {
                    derivedStateOf {
                        scrollState.value > 500
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Spacer(modifier = Modifier.size(24.dp)) // Consistent with main app
                    TopBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        isSearchActive = isSearchActive,
                        onSearchToggle = { isSearchActive = it },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Category(
                                selectedCategory = selectedCategory,
                                onCategoryClick = { selectedCategory = it }
                            )
                            FoodCategory(
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                allFoodItems = emptyList(),
                                recentNames = emptyList(),
                                onFoodClick = {},
                                onFavouriteToggle = {}
                            )
                        }

                        // Scroll to Top Button
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showButton,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        scrollState.animateScrollTo(0)
                                    }
                                },
                                containerColor = Color(0xFFFF69B4),
                                contentColor = Color(0xFF242424),
                                shape = CircleShape,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    Icons.Filled.ArrowUpward,
                                    contentDescription = "Scroll to top"
                                )
                            }
                        }
                    }
                    NavBar(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
