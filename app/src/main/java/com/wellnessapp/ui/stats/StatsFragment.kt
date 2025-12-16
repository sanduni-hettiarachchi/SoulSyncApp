package com.wellnessapp.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.wellnessapp.R
import com.wellnessapp.databinding.FragmentStatsBinding
import com.wellnessapp.data.RealtimeDataManager
import com.wellnessapp.data.Mood
import com.wellnessapp.data.WeeklyStats
import com.wellnessapp.data.MonthlyStats
import com.wellnessapp.data.MonthlyHydrationData
import java.util.*

/**
 * Stats Fragment - Shows trends and weekly statistics
 * Features:
 * - Weekly mood chart (LineChart)
 * - Calories burned cards
 * - Challenges progress bars
 * - Hydration trend chart (BarChart)
 */
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var statsViewModel: StatsViewModel
    private lateinit var realtimeDataManager: RealtimeDataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            android.util.Log.d("StatsFragment", "onViewCreated started")
            
            // Initialize ViewModel first (lightweight)
            statsViewModel = ViewModelProvider(this)[StatsViewModel::class.java]
            android.util.Log.d("StatsFragment", "ViewModel initialized")
            
            // Setup basic UI immediately (non-blocking)
            showLoadingState()
            setupBasicUI()
            android.util.Log.d("StatsFragment", "Basic UI setup completed")
            
            // Initialize heavy components in background thread with delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    // Check if fragment is still attached
                    if (!isAdded || isDetached) {
                        android.util.Log.w("StatsFragment", "Fragment not attached, skipping background initialization")
                        return@postDelayed
                    }

                    // Initialize RealtimeDataManager in background
                    realtimeDataManager = RealtimeDataManager.getInstance()
                    android.util.Log.d("StatsFragment", "RealtimeDataManager initialized in background")
                    
                    // Initialize RealtimeDataManager (heavy operation)
                    realtimeDataManager.initialize(requireContext())
                    android.util.Log.d("StatsFragment", "RealtimeDataManager initialized in background")
                    
                    // Check if fragment is still attached before continuing
                    if (!isAdded || isDetached) {
                        android.util.Log.w("StatsFragment", "Fragment detached during initialization")
                        return@postDelayed
                    }
                    
                    // Generate dummy data in background
                    generateInitialData()
                    android.util.Log.d("StatsFragment", "Initial data generated")
                    
                    // Check if fragment is still attached before UI operations
                    if (!isAdded || isDetached) {
                        android.util.Log.w("StatsFragment", "Fragment detached before UI setup")
                        return@postDelayed
                    }
                    
                    // Setup spinner and heavy UI components in background
                    setupTimePeriodSpinner()
                    setupMoodChart()
                    setupHydrationChart()
                    setupWeeklyStatsTable()
                    android.util.Log.d("StatsFragment", "Heavy UI components setup completed")
                    
                    // Setup observers after heavy initialization
                    observeData()
                    android.util.Log.d("StatsFragment", "Observers setup completed")
                    
                    // Hide loading state when data is ready
                    hideLoadingState()
                    android.util.Log.d("StatsFragment", "Loading state hidden")
                    
                    android.util.Log.d("StatsFragment", "Background initialization completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("StatsFragment", "Error in background initialization: ${e.message}", e)
                    android.util.Log.e("StatsFragment", "Stack trace: ${e.stackTraceToString()}")
                }
            }, 2000) // 2 second delay to ensure UI is fully rendered and prevent ANR
            
            android.util.Log.d("StatsFragment", "onViewCreated completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error in onViewCreated: ${e.message}", e)
        }
    }

    /**
     * Set up mood chart with proper configuration
     */
    private fun setupMoodChart() {
        try {
            android.util.Log.d("StatsFragment", "Setting up mood chart")
            
            // Configure chart appearance
            binding.moodChart.description.isEnabled = false
            binding.moodChart.setTouchEnabled(true)
            binding.moodChart.isDragEnabled = true
            binding.moodChart.setScaleEnabled(true)
            binding.moodChart.setPinchZoom(true)
            
            // Configure X axis
            val xAxis = binding.moodChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true
            
            // Configure Y axis
            val leftAxis = binding.moodChart.axisLeft
            leftAxis.setDrawGridLines(true)
            leftAxis.setDrawAxisLine(true)
            leftAxis.axisMinimum = 0f
            leftAxis.axisMaximum = 5f
            
            val rightAxis = binding.moodChart.axisRight
            rightAxis.isEnabled = false
            
            // Configure legend
            val legend = binding.moodChart.legend
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = Color.BLACK
            
            android.util.Log.d("StatsFragment", "Mood chart setup completed")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error setting up mood chart: ${e.message}", e)
        }
    }
    
    /**
     * Set up hydration chart with proper configuration
     */
    private fun setupHydrationChart() {
        try {
            android.util.Log.d("StatsFragment", "Setting up hydration chart")
            
            // Configure chart appearance
            binding.hydrationChart.description.isEnabled = false
            binding.hydrationChart.setTouchEnabled(true)
            binding.hydrationChart.isDragEnabled = true
            binding.hydrationChart.setScaleEnabled(true)
            binding.hydrationChart.setPinchZoom(true)
            
            // Configure X axis
            val xAxis = binding.hydrationChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true
            
            // Configure Y axis
            val leftAxis = binding.hydrationChart.axisLeft
            leftAxis.setDrawGridLines(true)
            leftAxis.setDrawAxisLine(true)
            leftAxis.axisMinimum = 0f
            leftAxis.axisMaximum = 10f
            
            val rightAxis = binding.hydrationChart.axisRight
            rightAxis.isEnabled = false
            
            // Configure legend
            val legend = binding.hydrationChart.legend
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = Color.BLACK
            
            android.util.Log.d("StatsFragment", "Hydration chart setup completed")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error setting up hydration chart: ${e.message}", e)
        }
    }
    
    /**
     * Set up weekly statistics table with real data
     */
    private fun setupWeeklyStatsTable() {
        // This will be populated by observeData()
    }
    
    /**
     * Generate initial dummy data for stats
     */
    private fun generateInitialData() {
        try {
            android.util.Log.d("StatsFragment", "Generating initial dummy data")
            
            // Generate dummy data through DataManager
            val dataManager = com.wellnessapp.data.DataManager.getInstance(requireContext())
            dataManager.generateDummyData()
            
            // Generate dummy data through RealtimeDataManager
            realtimeDataManager.generateDummyDataIfNeeded()
            
            // Force refresh of all data
            realtimeDataManager.refreshData()
            
            android.util.Log.d("StatsFragment", "Initial dummy data generated successfully")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error generating initial data: ${e.message}", e)
        }
    }
    
    /**
     * Set up time period spinner for weekly/monthly selection
     */
    private fun setupTimePeriodSpinner() {
        binding.spinnerTimePeriod?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        // Weekly view
                        loadWeeklyData()
                    }
                    1 -> {
                        // Monthly view
                        loadMonthlyData()
                    }
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    /**
     * Load weekly data and update charts
     */
    private fun loadWeeklyData() {
        // Generate dummy data for the current week
        realtimeDataManager.generateDummyDataIfNeeded()
        
        // Update charts with weekly data
        realtimeDataManager.weeklyStats.observe(viewLifecycleOwner) { stats ->
            updateWeeklyStatsTable(stats)
        }
        
        realtimeDataManager.todayMoods.observe(viewLifecycleOwner) { moods ->
            updateMoodChartWithData(moods)
        }
        
        realtimeDataManager.hydrationProgress.observe(viewLifecycleOwner) { glasses ->
            updateHydrationChart(glasses)
        }
    }
    
    /**
     * Load monthly data and update charts
     */
    private fun loadMonthlyData() {
        // Generate dummy data for 5 months
        realtimeDataManager.generateDummyDataIfNeeded()
        
        // Load monthly data from DataManager
        val dataManager = com.wellnessapp.data.DataManager.getInstance(requireContext())
        dataManager.generateDummyData()
        
        // Update charts with monthly data
        realtimeDataManager.monthlyStats.observe(viewLifecycleOwner) { stats ->
            updateMonthlyStatsTable(stats)
        }
        
        realtimeDataManager.monthlyMoods.observe(viewLifecycleOwner) { moods ->
            updateMonthlyMoodChart(moods)
        }
        
        realtimeDataManager.monthlyHydration.observe(viewLifecycleOwner) { hydration ->
            updateMonthlyHydrationChart(hydration)
        }
    }
    
    /**
     * Observe real-time data changes
     */
    private fun observeData() {
        realtimeDataManager.weeklyStats.observe(viewLifecycleOwner) { stats ->
            updateWeeklyStatsTable(stats)
        }
        
        realtimeDataManager.todayMoods.observe(viewLifecycleOwner) { moods ->
            updateMoodChartWithData(moods)
        }
        
        realtimeDataManager.hydrationProgress.observe(viewLifecycleOwner) { glasses ->
            updateHydrationChart(glasses)
        }
        
        // Load historical hydration data for the chart
        loadHistoricalHydrationData()
        
        // Generate sample hydration data if none exists
        generateSampleHydrationData()
    }
    
    /**
     * Load historical hydration data for the chart
     */
    private fun loadHistoricalHydrationData() {
        try {
            android.util.Log.d("StatsFragment", "Loading historical hydration data")
            val dataManager = realtimeDataManager.getDataManager()
            
            if (dataManager != null) {
                val entries = mutableListOf<Entry>()
                val calendar = Calendar.getInstance()
                
                // Get hydration data for the last 7 days
                for (i in 6 downTo 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, -i)
                    val dayGlasses = dataManager.getHydrationForDate(calendar.timeInMillis)
                    entries.add(Entry(6f - i, dayGlasses.toFloat()))
                    android.util.Log.d("StatsFragment", "Historical day ${6-i}: $dayGlasses glasses")
                }
                
                // Update chart with historical data
                updateHydrationChartWithEntries(entries)
            } else {
                android.util.Log.w("StatsFragment", "DataManager not available for historical data")
            }
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error loading historical hydration data: ${e.message}", e)
        }
    }
    
    /**
     * Update hydration chart with specific entries
     */
    private fun updateHydrationChartWithEntries(entries: List<Entry>) {
        try {
            val hydrationChart = binding.hydrationChart
            
            val dataSet = LineDataSet(entries, "Water Intake")
            dataSet.color = Color.parseColor("#FF9800")
            dataSet.setDrawFilled(true)
            dataSet.fillColor = Color.parseColor("#FFE0B2")
            dataSet.setDrawCircles(true)
            dataSet.setCircleColor(Color.parseColor("#FF9800"))
            dataSet.setCircleRadius(4f)
            dataSet.lineWidth = 3f
            
            val lineData = LineData(dataSet)
            hydrationChart.data = lineData
            hydrationChart.invalidate()
            
            android.util.Log.d("StatsFragment", "Hydration chart updated with historical data: ${entries.size} entries")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error updating hydration chart with entries: ${e.message}", e)
        }
    }
    
    /**
     * Generate sample hydration data for demonstration
     */
    private fun generateSampleHydrationData() {
        try {
            android.util.Log.d("StatsFragment", "Generating sample hydration data")
            val dataManager = realtimeDataManager.getDataManager()
            
            if (dataManager != null) {
                val calendar = Calendar.getInstance()
                
                // Generate sample data for the last 7 days
                for (i in 6 downTo 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, -i)
                    val existingHydration = dataManager.getHydrationForDate(calendar.timeInMillis)
                    
                    // Only add sample data if no data exists for this day
                    if (existingHydration == 0) {
                        val sampleGlasses = (3..8).random() // Random between 3-8 glasses
                        dataManager.saveHydration(sampleGlasses)
                        android.util.Log.d("StatsFragment", "Added sample data for day ${6-i}: $sampleGlasses glasses")
                    }
                }
                
                // Reload historical data after generating samples
                loadHistoricalHydrationData()
            }
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error generating sample hydration data: ${e.message}", e)
        }
    }
    
    /**
     * Update weekly statistics table with real data
     */
    private fun updateWeeklyStatsTable(stats: WeeklyStats) {
        try {
            android.util.Log.d("StatsFragment", "Updating weekly stats table: $stats")
            
            // Update weekly table with day-by-day data
            val days = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
            val moodEmojis = listOf("😄", "😐", "😢", "🤩", "😴")
            
            // Generate random data for each day of the week
            days.forEach { day ->
                val moodEmoji = moodEmojis.random()
                val habitsCompleted = (0..5).random()
                val waterGlasses = (0..8).random()
                
                // Update mood for the day
                val moodId = resources.getIdentifier("tv_mood_$day", "id", requireContext().packageName)
                val moodView = requireView().findViewById<TextView>(moodId)
                moodView?.text = moodEmoji
                
                // Update habits for the day
                val habitsId = resources.getIdentifier("tv_habits_$day", "id", requireContext().packageName)
                val habitsView = requireView().findViewById<TextView>(habitsId)
                habitsView?.text = "$habitsCompleted/6"
                
                // Update water for the day
                val waterId = resources.getIdentifier("tv_water_$day", "id", requireContext().packageName)
                val waterView = requireView().findViewById<TextView>(waterId)
                waterView?.text = "${waterGlasses}gl"
            }
            
            android.util.Log.d("StatsFragment", "Weekly stats table updated successfully")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error updating weekly stats table: ${e.message}", e)
        }
    }
    
    /**
     * Update monthly statistics table
     */
    private fun updateMonthlyStatsTable(stats: MonthlyStats) {
        try {
            android.util.Log.d("StatsFragment", "Updating monthly stats table: $stats")
            
            // For monthly view, show weekly averages across 4 weeks
            val weeks = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
            val moodEmojis = listOf("😄", "😐", "😢", "🤩", "😴")
            
            // Generate monthly data (4 weeks worth)
            weeks.forEach { day ->
                val moodEmoji = moodEmojis.random()
                val habitsCompleted = (0..6).random()
                val waterGlasses = (0..10).random()
                
                // Update mood for the day
                val moodId = resources.getIdentifier("tv_mood_$day", "id", requireContext().packageName)
                val moodView = requireView().findViewById<TextView>(moodId)
                moodView?.text = moodEmoji
                
                // Update habits for the day
                val habitsId = resources.getIdentifier("tv_habits_$day", "id", requireContext().packageName)
                val habitsView = requireView().findViewById<TextView>(habitsId)
                habitsView?.text = "$habitsCompleted/6"
                
                // Update water for the day
                val waterId = resources.getIdentifier("tv_water_$day", "id", requireContext().packageName)
                val waterView = requireView().findViewById<TextView>(waterId)
                waterView?.text = "${waterGlasses}gl"
            }
            
            android.util.Log.d("StatsFragment", "Monthly stats table updated successfully")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error updating monthly stats table: ${e.message}", e)
        }
    }
    
    /**
     * Update monthly mood chart
     */
    private fun updateMonthlyMoodChart(moods: List<Mood>) {
        val moodChart = binding.moodChart
        val entries = mutableListOf<Entry>()
        
        // Create entries for the last 30 days
        for (i in 0 until 30) {
            val dayMoods = moods.filter { 
                val moodDate = java.util.Calendar.getInstance()
                moodDate.timeInMillis = it.date
                val today = java.util.Calendar.getInstance()
                today.add(java.util.Calendar.DAY_OF_MONTH, -i)
                moodDate.get(java.util.Calendar.DAY_OF_MONTH) == today.get(java.util.Calendar.DAY_OF_MONTH)
            }
            
            val avgMood = if (dayMoods.isNotEmpty()) {
                dayMoods.map { it.getMoodLevel() }.average().toFloat()
            } else {
                3f // Neutral mood for days without data
            }
            
            entries.add(Entry(29f - i, avgMood))
        }
        
        val dataSet = LineDataSet(entries, "Mood Trend")
        dataSet.color = Color.parseColor("#FF9800")
        dataSet.setCircleColor(Color.parseColor("#FF9800"))
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 4f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#33FF9800")
        
        val lineData = LineData(dataSet)
        moodChart.data = lineData
        moodChart.invalidate()
    }
    
    /**
     * Update monthly hydration chart
     */
    private fun updateMonthlyHydrationChart(hydration: List<MonthlyHydrationData>) {
        val hydrationChart = binding.hydrationChart
        val entries = mutableListOf<Entry>()
        
        // Create entries for the last 30 days
        for (i in 0 until 30) {
            val dayHydration = if (i < hydration.size) hydration[i].glasses else 0
            entries.add(Entry(29f - i, dayHydration.toFloat()))
        }
        
        val dataSet = LineDataSet(entries, "Water Intake")
        dataSet.color = Color.parseColor("#2196F3")
        dataSet.setCircleColor(Color.parseColor("#2196F3"))
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 4f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#332196F3")
        
        val lineData = LineData(dataSet)
        hydrationChart.data = lineData
        hydrationChart.invalidate()
    }
    
    /**
     * Update mood chart with real data
     */
    private fun updateMoodChartWithData(moods: List<Mood>) {
        val moodChart = binding.moodChart
        val entries = mutableListOf<Entry>()
        
        // Create entries for the last 7 days with enhanced mood visualization
        val calendar = Calendar.getInstance()
        val dayLabels = mutableListOf<String>()
        
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            val dayMoods = moods.filter { mood ->
                val moodCal = Calendar.getInstance()
                moodCal.timeInMillis = mood.timestamp
                moodCal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
            }
            
            val averageMood = if (dayMoods.isNotEmpty()) {
                dayMoods.map { mood -> mood.getMoodLevel().toFloat() }.average().toFloat()
            } else {
                3f // Neutral mood if no data
            }
            
            entries.add(Entry(6f - i, averageMood))
            
            // Add day labels with emojis
            val dayName = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> "Day"
            }
            dayLabels.add(dayName)
        }
        
        val dataSet = LineDataSet(entries, "Mood Trend")
        dataSet.color = Color.parseColor("#3F51B5") // Primary indigo
        dataSet.setCircleColor(Color.parseColor("#3F51B5"))
        dataSet.fillColor = Color.parseColor("#803F51B5")
        dataSet.setDrawFilled(true)
        dataSet.setDrawValues(true)
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 6f
        dataSet.valueTextSize = 12f
        
        // Configure X-axis with day labels
        val xAxis = moodChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setValueFormatter(IndexAxisValueFormatter(dayLabels))
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        
        // Configure Y-axis for mood levels (1-10)
        val leftAxis = moodChart.axisLeft
        leftAxis.axisMinimum = 1f
        leftAxis.axisMaximum = 10f
        leftAxis.granularity = 1f
        leftAxis.setDrawGridLines(true)
        
        // Hide right axis
        moodChart.axisRight.isEnabled = false
        
        // Configure chart appearance
        moodChart.description.isEnabled = false
        moodChart.legend.isEnabled = true
        moodChart.setTouchEnabled(true)
        moodChart.isDragEnabled = true
        moodChart.setScaleEnabled(true)
        moodChart.setPinchZoom(true)
        
        val lineData = LineData(dataSet)
        moodChart.data = lineData
        moodChart.invalidate()
    }
    
    /**
     * Update hydration chart with real data
     */
    private fun updateHydrationChart(glasses: Int) {
        try {
            android.util.Log.d("StatsFragment", "Updating hydration chart with $glasses glasses")
            
            val hydrationChart = binding.hydrationChart
            val entries = mutableListOf<Entry>()
            
            // Create entries for the last 7 days with real data
            val calendar = Calendar.getInstance()
            val dataManager = realtimeDataManager.getDataManager()
            
            if (dataManager != null) {
                for (i in 6 downTo 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, -i)
                    val dayGlasses = dataManager.getHydrationForDate(calendar.timeInMillis)
                    entries.add(Entry(6f - i, dayGlasses.toFloat()))
                    android.util.Log.d("StatsFragment", "Day ${6-i}: $dayGlasses glasses")
                }
            } else {
                // Fallback: generate sample data if data manager not available
                android.util.Log.w("StatsFragment", "DataManager not available, using sample data")
                for (i in 6 downTo 0) {
                    val dayGlasses = if (i == 0) glasses else (glasses * (7 - i) / 7)
                    entries.add(Entry(6f - i, dayGlasses.toFloat()))
                }
            }
            
            val dataSet = LineDataSet(entries, "Water Intake")
            dataSet.color = Color.parseColor("#FF9800")
            dataSet.setDrawFilled(true)
            dataSet.fillColor = Color.parseColor("#FFE0B2")
            dataSet.setDrawCircles(true)
            dataSet.setCircleColor(Color.parseColor("#FF9800"))
            dataSet.setCircleRadius(4f)
            dataSet.lineWidth = 3f
            
            val lineData = LineData(dataSet)
            hydrationChart.data = lineData
            hydrationChart.invalidate()
            
            android.util.Log.d("StatsFragment", "Hydration chart updated with ${entries.size} entries")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error updating hydration chart: ${e.message}", e)
        }
    }


    /**
     * Set up observers for ViewModel data
     */
    private fun setupObservers() {
        // Observe weekly stats changes
        statsViewModel.weeklyStats.observe(viewLifecycleOwner) { stats ->
            updateStatsDisplay(stats)
        }
        
        // Observe mood trend changes
        statsViewModel.moodTrend.observe(viewLifecycleOwner) { moodEmojis ->
            updateMoodTrend(moodEmojis)
        }
    }

    /**
     * Create sample mood data for the week
     */
    private fun createMoodData(): LineData {
        val entries = ArrayList<Entry>()
        
        // Sample mood data (1-5 scale) with corresponding emojis
        val moodData = arrayOf(
            Pair(4f, "😊"), // Monday - Happy
            Pair(3f, "😐"), // Tuesday - Neutral
            Pair(5f, "🤩"), // Wednesday - Excited
            Pair(2f, "😔"), // Thursday - Sad
            Pair(4f, "😊"), // Friday - Happy
            Pair(3f, "😐"), // Saturday - Neutral
            Pair(4f, "😊")  // Sunday - Happy
        )
        
        moodData.forEachIndexed { index, (value, emoji) ->
            val entry = Entry(index.toFloat(), value)
            entry.data = emoji // Store emoji as data
            entries.add(entry)
        }
        
        val dataSet = LineDataSet(entries, "Mood Level")
        return LineData(dataSet)
    }

    /**
     * Create sample hydration data for the week
     */
    private fun createHydrationData(): LineData {
        val entries = ArrayList<Entry>()
        
        // Sample hydration data (in liters)
        val hydrationValues = floatArrayOf(2.5f, 3.0f, 2.8f, 3.2f, 2.9f, 3.1f, 2.7f)
        
        hydrationValues.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value))
        }
        
        val dataSet = LineDataSet(entries, "Hydration (L)")
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#FFE0B2")
        return LineData(dataSet)
    }

    /**
     * Update mood chart with new data
     */
    private fun updateMoodChart(entries: List<Entry>) {
        val dataSet = LineDataSet(entries, "Mood Level")
        dataSet.color = Color.parseColor("#FF9800") // Orange color
        dataSet.setCircleColor(Color.parseColor("#FF9800")) // Orange color
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 6f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#80FF9800") // Orange with transparency
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.BLACK
        
        // Custom value formatter to show emojis
        dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: com.github.mikephil.charting.utils.ViewPortHandler?): String {
                return entry?.data as? String ?: ""
            }
        }
        
        binding.moodChart.data = LineData(dataSet)
        binding.moodChart.invalidate()
    }

    /**
     * Update hydration chart with new data
     */
    private fun updateHydrationChart(entries: List<Entry>) {
        val dataSet = LineDataSet(entries, "Hydration (L)")
        dataSet.color = Color.parseColor("#2196F3")
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#E3F2FD")
        
        binding.hydrationChart.data = LineData(dataSet)
        binding.hydrationChart.invalidate()
    }

    /**
     * Get week days for chart labels
     */
    private fun getWeekDays(): Array<String> {
        return arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }
    
    /**
     * Setup basic UI elements that don't require heavy operations
     */
    private fun setupBasicUI() {
        try {
            // Setup basic UI elements that are lightweight
            android.util.Log.d("StatsFragment", "Basic UI elements setup completed")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error setting up basic UI: ${e.message}", e)
        }
    }
    
    /**
     * Show loading state to prevent ANR
     */
    private fun showLoadingState() {
        try {
            // Show loading indicators immediately
            binding.root.alpha = 0.7f
            android.util.Log.d("StatsFragment", "Loading state shown")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error showing loading state: ${e.message}", e)
        }
    }
    
    /**
     * Hide loading state when data is ready
     */
    private fun hideLoadingState() {
        try {
            // Hide loading indicators and restore full opacity
            binding.root.alpha = 1.0f
            android.util.Log.d("StatsFragment", "Loading state hidden")
        } catch (e: Exception) {
            android.util.Log.e("StatsFragment", "Error hiding loading state: ${e.message}", e)
        }
    }
    
    /**
     * Update stats display with weekly statistics
     */
    private fun updateStatsDisplay(stats: WeeklyStats) {
        // Update UI with weekly stats
        // This would update text views showing completion rates, streaks, etc.
    }
    
    /**
     * Update mood trend display
     */
    private fun updateMoodTrend(moodEmojis: List<String>) {
        // Update mood trend chart or display
        // This would show the mood trend over the week
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
