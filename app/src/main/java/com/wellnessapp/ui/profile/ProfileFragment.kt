package com.wellnessapp.ui.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wellnessapp.R
import com.wellnessapp.databinding.FragmentProfileBinding

/**
 * Profile Fragment - Settings, personal info, toggles
 * Features:
 * - User Info: Name, profile pic, age
 * - Settings: Hydration reminder, Dark Mode, Notifications, Shake-to-Mood toggles
 * - History shortcut: View detailed stats & charts
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        
        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("wellness_prefs", 0)
        
        setupUI()
        setupToggles()
        setupObservers()
    }

    /**
     * Set up the UI components
     */
    private fun setupUI() {
        // Set user information
        binding.tvUserName.text = "Sanduni"
        binding.tvUserAge.text = "Age: 22"
        binding.tvMemberSince.text = "Member since Dec 2024"
        
        // Set up settings icon click listener
        binding.ivSettings.setOnClickListener {
            // Navigate to settings activity
            val intent = android.content.Intent(requireContext(), com.wellnessapp.SettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Load saved settings
        loadSettings()
    }

    /**
     * Set up toggle listeners
     */
    private fun setupToggles() {
        binding.switchHydrationReminder.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("hydration_reminder", isChecked)
            if (isChecked) {
                Toast.makeText(context, "💧 Hydration reminders enabled", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("dark_mode", isChecked)
            if (isChecked) {
                Toast.makeText(context, "🌙 Dark mode enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "☀️ Light mode enabled", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("notifications", isChecked)
            if (isChecked) {
                Toast.makeText(context, "🔔 Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "🔕 Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.switchShakeMood.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("shake_mood", isChecked)
            if (isChecked) {
                Toast.makeText(context, "📱 Shake-to-mood enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "📱 Shake-to-mood disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Set up observers for ViewModel data
     */
    private fun setupObservers() {
        profileViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvUserName.text = name
        }
        
        profileViewModel.totalHabits.observe(viewLifecycleOwner) { total ->
            // Update total habits display
            // binding.tvTotalHabits.text = "Total Habits: $total"
        }
        
        profileViewModel.completedHabits.observe(viewLifecycleOwner) { completed ->
            // Update completed habits display
            // binding.tvCompletedHabits.text = "Completed Today: $completed"
        }
    }

    /**
     * Load settings from SharedPreferences
     */
    private fun loadSettings() {
        binding.switchHydrationReminder.isChecked = sharedPreferences.getBoolean("hydration_reminder", true)
        binding.switchDarkMode.isChecked = sharedPreferences.getBoolean("dark_mode", false)
        binding.switchNotifications.isChecked = sharedPreferences.getBoolean("notifications", true)
        binding.switchShakeMood.isChecked = sharedPreferences.getBoolean("shake_mood", true)
    }

    /**
     * Save setting to SharedPreferences
     */
    private fun saveSetting(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
        // Settings updated in SharedPreferences
    }

    /**
     * Update UI with settings from ViewModel
     */
    private fun updateUIWithSettings(settings: UserSettings) {
        // Update UI based on settings if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Data class for user settings
 */
data class UserSettings(
    val hydrationReminder: Boolean = true,
    val darkMode: Boolean = false,
    val notifications: Boolean = true,
    val shakeMood: Boolean = true
)
