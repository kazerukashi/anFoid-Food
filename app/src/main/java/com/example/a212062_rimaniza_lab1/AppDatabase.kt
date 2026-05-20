package com.example.a212062_rimaniza_lab1

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
    @Query("SELECT * FROM food_items")
    fun getAllFoodItems(): Flow<List<FoodItemData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemData>)

    @Update
    suspend fun updateFoodItem(item: FoodItemData)
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items")
    fun getShoppingItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE ingredient = :ingredient")
    suspend fun deleteByIngredient(ingredient: String)

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAll()
}

@Dao
interface PlannerDao {
    @Query("SELECT * FROM planner_events")
    fun getPlannerEvents(): Flow<List<PlannerEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: PlannerEvent)

    @Update
    suspend fun updateEvent(event: PlannerEvent)

    @Delete
    suspend fun deleteEvent(event: PlannerEvent)
}

@Dao
interface RecentDao {
    @Query("SELECT * FROM recent_items ORDER BY timestamp DESC")
    fun getRecentItems(): Flow<List<RecentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(item: RecentItem)

    @Query("DELETE FROM recent_items WHERE foodName NOT IN (SELECT foodName FROM recent_items ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun trimRecent(limit: Int)
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSetting)
}

@Database(entities = [FoodItemData::class, ShoppingItem::class, PlannerEvent::class, RecentItem::class, AppSetting::class], version = 2, exportSchema = false)
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
