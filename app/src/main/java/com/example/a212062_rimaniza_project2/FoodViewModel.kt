package com.example.a212062_rimaniza_project2

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.text.SimpleDateFormat
import java.util.*

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodRepository
    private val firestoreManager = FirestoreManager()

    var currentUser by mutableStateOf<FirebaseUser?>(FirebaseAuth.getInstance().currentUser)
        private set

    fun isOnline(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FoodRepository(
            database.foodDao(),
            database.shoppingDao(),
            database.plannerDao(),
            database.recentDao(),
            database.settingsDao(),
            firestoreManager
        )

        // Listen for Auth changes
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                fetchUserProfile(currentUser!!.uid)
            } else {
                userProfile = null
            }
        }

        // Initialize/Sync database from cloud on startup
        viewModelScope.launch {
            repository.initializeFoodItems()
        }

        // Load Local Settings
        viewModelScope.launch {
            repository.getSetting("isDarkTheme")?.let { isDarkTheme = it.toBoolean() }
            repository.getSetting("maxRecentItems")?.let { maxRecentItems = it.toInt() }
        }
    }

    fun syncAllUserData() {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch {
            fetchUserProfile(userId)
            repository.syncShoppingFromCloud(userId)
            repository.syncPlannerFromCloud(userId)
        }
    }

    // --- UI State (Shared across screens) ---
    var searchQuery by mutableStateOf("")
    var isSearchActive by mutableStateOf(false)
    var selectedCategory by mutableStateOf("Origin")
    var shoppingDisplayMode by mutableStateOf("By Food")
    
    // Persistent settings
    var maxRecentItems by mutableIntStateOf(30)
        private set
    var isDarkTheme by mutableStateOf(true)
        private set

    // --- Community Posts ---
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    fun fetchPosts() {
        viewModelScope.launch {
            val newPosts = repository.getPosts()
            _posts.value = newPosts
        }
    }

    fun updatePost(post: Post) {
        viewModelScope.launch {
            repository.updatePost(post)
            fetchPosts()
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
            fetchPosts()
        }
    }

    fun addPost(userName: String, foodName: String, components: List<RecipeSection>) {
        viewModelScope.launch {
            val newPost = Post(
                userName = userName,
                foodName = foodName,
                components = components,
                userId = currentUser?.uid ?: "Anonymous"
            )
            repository.addPost(newPost)
            fetchPosts() // Refresh
        }
    }

    fun addFoodToDatabase(
        foodName: String,
        origin: String,
        components: List<RecipeSection>,
        imageUrl: String? = null,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newItem = FoodItemData(
                    name = foodName,
                    origin = origin,
                    sections = components,
                    imageUrl = imageUrl,
                    isCommunity = false
                )
                repository.addFoodItemToCloud(newItem)
                // Refresh local data
                repository.initializeFoodItems()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun updateFoodInDatabase(
        foodId: String,
        foodName: String,
        origin: String,
        components: List<RecipeSection>,
        imageUrl: String? = null,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existingItem = allFoodItems.value.find { it.id == foodId }
                val updatedItem = FoodItemData(
                    id = foodId,
                    name = foodName,
                    origin = origin,
                    sections = components,
                    imageUrl = imageUrl,
                    isCommunity = false,
                    isFavourite = existingItem?.isFavourite ?: false
                )
                repository.updateFoodItemInCloud(updatedItem)
                repository.updateFoodItem(updatedItem) // Update local cache
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun deleteFoodFromDatabase(foodId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteFoodItemFromCloud(foodId)
                // We'd also need to delete from local DB or re-sync
                repository.initializeFoodItems()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun renameOrigin(oldName: String, newName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.renameOrigin(oldName, newName)
            onSuccess()
        }
    }

    fun deleteOrigin(originName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteOrigin(originName)
            onSuccess()
        }
    }

    fun shareRecipe(
        foodName: String,
        components: List<RecipeSection>,
        imageUrl: String? = null,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = currentUser ?: run { onFailure("Not logged in"); return }
        viewModelScope.launch {
            try {
                val newPost = Post(
                    userId = user.uid,
                    userName = userProfile?.name ?: "User",
                    foodName = foodName,
                    components = components,
                    imageUrl = imageUrl
                )
                repository.addPost(newPost)
                fetchPosts()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // --- User Profile ---
    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    val isAdmin: Boolean get() = userProfile?.role == "admin"

    fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            userProfile = repository.getUserProfile(userId)
            if (userProfile == null && currentUser != null) {
                // Create profile if missing (e.g. first Google Login)
                val displayName = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "User"
                val newProfile = UserProfile(
                    id = userId,
                    name = displayName,
                    email = currentUser?.email ?: "",
                    bio = "Hello, I am $displayName",
                    role = "user"
                )
                saveUserProfile(newProfile)
            }
            // Sync data from cloud when profile is loaded
            repository.syncShoppingFromCloud(userId)
            repository.syncPlannerFromCloud(userId)
            
            // Sync Settings
            repository.syncSettingsFromCloud(userId)?.let { cloudSettings ->
                val dark = cloudSettings["isDarkTheme"] as? Boolean ?: isDarkTheme
                val max = (cloudSettings["maxRecentItems"] as? Long)?.toInt() ?: maxRecentItems
                updateTheme(dark, sync = false)
                updateMaxRecentItems(max, sync = false)
            }

            // Sync Favourites
            val cloudFavs = repository.syncFavouritesFromCloud(userId)
            if (cloudFavs.isNotEmpty()) {
                val currentItems = allFoodItems.value
                currentItems.forEach { item ->
                    if (cloudFavs.contains(item.id) && !item.isFavourite) {
                        repository.updateFoodItem(item.copy(isFavourite = true))
                    }
                }
            }
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            userProfile = profile
        }
    }

    fun updateEmail(newEmail: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = currentUser ?: return
        user.verifyBeforeUpdateEmail(newEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.localizedMessage ?: "Failed to update email")
                }
            }
    }

    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = currentUser ?: return
        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.localizedMessage ?: "Failed to update password")
                }
            }
    }

    fun sendPasswordReset(emailOrUsername: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            var targetEmail = emailOrUsername
            if (!emailOrUsername.contains("@")) {
                targetEmail = getEmailByUsername(emailOrUsername) ?: ""
            }
            
            if (targetEmail.isEmpty()) {
                onFailure("Username or Email not found")
                return@launch
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(targetEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure(task.exception?.localizedMessage ?: "Failed to send reset email")
                    }
                }
        }
    }

    fun encodeImage(bitmap: Bitmap): String {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos)
        val b = bos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun decodeImage(encodedString: String): Bitmap? {
        return try {
            val b = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(b, 0, b.size)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getProfilePicUrl(userId: String): String? {
        return repository.getUserProfile(userId)?.profilePicUrl
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return repository.getUserProfile(userId)
    }

    suspend fun getEmailByUsername(username: String): String? {
        return repository.getEmailByUsername(username)
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return repository.isUsernameTaken(username)
    }

    suspend fun isEmailTaken(email: String): Boolean {
        return repository.isEmailTaken(email)
    }

    fun syncDataToCloud() {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch {
            repository.syncShoppingToCloud(userId, shoppingItems.value)
            repository.syncPlannerToCloud(userId, plannerEvents.value)
            repository.syncSettingsToCloud(userId, isDarkTheme, maxRecentItems)
            
            val favIds = allFoodItems.value.filter { it.isFavourite }.map { it.id }
            repository.syncFavouritesToCloud(userId, favIds)
        }
    }

    // --- Required Component 5: ViewModel exposing StateFlow to UI ---
    
    val allFoodItems: StateFlow<List<FoodItemData>> = repository.allFoodItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUserFlow = MutableStateFlow<FirebaseUser?>(FirebaseAuth.getInstance().currentUser)
    
    init {
        FirebaseAuth.getInstance().addAuthStateListener { _currentUserFlow.value = it.currentUser }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recentNames: StateFlow<List<String>> = _currentUserFlow.flatMapLatest { user ->
        if (user == null) MutableStateFlow(emptyList())
        else repository.getRecentItems(user.uid).map { items -> items.map { it.foodName } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val shoppingItems: StateFlow<List<ShoppingItem>> = _currentUserFlow.flatMapLatest { user ->
        if (user == null) MutableStateFlow(emptyList())
        else repository.getShoppingItems(user.uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val plannerEvents: StateFlow<List<PlannerEvent>> = _currentUserFlow.flatMapLatest { user ->
        if (user == null) MutableStateFlow(emptyList())
        else repository.getPlannerEvents(user.uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Process / Business Logic (Calling Repository) ---

    fun updateTheme(dark: Boolean, sync: Boolean = true) {
        isDarkTheme = dark
        viewModelScope.launch {
            repository.saveSetting("isDarkTheme", dark.toString())
            if (sync) syncDataToCloud()
        }
    }

    fun updateMaxRecentItems(limit: Int, sync: Boolean = true) {
        maxRecentItems = limit
        viewModelScope.launch {
            repository.saveSetting("maxRecentItems", limit.toString())
            if (sync) syncDataToCloud()
        }
    }
    
    fun toggleFavourite(foodId: String) {
        viewModelScope.launch {
            allFoodItems.value.find { it.id == foodId }?.let {
                repository.updateFoodItem(it.copy(isFavourite = !it.isFavourite))
                syncDataToCloud()
            }
        }
    }

    fun togglePostFavourite(post: Post) {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch {
            // Check if it already exists as a favourite
            val existing = allFoodItems.value.find { it.id == post.id }
            val isFav = existing?.isFavourite ?: false
            
            val item = FoodItemData(
                id = post.id,
                name = post.foodName,
                sections = post.components,
                imageResName = "",
                imageUrl = post.imageUrl,
                isFavourite = !isFav,
                isCommunity = true
            )
            
            // Insert or update local item
            repository.updateFoodItem(item)
            
            syncDataToCloud()
        }
    }

    fun addToRecent(food: FoodItemData) {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch {
            repository.addToRecent(userId, food.name, maxRecentItems)
        }
    }

    fun addIngredientsToShoppingList(food: FoodItemData) {
        val userId = currentUser?.uid ?: return
        food.sections.forEach { section ->
            section.ingredients.forEach { rawIngredient ->
                val trimmed = rawIngredient.trim()
                val parts = trimmed.split(" ")
                
                val (amt, ing) = when {
                    parts.size == 1 -> "" to trimmed
                    else -> {
                        val amountPart = parts.dropLast(1).joinToString(" ")
                        val ingredientPart = parts.last()
                        amountPart to ingredientPart
                    }
                }

                addShoppingItem(
                    ShoppingItem(
                        userId = userId,
                        foodName = food.name,
                        ingredient = ing,
                        amount = amt
                    )
                )
            }
        }
    }

    // --- Planner Processes ---
    fun addPlannerEvent(context: Context, event: PlannerEvent) {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch {
            repository.addPlannerEvent(event.copy(userId = userId))
            scheduleNotification(context, event)
            syncDataToCloud()
        }
    }

    fun updatePlannerEvent(context: Context, event: PlannerEvent) {
        val existing = plannerEvents.value.find { it.id == event.id }
        viewModelScope.launch {
            existing?.let { cancelNotification(context, it) }
            repository.updatePlannerEvent(event)
            scheduleNotification(context, event)
            syncDataToCloud()
        }
    }

    fun deletePlannerEvent(context: Context, event: PlannerEvent) {
        viewModelScope.launch {
            cancelNotification(context, event)
            repository.deletePlannerEvent(event)
            syncDataToCloud()
        }
    }

    // Notification helpers
    private fun scheduleNotification(context: Context, event: PlannerEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.a212062_rimaniza_project2.ALARM_ACTION"
            putExtra("EXTRA_TITLE", "Meal Reminder: ${event.title}")
            putExtra("EXTRA_MESSAGE", "Time for your scheduled ${event.title}!")
            putExtra("EXTRA_ID", event.id.hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, event.id.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        try {
            val date = sdf.parse("${event.date} ${event.time}")
            if (date != null && date.after(Date())) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date.time, pendingIntent)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun cancelNotification(context: Context, event: PlannerEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.a212062_rimaniza_project2.ALARM_ACTION"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, event.id.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    // --- Shopping List Processes ---
    fun addShoppingItem(newItem: ShoppingItem) {
        val userId = currentUser?.uid ?: return
        val trimmedIngredient = newItem.ingredient.trim()
        val trimmedFoodName = newItem.foodName?.trim()?.takeIf { it.isNotBlank() } ?: "Ungrouped"
        
        val existing = shoppingItems.value.find { 
            it.ingredient.trim().equals(trimmedIngredient, ignoreCase = true) &&
            (it.foodName?.trim() ?: "Ungrouped").equals(trimmedFoodName, ignoreCase = true)
        }

        viewModelScope.launch {
            if (existing != null) {
                repository.updateShoppingItem(existing.copy(
                    amount = mergeAmounts(existing.amount, newItem.amount),
                    isChecked = false
                ))
            } else {
                repository.addShoppingItem(newItem.copy(
                    userId = userId,
                    ingredient = trimmedIngredient,
                    foodName = trimmedFoodName
                ))
            }
            syncDataToCloud()
        }
    }

    private fun mergeAmounts(amount1: String, amount2: String): String {
        if (amount1.isBlank()) return amount2
        if (amount2.isBlank()) return amount1
        val regex = """^(\d+\.?\d*)\s*(.*)$""".toRegex()
        val match1 = regex.find(amount1.trim()); val match2 = regex.find(amount2.trim())
        if (match1 != null && match2 != null) {
            val val1 = match1.groupValues[1].toDoubleOrNull() ?: 0.0
            val unit1 = match1.groupValues[2].trim()
            val val2 = match2.groupValues[1].toDoubleOrNull() ?: 0.0
            val unit2 = match2.groupValues[2].trim()
            fun normalize(u: String) = u.lowercase().removeSuffix("es").removeSuffix("s")
            if (normalize(unit1) == normalize(unit2)) {
                val total = val1 + val2
                val displayTotal = if (total % 1.0 == 0.0) total.toInt().toString() else total.toString()
                val finalUnit = if (total > 1) (if (unit1.length > unit2.length) unit1 else unit2) else (if (unit1.length < unit2.length) unit1 else unit2)
                return if (amount1.trim().contains(" ") || amount2.trim().contains(" ")) "$displayTotal $finalUnit" else "$displayTotal$finalUnit"
            }
        }
        return if (amount1.contains(amount2)) amount1 else "$amount1, $amount2"
    }

    fun updateShoppingItem(item: ShoppingItem) {
        viewModelScope.launch { 
            repository.updateShoppingItem(item)
            syncDataToCloud()
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch { 
            repository.deleteShoppingItem(item)
            syncDataToCloud()
        }
    }

    fun deleteShoppingItemsByIngredient(ingredient: String) {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch { 
            repository.deleteShoppingItemsByIngredient(userId, ingredient)
            syncDataToCloud()
        }
    }

    fun deleteShoppingItemsByFoodName(foodName: String) {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch {
            repository.deleteShoppingItemsByFoodName(userId, foodName)
            syncDataToCloud()
        }
    }

    fun renameShoppingFood(oldName: String, newName: String) {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch {
            repository.renameShoppingFood(userId, oldName, newName)
            syncDataToCloud()
        }
    }

    fun toggleShoppingItemChecked(ingredient: String, isChecked: Boolean) {
        shoppingItems.value.filter { it.ingredient == ingredient }.forEach { item ->
            viewModelScope.launch { 
                repository.updateShoppingItem(item.copy(isChecked = isChecked))
                syncDataToCloud()
            }
        }
    }

    fun toggleShoppingItemCheckedById(id: String, isChecked: Boolean) {
        shoppingItems.value.find { it.id == id }?.let { item ->
            viewModelScope.launch { 
                repository.updateShoppingItem(item.copy(isChecked = isChecked))
                syncDataToCloud()
            }
        }
    }
}
