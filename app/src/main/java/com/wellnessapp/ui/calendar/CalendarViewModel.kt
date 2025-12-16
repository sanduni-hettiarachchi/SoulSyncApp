package com.wellnessapp.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wellnessapp.data.DataManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for Calendar Fragment
 * Manages calendar data and selected date information
 */
class CalendarViewModel : ViewModel() {

    private lateinit var dataManager: DataManager
    
    private val _selectedDateData = MutableLiveData<DateData?>()
    val selectedDateData: LiveData<DateData?> = _selectedDateData
    
    private val _calendarData = MutableLiveData<Map<String, HabitCompletionStatus>>()
    val calendarData: LiveData<Map<String, HabitCompletionStatus>> = _calendarData

    fun setDataManager(dataManager: DataManager) {
        this.dataManager = dataManager
        loadCalendarData()
    }

    /**
     * Load calendar data for the current month
     */
    private fun loadCalendarData() {
        val calendar = Calendar.getInstance()
        val data = mutableMapOf<String, HabitCompletionStatus>()
        
        // Generate sample data for the current month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            val dateKey = getDateKey(calendar, day)
            val status = getHabitCompletionStatus(day)
            data[dateKey] = status
        }
        
        _calendarData.value = data
    }

    /**
     * Get habit completion status for a specific day
     */
    private fun getHabitCompletionStatus(day: Int): HabitCompletionStatus {
        // Sample implementation - in real app, this would check SharedPreferences
        return when (day % 3) {
            0 -> HabitCompletionStatus.ALL_COMPLETED
            1 -> HabitCompletionStatus.PARTIAL
            else -> HabitCompletionStatus.NONE
        }
    }

    /**
     * Get date key for a specific day
     */
    private fun getDateKey(calendar: Calendar, day: Int): String {
        val date = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, day)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
    }

    /**
     * Select a date and load its data
     */
    fun selectDate(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        
        val dateString = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(calendar.time)
        val mood = getMoodForDate(day)
        val habits = getHabitsForDate(day)
        
        val dateData = DateData(
            dateString = dateString,
            mood = mood,
            habits = habits
        )
        
        _selectedDateData.value = dateData
    }

    /**
     * Get mood for a specific date
     */
    private fun getMoodForDate(day: Int): String {
        val moods = listOf("😊 Happy", "😢 Sad", "😡 Angry", "😴 Tired", "🤩 Excited")
        return moods[day % moods.size]
    }

    /**
     * Get habits for a specific date
     */
    private fun getHabitsForDate(day: Int): List<String> {
        return when (day % 3) {
            0 -> listOf("✅ Meditate 10 min", "✅ Read 20 pages", "✅ Exercise 30 min")
            1 -> listOf("✅ Meditate 10 min", "❌ Read 20 pages", "❌ Exercise 30 min")
            else -> listOf("❌ Meditate 10 min", "❌ Read 20 pages", "❌ Exercise 30 min")
        }
    }

    /**
     * Refresh calendar data
     */
    fun refreshData() {
        loadCalendarData()
    }
}

/**
 * Data class for selected date information
 */
data class DateData(
    val dateString: String,
    val mood: String,
    val habits: List<String>
)
