package com.example.a212062_rimaniza_project2

import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val foodDao: FoodDao,
    private val shoppingDao: ShoppingDao,
    private val plannerDao: PlannerDao,
    private val recentDao: RecentDao,
    private val settingsDao: SettingsDao,
    private val firestoreManager: FirestoreManager
) {
    // --- Food Items ---
    val allFoodItems: Flow<List<FoodItemData>> = foodDao.getAllFoodItems()

    suspend fun initializeFoodItems() {
        val staticItems = getStaticFoodItems()
        val remoteItems = firestoreManager.getAllFoodItems()
        
        // If remote is missing items, upload them
        if (remoteItems.size < staticItems.size) {
            firestoreManager.uploadInitialData(staticItems)
            // Re-fetch to get the latest state
            val updatedRemote = firestoreManager.getAllFoodItems()
            foodDao.deleteAll()
            foodDao.insertFoodItems(updatedRemote)
        } else {
            foodDao.deleteAll()
            foodDao.insertFoodItems(remoteItems)
        }
    }

    suspend fun updateFoodItem(item: FoodItemData) {
        foodDao.updateFoodItem(item)
    }

    suspend fun addFoodItemToCloud(item: FoodItemData) = firestoreManager.addFoodItem(item)
    suspend fun updateFoodItemInCloud(item: FoodItemData) = firestoreManager.updateFoodItem(item)
    suspend fun deleteFoodItemFromCloud(foodId: String) {
        firestoreManager.deleteFoodItem(foodId)
        foodDao.deleteFoodItem(foodId)
    }

    suspend fun renameOrigin(oldName: String, newName: String) {
        firestoreManager.renameOrigin(oldName, newName)
        // No direct way to update all in Room easily without custom query, 
        // but we can just clear and re-sync
        initializeFoodItems()
    }

    suspend fun deleteOrigin(originName: String) {
        firestoreManager.deleteOrigin(originName)
        initializeFoodItems()
    }

    // --- Community ---
    suspend fun getPosts(): List<Post> = firestoreManager.getPosts()
    suspend fun addPost(post: Post) = firestoreManager.addPost(post)
    suspend fun updatePost(post: Post) = firestoreManager.updatePost(post)
    suspend fun deletePost(postId: String) = firestoreManager.deletePost(postId)

    // --- User Profile ---
    suspend fun saveUserProfile(profile: UserProfile) = firestoreManager.saveUserProfile(profile)
    suspend fun getUserProfile(userId: String) = firestoreManager.getUserProfile(userId)
    suspend fun getEmailByUsername(username: String) = firestoreManager.getEmailByUsername(username)
    suspend fun isUsernameTaken(username: String) = firestoreManager.isUsernameTaken(username)
    suspend fun isEmailTaken(email: String) = firestoreManager.isEmailTaken(email)

    // --- Cloud Sync ---
    suspend fun syncShoppingToCloud(userId: String, items: List<ShoppingItem>) = firestoreManager.saveShoppingItems(userId, items)
    suspend fun syncShoppingFromCloud(userId: String) {
        val cloudItems = firestoreManager.getShoppingItems(userId)
        if (cloudItems.isNotEmpty()) {
            shoppingDao.clearAll(userId)
            shoppingDao.insertItems(cloudItems)
        }
    }

    suspend fun syncPlannerToCloud(userId: String, events: List<PlannerEvent>) = firestoreManager.savePlannerEvents(userId, events)
    suspend fun syncPlannerFromCloud(userId: String) {
        val cloudEvents = firestoreManager.getPlannerEvents(userId)
        if (cloudEvents.isNotEmpty()) {
            plannerDao.clearAll(userId)
            plannerDao.insertEvents(cloudEvents)
        }
    }

    suspend fun syncSettingsToCloud(userId: String, isDark: Boolean, maxRecent: Int) = firestoreManager.saveSettings(userId, isDark, maxRecent)
    suspend fun syncSettingsFromCloud(userId: String): Map<String, Any>? = firestoreManager.getSettings(userId)

    suspend fun syncFavouritesToCloud(userId: String, favIds: List<String>) = firestoreManager.saveFavourites(userId, favIds)
    suspend fun syncFavouritesFromCloud(userId: String): List<String> = firestoreManager.getFavourites(userId)

    // --- Shopping List ---
    fun getShoppingItems(userId: String): Flow<List<ShoppingItem>> = shoppingDao.getShoppingItems(userId)

    suspend fun addShoppingItem(item: ShoppingItem) {
        shoppingDao.insertItem(item)
    }

    suspend fun updateShoppingItem(item: ShoppingItem) {
        shoppingDao.updateItem(item)
    }

    suspend fun deleteShoppingItem(item: ShoppingItem) {
        shoppingDao.deleteItem(item)
    }

    suspend fun deleteShoppingItemsByIngredient(userId: String, ingredient: String) {
        shoppingDao.deleteByIngredient(userId, ingredient)
    }

    suspend fun deleteShoppingItemsByFoodName(userId: String, foodName: String) {
        shoppingDao.deleteByFoodName(userId, foodName)
    }

    suspend fun clearShoppingList(userId: String) {
        shoppingDao.clearAll(userId)
    }

    suspend fun renameShoppingFood(userId: String, oldName: String, newName: String) {
        shoppingDao.updateFoodName(userId, oldName, newName)
    }

    // --- Planner ---
    fun getPlannerEvents(userId: String): Flow<List<PlannerEvent>> = plannerDao.getPlannerEvents(userId)

    suspend fun addPlannerEvent(event: PlannerEvent) {
        plannerDao.insertEvent(event)
    }

    suspend fun updatePlannerEvent(event: PlannerEvent) {
        plannerDao.updateEvent(event)
    }

    suspend fun deletePlannerEvent(event: PlannerEvent) {
        plannerDao.deleteEvent(event)
    }

    suspend fun clearPlanner(userId: String) {
        plannerDao.clearAll(userId)
    }

    // --- Recent Items ---
    fun getRecentItems(userId: String): Flow<List<RecentItem>> = recentDao.getRecentItems(userId)

    suspend fun addToRecent(userId: String, foodName: String, limit: Int) {
        recentDao.insertRecent(RecentItem(userId = userId, foodName = foodName))
        recentDao.trimRecent(userId, limit)
    }

    // --- Settings ---
    suspend fun getSetting(key: String): String? = settingsDao.getSetting(key)
    suspend fun saveSetting(key: String, value: String) {
        settingsDao.saveSetting(AppSetting(key, value))
    }

    // Helper for static data (used during first launch)
    private fun getStaticFoodItems(): List<FoodItemData> {
        return listOf(
            FoodItemData(
                id = "1",
                imageResName = "nasmak",
                name = "Nasi Lemak",
                origin = "Malay",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Rice",
                        ingredients = listOf("2 cups rice", "1 cup coconut milk", "1 cup water", "1 tsp salt", "2 pandan leaves"),
                        instructions = listOf("Wash rice.", "Add coconut milk, water, salt and pandan leaves.", "Cook in a rice cooker.")
                    ),
                    RecipeSection(
                        sectionName = "Sambal",
                        ingredients = listOf("10 dried chilies", "1 onion", "3 garlic cloves", "1 tbsp tamarind juice", "Sugar and salt to taste", "1/2 cup dried anchovies"),
                        instructions = listOf("Blend chilies, onion, and garlic.", "Sauté the blended paste until fragrant.", "Add tamarind juice, sugar, and salt.", "Stir in the fried anchovies.")
                    ),
                    RecipeSection(
                        sectionName = "Accompaniments",
                        ingredients = listOf("2 boiled eggs", "1 cucumber, sliced", "1/2 cup roasted peanuts", "1/2 cup fried anchovies"),
                        instructions = listOf("Serve everything together with the rice and sambal.")
                    )
                ),
            ),
            FoodItemData(
                id = "2",
                imageResName = "margherita_pizza",
                name = "Margherita Pizza",
                origin = "Italian",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Pizza Dough",
                        ingredients = listOf("500g flour", "325ml water", "7g yeast", "1 tsp salt", "1 tbsp olive oil"),
                        instructions = listOf("Mix flour, water, yeast, salt and oil.", "Knead for 10 minutes.", "Let rise for 1 hour.", "Roll out into a pizza shape.")
                    ),
                    RecipeSection(
                        sectionName = "Pizza Sauce",
                        ingredients = listOf("1 can peeled tomatoes", "1 clove garlic, minced", "1 tbsp olive oil", "Handful of fresh basil", "Salt to taste"),
                        instructions = listOf("Crush tomatoes.", "Sauté garlic in olive oil.", "Add tomatoes, basil and salt.", "Simmer for 15 minutes.")
                    ),
                    RecipeSection(
                        sectionName = "Toppings",
                        ingredients = listOf("200g mozzarella cheese", "Fresh basil leaves", "Extra virgin olive oil"),
                        instructions = listOf("Spread sauce on dough.", "Top with cheese and basil.", "Bake at 250°C for 10-12 minutes.", "Drizzle with olive oil.")
                    )
                ),
            ),
            FoodItemData(
                id = "3",
                imageResName = "beefrendang",
                name = "Beef Rendang",
                origin = "Malay",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Rendang",
                        ingredients = listOf("1kg beef, cubed", "2 cups coconut milk", "1 tbsp tamarind paste", "2 turmeric leaves, sliced", "Salt and sugar to taste"),
                        instructions = listOf("Simmer beef with coconut milk and spices.", "Cook until the liquid is absorbed and beef is tender.")
                    )
                )
            ),
            FoodItemData(
                id = "4",
                imageResName = "satay",
                name = "Satay",
                origin = "Malay",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Chicken Skewers",
                        ingredients = listOf("500g chicken breast", "1 tsp turmeric", "1 tbsp lemongrass paste", "Sugar and salt"),
                        instructions = listOf("Marinate chicken.", "Thread onto skewers.", "Grill until cooked.")
                    )
                )
            ),
            FoodItemData(
                id = "5",
                imageResName = "pekingduck",
                name = "Peking Duck",
                origin = "Chinese",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Duck",
                        ingredients = listOf("1 whole duck", "Honey", "Spices"),
                        instructions = listOf("Roast the duck until skin is crispy.")
                    )
                )
            ),
            FoodItemData(
                id = "6",
                imageResName = "xiaolongbao",
                name = "Xiaolongbao",
                origin = "Chinese",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Filling",
                        ingredients = listOf("Pork", "Ginger", "Soup gelatin"),
                        instructions = listOf("Wrap filling in thin dough.", "Steam for 8 minutes.")
                    )
                ),
            ),
            FoodItemData(
                id = "7",
                imageResName = "chowmein",
                name = "Chow Mein",
                origin = "Chinese",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Noodles",
                        ingredients = listOf("Egg noodles", "Soy sauce", "Cabbage", "Pork"),
                        instructions = listOf("Stir fry everything together.")
                    )
                )
            ),
            FoodItemData(
                id = "8",
                imageResName = "butterchicken",
                name = "Butter Chicken",
                origin = "Indian",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Chicken & Sauce",
                        ingredients = listOf("Chicken", "Butter", "Cream", "Tomato", "Garam Masala"),
                        instructions = listOf("Cook chicken.", "Simmer in creamy tomato sauce.")
                    )
                ),
            ),
            FoodItemData(
                id = "9",
                imageResName = "biryani",
                name = "Biryani",
                origin = "Indian",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Rice & Chicken",
                        ingredients = listOf("Basmati rice", "Chicken", "Yogurt", "Saffron"),
                        instructions = listOf("Layer rice and chicken curry.", "Cook together on low heat.")
                    )
                )
            ),
            FoodItemData(
                id = "10",
                imageResName = "samosa",
                name = "Samosa",
                origin = "Indian",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Filling & Pastry",
                        ingredients = listOf("Potato", "Peas", "Spices", "Wheat flour"),
                        instructions = listOf("Fill pastry with spiced potatoes.", "Deep fry until golden.")
                    )
                )
            ),
            FoodItemData(
                id = "11",
                imageResName = "lasagna",
                name = "Lasagna",
                origin = "Italian",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Layers",
                        ingredients = listOf("Pasta sheets", "Minced beef", "Tomato sauce", "Béchamel", "Cheese"),
                        instructions = listOf("Layer everything.", "Bake until cheese is bubbly.")
                    )
                )
            ),
            FoodItemData(
                id = "12",
                imageResName = "spaghetti_bolognese",
                name = "Spaghetti Bolognese",
                origin = "Italian",
                sections = listOf(
                    RecipeSection(
                        sectionName = "Pasta & Sauce",
                        ingredients = listOf("Spaghetti", "Beef", "Tomato", "Onion"),
                        instructions = listOf("Cook spaghetti.", "Prepare sauce and mix.")
                    )
                )
            )
        )
    }

    companion object {
        fun getInitialFoodItems() = emptyList<FoodItemData>() // Compatibility
    }
}
