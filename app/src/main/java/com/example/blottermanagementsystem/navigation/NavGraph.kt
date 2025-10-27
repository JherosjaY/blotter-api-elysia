package com.example.blottermanagementsystem.navigation

/**
 * ============================================
 * BLOTTER MANAGEMENT SYSTEM - NAVIGATION
 * ============================================
 * 
 * ROLE-BASED NAVIGATION STRUCTURE:
 * 
 * 👤 USER ROLE:
 *    - Dashboard: UserDashboardScreen
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
import com.example.blottermanagementsystem.ui.screens.auth.ForceChangePasswordScreen

// ========== DASHBOARDS (Role-Specific) ==========
import com.example.blottermanagementsystem.ui.screens.dashboard.AdminDashboardScreen // ADMIN ONLY
import com.example.blottermanagementsystem.ui.screens.admin.RecordsArchiveScreen // ADMIN ONLY
import com.example.blottermanagementsystem.ui.screens.dashboard.UserDashboardScreen // USER ONLY
import com.example.blottermanagementsystem.ui.screens.onboarding.OnboardingScreen
import com.example.blottermanagementsystem.ui.screens.permissions.PermissionsSetupScreen
import com.example.blottermanagementsystem.ui.screens.profile.ProfileScreen
import com.example.blottermanagementsystem.ui.screens.profile.UserProfileScreen
import com.example.blottermanagementsystem.ui.screens.profile.AdminProfileScreen
import com.example.blottermanagementsystem.ui.screens.profile.OfficerProfileScreen
import com.example.blottermanagementsystem.ui.screens.profile.ProfilePictureSelectionScreen // USER/CLERK first login
import com.example.blottermanagementsystem.ui.screens.notifications.NotificationsScreen
import com.example.blottermanagementsystem.ui.screens.notifications.AdminNotificationSenderScreen
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
import com.example.blottermanagementsystem.ui.screens.hearings.AddHearingScreen // OFFICER schedules
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
                    navController.navigate(Screen.PermissionsSetup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Permissions Setup
        composable(Screen.PermissionsSetup.route) {
            PermissionsSetupScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.PermissionsSetup.route) { inclusive = true }
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
                            // OFFICER: Go directly to dashboard
                            Screen.OfficerDashboard.route
                        }
                        else -> {
                            // USER: Check if profile picture selected
                            if (!preferencesManager.hasSelectedProfilePicture) {
                                // First login: Must select profile picture
                                Screen.ProfilePictureSelection.route
                            } else {
                                // Subsequent logins: Direct to dashboard
                                Screen.UserDashboard.route
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
        
        // Officer Welcome Splash
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
                    // Only regular users reach this screen, go to User Dashboard
                    navController.navigate(Screen.UserDashboard.route) {
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
                onNavigateToUsers = {
                    navController.navigate(Screen.UserManagement.route)
                },
                onNavigateToOfficers = {
                    navController.navigate(Screen.OfficerManagement.route)
                },
                onNavigateToRecordsArchive = {
                    navController.navigate(Screen.RecordsArchive.route)
                },
                onNavigateToQRScanner = {
                    navController.navigate(Screen.QRScanner.route)
                },
                onNavigateToNotificationSender = {
                    navController.navigate(Screen.AdminNotificationSender.route)
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
        
        // Records Archive Screen
        composable(Screen.RecordsArchive.route) {
            RecordsArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReportDetail = { reportId ->
                    navController.navigate(Screen.AdminReportDetail.createRoute(reportId))
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
                        db.respondentStatementDao(), db.summonsDao(), db.kpFormDao(), db.mediationSessionDao(),
                        db.caseTimelineDao(), db.caseTemplateDao()
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
                    navController.navigate(Screen.OfficerReportDetail.createRoute(reportId))
                },
                onNavigateToQRScanner = {
                    navController.navigate(Screen.QRScanner.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.OfficerAnalytics.createRoute(officerId))
                },
                onNavigateToSmsNotifications = {
                    navController.navigate(Screen.SmsNotificationList.createRoute())
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
        // USER DASHBOARD & SCREENS
        // ============================================
        
        // User Dashboard
        composable(Screen.UserDashboard.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            
            UserDashboardScreen(
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
            
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.userRole ?: "User"
            
            ReportListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { reportId, isEditable ->
                    // Navigate to role-specific screen
                    when (userRole) {
                        "Admin" -> navController.navigate(Screen.AdminReportDetail.createRoute(reportId))
                        "Officer" -> navController.navigate(Screen.OfficerReportDetail.createRoute(reportId))
                        else -> navController.navigate(Screen.UserReportDetail.createRoute(reportId))
                    }
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
                onNavigateToCaseTimeline = { id, caseNumber ->
                    navController.navigate(Screen.CaseTimeline.createRoute(id, caseNumber))
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
        
        // ADMIN Report Detail Screen
        composable(
            route = Screen.AdminReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            
            com.example.blottermanagementsystem.ui.screens.admin.AdminReportDetailScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
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
                onNavigateToHearingList = { id ->
                    navController.navigate(Screen.HearingList.route)
                },
                onNavigateToLegalDocuments = { id ->
                    navController.navigate(Screen.LegalDocumentsDashboard.createRoute(id))
                },
                onNavigateToCaseTimeline = { id, caseNumber ->
                    navController.navigate(Screen.CaseTimeline.createRoute(id, caseNumber))
                },
                onNavigateToEnhancedQR = { id, caseNumber ->
                    navController.navigate(Screen.EnhancedQR.createRoute(id, caseNumber))
                },
                onNavigateToAdvancedAnalytics = {
                    navController.navigate(Screen.AdvancedAnalytics.route)
                },
                onNavigateToIncidentMap = {
                    navController.navigate(Screen.IncidentMap.route)
                }
            )
        }
        
        // OFFICER Report Detail Screen
        composable(
            route = Screen.OfficerReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            
            com.example.blottermanagementsystem.ui.screens.officer.OfficerReportDetailScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddRespondent = { id ->
                    navController.navigate(Screen.AddRespondent.createRoute(id))
                },
                onNavigateToRespondentList = { id ->
                    navController.navigate(Screen.RespondentList.createRoute(id))
                },
                onNavigateToAddSuspect = { id ->
                    navController.navigate(Screen.AddSuspect.createRoute(id))
                },
                onNavigateToSuspectList = { id ->
                    navController.navigate(Screen.SuspectList.createRoute(id))
                },
                onNavigateToAddWitness = { id ->
                    navController.navigate(Screen.AddWitness.createRoute(id))
                },
                onNavigateToWitnessList = { id ->
                    navController.navigate(Screen.WitnessList.createRoute(id))
                },
                onNavigateToAddEvidence = { id ->
                    navController.navigate(Screen.AddEvidence.createRoute(id))
                },
                onNavigateToEvidenceList = { id ->
                    navController.navigate(Screen.EvidenceList.createRoute(id))
                },
                onNavigateToAddHearing = { id ->
                    navController.navigate(Screen.AddHearing.createRoute(id))
                },
                onNavigateToHearingList = { id ->
                    navController.navigate(Screen.HearingList.route)
                },
                onNavigateToAddResolution = { id ->
                    navController.navigate(Screen.AddResolution.createRoute(id))
                },
                onNavigateToLegalDocuments = { id ->
                    navController.navigate(Screen.LegalDocumentsDashboard.createRoute(id))
                },
                onNavigateToCaseTimeline = { id, caseNumber ->
                    navController.navigate(Screen.CaseTimeline.createRoute(id, caseNumber))
                },
                onNavigateToPhotoGallery = { id ->
                    navController.navigate(Screen.PhotoGallery.createRoute(id))
                },
                onNavigateToEnhancedQR = { id, caseNumber ->
                    navController.navigate(Screen.EnhancedQR.createRoute(id, caseNumber))
                },
                onNavigateToHearingCalendar = {
                    navController.navigate(Screen.HearingCalendar.route)
                },
                onNavigateToIncidentMap = {
                    navController.navigate(Screen.IncidentMap.route)
                },
                onNavigateToVoiceToText = {
                    navController.navigate(Screen.VoiceToText.route)
                }
            )
        }
        
        // USER Report Detail Screen
        composable(
            route = Screen.UserReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userId = preferencesManager.userId
            
            com.example.blottermanagementsystem.ui.screens.user.UserReportDetailScreen(
                reportId = reportId,
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditReport.createRoute(id))
                },
                onNavigateToHearingList = { id ->
                    navController.navigate(Screen.HearingList.route)
                },
                onNavigateToCaseTimeline = { id, caseNumber ->
                    navController.navigate(Screen.CaseTimeline.createRoute(id, caseNumber))
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
                    navController.navigate(Screen.AdminReportDetail.createRoute(reportId))
                }
            )
        }
        
        // Other Screens
        composable(Screen.Notifications.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.userRole ?: "User"
            
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReport = { reportId ->
                    when (userRole) {
                        "Admin" -> navController.navigate(Screen.AdminReportDetail.createRoute(reportId))
                        "Officer" -> navController.navigate(Screen.OfficerReportDetail.createRoute(reportId))
                        else -> navController.navigate(Screen.UserReportDetail.createRoute(reportId))
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.userRole ?: "User"
            
            // Route to appropriate profile screen based on role
            when (userRole) {
                "Admin" -> {
                    AdminProfileScreen(
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
                "Officer" -> {
                    OfficerProfileScreen(
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
                else -> {
                    // User/Clerk role - with photo selection
                    UserProfileScreen(
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
            }
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackupRestore = {
                    navController.navigate(Screen.BackupRestore.route)
                }
            )
        }
        
        composable(Screen.AdminNotificationSender.route) {
            AdminNotificationSenderScreen(
                onNavigateBack = { navController.popBackStack() }
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
                        // Valid blotter report QR code - Officers use QR scanner
                        navController.navigate(Screen.OfficerReportDetail.createRoute(reportId)) {
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
                    navController.navigate(Screen.OfficerReportDetail.createRoute(reportId))
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
        
        // Case Timeline Screen
        composable(
            route = Screen.CaseTimeline.route,
            arguments = listOf(
                navArgument("reportId") { type = NavType.IntType },
                navArgument("caseNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val caseNumber = backStackEntry.arguments?.getString("caseNumber") ?: ""
            
            com.example.blottermanagementsystem.ui.screens.timeline.CaseTimelineScreen(
                reportId = reportId,
                caseNumber = caseNumber,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Case Templates Screen
        composable(Screen.CaseTemplates.route) {
            com.example.blottermanagementsystem.ui.screens.templates.CaseTemplatesScreen(
                onNavigateBack = { navController.popBackStack() },
                onTemplateSelected = { template ->
                    // Navigate to add report with template
                    navController.navigate(Screen.AddReport.route)
                    navController.popBackStack()
                }
            )
        }
        
        // Advanced Search Screen
        composable(Screen.AdvancedSearch.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.userRole ?: "User"
            
            com.example.blottermanagementsystem.ui.screens.search.AdvancedSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onReportClick = { reportId ->
                    when (userRole) {
                        "Admin" -> navController.navigate(Screen.AdminReportDetail.createRoute(reportId))
                        "Officer" -> navController.navigate(Screen.OfficerReportDetail.createRoute(reportId))
                        else -> navController.navigate(Screen.UserReportDetail.createRoute(reportId))
                    }
                }
            )
        }
        
        // Hearing Calendar Screen
        composable(Screen.HearingCalendar.route) {
            com.example.blottermanagementsystem.ui.screens.calendar.HearingCalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onHearingClick = { hearingId ->
                    // Navigate to hearing detail if needed
                }
            )
        }
        
        // Photo Gallery Screen
        composable(
            route = Screen.PhotoGallery.route,
            arguments = listOf(navArgument("reportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            
            com.example.blottermanagementsystem.ui.screens.evidence.PhotoGalleryScreen(
                reportId = reportId,
                photoUris = emptyList(), // TODO: Load from evidence
                onNavigateBack = { navController.popBackStack() },
                onAddPhoto = {
                    navController.navigate(Screen.AddEvidence.createRoute(reportId))
                }
            )
        }
        
        // Analytics Screen
        composable(Screen.AdvancedAnalytics.route) {
            com.example.blottermanagementsystem.ui.screens.analytics.AnalyticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Enhanced QR Screen
        composable(
            route = Screen.EnhancedQR.route,
            arguments = listOf(
                navArgument("reportId") { type = NavType.IntType },
                navArgument("caseNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
            val caseNumber = backStackEntry.arguments?.getString("caseNumber") ?: ""
            
            com.example.blottermanagementsystem.ui.screens.qr.EnhancedQRScreen(
                reportId = reportId,
                caseNumber = caseNumber,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Incident Map Screen
        composable(Screen.IncidentMap.route) {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val userRole = preferencesManager.userRole ?: "User"
            
            com.example.blottermanagementsystem.ui.screens.map.IncidentMapScreen(
                onNavigateBack = { navController.popBackStack() },
                onReportClick = { reportId ->
                    when (userRole) {
                        "Admin" -> navController.navigate(Screen.AdminReportDetail.createRoute(reportId))
                        "Officer" -> navController.navigate(Screen.OfficerReportDetail.createRoute(reportId))
                        else -> navController.navigate(Screen.UserReportDetail.createRoute(reportId))
                    }
                }
            )
        }
        
        // Voice-to-Text Screen
        composable(Screen.VoiceToText.route) {
            com.example.blottermanagementsystem.ui.screens.voice.VoiceToTextScreen(
                onNavigateBack = { navController.popBackStack() },
                onTextGenerated = { text ->
                    // Navigate back with result
                    navController.previousBackStackEntry?.savedStateHandle?.set("voiceText", text)
                    navController.popBackStack()
                }
            )
        }
    }
}
