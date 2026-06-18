package com.example.a212062_rimaniza_project2

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val foodCollection = db.collection("food_items")
    private val postsCollection = db.collection("posts")
    private val usersCollection = db.collection("users")

    suspend fun getAllFoodItems(): List<FoodItemData> {
        return try {
            foodCollection.get(com.google.firebase.firestore.Source.SERVER).await().toObjects(FoodItemData::class.java)
        } catch (e: Exception) {
            try {
                foodCollection.get(com.google.firebase.firestore.Source.CACHE).await().toObjects(FoodItemData::class.java)
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    // --- Shopping List Sync ---
    suspend fun saveShoppingItems(userId: String, items: List<ShoppingItem>) {
        try {
            val userShopping = usersCollection.document(userId).collection("shopping")
            // Simple approach: replace all
            items.forEach { item ->
                userShopping.document(item.id).set(item).await()
            }
        } catch (e: Exception) {}
    }

    suspend fun getShoppingItems(userId: String): List<ShoppingItem> {
        return try {
            usersCollection.document(userId).collection("shopping")
                .get().await().toObjects(ShoppingItem::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Planner Sync ---
    suspend fun savePlannerEvents(userId: String, events: List<PlannerEvent>) {
        try {
            val userPlanner = usersCollection.document(userId).collection("planner")
            events.forEach { event ->
                userPlanner.document(event.id).set(event).await()
            }
        } catch (e: Exception) {}
    }

    suspend fun getPlannerEvents(userId: String): List<PlannerEvent> {
        return try {
            usersCollection.document(userId).collection("planner")
                .get().await().toObjects(PlannerEvent::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Settings Sync ---
    suspend fun saveSettings(userId: String, isDark: Boolean, maxRecent: Int) {
        try {
            val settings = mapOf("isDarkTheme" to isDark, "maxRecentItems" to maxRecent)
            usersCollection.document(userId).collection("settings").document("preferences").set(settings).await()
        } catch (e: Exception) {}
    }

    suspend fun getSettings(userId: String): Map<String, Any>? {
        return try {
            usersCollection.document(userId).collection("settings").document("preferences")
                .get(com.google.firebase.firestore.Source.SERVER).await().data
        } catch (e: Exception) { null }
    }

    // --- Favourites Sync ---
    suspend fun saveFavourites(userId: String, favIds: List<String>) {
        try {
            usersCollection.document(userId).collection("settings").document("favourites")
                .set(mapOf("ids" to favIds)).await()
        } catch (e: Exception) {}
    }

    suspend fun getFavourites(userId: String): List<String> {
        return try {
            val data = usersCollection.document(userId).collection("settings").document("favourites")
                .get(com.google.firebase.firestore.Source.SERVER).await().data
            @Suppress("UNCHECKED_CAST")
            (data?.get("ids") as? List<String>) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addPost(post: Post) {
        try {
            val docRef = postsCollection.document()
            postsCollection.document(docRef.id).set(post.copy(id = docRef.id)).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun updatePost(post: Post) {
        try {
            postsCollection.document(post.id).set(post).await()
        } catch (e: Exception) {}
    }

    suspend fun deletePost(postId: String) {
        try {
            postsCollection.document(postId).delete().await()
        } catch (e: Exception) {}
    }

    suspend fun getPosts(): List<Post> {
        return try {
            // Use Source.SERVER to force fetching from the network and ignore cache
            val snapshot = postsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                .get(com.google.firebase.firestore.Source.SERVER).await()
            snapshot.toObjects(Post::class.java)
        } catch (e: Exception) {
            // Fallback to cache if server fetch fails
            try {
                postsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                    .get(com.google.firebase.firestore.Source.CACHE).await().toObjects(Post::class.java)
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        try {
            usersCollection.document(profile.id).set(profile).await()
        } catch (e: Exception) {}
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            usersCollection.document(userId).get().await().toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getEmailByUsername(username: String): String? {
        return try {
            usersCollection.whereEqualTo("name", username).limit(1).get().await()
                .documents.firstOrNull()?.getString("email")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val result = usersCollection.whereEqualTo("name", username).limit(1)
                .get(com.google.firebase.firestore.Source.SERVER).await()
            if (result.isEmpty) return false
            
            // Optional: If we want to allow taking back a username from a deleted account
            // we'd need to verify if the UID in the doc is still valid in Auth.
            // For now, let's just return true if it exists.
            !result.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isEmailTaken(email: String): Boolean {
        return try {
            val result = usersCollection.whereEqualTo("email", email).limit(1).get().await()
            !result.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    // Initial data upload helper (to be used once to populate Firestore)
    suspend fun uploadInitialData(items: List<FoodItemData>) {
        items.forEach { item ->
            foodCollection.document(item.id).set(item).await()
        }
    }

    suspend fun addFoodItem(item: FoodItemData) {
        try {
            val docRef = foodCollection.document()
            foodCollection.document(docRef.id).set(item.copy(id = docRef.id)).await()
        } catch (e: Exception) {}
    }

    suspend fun updateFoodItem(item: FoodItemData) {
        try {
            foodCollection.document(item.id).set(item).await()
        } catch (e: Exception) {}
    }

    suspend fun deleteFoodItem(foodId: String) {
        try {
            foodCollection.document(foodId).delete().await()
        } catch (e: Exception) {}
    }

    suspend fun renameOrigin(oldName: String, newName: String) {
        try {
            val items = foodCollection.whereEqualTo("origin", oldName).get().await()
            for (doc in items.documents) {
                doc.reference.update("origin", newName).await()
            }
        } catch (e: Exception) {}
    }

    suspend fun deleteOrigin(originName: String) {
        try {
            val items = foodCollection.whereEqualTo("origin", originName).get().await()
            for (doc in items.documents) {
                doc.reference.update("origin", "").await()
            }
        } catch (e: Exception) {}
    }
}
