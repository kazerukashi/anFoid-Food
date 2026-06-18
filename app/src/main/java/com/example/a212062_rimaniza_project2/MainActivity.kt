package com.example.a212062_rimaniza_project2

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.a212062_rimaniza_project2.ui.theme.A212062_Rimaniza_Project2Theme
import com.example.a212062_rimaniza_project2.ui.theme.AppPink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        enableEdgeToEdge()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val viewModel: FoodViewModel = viewModel()
            
            // Collect StateFlows as State for Compose
            val allFoodItems by viewModel.allFoodItems.collectAsState()
            val recentNames by viewModel.recentNames.collectAsState()
            val shoppingItems by viewModel.shoppingItems.collectAsState()
            val plannerEvents by viewModel.plannerEvents.collectAsState()

            A212062_Rimaniza_Project2Theme(darkTheme = viewModel.isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var communityRefreshTrigger by remember { mutableIntStateOf(0) }

                val hideNavBarRoutes = listOf("Profile", "Settings", "Auth")
                val isNavBarVisible = currentRoute !in hideNavBarRoutes && currentRoute?.startsWith("Detail") == false && currentRoute?.startsWith("FoodDetail") == false && currentRoute != "Auth"

                val startDestination = "Home"

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
                                startDestination = startDestination,
                                modifier = Modifier.weight(1f),
                                enterTransition = {
                                    val target = targetState.destination.route ?: ""
                                    val initial = initialState.destination.route ?: ""
                                    val order = listOf("Auth", "Home", "Shopping", "Planner", "Community", "Profile", "Settings", "Detail", "FoodDetail", "AddFood")
                                    
                                    val targetIndex = order.indexOfFirst { target.startsWith(it) }
                                    val initialIndex = order.indexOfFirst { initial.startsWith(it) }
                                    
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
                                    val order = listOf("Auth", "Home", "Shopping", "Planner", "Community", "Profile", "Settings", "Detail", "FoodDetail", "AddFood")
                                    
                                    val targetIndex = order.indexOfFirst { target.startsWith(it) }
                                    val initialIndex = order.indexOfFirst { initial.startsWith(it) }

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
                                    val order = listOf("Auth", "Home", "Shopping", "Planner", "Community", "Profile", "Settings", "Detail", "FoodDetail", "AddFood")
                                    
                                    val targetIndex = order.indexOfFirst { target.startsWith(it) }
                                    val initialIndex = order.indexOfFirst { initial.startsWith(it) }

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
                                    val order = listOf("Auth", "Home", "Shopping", "Planner", "Community", "Profile", "Settings", "Detail", "FoodDetail", "AddFood")
                                    
                                    val targetIndex = order.indexOfFirst { target.startsWith(it) }
                                    val initialIndex = order.indexOfFirst { initial.startsWith(it) }

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
                                composable("Auth") {
                                    AuthScreen(
                                        onAuthSuccess = {
                                            viewModel.syncAllUserData()
                                            navController.popBackStack()
                                        },
                                        onBackClick = {
                                            navController.popBackStack()
                                        },
                                        viewModel = viewModel
                                    )
                                }
                                composable("Home") {
                                    HomeScreen(
                                        searchQuery = viewModel.searchQuery,
                                        onQueryChange = { viewModel.searchQuery = it },
                                        isSearchActive = viewModel.isSearchActive,
                                        onSearchToggle = { viewModel.isSearchActive = it },
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        selectedCategory = viewModel.selectedCategory,
                                        onCategoryClick = { viewModel.selectedCategory = it },
                                        allFoodItems = allFoodItems,
                                        recentNames = recentNames,
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
                                        },
                                        onFavouriteToggle = { toggledItem ->
                                            viewModel.toggleFavourite(toggledItem.id)
                                        },
                                        foodViewModel = viewModel
                                    )
                                }
                                composable("Shopping") {
                                    ShoppingScreen(
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        shoppingItems = shoppingItems,
                                        onAddItem = { viewModel.addShoppingItem(it) },
                                        onUpdateItem = { viewModel.updateShoppingItem(it) },
                                        onDeleteItem = { viewModel.deleteShoppingItem(it) },
                                        onDeleteIngredient = { viewModel.deleteShoppingItemsByIngredient(it) },
                                        onDeleteFood = { viewModel.deleteShoppingItemsByFoodName(it) },
                                        onRenameFood = { old, new -> viewModel.renameShoppingFood(old, new) },
                                        onCheckedChange = { ingredient, checked -> 
                                            viewModel.toggleShoppingItemChecked(ingredient, checked)
                                        },
                                        onItemCheckedChange = { id, checked ->
                                            viewModel.toggleShoppingItemCheckedById(id, checked)
                                        },
                                        displayMode = viewModel.shoppingDisplayMode,
                                        onDisplayModeChange = { viewModel.shoppingDisplayMode = it }
                                    )
                                }
                                composable(
                                    route = "Planner?foodId={foodId}",
                                    arguments = listOf(
                                        androidx.navigation.navArgument("foodId") {
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { backStackEntry ->
                                    val context = LocalContext.current
                                    val preSelectedFoodId = backStackEntry.arguments?.getString("foodId")
                                    
                                    PlannerScreen(
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        plannerEvents = plannerEvents,
                                        allFoodItems = allFoodItems,
                                        onAddEvent = { viewModel.addPlannerEvent(context, it) },
                                        onUpdateEvent = { viewModel.updatePlannerEvent(context, it) },
                                        onDeleteEvent = { viewModel.deletePlannerEvent(context, it) },
                                        preSelectedFoodId = preSelectedFoodId
                                    )
                                }
                                composable("Community") {
                                    CommunityScreen(
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onNavigateToAuth = { navController.navigate("Auth") },
                                        onShareRecipeClick = { postId ->
                                            if (postId != null) navController.navigate("ShareRecipe?postId=$postId")
                                            else navController.navigate("ShareRecipe")
                                        },
                                        viewModel = viewModel,
                                        refreshTrigger = communityRefreshTrigger,
                                        onNavigateToProfile = { userId ->
                                            navController.navigate("Profile?userId=$userId")
                                        },
                                        onNavigateToPostDetail = { post ->
                                            // Create a temporary FoodItemData to reuse FoodDetailScreen
                                            val dummy = FoodItemData(
                                                id = post.id,
                                                name = post.foodName,
                                                sections = post.components,
                                                imageResName = ""
                                            )
                                            // We need a way to pass this data. 
                                            // For now, let's navigate and hope it fetches or use a shared state.
                                            navController.navigate("FoodDetail/${post.id}")
                                        }
                                    )
                                }
                                composable(
                                    route = "ShareRecipe?postId={postId}",
                                    arguments = listOf(
                                        androidx.navigation.navArgument("postId") {
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { backStackEntry ->
                                    val postId = backStackEntry.arguments?.getString("postId")
                                    ShareRecipeScreen(
                                        onBackClick = { navController.popBackStack() },
                                        onPostSuccess = { navController.popBackStack() },
                                        viewModel = viewModel,
                                        editPostId = postId
                                    )
                                }
                                composable(
                                    route = "Profile?userId={userId}",
                                    arguments = listOf(
                                        androidx.navigation.navArgument("userId") {
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { backStackEntry ->
                                    val userId = backStackEntry.arguments?.getString("userId")
                                    ProfileScreen(
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onBackClick = { navController.popBackStack() },
                                        onNavigateToAuth = { navController.navigate("Auth") },
                                        onNavigateToPostDetail = { post ->
                                            navController.navigate("FoodDetail/${post.id}")
                                        },
                                        onNavigateToAddFood = {
                                            navController.navigate("AddFood")
                                        },
                                        viewModel = viewModel,
                                        targetUserId = userId
                                    )
                                }
                                composable(
                                    route = "AddFood?foodId={foodId}",
                                    arguments = listOf(
                                        androidx.navigation.navArgument("foodId") {
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { backStackEntry ->
                                    val foodId = backStackEntry.arguments?.getString("foodId")
                                    ShareRecipeScreen(
                                        onBackClick = { navController.popBackStack() },
                                        onPostSuccess = { navController.popBackStack() },
                                        viewModel = viewModel,
                                        isDatabaseItem = true,
                                        editFoodId = foodId
                                    )
                                }
                                composable("Settings") {
                                    SettingsScreen(
                                        isDarkTheme = viewModel.isDarkTheme,
                                        onThemeChange = { viewModel.updateTheme(it) },
                                        maxRecentItems = viewModel.maxRecentItems,
                                        onMaxRecentItemsChange = { viewModel.updateMaxRecentItems(it) },
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onBackClick = { navController.popBackStack() }
                                    )
                                }
                                composable("Detail/{categoryName}") { backStackEntry ->
                                    val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                                    
                                    val filteredItems = when (viewModel.selectedCategory) {
                                        "Origin" -> allFoodItems.filter { !it.isCommunity && it.origin == categoryName }
                                        "Type" -> allFoodItems.filter { !it.isCommunity && it.tags.contains(categoryName) }
                                        "Favourite" -> allFoodItems.filter { it.isFavourite }
                                        "Recent" -> recentNames.mapNotNull { name -> allFoodItems.find { it.name == name } }
                                        else -> allFoodItems.filter { !it.isCommunity }
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
                                        },
                                        maxRecentItems = viewModel.maxRecentItems,
                                        foodViewModel = viewModel
                                    )
                                }
                                composable("FoodDetail/{foodId}") { backStackEntry ->
                                    val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
                                    val posts by viewModel.posts.collectAsState()
                                    
                                    val foodItem = allFoodItems.find { it.id == foodId } 
                                        ?: posts.find { it.id == foodId }?.let { post ->
                                            FoodItemData(
                                                id = post.id,
                                                name = post.foodName,
                                                sections = post.components,
                                                imageResName = "",
                                                imageUrl = post.imageUrl
                                            )
                                        }
                                    
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
                                                navController.navigate("Planner?foodId=${foodItem.id}")
                                            },
                                            isAdmin = viewModel.isAdmin,
                                            onEditFood = {
                                                navController.navigate("AddFood?foodId=${foodItem.id}")
                                            },
                                            onDeleteFood = {
                                                viewModel.deleteFoodFromDatabase(
                                                    foodId = foodItem.id,
                                                    onSuccess = { navController.popBackStack() },
                                                    onFailure = { /* Handle error */ }
                                                )
                                            },
                                            viewModel = viewModel
                                        )
                                    }
                                }
                            }

                            if (isNavBarVisible) {
                                NavBar(
                                    navController = navController, 
                                    modifier = Modifier.padding(top = 8.dp),
                                    onCommunityRefresh = { communityRefreshTrigger++ }
                                )
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
    allFoodItems: List<FoodItemData>,
    recentNames: List<String>,
    maxRecentItems: Int,
    lazyListState: LazyListState,
    showButton: Boolean,
    coroutineScope: CoroutineScope,
    onMoreClick: (String) -> Unit,
    onFoodClick: (FoodItemData) -> Unit,
    onFavouriteToggle: (FoodItemData) -> Unit,
    foodViewModel: FoodViewModel
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
        
        if (selectedCategory.equals("Recent", ignoreCase = true) && !isSearchActive) {
            Text(
                text = "Number of Recents Saved: $maxRecentItems",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AppPink
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

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
                            onFavouriteToggle = onFavouriteToggle,
                            onMoreClick = onMoreClick,
                            foodViewModel = foodViewModel
                        )
                }
            }

            // Scroll to Top Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                ScrollToTopButton(
                    visible = showButton,
                    onClick = {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ScrollToTopButton(
    visible: Boolean,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        FloatingActionButton(
            onClick = onClick,
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

@Composable
fun TopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchToggle: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isSearchActive) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = AppPink)
            }
            
            Text(
                text = "anFoid Food",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = AppPink
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

        IconButton(
            onClick = { onSearchToggle(!isSearchActive) },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                if (isSearchActive) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.Filled.Search,
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
    foodViewModel: FoodViewModel,
    modifier: Modifier = Modifier
) {
    val filteredCategories by remember(searchQuery, selectedCategory, allFoodItems) {
        derivedStateOf {
            val baseList = when (selectedCategory) {
                "Favourite" -> allFoodItems.filter { it.isFavourite }
                "Recent" -> recentNames.mapNotNull { name -> allFoodItems.find { it.name == name } }
                else -> allFoodItems.filter { !it.isCommunity }
            }

            val itemsWithSearch = if (searchQuery.isBlank()) baseList else {
                baseList.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            if (selectedCategory == "Favourite" || selectedCategory == "Recent") {
                if (itemsWithSearch.isEmpty()) emptyMap()
                else mapOf((if (selectedCategory == "Favourite") "Your Favourites" else "Recently Viewed") to itemsWithSearch)
            } else {
                val groups = mutableMapOf<String, List<FoodItemData>>()
                if (selectedCategory == "Origin") {
                    val originOrder = listOf("Malay", "Chinese", "Indian", "Italian")
                    originOrder.forEach { origin ->
                        val itemsForOrigin = itemsWithSearch.filter { it.origin == origin }
                        if (itemsForOrigin.isNotEmpty()) {
                            groups[origin] = itemsForOrigin
                        }
                    }
                    // Handle any origins not in the explicit list
                    itemsWithSearch.forEach { item ->
                        if (item.origin !in originOrder && !groups.containsKey(item.origin)) {
                            groups[item.origin] = itemsWithSearch.filter { it.origin == item.origin }
                        }
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

    AnimatedContent(
        targetState = filteredCategories,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "CategoryTransition"
    ) { targetCategories ->
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (targetCategories.isEmpty()) {
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
                targetCategories.forEach { (categoryName, items) ->
                    key(categoryName) {
                        var isInfoExpanded by remember { mutableStateOf(false) }

                        Column {
                            FoodSectionHeader(
                                title = categoryName, 
                                onClick = { onMoreClick(categoryName) },
                                showInfoIcon = true,
                                onInfoClick = { isInfoExpanded = !isInfoExpanded }
                            )
                            
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
                                        text = when(categoryName) {
                                            "Malay" -> "Malay food originated from the Malay culture and is known for its rich flavors, featuring coconut milk, lemongrass, and a variety of aromatic spices."
                                            "Chinese" -> "Chinese cuisine in Malaysia features diverse regional styles, emphasizing stir-frying, steaming, and a balance of flavors like soy sauce and ginger."
                                            "Indian" -> "Indian cuisine is celebrated for its complex spice blends, curries, and breads like naan and roti, often using tandoors and clay pots."
                                            "Appetizer" -> "Appetizers are small dishes served before the main course to stimulate the appetite, ranging from savory snacks to light salads."
                                            "Main Course" -> "Main courses are the primary featured dishes of a meal, typically more substantial and satisfying."
                                            else -> "Explore the unique ingredients and traditional cooking methods that make $categoryName dishes so special and delicious."
                                        },
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            if (selectedCategory == "Favourite" || selectedCategory == "Recent") {
                                FoodGridRow(items, onFoodClick = onFoodClick, onFavouriteToggle = onFavouriteToggle, foodViewModel = foodViewModel)
                            } else {
                                FoodRow(items, onFoodClick = onFoodClick, onFavouriteToggle = onFavouriteToggle, foodViewModel = foodViewModel)
                            }
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
    onFavouriteToggle: (FoodItemData) -> Unit,
    foodViewModel: FoodViewModel
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(items) { item ->
            FoodItem(
                item = item,
                onClick = { onFoodClick(item) },
                onFavouriteToggle = { onFavouriteToggle(item) },
                foodViewModel = foodViewModel
            )
        }
    }
}

@Composable
fun FoodGridRow(
    items: List<FoodItemData>,
    onFoodClick: (FoodItemData) -> Unit,
    onFavouriteToggle: (FoodItemData) -> Unit,
    foodViewModel: FoodViewModel
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
                        onFavouriteToggle = { onFavouriteToggle(item) },
                        foodViewModel = foodViewModel
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
    onFavouriteToggle: () -> Unit,
    foodViewModel: FoodViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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
                if (!item.imageUrl.isNullOrEmpty()) {
                    val bitmap = remember(item.imageUrl) {
                        if (item.imageUrl.startsWith("data:image")) {
                            foodViewModel.decodeImage(item.imageUrl.substringAfter("base64,"))
                        } else null
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = item.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.default_pic),
                            error = painterResource(id = R.drawable.default_pic)
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = getImageResource(context, item.imageResName)),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Crop
                    )
                }
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
fun NavBar(
    navController: NavController, 
    onCommunityRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                icon = Icons.AutoMirrored.Filled.EventNote,
                label = "Planner", 
                selected = currentRoute?.startsWith("Planner") == true,
                onClick = { navController.navigate("Planner") }
            )
            NavItem(
                icon = Icons.Filled.Groups, 
                label = "Community", 
                selected = currentRoute == "Community",
                onClick = { 
                    if (currentRoute == "Community") onCommunityRefresh()
                    else navController.navigate("Community") 
                }
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
    A212062_Rimaniza_Project2Theme(darkTheme = true) {
        // Simple preview with hardcoded values
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Preview requires active NavController", modifier = Modifier.padding(16.dp))
        }
    }
}
