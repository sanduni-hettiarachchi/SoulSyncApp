package com.wellnessapp.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wellnessapp.R
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.Mood
import com.wellnessapp.data.MoodTypes
import com.wellnessapp.data.RealtimeDataManager
import com.wellnessapp.databinding.FragmentMoodBinding
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {
    
    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var moodViewModel: MoodViewModel
    private lateinit var dataManager: DataManager
    private lateinit var realtimeDataManager: RealtimeDataManager
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter
    
    private var selectedMood: String? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            // Initialize data managers
            dataManager = DataManager.getInstance(requireContext())
            moodViewModel = ViewModelProvider(this)[MoodViewModel::class.java]
            realtimeDataManager = RealtimeDataManager.getInstance()
            realtimeDataManager.initialize(requireContext())
            
            // Set data manager in ViewModel
            moodViewModel.setDataManager(dataManager)
            
            setupUI()
            setupRecyclerView()
            setupObservers()
            loadMoodHistory()
            
            android.util.Log.d("MoodFragment", "Mood fragment initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("MoodFragment", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(context, "Error initializing mood fragment: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupUI() {
        try {
            // Setup back button
            binding.btnBack.setOnClickListener {
                requireActivity().onBackPressed()
            }
            
            // Setup mood selection buttons
            setupMoodButtons()
            
            android.util.Log.d("MoodFragment", "UI setup completed")
        } catch (e: Exception) {
            android.util.Log.e("MoodFragment", "Error in setupUI: ${e.message}", e)
            Toast.makeText(context, "Error setting up mood UI: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupMoodButtons() {
        val moodButtons = mapOf(
            binding.btnMoodHappy to "happy",
            binding.btnMoodGood to "happy",
            binding.btnMoodNeutral to "neutral",
            binding.btnMoodSad to "sad",
            binding.btnMoodTired to "tired"
        )
        
        moodButtons.forEach { (button, moodType) ->
            button.setOnClickListener {
                selectMood(moodType, button.text.toString())
            }
        }
    }
    
    private fun selectMood(moodType: String, emoji: String) {
        try {
            selectedMood = moodType
            
            // Update UI to show selected mood
            binding.layoutSelectedMood.visibility = View.VISIBLE
            binding.tvSelectedMood.text = emoji
            
            // Get current time
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            binding.tvMoodTime.text = "at $currentTime"
            
            // Save mood to data manager
            val mood = Mood(
                id = UUID.randomUUID().toString(),
                emoji = emoji,
                type = moodType,
                timestamp = System.currentTimeMillis(),
                notes = ""
            )
            
            dataManager.saveMood(mood)
            
            // Update real-time data manager
            realtimeDataManager.currentMood.value = mood
            realtimeDataManager.todayMoods.value = dataManager.getMoodsForToday()
            
            // Show feedback
            Toast.makeText(requireContext(), "Mood logged: $emoji", Toast.LENGTH_SHORT).show()
            
            // Refresh mood history
            loadMoodHistory()
            
            android.util.Log.d("MoodFragment", "Mood selected: $emoji ($moodType)")
        } catch (e: Exception) {
            android.util.Log.e("MoodFragment", "Error selecting mood: ${e.message}", e)
            Toast.makeText(context, "Error logging mood: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupRecyclerView() {
        try {
            moodHistoryAdapter = MoodHistoryAdapter { mood ->
                // Handle mood item click if needed
                android.util.Log.d("MoodFragment", "Mood history item clicked: ${mood.emoji}")
            }
            
            binding.recyclerMoodHistory.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = moodHistoryAdapter
            }
            
            android.util.Log.d("MoodFragment", "RecyclerView setup completed")
        } catch (e: Exception) {
            android.util.Log.e("MoodFragment", "Error setting up RecyclerView: ${e.message}", e)
        }
    }
    
    private fun setupObservers() {
        try {
            // Observe mood history changes
            moodViewModel.moodHistory.observe(viewLifecycleOwner) { moods ->
                moods?.let {
                    moodHistoryAdapter.updateMoods(it)
                    android.util.Log.d("MoodFragment", "Mood history updated: ${it.size} moods")
                }
            }
            
            android.util.Log.d("MoodFragment", "Observers setup completed")
        } catch (e: Exception) {
            android.util.Log.e("MoodFragment", "Error setting up observers: ${e.message}", e)
        }
    }
    
    private fun loadMoodHistory() {
        try {
            val moods = dataManager.getMoodsForToday()
            moodHistoryAdapter.updateMoods(moods)
            
            android.util.Log.d("MoodFragment", "Loaded ${moods.size} moods for today")
        } catch (e: Exception) {
            android.util.Log.e("MoodFragment", "Error loading mood history: ${e.message}", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
