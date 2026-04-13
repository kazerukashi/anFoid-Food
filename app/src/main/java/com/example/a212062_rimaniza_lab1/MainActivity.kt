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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
data class FoodItemData(val imageRes: Int, val name: String, val isFavourite: Boolean = false)

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

                // State for recent items.
                val recentItems = remember { mutableStateListOf<FoodItemData>() }
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
                            Spacer(modifier = Modifier.size(24.dp)) // Extra padding for the top bar
                            TopBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                isSearchActive = isSearchActive,
                                onSearchToggle = { isSearchActive = it },
                                onMenuClick = { scope.launch { drawerState.open() } }
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Box to allow FloatingActionButton to overlay the scrollable section
                            Box(modifier = Modifier.weight(1f)) {
                                // Scrollable section
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
                                        recentItems = recentItems,
                                        onFoodClick = { clickedItem ->
                                            // Update recent items list: move clicked item to top and limit size
                                            recentItems.removeAll { it.name == clickedItem.name }
                                            recentItems.add(0, clickedItem)
                                            if (recentItems.size > MAX_RECENT_ITEMS) {
                                                recentItems.removeAt(recentItems.size - 1)
                                            }
                                        }
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

                            // Fixed section
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
            }) {
                Icon(Icons.Filled.Menu, contentDescription = "Back", tint = pinkColor)
            }
            TextField(
                singleLine = true,
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search food...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            IconButton(onClick = { /* Action */ }) {
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
    recentItems: List<FoodItemData>,
    onFoodClick: (FoodItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    // Defined once for reuse across categories
    val nasmak = FoodItemData(R.drawable.nasmak, "Nasi Lemak", isFavourite = true)
    val beefrendang = FoodItemData(R.drawable.beefrendang, "Beef Rendang")
    val satay = FoodItemData(R.drawable.satay, "Satay")
    val pekingduck = FoodItemData(R.drawable.pekingduck, "Peking Duck")
    val xiaolongbao = FoodItemData(R.drawable.xiaolongbao, "Xiaolongbao")
    val chowmein = FoodItemData(R.drawable.chowmein, "Chow Mein")
    val butterchicken = FoodItemData(R.drawable.butterchicken, "Butter Chicken", isFavourite = true)
    val biryani = FoodItemData(R.drawable.biryani, "Biryani")
    val samosa = FoodItemData(R.drawable.samosa, "Samosa")
    val pizza = FoodItemData(R.drawable.margherita_pizza, "Margherita Pizza")
    val lasagna = FoodItemData(R.drawable.lasagna, "Lasagna")
    val spaghetti = FoodItemData(R.drawable.spaghetti_bolognese, "Spaghetti Bolognese")

    val originCategories = mapOf(
        "Malay" to listOf(nasmak, beefrendang, satay),
        "Chinese" to listOf(pekingduck, xiaolongbao, chowmein),
        "Indian" to listOf(butterchicken, biryani, samosa),
        "Italian" to listOf(pizza, lasagna, spaghetti)
    )

    // TODO: Automatic labelling based on ingredients list (to be implemented)
    val typeCategories = mapOf(
        "Main Course" to listOf(nasmak, beefrendang, pekingduck, butterchicken, biryani, pizza, lasagna, spaghetti),
        "Appetizer" to listOf(satay, xiaolongbao, samosa),
        "Chicken-based" to listOf(pekingduck, butterchicken, satay),
        "Protein" to listOf(beefrendang, pekingduck, butterchicken, satay),
        "Carbs" to listOf(nasmak, chowmein, biryani, pizza, lasagna, spaghetti),
        "Vegan" to listOf(samosa),
        "Halal" to listOf(nasmak, beefrendang, satay, butterchicken, biryani, samosa),
        "Non-Halal" to listOf(pekingduck, xiaolongbao, chowmein, pizza, lasagna, spaghetti),
        "Dairy" to listOf(butterchicken, pizza, lasagna),
        "Non-Dairy" to listOf(nasmak, beefrendang, satay, pekingduck, xiaolongbao, chowmein, biryani, samosa, spaghetti)
    )

    val favouriteItems = listOf(nasmak, butterchicken)

    val displayCategories = when (selectedCategory) {
        "Origin" -> originCategories
        "Type" -> typeCategories
        "Favourite" -> mapOf("Your Favourites" to favouriteItems)
        "Recent" -> mapOf("Recently Viewed" to recentItems)
        else -> originCategories
    }

    val filteredCategories = displayCategories.mapValues { (_, items) ->
        items.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }.filter { it.value.isNotEmpty() }

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
                FoodRow(items, onFoodClick = onFoodClick)
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
fun FoodRow(items: List<FoodItemData>, onFoodClick: (FoodItemData) -> Unit) {
    // 2. Row handles the scroll. We remove weight(1f) from children so they can expand past screen width.
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        items.forEach { item ->
            FoodItem(item, onClick = { onFoodClick(item) })
        }
    }
}

@Composable
fun FoodItem(item: FoodItemData, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
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
                        .size(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                
                // Small heart icon with circle background for favourites
                if (item.isFavourite) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(32.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favourite",
                            tint = Color(0xFFFF69B4),
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxSize()
                        )
                    }
                }
            }
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
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
                                recentItems = emptyList(),
                                onFoodClick = {}
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
