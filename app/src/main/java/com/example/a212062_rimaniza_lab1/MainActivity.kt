package com.example.a212062_rimaniza_lab1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.a212062_rimaniza_lab1.ui.theme.A212062_Rimaniza_Lab1Theme
import com.example.a212062_rimaniza_lab1.ui.theme.AppPink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val viewModel: FoodViewModel = viewModel()
            
            A212062_Rimaniza_Lab1Theme(darkTheme = viewModel.isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val hideNavBarRoutes = listOf("Profile", "Settings")
                val isNavBarVisible = currentRoute !in hideNavBarRoutes && currentRoute?.startsWith("Detail") == false

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerState = drawerState,
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                            drawerContentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Spacer(Modifier.height(48.dp))
                            
                            Text(
                                text = "anFoid Food",
                                color = AppPink,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 28.dp)
                                    .padding(top = 16.dp, bottom = 32.dp),
                                textAlign = TextAlign.Start
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Home Screen") },
                                label = { Text("Home") },
                                selected = currentRoute == "Home",
                                onClick = {
                                    navController.navigate("Home") {
                                        popUpTo("Home") { inclusive = true }
                                    }
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = AppPink.copy(alpha = 0.2f),
                                    selectedIconColor = AppPink,
                                    selectedTextColor = AppPink,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            /*NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                                label = { Text("Community") },
                                selected = currentRoute == "Community",
                                onClick = {
                                    navController.navigate("Community")
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = AppPink.copy(alpha = 0.2f),
                                    selectedIconColor = AppPink,
                                    selectedTextColor = AppPink,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )*/
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Person, contentDescription = "Profile Screen") },
                                label = { Text("Profile") },
                                selected = currentRoute == "Profile",
                                onClick = {
                                    navController.navigate("Profile")
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = AppPink.copy(alpha = 0.2f),
                                    selectedIconColor = AppPink,
                                    selectedTextColor = AppPink,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings Screen") },
                                label = { Text("Settings") },
                                selected = currentRoute == "Settings",
                                onClick = {
                                    navController.navigate("Settings")
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = AppPink.copy(alpha = 0.2f),
                                    selectedIconColor = AppPink,
                                    selectedTextColor = AppPink,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                lazyListState.firstVisibleItemIndex > 0
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = "Home",
                                modifier = Modifier.weight(1f),
                                enterTransition = {
                                    val target = targetState.destination.route ?: ""
                                    val initial = initialState.destination.route ?: ""
                                    val order = listOf("Home", "Shopping", "Planner", "Community", "Profile", "Settings", "Detail", "FoodDetail")
                                    val targetIndex = order.indexOfFirst { target.split("/")[0] == it }
                                    val initialIndex = order.indexOfFirst { initial.split("/")[0] == it }
                                    
                                    val direction = if (targetIndex > initialIndex) {
                                        AnimatedContentTransitionScope.SlideDirection.Left
                                    } else {
                                        AnimatedContentTransitionScope.SlideDirection.Right
                                    }

                                    slideIntoContainer(
                                        direction,
                                        animationSpec = tween(400)
                                    ) + fadeIn(animationSpec = tween(400))
                                },
                                exitTransition = {
                                    val target = targetState.destination.route ?: ""
                                    val initial = initialState.destination.route ?: ""
                                    val order = listOf("Home", "Shopping", "Planner", "Community", "Profile", "Settings", "Detail", "FoodDetail")
                                    val targetIndex = order.indexOfFirst { target.split("/")[0] == it }
                                    val initialIndex = order.indexOfFirst { initial.split("/")[0] == it }

                                    val direction = if (targetIndex > initialIndex) {
                                        AnimatedContentTransitionScope.SlideDirection.Left
                                    } else {
                                        AnimatedContentTransitionScope.SlideDirection.Right
                                    }

                                    slideOutOfContainer(
                                        direction,
                                        animationSpec = tween(400)
                                    ) + fadeOut(animationSpec = tween(400))
                                },
                                popEnterTransition = {
                                    val target = targetState.destination.route ?: ""
                                    val initial = initialState.destination.route ?: ""
                                    val order = listOf("Home", "Shopping", "Planner", "Community", "Profile", "Settings", "Detail", "FoodDetail")
                                    val targetIndex = order.indexOfFirst { target.split("/")[0] == it }
                                    val initialIndex = order.indexOfFirst { initial.split("/")[0] == it }

                                    val direction = if (targetIndex > initialIndex) {
                                        AnimatedContentTransitionScope.SlideDirection.Left
                                    } else {
                                        AnimatedContentTransitionScope.SlideDirection.Right
                                    }

                                    slideIntoContainer(
                                        direction,
                                        animationSpec = tween(400)
                                    ) + fadeIn(animationSpec = tween(400))
                                },
                                popExitTransition = {
                                    val target = targetState.destination.route ?: ""
                                    val initial = initialState.destination.route ?: ""
                                    val order = listOf("Home", "Shopping", "Planner", "Community", "Profile", "Settings", "Detail", "FoodDetail")
                                    val targetIndex = order.indexOfFirst { target.split("/")[0] == it }
                                    val initialIndex = order.indexOfFirst { initial.split("/")[0] == it }

                                    val direction = if (targetIndex > initialIndex) {
                                        AnimatedContentTransitionScope.SlideDirection.Left
                                    } else {
                                        AnimatedContentTransitionScope.SlideDirection.Right
                                    }

                                    slideOutOfContainer(
                                        direction,
                                        animationSpec = tween(400)
                                    ) + fadeOut(animationSpec = tween(400))
                                }
                            ) {
                                composable("Home") {
                                    HomeScreen(
                                        searchQuery = viewModel.searchQuery,
                                        onQueryChange = { viewModel.searchQuery = it },
                                        isSearchActive = viewModel.isSearchActive,
                                        onSearchToggle = { viewModel.isSearchActive = it },
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        selectedCategory = viewModel.selectedCategory,
                                        onCategoryClick = { viewModel.selectedCategory = it },
                                        allFoodItems = viewModel.allFoodItems,
                                        recentNames = viewModel.recentNames,
                                        maxRecentItems = viewModel.maxRecentItems,
                                        lazyListState = lazyListState,
                                        showButton = showButton,
                                        coroutineScope = scope,
                                        onMoreClick = { categoryName ->
                                            navController.navigate("Detail/$categoryName")
                                        },
                                        onFoodClick = { clickedItem ->
                                            viewModel.addToRecent(clickedItem)
                                            navController.navigate("FoodDetail/${clickedItem.id}")
                                        }
                                    )
                                }
                                composable("Shopping") {
                                    ShoppingScreen(
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        shoppingItems = viewModel.shoppingItems,
                                        onAddItem = { viewModel.addShoppingItem(it) },
                                        onUpdateItem = { viewModel.updateShoppingItem(it) },
                                        onDeleteItem = { viewModel.deleteShoppingItem(it) },
                                        onDeleteIngredient = { viewModel.deleteShoppingItemsByIngredient(it) },
                                        onCheckedChange = { ingredient, checked -> 
                                            viewModel.toggleShoppingItemChecked(ingredient, checked)
                                        },
                                        onItemCheckedChange = { id, checked ->
                                            viewModel.toggleShoppingItemCheckedById(id, checked)
                                        }
                                    )
                                }
                                composable("Planner") {
                                    val context = LocalContext.current
                                    PlannerScreen(
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        plannerEvents = viewModel.plannerEvents,
                                        allFoodItems = viewModel.allFoodItems,
                                        onAddEvent = { viewModel.addPlannerEvent(context, it) },
                                        onUpdateEvent = { viewModel.updatePlannerEvent(context, it) },
                                        onDeleteEvent = { viewModel.deletePlannerEvent(context, it) }
                                    )
                                }
                                composable("Community") {
                                    CommunityScreen(
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onGoToProfile = { navController.navigate("Profile") }
                                    )
                                }
                                composable("Profile") {
                                    ProfileScreen(
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onBackClick = { navController.popBackStack() }
                                    )
                                }
                                composable("Settings") {
                                    SettingsScreen(
                                        isDarkTheme = viewModel.isDarkTheme,
                                        onThemeChange = { viewModel.isDarkTheme = it },
                                        maxRecentItems = viewModel.maxRecentItems,
                                        onMaxRecentItemsChange = { viewModel.maxRecentItems = it },
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onBackClick = { navController.popBackStack() }
                                    )
                                }
                                composable("Detail/{categoryName}") { backStackEntry ->
                                    val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                                    
                                    val filteredItems = when (viewModel.selectedCategory) {
                                        "Origin" -> viewModel.allFoodItems.filter { it.origin == categoryName }
                                        "Type" -> viewModel.allFoodItems.filter { it.tags.contains(categoryName) }
                                        "Favourite" -> viewModel.allFoodItems.filter { it.isFavourite }
                                        "Recent" -> viewModel.recentNames.mapNotNull { name -> viewModel.allFoodItems.find { it.name == name } }
                                        else -> viewModel.allFoodItems
                                    }

                                    DetailScreen(
                                        categoryName = categoryName,
                                        items = filteredItems,
                                        onBackClick = { navController.popBackStack() },
                                        onFoodClick = { clickedItem ->
                                            viewModel.addToRecent(clickedItem)
                                            navController.navigate("FoodDetail/${clickedItem.id}")
                                        },
                                        onFavouriteToggle = { toggledItem ->
                                            viewModel.toggleFavourite(toggledItem.id)
                                        }
                                    )
                                }
                                composable("FoodDetail/{foodId}") { backStackEntry ->
                                    val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
                                    val foodItem = viewModel.allFoodItems.find { it.id == foodId }
                                    
                                    if (foodItem != null) {
                                        FoodDetailScreen(
                                            foodItem = foodItem,
                                            onBackClick = { navController.popBackStack() },
                                            onFavouriteToggle = {
                                                viewModel.toggleFavourite(foodId)
                                            },
                                            onAddToShoppingList = {
                                                viewModel.addIngredientsToShoppingList(foodItem)
                                                navController.navigate("Shopping")
                                            },
                                            onAddToPlanner = {
                                                navController.navigate("Planner")
                                            }
                                        )
                                    }
                                }
                            }

                            if (isNavBarVisible) {
                                NavBar(navController = navController, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchToggle: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    selectedCategory: String,
    onCategoryClick: (String) -> Unit,
    allFoodItems: SnapshotStateList<FoodItemData>,
    recentNames: SnapshotStateList<String>,
    maxRecentItems: Int,
    lazyListState: LazyListState,
    showButton: Boolean,
    coroutineScope: CoroutineScope,
    onMoreClick: (String) -> Unit,
    onFoodClick: (FoodItemData) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.size(24.dp))
        TopBar(
            query = searchQuery,
            onQueryChange = onQueryChange,
            isSearchActive = isSearchActive,
            onSearchToggle = onSearchToggle,
            onMenuClick = onMenuClick
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Category(
                        selectedCategory = selectedCategory,
                        onCategoryClick = onCategoryClick
                    )
                }

                item {
                        FoodCategory(
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            allFoodItems = allFoodItems,
                            recentNames = recentNames,
                            onFoodClick = onFoodClick,
                            onFavouriteToggle = { toggledItem ->
                                val index = allFoodItems.indexOfFirst { it.id == toggledItem.id }
                                if (index != -1) {
                                    allFoodItems[index] = allFoodItems[index].copy(
                                        isFavourite = !allFoodItems[index].isFavourite
                                    )
                                }
                            },
                            onMoreClick = onMoreClick
                        )
                }
            }

            // Scroll to Top Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showButton,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(0)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = AppPink)
        }
        
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (!isSearchActive) {
                    Text(
                        text = "anFoid Food",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = AppPink,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = isSearchActive,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        TextField(
                            value = query,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .height(50.dp),
                            placeholder = { Text("Search recipes...", fontSize = 14.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(25.dp),
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AppPink) },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { onQueryChange("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Clear", tint = AppPink)
                                    }
                                }
                            }
                        )
                    }
                }
            }

        IconButton(onClick = { onSearchToggle(!isSearchActive) }) {
            Icon(
                if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                contentDescription = "Search",
                tint = AppPink
            )
        }
    }
}

