package com.wellnessapp.ui.mood

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.wellnessapp.R
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.Mood
import com.wellnessapp.data.MoodTypes
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for displaying mood trend charts using MPAndroidChart
 * 
 * Features:
 * - Interactive line chart showing mood trends over time
 * - Multiple time periods (week, month, year)
 * - Statistics display (average mood, total entries, streak)
 * - Share functionality for mood charts
 */
class MoodChartFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var btnWeek: Button
    private lateinit var btnMonth: Button
    private lateinit var btnYear: Button
    private lateinit var tvAverageMood: TextView
    private lateinit var tvTotalEntries: TextView
    private lateinit var tvStreak: TextView
    private lateinit var btnShareChart: Button
    private lateinit var btnChartSettings: Button

    private lateinit var dataManager: DataManager
    private var currentPeriod = ChartPeriod.WEEK
    private val moods = mutableListOf<Mood>()

    enum class ChartPeriod(val days: Int) {
        WEEK(7),
        MONTH(30),
        YEAR(365)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mood_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        lineChart = view.findViewById(R.id.lineChart)
        btnWeek = view.findViewById(R.id.btnWeek)
        btnMonth = view.findViewById(R.id.btnMonth)
        btnYear = view.findViewById(R.id.btnYear)
        tvAverageMood = view.findViewById(R.id.tvAverageMood)
        tvTotalEntries = view.findViewById(R.id.tvTotalEntries)
        tvStreak = view.findViewById(R.id.tvStreak)
        btnShareChart = view.findViewById(R.id.btnShareChart)
        btnChartSettings = view.findViewById(R.id.btnChartSettings)
        
        dataManager = DataManager.getInstance(requireContext())
        setupUI()
        setupChart()
        loadMoodData()
        setupListeners()
    }

    /**
     * Sets up the UI components
     */
    private fun setupUI() {
        // Set up period buttons
        updatePeriodButtons()
    }

    /**
     * Sets up the MPAndroidChart configuration
     */
    private fun setupChart() {
        // Configure chart appearance
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setBackgroundColor(Color.WHITE)

        // Configure legend
        val legend = lineChart.legend
        legend.isEnabled = false

        // Configure X-axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textColor = Color.parseColor("#666666")
        xAxis.textSize = 12f

        // Configure Y-axis
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawAxisLine(true)
        leftAxis.textColor = Color.parseColor("#666666")
        leftAxis.textSize = 12f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 5f
        leftAxis.setLabelCount(6, true)

        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false

        // Configure animation
        lineChart.animateX(1000)
    }

    /**
     * Loads mood data from DataManager
     */
    private fun loadMoodData() {
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (currentPeriod.days * 24 * 60 * 60 * 1000L)
        
        moods.clear()
        moods.addAll(dataManager.getMoodsInRange(startDate, endDate))
        
        updateChart()
        updateStatistics()
    }

    /**
     * Updates the chart with current mood data
     */
    private fun updateChart() {
        val entries = mutableListOf<Entry>()

        // Group moods by date and calculate average mood for each day
        val moodByDate = moods.groupBy { 
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }

        val sortedDates = moodByDate.keys.sorted()
        
        sortedDates.forEachIndexed { index, date ->
            val dayMoods = moodByDate[date] ?: return@forEachIndexed
            val averageMood = calculateAverageMood(dayMoods)
            val entry = Entry(index.toFloat(), averageMood)
            entry.data = getMoodEmoji(averageMood) // Add emoji based on mood value
            entries.add(entry)
        }

        // Create dataset
        val dataSet = LineDataSet(entries, "Mood Trend").apply {
            color = Color.parseColor("#FF9800") // Orange color
            setCircleColor(Color.parseColor("#FF9800")) // Orange color
            lineWidth = 3f
            circleRadius = 6f
            setDrawCircleHole(false)
            setDrawValues(true) // Enable values to show emojis
            valueTextSize = 16f
            valueTextColor = Color.BLACK
            setDrawFilled(true)
            fillColor = Color.parseColor("#80FF9800") // Orange with transparency
            mode = LineDataSet.Mode.CUBIC_BEZIER
            
            // Custom value formatter to show emojis
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: com.github.mikephil.charting.utils.ViewPortHandler?): String {
                    return entry?.data as? String ?: ""
                }
            }
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Update X-axis labels
        updateXAxisLabels(sortedDates)
        
        lineChart.invalidate()
    }

    /**
     * Calculates average mood value for a list of moods
     */
    private fun calculateAverageMood(dayMoods: List<Mood>): Float {
        val moodValues = dayMoods.map { mood ->
            when (mood.id.lowercase()) {
                "sad" -> 1f
                "neutral" -> 2f
                "happy" -> 3f
                "excited" -> 4f
                "calm" -> 3f
                "anxious" -> 2f
                "angry" -> 1f
                "grateful" -> 4f
                else -> 2.5f
            }
        }
        return moodValues.average().toFloat()
    }
    
    /**
     * Get emoji based on mood value
     */
    private fun getMoodEmoji(moodValue: Float): String {
        return when {
            moodValue >= 4.0f -> "🤩" // Excited
            moodValue >= 3.0f -> "😊" // Happy
            moodValue >= 2.0f -> "😐" // Neutral
            moodValue >= 1.0f -> "😔" // Sad
            else -> "😢" // Very sad
        }
    }

    /**
     * Updates X-axis labels with appropriate date format
     */
    private fun updateXAxisLabels(dates: List<Long>) {
        val xAxis = lineChart.xAxis
        val formatter = when (currentPeriod) {
            ChartPeriod.WEEK -> SimpleDateFormat("EEE", Locale.getDefault())
            ChartPeriod.MONTH -> SimpleDateFormat("MMM dd", Locale.getDefault())
            ChartPeriod.YEAR -> SimpleDateFormat("MMM", Locale.getDefault())
        }

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index < dates.size) {
                    formatter.format(Date(dates[index]))
                } else ""
            }
        }
    }

    /**
     * Updates statistics display
     */
    private fun updateStatistics() {
        if (moods.isEmpty()) {
            tvAverageMood.text = "😐"
            tvTotalEntries.text = "0"
            tvStreak.text = "0"
            return
        }

        // Calculate average mood
        val averageMoodValue = calculateAverageMood(moods)
        val averageMoodEmoji = when {
            averageMoodValue >= 4f -> "🤩"
            averageMoodValue >= 3f -> "😊"
            averageMoodValue >= 2f -> "😐"
            else -> "😢"
        }
        tvAverageMood.text = averageMoodEmoji

        // Total entries
        tvTotalEntries.text = moods.size.toString()

        // Calculate streak
        val streak = calculateMoodStreak()
        tvStreak.text = streak.toString()
    }

    /**
     * Calculates current mood logging streak
     */
    private fun calculateMoodStreak(): Int {
        if (moods.isEmpty()) return 0

        val sortedMoods = moods.sortedByDescending { it.date }
        val calendar = Calendar.getInstance()
        var streak = 0
        var currentDate = calendar.timeInMillis

        for (mood in sortedMoods) {
            val moodDate = Calendar.getInstance().apply {
                timeInMillis = mood.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val currentDateStart = Calendar.getInstance().apply {
                timeInMillis = currentDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            if (moodDate == currentDateStart) {
                streak++
                currentDate -= 24 * 60 * 60 * 1000L // Previous day
            } else if (moodDate < currentDateStart) {
                break
            }
        }

        return streak
    }

    /**
     * Sets up click listeners
     */
    private fun setupListeners() {
        // Period selection buttons
        btnWeek.setOnClickListener {
            currentPeriod = ChartPeriod.WEEK
            updatePeriodButtons()
            loadMoodData()
        }

        btnMonth.setOnClickListener {
            currentPeriod = ChartPeriod.MONTH
            updatePeriodButtons()
            loadMoodData()
        }

        btnYear.setOnClickListener {
            currentPeriod = ChartPeriod.YEAR
            updatePeriodButtons()
            loadMoodData()
        }

        // Share button
        btnShareChart.setOnClickListener {
            shareMoodChart()
        }

        // Settings button
        btnChartSettings.setOnClickListener {
            // Open chart settings dialog
            showChartSettingsDialog()
        }
    }

    /**
     * Updates the appearance of period selection buttons
     */
    private fun updatePeriodButtons() {
        // Reset all buttons
        btnWeek.apply {
            setBackgroundColor(resources.getColor(R.color.background_light, null))
            setTextColor(resources.getColor(R.color.primary_teal, null))
        }
        btnMonth.apply {
            setBackgroundColor(resources.getColor(R.color.background_light, null))
            setTextColor(resources.getColor(R.color.primary_teal, null))
        }
        btnYear.apply {
            setBackgroundColor(resources.getColor(R.color.background_light, null))
            setTextColor(resources.getColor(R.color.primary_teal, null))
        }

        // Highlight selected button
        when (currentPeriod) {
            ChartPeriod.WEEK -> btnWeek.apply {
                setBackgroundColor(resources.getColor(R.color.primary_teal, null))
                setTextColor(resources.getColor(R.color.text_white, null))
            }
            ChartPeriod.MONTH -> btnMonth.apply {
                setBackgroundColor(resources.getColor(R.color.primary_teal, null))
                setTextColor(resources.getColor(R.color.text_white, null))
            }
            ChartPeriod.YEAR -> btnYear.apply {
                setBackgroundColor(resources.getColor(R.color.primary_teal, null))
                setTextColor(resources.getColor(R.color.text_white, null))
            }
        }
    }

    /**
     * Shares the mood chart data
     */
    private fun shareMoodChart() {
        val periodText = when (currentPeriod) {
            ChartPeriod.WEEK -> "week"
            ChartPeriod.MONTH -> "month"
            ChartPeriod.YEAR -> "year"
        }

        val shareText = buildString {
            appendLine("📊 My Mood Chart - Last $periodText")
            appendLine("================================")
            appendLine()
            appendLine("Average Mood: ${tvAverageMood.text}")
            appendLine("Total Entries: ${tvTotalEntries.text}")
            appendLine("Current Streak: ${tvStreak.text} days")
            appendLine()
            appendLine("Track your mood with SoulSync! 💚")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Chart")
        }

        startActivity(Intent.createChooser(shareIntent, "Share Mood Chart"))
    }

    /**
     * Shows chart settings dialog
     */
    private fun showChartSettingsDialog() {
        // This would open a dialog for chart customization
        // For now, we'll just show a toast
        android.widget.Toast.makeText(
            requireContext(),
            "Chart settings coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

}
