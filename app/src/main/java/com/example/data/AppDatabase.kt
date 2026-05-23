package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCritical: Boolean = false,
    val isFlexible: Boolean = false,
    val scheduledTime: String? = null,
    val isCompleted: Boolean = false,
    val isShoppingListHeader: Boolean = false,
    val points: Int = 0
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val name: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val tag: String,
    val isVoiceMemo: Boolean = false,
    val duration: String? = null,
    val hasImage: Boolean = false,
    val imageUri: String? = null,
    val audioUri: String? = null,
    val drawingUri: String? = null,
    val fontName: String? = null,
    val textColor: String? = null,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_stats")
data class DailyStat(
    @PrimaryKey val date: String,
    val focusTimeMinutes: Int = 0,
    val mood: Int = -1,
    val tasksCompleted: Int = 0,
    val pointsEarned: Int = 0
)

@Dao
interface AppDao {

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM shopping_items WHERE taskId = :taskId")
    fun getShoppingItems(taskId: Int): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Query("SELECT COUNT(*) FROM focus_sessions")
    fun getFocusSessionCount(): Flow<Int>

    @Insert
    suspend fun insertFocusSession(session: FocusSession)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getDailyStat(date: String): DailyStat?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun getLast7DaysStats(): Flow<List<DailyStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStat(stat: DailyStat)
}

@Database(entities = [Task::class, ShoppingItem::class, Note::class, FocusSession::class, DailyStat::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}