@Composable
fun Category(
    selectedCategory: String,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Origin", "Type", "Favourite", "Recent")
    val icons = listOf(Icons.Filled.Public, Icons.Filled.Flatware, Icons.Filled.Favorite, Icons.Filled.AccessTime)

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            CategoryItem(
                icon = icons[index],
                label = category,
                isSelected = selectedCategory == category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) AppPink else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
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
    onMoreClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
            val emptyMessage = when {
                searchQuery.isNotEmpty() -> "No results found for \"$searchQuery\""
                selectedCategory == "Favourite" -> "You haven't added any recipes to your favourites."
                selectedCategory == "Recent" -> "You haven't clicked on any recipe."
                else -> "No recipes available."
            }
            
            Text(
                text = emptyMessage,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                textAlign = TextAlign.Center
            )
        } else {
            filteredCategories.forEach { (categoryName, items) ->
                key(categoryName) {
                    var isInfoExpanded by remember { mutableStateOf(false) }

                    Column {
                        FoodSectionHeader(
                            title = categoryName, 
                            onClick = { onMoreClick(categoryName) },
                            showInfoIcon = categoryName == "Malay",
                            onInfoClick = { isInfoExpanded = !isInfoExpanded }
                        )
                        
                        if (categoryName == "Malay") {
                            AnimatedVisibility(
                                visible = isInfoExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Text(
                                        text = "Malay food originated from the Malay culture and is known for its rich flavors, featuring coconut milk, lemongrass, and a variety of aromatic spices.",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        if (selectedCategory == "Favourite" || selectedCategory == "Recent") {
                            FoodGridRow(items, onFoodClick = onFoodClick, onFavouriteToggle = onFavouriteToggle)
                        } else {
                            FoodRow(items, onFoodClick = onFoodClick, onFavouriteToggle = onFavouriteToggle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodSectionHeader(
    title: String, 
    onClick: () -> Unit = {},
    showInfoIcon: Boolean = false,
    onInfoClick: () -> Unit = {}
) {
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
            tint = AppPink,
            modifier = Modifier.size(24.dp)
        )
        if (showInfoIcon) {
            IconButton(onClick = onInfoClick) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = AppPink,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FoodRow(
    items: List<FoodItemData>,
    onFoodClick: (FoodItemData) -> Unit,
    onFavouriteToggle: (FoodItemData) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(items) { item ->
            FoodItem(
                item = item,
                onClick = { onFoodClick(item) },
                onFavouriteToggle = { onFavouriteToggle(item) }
            )
        }
    }
}

@Composable
fun FoodGridRow(
    items: List<FoodItemData>,
    onFoodClick: (FoodItemData) -> Unit,
    onFavouriteToggle: (FoodItemData) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    FoodItem(
                        item = item,
                        modifier = Modifier.weight(1f),
                        onClick = { onFoodClick(item) },
                        onFavouriteToggle = { onFavouriteToggle(item) }
                    )
                }
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
    onClick: () -> Unit,
    onFavouriteToggle: () -> Unit
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Box {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onFavouriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (item.isFavourite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = item.origin,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NavBar(navController: NavController, modifier: Modifier = Modifier) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Filled.RestaurantMenu, 
                label = "Recipe", 
                selected = currentRoute == "Home",
                onClick = {
                    navController.navigate("Home") {
                        popUpTo("Home") { inclusive = true }
                    }
                }
            )
            NavItem(
                icon = Icons.Filled.ShoppingCart, 
                label = "Shopping", 
                selected = currentRoute == "Shopping",
                onClick = { navController.navigate("Shopping") }
            )
            NavItem(
                icon = Icons.Filled.EventNote,
                label = "Planner", 
                selected = currentRoute == "Planner",
                onClick = { navController.navigate("Planner") }
            )
            NavItem(
                icon = Icons.Filled.Groups, 
                label = "Community", 
                selected = currentRoute == "Community",
                onClick = { navController.navigate("Community") }
            )
        }
    }
}

@Composable
fun NavItem(
    icon: ImageVector, 
    label: String, 
    selected: Boolean, 
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) AppPink else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) AppPink else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FoodAppPreview() {
    A212062_Rimaniza_Lab1Theme(darkTheme = true) {
        // Simple preview with hardcoded values
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Preview requires active NavController", modifier = Modifier.padding(16.dp))
        }
    }
}
