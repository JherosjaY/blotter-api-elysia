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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.ui.theme.ElectricBlue
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
import com.example.blottermanagementsystem.data.repository.ApiRepository
import com.example.blottermanagementsystem.ui.components.UpdateDialog
import com.example.blottermanagementsystem.utils.FCMHelper
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.utils.PushNotificationHelper
import com.example.blottermanagementsystem.utils.SecurityUtils
import com.example.blottermanagementsystem.utils.VersionChecker
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
        
        // Initialize notification channels
        PushNotificationHelper.createNotificationChannels(this)
        
        // Initialize Firebase Cloud Messaging
        FCMHelper.initializeFCM(this)
        
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
                        
                        // What's New dialog state
                        var showWhatsNewDialog by remember { mutableStateOf(false) }
                        var whatsNewMessage by remember { mutableStateOf("") }
                        
                        // Check if app was just updated
                        LaunchedEffect(Unit) {
                            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            val lastVersion = prefs.getInt("last_version_code", 0)
                            val currentVersion = BuildConfig.VERSION_CODE
                            
                            android.util.Log.d("MainActivity", "🔍 Last version: $lastVersion, Current: $currentVersion")
                            
                            // Show "What's New" if version increased
                            if (currentVersion > lastVersion && lastVersion > 0) {
                                android.util.Log.d("MainActivity", "✅ App updated! Showing What's New dialog")
                                whatsNewMessage = getWhatsNewMessage(currentVersion)
                                showWhatsNewDialog = true
                                
                                // Save current version
                                prefs.edit().putInt("last_version_code", currentVersion).apply()
                            } else if (lastVersion == 0) {
                                // First install, just save version (don't show dialog)
                                android.util.Log.d("MainActivity", "📱 First install, saving version")
                                prefs.edit().putInt("last_version_code", currentVersion).apply()
                            }
                        }
                        
                        // Show What's New dialog
                        if (showWhatsNewDialog) {
                            AlertDialog(
                                onDismissRequest = { showWhatsNewDialog = false },
                                title = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NewReleases,
                                            contentDescription = null,
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "What's New in v${BuildConfig.VERSION_NAME}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 400.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Text(
                                            text = whatsNewMessage,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { showWhatsNewDialog = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Got it!")
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                        
                        // Network monitoring and auto-sync
                        LaunchedEffect(Unit) {
                            val networkMonitor = com.example.blottermanagementsystem.utils.NetworkMonitor(context)
                            val syncManager = com.example.blottermanagementsystem.utils.SyncManager(context)
                            
                            networkMonitor.observeNetworkConnectivity().collect { isOnline ->
                                if (isOnline) {
                                    android.util.Log.d("MainActivity", "🌐 Internet connected - Starting auto-sync...")
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        syncManager.processSyncQueue()
                                    }
                                } else {
                                    android.util.Log.d("MainActivity", "📵 Internet disconnected - Offline mode")
                                }
                            }
                        }
                        
                        // Handle notification navigation
                        LaunchedEffect(Unit) {
                            intent?.let { notificationIntent ->
                                val navigateTo = notificationIntent.getStringExtra("navigate_to")
                                val caseId = notificationIntent.getIntExtra("case_id", -1)
                                val notificationId = notificationIntent.getIntExtra("notification_id", -1)
                                
                                // Mark notification as read when tapped from system tray
                                if (notificationId != -1) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val database = BlotterDatabase.getDatabase(this@MainActivity)
                                            val notificationDao = database.notificationDao()
                                            notificationDao.markAsRead(notificationId)
                                        } catch (e: Exception) {
                                            // Ignore errors
                                        }
                                    }
                                }
                                
                                when (navigateTo) {
                                    "case_detail" -> {
                                        if (caseId != -1) {
                                            // Wait for navigation to be ready
                                            kotlinx.coroutines.delay(500)
                                            navController.navigate("report_detail/$caseId")
                                        }
                                    }
                                    "notifications" -> {
                                        kotlinx.coroutines.delay(500)
                                        navController.navigate(Screen.Notifications.route)
                                    }
                                }
                            }
                        }
                        
                        // Double-tap back to exit
                        var backPressedTime by remember { mutableStateOf(0L) }
                        var backPressedOnce by remember { mutableStateOf(false) }
                        
                        // Determine start destination
                        val startDestination = when {
                            !preferencesManager.onboardingCompleted -> Screen.Onboarding.route
                            !preferencesManager.permissionsGranted -> Screen.PermissionsSetup.route
                            !preferencesManager.isLoggedIn -> Screen.Login.route
                            !preferencesManager.hasSelectedProfilePicture -> Screen.ProfilePictureSelection.route
                            else -> {
                                when (preferencesManager.userRole) {
                                    "Admin" -> Screen.AdminDashboard.route
                                    "Officer" -> Screen.OfficerDashboard.route
                                    else -> Screen.UserDashboard.route
                                }
                            }
                        }
                        
                        // Handle back press on dashboard screens
                        val currentBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = currentBackStackEntry?.destination?.route
                        val isDashboard = currentRoute in listOf(
                            Screen.AdminDashboard.route,
                            Screen.OfficerDashboard.route,
                            Screen.UserDashboard.route
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
    
    /**
     * Get What's New message based on version code
     */
    private fun getWhatsNewMessage(versionCode: Int): String {
        return when (versionCode) {
            2 -> """
                Account Notifications
                • Get notified when your account is terminated or deleted
                • Clear communication from administrators
                
                User Management Improvements
                • Admins can now terminate or delete user accounts
                • All actions sync to cloud and across devices
                
                Cloud Sync Enhancements
                • Person history now syncs to cloud
                • Officers can view complete history from all devices
                
                Smart Caching
                • App automatically cleans up old data
                • Keeps app size small while maintaining access to all records
                
                Password Management
                • Improved password change functionality
                • Better error messages and debugging
                
                Bug Fixes
                • Fixed various issues and improved stability
            """.trimIndent()
            
            else -> """
                New features and improvements
                Bug fixes and performance enhancements
                Better user experience
            """.trimIndent()
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
                // Create admin user (will sync to cloud on first login)
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