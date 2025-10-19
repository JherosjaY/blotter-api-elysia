package com.example.blottermanagementsystem.navigation

/**
 * ============================================
 * BLOTTER MANAGEMENT SYSTEM - NAVIGATION
 * ============================================
 * 
 * ROLE-BASED NAVIGATION STRUCTURE:
 * 
 * 👤 USER/CLERK ROLE:
 *    - Dashboard: ClerkDashboardScreen
 *    - Can: File reports, view reports, view hearings
 *    - Cannot: Manage users, add respondents/suspects, assign officers
 * 
 * 👮 OFFICER ROLE:
 *    - Dashboard: EnhancedOfficerDashboardScreen
 *    - Can: Investigate cases, add respondents/suspects/witnesses/evidence
 *    - Cannot: Manage users, assign officers, close cases
 * 
 * 👔 ADMIN ROLE:
 *    - Dashboard: AdminDashboardScreen
 *    - Can: Manage users/officers, assign cases, close cases, view all
 *    - Cannot: Add respondents/suspects (VIEW ONLY - officers do this)
 * 
 * SHARED SCREENS: Auth, Profile, Notifications, Settings
 * ============================================
 */

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.utils.PreferencesManager
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.blottermanagementsystem.ui.screens.*

// ========== SHARED SCREENS (All Roles) ==========
import com.example.blottermanagementsystem.ui.screens.auth.LoginScreen
import com.example.blottermanagementsystem.ui.screens.auth.RegisterScreen
import com.example.blottermanagementsystem.ui.screens.auth.ForcePasswordChangeScreen

// ========== DASHBOARDS (Role-Specific) ==========
import com.example.blottermanagementsystem.ui.screens.dashboard.AdminDashboardScreen // ADMIN ONLY
import com.example.blottermanagementsystem.ui.screens.dashboard.ClerkDashboardScreen // USER/CLERK ONLY
import com.example.blottermanagementsystem.ui.screens.onboarding.OnboardingScreen
import com.example.blottermanagementsystem.ui.screens.profile.ProfileScreen
import com.example.blottermanagementsystem.ui.screens.profile.ProfilePictureSelectionScreen // USER/CLERK first login
import com.example.blottermanagementsystem.ui.screens.notifications.NotificationsScreen
import com.example.blottermanagementsystem.ui.screens.settings.SettingsScreen
import com.example.blottermanagementsystem.ui.screens.settings.BackupRestoreScreen // ADMIN ONLY

// ========== ADMIN-ONLY SCREENS ==========
import com.example.blottermanagementsystem.ui.screens.admin.UserManagementScreen
import com.example.blottermanagementsystem.ui.screens.admin.OfficerManagementScreen
import com.example.blottermanagementsystem.ui.screens.admin.ActivityLogsScreen
import com.example.blottermanagementsystem.ui.screens.admin.AdminReportOversightScreen
import com.example.blottermanagementsystem.ui.screens.analytics.AnalyticsScreen // ADMIN: System-wide analytics

// ========== OFFICER-ONLY SCREENS ==========
import com.example.blottermanagementsystem.ui.screens.officer.AssignedCasesScreen
import com.example.blottermanagementsystem.ui.screens.scanner.QRScannerScreen // OFFICER: QR scanning
import com.example.blottermanagementsystem.ui.screens.camera.CameraScreen // OFFICER: Evidence photos

