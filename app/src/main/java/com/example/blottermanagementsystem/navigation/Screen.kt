package com.example.blottermanagementsystem.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForcePasswordChange : Screen("force_password_change/{userId}/{currentUsername}") {
        fun createRoute(userId: Int, currentUsername: String) = "force_password_change/$userId/$currentUsername"
    }
    object OfficerWelcomeSplash : Screen("officer_welcome_splash/{officerName}") {
        fun createRoute(officerName: String) = "officer_welcome_splash/$officerName"
    }
    object ProfilePictureSelection : Screen("profile_picture_selection")
    object AdminDashboard : Screen("admin_dashboard")
    object OfficerDashboard : Screen("officer_dashboard")
    object ClerkDashboard : Screen("clerk_dashboard")
    object Camera : Screen("camera")
    object QRScanner : Screen("qr_scanner")
    object Analytics : Screen("analytics")
    object OfficerAnalytics : Screen("officer_analytics/{officerId}") {
        fun createRoute(officerId: Int) = "officer_analytics/$officerId"
    }
    object ReportList : Screen("report_list?filter={filter}&autoFocus={autoFocus}") {
        fun createRoute(filter: String = "All", autoFocus: Boolean = false) = 
            "report_list?filter=$filter&autoFocus=$autoFocus"
    }
    object ReportDetail : Screen("report_detail/{reportId}?isEditable={isEditable}") {
        fun createRoute(reportId: Int, isEditable: Boolean = false) = 
            "report_detail/$reportId?isEditable=$isEditable"
    }
    object AddReport : Screen("add_report")
    object EditReport : Screen("edit_report/{reportId}") {
        fun createRoute(reportId: Int) = "edit_report/$reportId"
    }
    object UserManagement : Screen("user_management")
    object OfficerManagement : Screen("officer_management")
    object ActivityLogs : Screen("activity_logs")
    object AdminReportOversight : Screen("admin_report_oversight")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object AddSuspect : Screen("add_suspect/{reportId}") {
        fun createRoute(reportId: Int) = "add_suspect/$reportId"
    }
    object AddWitness : Screen("add_witness/{reportId}") {
        fun createRoute(reportId: Int) = "add_witness/$reportId"
    }
    object AddEvidence : Screen("add_evidence/{reportId}") {
        fun createRoute(reportId: Int) = "add_evidence/$reportId"
    }
    object AddHearing : Screen("add_hearing/{reportId}") {
        fun createRoute(reportId: Int) = "add_hearing/$reportId"
    }
    object HearingList : Screen("hearing_list")
    object PersonHistory : Screen("person_history/{personId}") {
        fun createRoute(personId: Int) = "person_history/$personId"
    }
    object AssignedCases : Screen("assigned_cases/{officerId}") {
        fun createRoute(officerId: Int) = "assigned_cases/$officerId"
    }
    object WitnessList : Screen("witness_list/{reportId}") {
        fun createRoute(reportId: Int) = "witness_list/$reportId"
    }
    object SuspectList : Screen("suspect_list/{reportId}") {
        fun createRoute(reportId: Int) = "suspect_list/$reportId"
    }
    object EvidenceList : Screen("evidence_list/{reportId}") {
        fun createRoute(reportId: Int) = "evidence_list/$reportId"
    }
    object AddResolution : Screen("add_resolution/{reportId}") {
        fun createRoute(reportId: Int) = "add_resolution/$reportId"
    }
    object AddRespondentStatement : Screen("add_respondent_statement/{respondentId}") {
        fun createRoute(respondentId: Int) = "add_respondent_statement/$respondentId"
    }
    object AddRespondent : Screen("add_respondent/{reportId}") {
        fun createRoute(reportId: Int) = "add_respondent/$reportId"
    }
    object RespondentList : Screen("respondent_list/{reportId}") {
        fun createRoute(reportId: Int) = "respondent_list/$reportId"
    }
    object RespondentDetail : Screen("respondent_detail/{respondentId}") {
        fun createRoute(respondentId: Int) = "respondent_detail/$respondentId"
    }
    object SmsNotificationList : Screen("sms_notification_list?reportId={reportId}") {
        fun createRoute(reportId: Int? = null) = if (reportId != null) {
            "sms_notification_list?reportId=$reportId"
        } else {
            "sms_notification_list"
        }
    }
    object LegalDocumentsDashboard : Screen("legal_documents/{reportId}") {
        fun createRoute(reportId: Int) = "legal_documents/$reportId"
    }
    object SummonsManagement : Screen("summons_management/{reportId}") {
        fun createRoute(reportId: Int) = "summons_management/$reportId"
    }
    object KPForms : Screen("kp_forms/{reportId}") {
        fun createRoute(reportId: Int) = "kp_forms/$reportId"
    }
    object MediationSession : Screen("mediation_session/{reportId}") {
        fun createRoute(reportId: Int) = "mediation_session/$reportId"
    }
    object BackupRestore : Screen("backup_restore")
}
