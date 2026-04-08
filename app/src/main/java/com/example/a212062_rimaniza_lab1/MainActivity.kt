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
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a212062_rimaniza_lab1.ui.theme.A212062_Rimaniza_Lab1Theme
import kotlinx.coroutines.launch

// Data class for easier item management
data class FoodItemData(val imageRes: Int, val name: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A212062_Rimaniza_Lab1Theme {
                var searchQuery by remember { mutableStateOf("") }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var selectedDrawerItem by remember { mutableStateOf("Home") }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
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
                                onClick = {
                                    selectedDrawerItem = "Home"
                                    scope.launch { drawerState.close() }
                                },
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
                                onClick = {
                                    selectedDrawerItem = "Profile"
                                    scope.launch { drawerState.close() }
                                },
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
                                onClick = {
                                    selectedDrawerItem = "Settings"
                                    scope.launch { drawerState.close() }
                                },
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
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Black
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
                            Search(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
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
                                    Category(onCategoryClick = { /* Handle category selection if needed */ })
                                    FoodCategory(searchQuery = searchQuery)
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
fun Search(
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF242424), shape = RoundedCornerShape(25.dp))
            .padding(horizontal = 4.dp)
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color(0xFFFF69B4))
        }
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search", color = Color.Gray) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        IconButton(onClick = { /* TODO: Perform search */ }) {
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFFFF69B4))
        }
    }
}

@Composable
fun Category(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf("Origin") }

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
                onClick = {
                    selectedCategory = "Favourite"
                    onCategoryClick("Favourite")
                }
            )
            CategoryItem(
                icon = Icons.Filled.AccessTime,
                label = "Recent",
                modifier = Modifier.weight(1f),
                isSelected = selectedCategory == "Recent",
                onClick = {
                    selectedCategory = "Recent"
                    onCategoryClick("Recent")
                }
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
                onClick = {
                    selectedCategory = "Origin"
                    onCategoryClick("Origin")
                }
            )
            CategoryItem(
                icon = Icons.Filled.Flatware,
                label = "Type",
                modifier = Modifier.weight(1f),
                isSelected = selectedCategory == "Type",
                onClick = {
                    selectedCategory = "Type"
                    onCategoryClick("Type")
                }
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
    val backgroundColor = if (isSelected) Color(0xFFFF69B4) else Color(0xFF242424)
    val iconTint = if (isSelected) Color(0xFF242424) else Color(0xFFFF69B4)
    val fontColor = if (isSelected) Color(0xFF242424) else Color.White


    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = iconTint)
        Text(
            text = label,
            fontSize = 18.sp,
            textAlign = TextAlign.Left,
            color = fontColor,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun FoodCategory(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val allCategories = mapOf(
        "Malay" to listOf(
            FoodItemData(R.drawable.nasmak, "Nasi Lemak"),
            FoodItemData(R.drawable.beefrendang, "Beef Rendang"),
            FoodItemData(R.drawable.satay, "Satay"),
        ),
        "Chinese" to listOf(
            FoodItemData(R.drawable.pekingduck, "Peking Duck"),
            FoodItemData(R.drawable.xiaolongbao, "Xiaolongbao"),
            FoodItemData(R.drawable.chowmein, "Chow Mein"),
        ),
        "Indian" to listOf(
            FoodItemData(R.drawable.butterchicken, "Butter Chicken"),
            FoodItemData(R.drawable.biryani, "Biryani"),
            FoodItemData(R.drawable.samosa, "Samosa"),
        ),
        "Italian" to listOf(
            FoodItemData(R.drawable.margherita_pizza, "Margherita Pizza"),
            FoodItemData(R.drawable.lasagna, "Lasagna"),
            FoodItemData(R.drawable.spaghetti_bolognese, "Spaghetti Bolognese"),
        )
    )

    val filteredCategories = allCategories.mapValues { (categoryName, items) ->
        if (categoryName.contains(searchQuery, ignoreCase = true)) {
            items
        } else {
            items.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }.filter { it.value.isNotEmpty() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filteredCategories.isEmpty()) {
            Text(
                text = "No results found for \"$searchQuery\"",
                color = Color.Gray,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                textAlign = TextAlign.Center
            )
        } else {
            filteredCategories.forEach { (categoryName, items) ->
                FoodSectionHeader(categoryName, onClick = { /* Action for category */ })
                FoodRow(items)
            }
        }
    }
}

@Composable
fun FoodSectionHeader(title: String, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            color = Color.White
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "More", tint = Color(0xFFFF69B4))
    }
}

@Composable
fun FoodRow(items: List<FoodItemData>) {
    // 2. Row handles the scroll. We remove weight(1f) from children so they can expand past screen width.
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        items.forEach { item ->
            FoodItem(item.imageRes, item.name)
        }
    }
}

@Composable
fun FoodItem(imageRes: Int, name: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
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
            Image(
                painter = painterResource(imageRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = name,
                fontSize = 18.sp,
                color = Color.White,
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

    NavigationBar(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        containerColor = Color(0xFF242424),
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItem == index
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item, fontSize = 12.sp) },
                selected = isSelected,
                onClick = { selectedItem = index },
                modifier = if (isSelected) {
                    Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF69B4))
                } else Modifier,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF242424),
                    selectedTextColor = Color(0xFF242424),
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodAppPreview() {
    A212062_Rimaniza_Lab1Theme {
        var searchQuery by remember { mutableStateOf("") }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var selectedDrawerItem by remember { mutableStateOf("Home") }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
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
                    Search(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
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
                            Category(onCategoryClick = {})
                            FoodCategory(searchQuery = searchQuery)
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
