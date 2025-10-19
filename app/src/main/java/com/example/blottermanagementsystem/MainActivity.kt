package com.example.blottermanagementsystem

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.blottermanagementsystem.navigation.NavGraph
import com.example.blottermanagementsystem.navigation.Screen
import com.example.blottermanagementsystem.ui.screens.LoadingScreen
import com.example.blottermanagementsystem.ui.theme.BlotterManagementSystemTheme
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.User
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.utils.SecurityUtils
import com.example.blottermanagementsystem.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        preferencesManager = PreferencesManager(this)
        
        // Setup lifecycle observer for admin auto-logout
        setupAdminAutoLogout()
        
        // Ensure admin user exists
        ensureAdminExists()
        
        setContent {
            // Use system default theme (no manual toggle)
            BlotterManagementSystemTheme {
                var showLoading by remember { mutableStateOf(true) }
                
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (showLoading) {
                        LoadingScreen(
                            onLoadingComplete = { showLoading = false }
                        )
                    } else {
                        val navController = rememberNavController()
                        val authViewModel: AuthViewModel = viewModel()
                        val context = LocalContext.current
                        
                        // Double-tap back to exit
                        var backPressedTime by remember { mutableStateOf(0L) }
                        var backPressedOnce by remember { mutableStateOf(false) }
                        
                        // Determine start destination
                        val startDestination = when {
                            !preferencesManager.onboardingCompleted -> Screen.Onboarding.route
                            !preferencesManager.isLoggedIn -> Screen.Login.route
                            !preferencesManager.hasSelectedProfilePicture -> Screen.ProfilePictureSelection.route
                            else -> {
                                when (preferencesManager.userRole) {
                                    "Admin" -> Screen.AdminDashboard.route
                                    "Officer" -> Screen.OfficerDashboard.route
                                    else -> Screen.ClerkDashboard.route
                                }
                            }
                        }
                        
                        // Handle back press on dashboard screens
                        val currentBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = currentBackStackEntry?.destination?.route
                        val isDashboard = currentRoute in listOf(
                            Screen.AdminDashboard.route,
                            Screen.OfficerDashboard.route,
                            Screen.ClerkDashboard.route
                        )
                        
                        // Reset back press after 2 seconds
                        LaunchedEffect(backPressedTime) {
                            if (backPressedTime > 0) {
                                kotlinx.coroutines.delay(2000)
                                backPressedOnce = false
                            }
                        }
                        
                        if (isDashboard) {
                            BackHandler {
                                val currentTime = System.currentTimeMillis()
                                if (backPressedOnce && currentTime - backPressedTime < 2000) {
                                    // Double tap detected - exit app
                                    finish()
                                } else {
                                    // First tap - show toast and vibrate
                                    backPressedOnce = true
                                    backPressedTime = currentTime
                                    
                                    // Vibrate
                                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                        vibratorManager.defaultVibrator
                                    } else {
                                        @Suppress("DEPRECATION")
                                        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                    }
                                    
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        vibrator.vibrate(100)
                                    }
                                    
                                    Toast.makeText(
                                        context,
                                        "Press back again to exit",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
    
    private fun setupAdminAutoLogout() {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // App went to background
                if (preferencesManager.isLoggedIn && preferencesManager.userRole == "Admin") {
                    // Auto-logout admin for security
                    preferencesManager.clearSession()
                }
            }
        })
    }
    
    private fun ensureAdminExists() {
        CoroutineScope(Dispatchers.IO).launch {
            val database = BlotterDatabase.getDatabase(applicationContext)
            val userDao = database.userDao()
            
            // Check if admin exists
            val adminUser = userDao.getUserByUsername("admin")
            
            if (adminUser == null) {
                // Create admin user
                val hashedPassword = SecurityUtils.hashPassword("admin123")
                val admin = User(
                    firstName = "System",
                    lastName = "Administrator",
                    username = "admin",
                    password = hashedPassword,
                    role = "Admin",
                    profileCompleted = true,
                    badgeNumber = "ADMIN001",
                    rank = "Administrator",
                    dutyStatus = "Active"
                )
                userDao.insertUser(admin)
            }
        }
    }
}