// ========== INVESTIGATION SCREENS (Officer Primary, Admin View-Only) ==========
import com.example.blottermanagementsystem.ui.screens.respondent.AddRespondentScreen // OFFICER adds
import com.example.blottermanagementsystem.ui.screens.respondent.RespondentListScreen
import com.example.blottermanagementsystem.ui.screens.respondent.RespondentDetailScreen
import com.example.blottermanagementsystem.ui.screens.respondent.AddRespondentStatementScreen
import com.example.blottermanagementsystem.ui.screens.suspect.AddSuspectScreen // OFFICER adds
import com.example.blottermanagementsystem.ui.screens.suspect.SuspectListScreen
import com.example.blottermanagementsystem.ui.screens.witness.AddWitnessScreen // OFFICER adds
import com.example.blottermanagementsystem.ui.screens.witness.WitnessListScreen
import com.example.blottermanagementsystem.ui.screens.evidence.AddEvidenceScreen // OFFICER adds
import com.example.blottermanagementsystem.ui.screens.evidence.EvidenceListScreen
import com.example.blottermanagementsystem.ui.screens.hearing.AddHearingScreen // OFFICER schedules
import com.example.blottermanagementsystem.ui.screens.hearings.HearingListScreen
import com.example.blottermanagementsystem.ui.screens.resolution.AddResolutionScreen // OFFICER proposes, ADMIN closes
import com.example.blottermanagementsystem.ui.screens.legal.LegalDocumentsDashboardScreen
import com.example.blottermanagementsystem.ui.screens.legal.SummonsManagementScreen
import com.example.blottermanagementsystem.ui.screens.legal.KPFormsScreen
import com.example.blottermanagementsystem.ui.screens.legal.MediationSessionScreen
import com.example.blottermanagementsystem.ui.screens.sms.SmsNotificationListScreen
import com.example.blottermanagementsystem.ui.screens.person.PersonHistoryScreen

