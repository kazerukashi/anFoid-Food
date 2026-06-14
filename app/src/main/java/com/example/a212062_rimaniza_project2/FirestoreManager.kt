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
            foodCollection.get().await().toObjects(FoodItemData::class.java)
        } catch (e: Exception) {
            emptyList()
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

    suspend fun addPost(post: Post) {
        try {
            val docRef = postsCollection.document()
            postsCollection.document(docRef.id).set(post.copy(id = docRef.id)).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun getPosts(): List<Post> {
        return try {
            postsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await().toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
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
    
    // Initial data upload helper (to be used once to populate Firestore)
    suspend fun uploadInitialData(items: List<FoodItemData>) {
        items.forEach { item ->
            foodCollection.document(item.id).set(item).await()
        }
    }
}
