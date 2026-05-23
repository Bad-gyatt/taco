package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val userName: StateFlow<String> = authRepository.userName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val theme: StateFlow<String> = authRepository.theme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "system"
    )

    val lang: StateFlow<String> = authRepository.lang.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "uk"
    )

    fun setTheme(t: String) { viewModelScope.launch { authRepository.updateTheme(t) } }
    fun setLanguage(l: String) { viewModelScope.launch { authRepository.updateLang(l) } }

    val tasks: StateFlow<List<Task>> = appRepository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shoppingItems: StateFlow<List<ShoppingItem>> = appRepository.allShoppingItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notes: StateFlow<List<Note>> = appRepository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val focusSessionsData: StateFlow<Int> = appRepository.focusSessionCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val last7DaysStats: StateFlow<List<DailyStat>> = appRepository.last7DaysStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun todayString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    fun updateMood(mood: Int) {
        viewModelScope.launch {
            val date = todayString()
            val stat = appRepository.getDailyStat(date) ?: DailyStat(date)
            appRepository.insertDailyStat(stat.copy(mood = mood))
        }
    }

    fun login(name: String) {
        viewModelScope.launch {
            authRepository.login(name)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun addTask(title: String, desc: String, points: Int, isCritical: Boolean, isFlexible: Boolean, isShopping: Boolean = false, scheduledTime: String? = null) {
        viewModelScope.launch {
            appRepository.insertTask(Task(title = title, description = desc, points = points, isCritical = isCritical, isFlexible = isFlexible, isShoppingListHeader = isShopping, scheduledTime = scheduledTime))
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val willBeCompleted = !task.isCompleted
            appRepository.updateTask(task.copy(isCompleted = willBeCompleted))
            
            if (willBeCompleted) {
                val date = todayString()
                val stat = appRepository.getDailyStat(date) ?: DailyStat(date)
                appRepository.insertDailyStat(stat.copy(
                    tasksCompleted = stat.tasksCompleted + 1,
                    pointsEarned = stat.pointsEarned + task.points
                ))
            } else {
                 // optionally remove points if toggled back, but maybe not since tasksCompleted isn't decremented
                val date = todayString()
                val stat = appRepository.getDailyStat(date) ?: DailyStat(date)
                appRepository.insertDailyStat(stat.copy(
                    tasksCompleted = maxOf(0, stat.tasksCompleted - 1),
                    pointsEarned = maxOf(0, stat.pointsEarned - task.points)
                ))
            }
        }
    }

    fun addNote(note: Note) {
        viewModelScope.launch {
            appRepository.insertNote(note)
        }
    }

    fun finishFocusSession(duration: Int) {
        viewModelScope.launch {
            appRepository.insertFocusSession(FocusSession(durationMinutes = duration))
            val date = todayString()
            val stat = appRepository.getDailyStat(date) ?: DailyStat(date)
            appRepository.insertDailyStat(stat.copy(focusTimeMinutes = stat.focusTimeMinutes + duration))
        }
    }
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val authRepo = AuthRepository(context)
            val db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_db")
                .fallbackToDestructiveMigration(true)
                .build()
            val appRepo = AppRepository(db.appDao())
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(authRepo, appRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
