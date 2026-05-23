package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    val allTasks: Flow<List<Task>> = appDao.getAllTasks()
    val allShoppingItems: Flow<List<ShoppingItem>> = appDao.getAllShoppingItems()
    val allNotes: Flow<List<Note>> = appDao.getAllNotes()
    val focusSessionCount: Flow<Int> = appDao.getFocusSessionCount()

    suspend fun insertTask(task: Task) = appDao.insertTask(task)
    suspend fun updateTask(task: Task) = appDao.updateTask(task)
    suspend fun deleteTask(task: Task) = appDao.deleteTask(task)

    fun getShoppingItemsForTask(taskId: Int) = appDao.getShoppingItems(taskId)
    suspend fun insertShoppingItem(item: ShoppingItem) = appDao.insertShoppingItem(item)
    suspend fun updateShoppingItem(item: ShoppingItem) = appDao.updateShoppingItem(item)

    suspend fun insertNote(note: Note) = appDao.insertNote(note)
    suspend fun updateNote(note: Note) = appDao.updateNote(note)
    suspend fun deleteNote(note: Note) = appDao.deleteNote(note)

    suspend fun insertFocusSession(session: FocusSession) = appDao.insertFocusSession(session)

    val last7DaysStats: Flow<List<DailyStat>> = appDao.getLast7DaysStats()
    suspend fun getDailyStat(date: String): DailyStat? = appDao.getDailyStat(date)
    suspend fun insertDailyStat(stat: DailyStat) = appDao.insertDailyStat(stat)
}
