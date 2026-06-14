package com.example.a212062_rimaniza_project2

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val foodCollection = db.collection("food_items")
    private val postsCollection = db.collection("posts")

    suspend fun getAllFoodItems(): List<FoodItemData> {
        return try {
            foodCollection.get().await().toObjects(FoodItemData::class.java)
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
    
    // Initial data upload helper (to be used once to populate Firestore)
    suspend fun uploadInitialData(items: List<FoodItemData>) {
        items.forEach { item ->
            foodCollection.document(item.id).set(item).await()
        }
    }
}