// ========== REPORT SCREENS (Shared with Role-Based Permissions) ==========
import com.example.blottermanagementsystem.ui.screens.reports.AddReportScreen // USER/CLERK adds
import com.example.blottermanagementsystem.ui.screens.reports.EditReportScreen // USER/CLERK edits own
import com.example.blottermanagementsystem.ui.screens.reports.ReportDetailScreen // All roles (different permissions)
import com.example.blottermanagementsystem.ui.screens.reports.ReportListScreen // All roles
import com.example.blottermanagementsystem.viewmodel.AuthViewModel
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ============================================
        // SHARED SCREENS (All Roles Can Access)
        // ============================================
        
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Authentication
        composable(Screen.Login.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            
            LoginScreen(
                onLoginSuccess = { role ->
                    val userId = preferencesManager.userId
                    val username = preferencesManager.username ?: ""
                    
                    // ========================================
                    // ROLE-BASED NAVIGATION LOGIC (SEPARATED)
                    // ========================================
                    val destination = when (role) {
                        "Admin" -> {
                            // ADMIN: Direct to dashboard (no profile picture needed)
                            Screen.AdminDashboard.route
                        }
                        "Officer" -> {
                            // OFFICER: Check if must change password
                            // Also check if username still has "off." prefix (legacy check for existing officers)
                            val needsPasswordChange = preferencesManager.mustChangePassword || 
                                                     username.startsWith("off.", ignoreCase = true)
                            
                            // DEBUG: Show what's happening
                            android.util.Log.d("NavGraph", "Officer Login - Username: $username, mustChangePassword: ${preferencesManager.mustChangePassword}, needsPasswordChange: $needsPasswordChange")
                            
                            if (needsPasswordChange) {
                                // First login: Force password change → Welcome Splash → Dashboard
                                Screen.ForcePasswordChange.createRoute(userId, username)
                            } else {
                                // Subsequent logins: Direct to dashboard
                                Screen.OfficerDashboard.route
                            }
                        }
                        else -> {
                            // USER/CLERK: Check if profile picture selected
                            if (!preferencesManager.hasSelectedProfilePicture) {
                                // First login: Must select profile picture
                                Screen.ProfilePictureSelection.route
                            } else {
                                // Subsequent logins: Direct to dashboard
                                Screen.ClerkDashboard.route
                            }
                        }
                    }
                    
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        // Force Password Change (For officers with temporary credentials)
        composable(
            route = Screen.ForcePasswordChange.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("currentUsername") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val currentUsername = backStackEntry.arguments?.getString("currentUsername") ?: ""
            
            ForcePasswordChangeScreen(
                userId = userId,
                currentUsername = currentUsername,
                onPasswordChanged = {
                    // After password change, show welcome splash
                    val officerName = preferencesManager.firstName ?: "Officer"
                    navController.navigate(Screen.OfficerWelcomeSplash.createRoute(officerName)) {
                        popUpTo(Screen.ForcePasswordChange.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Officer Welcome Splash (After password change)
        composable(
            route = Screen.OfficerWelcomeSplash.route,
            arguments = listOf(
                navArgument("officerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val officerName = backStackEntry.arguments?.getString("officerName") ?: "Officer"
            
            com.example.blottermanagementsystem.ui.screens.officer.OfficerWelcomeSplashScreen(
                officerName = officerName,
                onAnimationComplete = {
                    // After splash, go to Officer Dashboard
                    navController.navigate(Screen.OfficerDashboard.route) {
                        popUpTo(Screen.OfficerWelcomeSplash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // ========================================
        // PROFILE PICTURE SELECTION
        // ========================================
        // IMPORTANT: This screen is ONLY for regular Users/Clerks
        // Admin and Officer roles NEVER reach this screen
        // They use icons (AdminPanelSettings/Shield) instead of profile pictures
        composable(Screen.ProfilePictureSelection.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            
            ProfilePictureSelectionScreen(
                onComplete = {
                    // Only regular users reach this screen, go to Clerk Dashboard
                    navController.navigate(Screen.ClerkDashboard.route) {
                        popUpTo(Screen.ProfilePictureSelection.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        // ============================================
        // ADMIN DASHBOARD & SCREENS
        // ============================================
        
        // Admin Dashboard
        composable(Screen.AdminDashboard.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            
            AdminDashboardScreen(
                firstName = preferencesManager.firstName ?: "Admin",
                onNavigateToReports = {
                    navController.navigate(Screen.ReportList.route)
                },
                onNavigateToUsers = {
                    navController.navigate(Screen.UserManagement.route)
                },
                onNavigateToOfficers = {
                    navController.navigate(Screen.OfficerManagement.route)
                },
                onNavigateToActivityLogs = {
                    navController.navigate(Screen.ActivityLogs.route)
                },
                onNavigateToReportOversight = {
                    navController.navigate(Screen.AdminReportOversight.route)
                },
                onNavigateToHearings = {
                    navController.navigate(Screen.HearingList.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // ============================================
        // OFFICER DASHBOARD & SCREENS
        // ============================================
        
        // Officer Dashboard
        composable(Screen.OfficerDashboard.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val repository = remember { 
                com.example.blottermanagementsystem.data.database.BlotterDatabase.getDatabase(context).let { db ->
                    com.example.blottermanagementsystem.data.repository.BlotterRepository(
                        db.userDao(), db.blotterReportDao(), db.suspectDao(), db.witnessDao(),
                        db.evidenceDao(), db.hearingDao(), db.statusHistoryDao(), db.resolutionDao(),
                        db.officerDao(), db.activityLogDao(), db.notificationDao(), db.statusDao(),
                        db.personDao(), db.respondentDao(), db.personHistoryDao(), db.smsNotificationDao(),
                        db.respondentStatementDao(), db.summonsDao(), db.kpFormDao(), db.mediationSessionDao()
                    )
                }
            }
            
            // Get officer record by userId to get the actual officer ID
            val userId = preferencesManager.userId ?: 0
            var officerId by remember { mutableStateOf(userId) } // Fallback to userId
            
            LaunchedEffect(userId) {
                val officer = repository.getOfficerByUserId(userId)
                if (officer != null) {
                    officerId = officer.id // Use the actual officer ID from the officers table
                }
            }
            
            com.example.blottermanagementsystem.ui.screens.officer.EnhancedOfficerDashboardScreen(
                officerName = preferencesManager.firstName ?: "Officer",
                officerId = officerId,
                onNavigateToAssignedCases = {
                    navController.navigate(Screen.AssignedCases.createRoute(officerId))
                },
                onNavigateToHearings = {
                    navController.navigate(Screen.HearingList.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToReportDetail = { reportId ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId))
                },
                onNavigateToQRScanner = {
                    navController.navigate(Screen.QRScanner.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.OfficerAnalytics.createRoute(officerId))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // ============================================
        // USER/CLERK DASHBOARD & SCREENS
        // ============================================
        
        // Clerk Dashboard
        composable(Screen.ClerkDashboard.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            
            ClerkDashboardScreen(
                userId = preferencesManager.userId,
                firstName = preferencesManager.firstName ?: "User",
                onNavigateToAddReport = {
                    navController.navigate(Screen.AddReport.route)
                },
                onNavigateToViewReports = {
                    navController.navigate(Screen.ReportList.createRoute(filter = "All", autoFocus = false))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.ReportList.createRoute(filter = "All", autoFocus = true))
                },
                onNavigateToPendingReports = {
                    navController.navigate(Screen.ReportList.createRoute(filter = "Pending", autoFocus = false))
                },
                onNavigateToHearings = {
                    navController.navigate(Screen.HearingList.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Report Screens
        composable(
            route = Screen.ReportList.route,
            arguments = listOf(
                navArgument("filter") { 
                    type = NavType.StringType
                    defaultValue = "All"
                },
                navArgument("autoFocus") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter") ?: "All"
            val autoFocus = backStackEntry.arguments?.getBoolean("autoFocus") ?: false
            
            ReportListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { reportId, isEditable ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId, isEditable))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.AddReport.route)
                },
                initialFilter = filter,
                autoFocusSearch = autoFocus
            )
        }
        
        composable(Screen.AddReport.route) { backStackEntry ->
            AddReportScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                savedStateHandle = backStackEntry.savedStateHandle
            )
        }
        
        composable(
            route = Screen.ReportDetail.route,
            arguments = listOf(
                navArgument("reportId") { type = NavType.IntType },
                navArgument("isEditable") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val isEditable = backStackEntry.arguments?.getBoolean("isEditable") ?: false
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.userRole ?: "User"
            val dashboardViewModel: DashboardViewModel = viewModel()
            val allReports by dashboardViewModel.allReports.collectAsState(initial = emptyList())
            
            ReportDetailScreen(
                reportId = reportId,
                isEditable = isEditable,
                isAdmin = userRole == "Admin",
                isOfficer = userRole == "Officer",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditReport.createRoute(id))
                },
                onNavigateToRespondentList = { id ->
                    navController.navigate(Screen.RespondentList.createRoute(id))
                },
                onNavigateToSuspectList = { id ->
                    navController.navigate(Screen.SuspectList.createRoute(id))
                },
                onNavigateToWitnessList = { id ->
                    navController.navigate(Screen.WitnessList.createRoute(id))
                },
                onNavigateToEvidenceList = { id ->
                    navController.navigate(Screen.EvidenceList.createRoute(id))
                },
                onNavigateToAddHearing = { id ->
                    navController.navigate(Screen.AddHearing.createRoute(id))
                },
                onNavigateToAddResolution = { id ->
                    navController.navigate(Screen.AddResolution.createRoute(id))
                },
                onNavigateToLegalDocuments = { id ->
                    navController.navigate(Screen.LegalDocumentsDashboard.createRoute(id))
                },
                onExportPDF = {
                    // Export PDF with evidence
                    val report = allReports.find { it.id == reportId }
                    report?.let { reportData ->
                        try {
                            // Parse evidence URIs from JSON strings with error handling
                            val imageUris = try {
                                if (reportData.imageUris.isNotBlank()) {
                                    reportData.imageUris.split(",")
                                        .filter { it.isNotBlank() }
                                        .map { android.net.Uri.parse(it.trim()) }
                                } else emptyList()
                            } catch (e: Exception) {
                                emptyList()
                            }
                            
                            val videoUris = try {
                                if (reportData.videoUris.isNotBlank()) {
                                    reportData.videoUris.split(",")
                                        .filter { it.isNotBlank() }
                                        .map { android.net.Uri.parse(it.trim()) }
                                } else emptyList()
                            } catch (e: Exception) {
                                emptyList()
                            }
                            
                            val videoDurations = try {
                                if (reportData.videoDurations.isNotBlank()) {
                                    reportData.videoDurations.split(",")
                                        .filter { it.isNotBlank() && it.contains(":") }
                                        .mapNotNull { entry ->
                                            val parts = entry.split(":", limit = 2)
                                            if (parts.size == 2) {
                                                try {
                                                    val uri = android.net.Uri.parse(parts[0].trim())
                                                    val duration = parts[1].trim().toLongOrNull() ?: 0L
                                                    uri to duration
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            } else null
                                        }
                                        .toMap()
                                } else emptyMap()
                            } catch (e: Exception) {
                                emptyMap()
                            }
                            
                            val pdfExporter = com.example.blottermanagementsystem.utils.PdfExporterEnhanced(context)
                            val file = pdfExporter.exportReportToPdf(reportData, imageUris, videoUris, videoDurations)
                            
                            if (file != null) {
                                android.widget.Toast.makeText(
                                    context,
                                    "PDF exported to Downloads: ${file.name}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to export PDF",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.widget.Toast.makeText(
                                context,
                                "Error exporting PDF: ${e.message}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onNavigateToPersonHistory = { personId ->
                    navController.navigate(Screen.PersonHistory.createRoute(personId))
                }
            )
        }
        
        composable(
            route = Screen.EditReport.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            EditReportScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                savedStateHandle = backStackEntry.savedStateHandle
            )
        }
        
        composable(Screen.UserManagement.route) {
            UserManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.OfficerManagement.route) {
            OfficerManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ActivityLogs.route) {
            ActivityLogsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.AdminReportOversight.route) {
            AdminReportOversightScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReportDetail = { reportId ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId))
                }
            )
        }
        
        // Other Screens
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReport = { reportId ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId))
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackupRestore = {
                    navController.navigate(Screen.BackupRestore.route)
                }
            )
        }
        
        composable(Screen.HearingList.route) {
            HearingListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Camera Screen
        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { uri ->
                    // Pass captured image URI back to previous screen
                    navController.previousBackStackEntry?.savedStateHandle?.set("captured_image_uri", uri.toString())
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // QR Scanner Screen
        composable(Screen.QRScanner.route) {
            val context = LocalContext.current
            QRScannerScreen(
                onCodeScanned = { code ->
                    // Parse QR code and navigate to report
                    val reportId = com.example.blottermanagementsystem.utils.QRCodeGenerator.parseReportQRCode(code)
                    if (reportId != null) {
                        // Valid blotter report QR code
                        navController.navigate(Screen.ReportDetail.createRoute(reportId)) {
                            popUpTo(Screen.QRScanner.route) { inclusive = true }
                        }
                        android.widget.Toast.makeText(
                            context,
                            "Opening Case #$reportId",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Invalid QR code
                        android.widget.Toast.makeText(
                            context,
                            "Invalid QR Code. Please scan a valid Blotter Report QR code.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Analytics Screen (Admin)
        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Officer Analytics Screen
        composable(
            route = Screen.OfficerAnalytics.route,
            arguments = listOf(navArgument("officerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val officerId = backStackEntry.arguments?.getInt("officerId") ?: 0
            com.example.blottermanagementsystem.ui.screens.officer.OfficerAnalyticsScreen(
                officerId = officerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Person History Screen
        composable(
            route = Screen.PersonHistory.route,
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: 0
            PersonHistoryScreen(
                personId = personId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Assigned Cases Screen (Officer)
        composable(
            route = Screen.AssignedCases.route,
            arguments = listOf(navArgument("officerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val officerId = backStackEntry.arguments?.getInt("officerId") ?: 0
            AssignedCasesScreen(
                officerId = officerId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReportDetail = { reportId ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId, false))
                }
            )
        }
        
        // Add Suspect Screen
        composable(
            route = Screen.AddSuspect.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val dashboardViewModel: DashboardViewModel = viewModel()
            var caseNumber by remember { mutableStateOf("") }
            
            LaunchedEffect(reportId) {
                dashboardViewModel.allReports.collect { reports ->
                    reports.find { it.id == reportId }?.let {
                        caseNumber = it.caseNumber
                    }
                }
            }
            
            AddSuspectScreen(
                reportId = reportId,
                caseNumber = caseNumber,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Suspect List Screen
        composable(
            route = Screen.SuspectList.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.role
            
            SuspectListScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onAddSuspect = {
                    navController.navigate(Screen.AddSuspect.createRoute(reportId))
                },
                isAdmin = userRole == "Admin"
            )
        }
        
        // Add Witness Screen
        composable(
            route = Screen.AddWitness.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val dashboardViewModel: DashboardViewModel = viewModel()
            var caseNumber by remember { mutableStateOf("") }
            
            LaunchedEffect(reportId) {
                dashboardViewModel.allReports.collect { reports ->
                    reports.find { it.id == reportId }?.let {
                        caseNumber = it.caseNumber
                    }
                }
            }
            
            AddWitnessScreen(
                reportId = reportId,
                caseNumber = caseNumber,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Witness List Screen
        composable(
            route = Screen.WitnessList.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.role
            
            WitnessListScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onAddWitness = {
                    navController.navigate(Screen.AddWitness.createRoute(reportId))
                },
                isAdmin = userRole == "Admin"
            )
        }
        
        // Add Evidence Screen
        composable(
            route = Screen.AddEvidence.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val dashboardViewModel: DashboardViewModel = viewModel()
            var caseNumber by remember { mutableStateOf("") }
            
            LaunchedEffect(reportId) {
                dashboardViewModel.allReports.collect { reports ->
                    reports.find { it.id == reportId }?.let {
                        caseNumber = it.caseNumber
                    }
                }
            }
            
            AddEvidenceScreen(
                reportId = reportId,
                caseNumber = caseNumber,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onSaveSuccess = { navController.popBackStack() },
                savedStateHandle = backStackEntry.savedStateHandle
            )
        }
        
        // Evidence List Screen
        composable(
            route = Screen.EvidenceList.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.role
            
            EvidenceListScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onAddEvidence = {
                    navController.navigate(Screen.AddEvidence.createRoute(reportId))
                },
                isAdmin = userRole == "Admin"
            )
        }
        
        // Add Hearing Screen
        composable(
            route = Screen.AddHearing.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            AddHearingScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Add Resolution Screen
        composable(
            route = Screen.AddResolution.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            AddResolutionScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Add Respondent Statement Screen
        composable(
            route = Screen.AddRespondentStatement.route,
            arguments = listOf(navArgument("respondentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val respondentId = backStackEntry.arguments?.getInt("respondentId") ?: 0
            AddRespondentStatementScreen(
                respondentId = respondentId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Add Respondent Screen
        composable(
            route = Screen.AddRespondent.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            AddRespondentScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Respondent List Screen
        composable(
            route = Screen.RespondentList.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.role
            
            RespondentListScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddRespondent = {
                    navController.navigate(Screen.AddRespondent.createRoute(reportId))
                },
                onNavigateToRespondentDetail = { respondentId ->
                    navController.navigate(Screen.RespondentDetail.createRoute(respondentId))
                },
                isAdmin = userRole == "Admin"
            )
        }
        
        // Respondent Detail Screen
        composable(
            route = Screen.RespondentDetail.route,
            arguments = listOf(navArgument("respondentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val respondentId = backStackEntry.arguments?.getInt("respondentId") ?: 0
            RespondentDetailScreen(
                respondentId = respondentId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddStatement = { id ->
                    navController.navigate(Screen.AddRespondentStatement.createRoute(id))
                }
            )
        }
        
        // SMS Notification List Screen
        composable(
            route = Screen.SmsNotificationList.route,
            arguments = listOf(
                navArgument("reportId") { 
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId")?.takeIf { it != -1 }
            SmsNotificationListScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Legal Documents Dashboard
        composable(
            route = Screen.LegalDocumentsDashboard.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            LegalDocumentsDashboardScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSummons = { id ->
                    navController.navigate(Screen.SummonsManagement.createRoute(id))
                },
                onNavigateToKPForms = { id ->
                    navController.navigate(Screen.KPForms.createRoute(id))
                },
                onNavigateToMediation = { id ->
                    navController.navigate(Screen.MediationSession.createRoute(id))
                }
            )
        }
        
        // Summons Management Screen
        composable(
            route = Screen.SummonsManagement.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            SummonsManagementScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // KP Forms Screen
        composable(
            route = Screen.KPForms.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            KPFormsScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Mediation Session Screen
        composable(
            route = Screen.MediationSession.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            MediationSessionScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Backup & Restore Screen
        composable(Screen.BackupRestore.route) {
            BackupRestoreScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
