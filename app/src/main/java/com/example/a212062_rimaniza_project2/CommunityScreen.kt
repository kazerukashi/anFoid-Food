package com.example.a212062_rimaniza_project2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.a212062_rimaniza_project2.ui.theme.AppPink
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onMenuClick: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onShareRecipeClick: (String?) -> Unit,
    onNavigateToPostDetail: (Post) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: FoodViewModel,
    refreshTrigger: Int = 0 // Used to trigger refresh from Navbar
) {
    val posts by viewModel.posts.collectAsState()
    val allFoodItems by viewModel.allFoodItems.collectAsState()
    val currentUser = viewModel.currentUser
    var isRefreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val filteredPosts = remember(posts, searchQuery) {
        if (searchQuery.isBlank()) posts
        else posts.filter { it.foodName.contains(searchQuery, ignoreCase = true) }
    }

    var lastScrollIndex by remember { mutableIntStateOf(0) }
    var lastScrollOffset by remember { mutableIntStateOf(0) }
    var isScrollingUp by remember { mutableStateOf(false) }

    val showScrollToTop by remember {
        derivedStateOf {
            val isNotAtTop = lazyListState.firstVisibleItemIndex > 0
            val isCurrentlyScrollingUp = if (lazyListState.firstVisibleItemIndex < lastScrollIndex) {
                true
            } else if (lazyListState.firstVisibleItemIndex == lastScrollIndex) {
                lazyListState.firstVisibleItemScrollOffset < lastScrollOffset
            } else {
                false
            }
            
            if (lazyListState.isScrollInProgress) {
                isScrollingUp = isCurrentlyScrollingUp
            }
            
            lastScrollIndex = lazyListState.firstVisibleItemIndex
            lastScrollOffset = lazyListState.firstVisibleItemScrollOffset
            
            isNotAtTop && isScrollingUp
        }
    }

    val onRefresh: () -> Unit = {
        if (!isRefreshing) {
            isRefreshing = true
            viewModel.fetchPosts()
        }
    }

    // Safety timeout to reset refresh state if network hangs
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            kotlinx.coroutines.delay(5000)
            isRefreshing = false
        }
    }

    LaunchedEffect(posts) {
        isRefreshing = false
    }

    LaunchedEffect(Unit) {
        viewModel.fetchPosts()
    }

    // Refresh when navbar icon is tapped
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            onRefresh()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (!isSearchActive) {
                        Text(
                            text = "Community",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = AppPink,
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    lazyListState.animateScrollToItem(0)
                                }
                            }
                        )
                    } else {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search food in posts...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = AppPink,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = AppPink)
                                    }
                                }
                            }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = AppPink)
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            if (isSearchActive) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = AppPink
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ScrollToTopButton(
                visible = showScrollToTop,
                onClick = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // "Add Post" button like Facebook
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            onClick = {
                                if (currentUser != null) onShareRecipeClick(null)
                                else onNavigateToAuth()
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val userPfp = remember(viewModel.userProfile?.profilePicUrl) {
                                    viewModel.userProfile?.profilePicUrl?.let { viewModel.decodeImage(it) }
                                }
                                if (userPfp != null) {
                                    Image(
                                        bitmap = userPfp.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.AccountCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = AppPink
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Share a recipe with the community...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (filteredPosts.isEmpty() && !isRefreshing) {
                        item {
                            val isOnline = viewModel.isOnline()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Filled.Dashboard else Icons.Filled.WifiOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp),
                                    tint = AppPink.copy(alpha = 0.6f)
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Text(
                                    text = if (isOnline) "No posts found!" else "Not Connected to Internet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(filteredPosts.size) { index ->
                            val post = filteredPosts[index]
                            val isFav = allFoodItems.find { it.id == post.id }?.isFavourite ?: false
                            PostItem(
                                post = post,
                                isFavourite = isFav,
                                onClick = { onNavigateToPostDetail(post) },
                                onFavouriteToggle = {
                                    viewModel.togglePostFavourite(post)
                                },
                                onProfileClick = { onNavigateToProfile(post.userId) },
                                onDeleteClick = { viewModel.deletePost(post.id) },
                                onEditClick = {
                                    onShareRecipeClick(post.id)
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post, 
    onClick: () -> Unit, 
    isFavourite: Boolean = false,
    onFavouriteToggle: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    viewModel: FoodViewModel? = null
) {
    var authorPfp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val currentUser = viewModel?.currentUser
    val isOwnPost = currentUser?.uid == post.userId
    val isAdmin = viewModel?.isAdmin ?: false
    var showDeleteWarning by remember { mutableStateOf(false) }

    LaunchedEffect(post.userId) {
        viewModel?.getProfilePicUrl(post.userId)?.let { encoded ->
            authorPfp = viewModel.decodeImage(encoded)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).clickable(onClick = onProfileClick)
                ) {
                    if (authorPfp != null) {
                        Image(
                            bitmap = authorPfp!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_pfp),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = post.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(post.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (isOwnPost || isAdmin) {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isOwnPost) {
                                DropdownMenuItem(
                                    text = { Text("Edit Post") },
                                    onClick = {
                                        showMenu = false
                                        onEditClick()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    showDeleteWarning = true
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (!post.imageUrl.isNullOrEmpty()) {
                val bitmap = remember(post.imageUrl) {
                    if (post.imageUrl.startsWith("data:image")) {
                        viewModel?.decodeImage(post.imageUrl.substringAfter("base64,"))
                    } else null
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.default_pic),
                        error = painterResource(id = R.drawable.default_pic)
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_pic),
                    contentDescription = "Default Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = post.foodName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppPink
            )
            
            post.components.forEach { component ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = component.sectionName, fontWeight = FontWeight.Bold)
                Text(text = "Ingredients: ${component.ingredients.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Recipe: ${component.instructions.joinToString(". ")}", style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onFavouriteToggle) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, 
                        contentDescription = "Favourite", 
                        modifier = Modifier.size(24.dp),
                        tint = if (isFavourite) Color.Red else AppPink
                    )
                }
            }
        }
    }

    if (showDeleteWarning) {
        AlertDialog(
            onDismissRequest = { showDeleteWarning = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this recipe post? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteWarning = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWarning = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
