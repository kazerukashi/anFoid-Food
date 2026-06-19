package com.example.a212062_rimaniza_project2

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_items ORDER BY name ASC")
    fun getAllFoodItems(): Flow<List<FoodItemData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemData>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateFoodItem(item: FoodItemData)

    @Query("DELETE FROM food_items WHERE id = :foodId")
    suspend fun deleteFoodItem(foodId: String)

    @Query("DELETE FROM food_items")
    suspend fun deleteAll()
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items WHERE userId = :userId")
    fun getShoppingItems(userId: String): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingItem>)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE userId = :userId AND ingredient = :ingredient")
    suspend fun deleteByIngredient(userId: String, ingredient: String)

    @Query("DELETE FROM shopping_items WHERE userId = :userId AND foodName = :foodName")
    suspend fun deleteByFoodName(userId: String, foodName: String)

    @Query("UPDATE shopping_items SET foodName = :newName WHERE userId = :userId AND foodName = :oldName")
    suspend fun updateFoodName(userId: String, oldName: String, newName: String)

    @Query("DELETE FROM shopping_items WHERE userId = :userId OR userId = ''")
    suspend fun clearAll(userId: String)
}

@Dao
interface PlannerDao {
    @Query("SELECT * FROM planner_events WHERE userId = :userId")
    fun getPlannerEvents(userId: String): Flow<List<PlannerEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: PlannerEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<PlannerEvent>)

    @Update
    suspend fun updateEvent(event: PlannerEvent)

    @Delete
    suspend fun deleteEvent(event: PlannerEvent)

    @Query("DELETE FROM planner_events WHERE userId = :userId OR userId = ''")
    suspend fun clearAll(userId: String)
}

@Dao
interface RecentDao {
    @Query("SELECT * FROM recent_items WHERE userId = :userId ORDER BY timestamp DESC")
    fun getRecentItems(userId: String): Flow<List<RecentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(item: RecentItem)

    @Query("DELETE FROM recent_items WHERE userId = :userId AND foodName NOT IN (SELECT foodName FROM recent_items WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun trimRecent(userId: String, limit: Int)

    @Query("DELETE FROM recent_items WHERE userId = :userId OR userId = ''")
    suspend fun clearAll(userId: String)
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSetting)
}

@Database(entities = [FoodItemData::class, ShoppingItem::class, PlannerEvent::class, RecentItem::class, AppSetting::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun plannerDao(): PlannerDao
    abstract fun recentDao(): RecentDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
