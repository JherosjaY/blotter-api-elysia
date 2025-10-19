package com.example.blottermanagementsystem.utils

/**
 * Language Manager for Bisaya/English translations
 * Focused on User-facing screens (casual Bisaya for barangay residents)
 */
object LanguageManager {
    
    fun getString(key: String, language: String = "en"): String {
        return when (language) {
            "bisaya" -> bisayaStrings[key] ?: englishStrings[key] ?: key
            else -> englishStrings[key] ?: key
        }
    }
    
    // English translations
    private val englishStrings = mapOf(
        // Common
        "app_name" to "Blotter Management",
        "submit" to "Submit",
        "cancel" to "Cancel",
        "save" to "Save",
        "delete" to "Delete",
        "edit" to "Edit",
        "search" to "Search",
        "filter" to "Filter",
        "back" to "Back",
        "next" to "Next",
        "done" to "Done",
        "yes" to "Yes",
        "no" to "No",
        "ok" to "OK",
        "loading" to "Loading...",
        "add" to "Add",
        "view" to "View",
        "update" to "Update",
        "confirm" to "Confirm",
        "close" to "Close",
        
        // Report Filing
        "file_report" to "File a Report",
        "new_report" to "New Report",
        "my_reports" to "My Reports",
        "all_reports" to "All Reports",
        "complainant_info" to "Complainant Information",
        "complainant_name" to "Complainant Name",
        "contact_number" to "Contact Number",
        "address" to "Address",
        "incident_details" to "Incident Details",
        "incident_type" to "Type of Incident",
        "incident_date" to "Date of Incident",
        "incident_time" to "Time of Incident",
        "incident_location" to "Location of Incident",
        "description" to "Description",
        "what_happened" to "What happened?",
        "respondent_info" to "Respondent Information",
        "respondent_name" to "Respondent Name",
        "respondent_address" to "Respondent Address",
        "case_number" to "Case Number",
        "date_filed" to "Date Filed",
        
        // Evidence
        "evidence" to "Evidence",
        "add_evidence" to "Add Evidence",
        "evidence_type" to "Type of Evidence",
        "evidence_description" to "Evidence Description",
        "location_found" to "Location Found",
        "collected_by" to "Collected By",
        "take_photo" to "Take Photo",
        "select_photo" to "Select Photo",
        "no_evidence" to "No evidence added yet",
        
        // Witnesses
        "witness" to "Witness",
        "witnesses" to "Witnesses",
        "add_witness" to "Add Witness",
        "witness_name" to "Witness Name",
        "witness_statement" to "Witness Statement",
        "no_witnesses" to "No witnesses added yet",
        
        // Suspects
        "suspect" to "Suspect",
        "suspects" to "Suspects",
        "add_suspect" to "Add Suspect",
        "suspect_name" to "Suspect Name",
        "suspect_alias" to "Alias",
        "age" to "Age",
        "gender" to "Gender",
        "no_suspects" to "No suspects added yet",
        
        // Status
        "status" to "Status",
        "pending" to "Pending",
        "assigned" to "Assigned",
        "under_investigation" to "Under Investigation",
        "resolved" to "Resolved",
        "closed" to "Closed",
        
        // Notifications
        "notifications" to "Notifications",
        "no_notifications" to "No notifications yet",
        "mark_as_read" to "Mark as read",
        "mark_all_read" to "Mark all as read",
        "new_case_assigned" to "New case assigned to you",
        "status_updated" to "Case status updated",
        
        // Settings
        "settings" to "Settings",
        "language" to "Language",
        "english" to "English",
        "bisaya" to "Bisaya",
        "change_language" to "Change Language",
        "dark_mode" to "Dark Mode",
        "profile" to "Profile",
        "logout" to "Logout",
        
        // Dashboard
        "dashboard" to "Dashboard",
        "welcome" to "Welcome",
        "total_reports" to "Total Reports",
        "pending_cases" to "Pending Cases",
        "resolved_cases" to "Resolved Cases",
        "my_cases" to "My Cases",
        
        // Messages
        "report_submitted" to "Report submitted successfully",
        "thank_you" to "Thank you for reporting",
        "we_will_contact" to "We will contact you soon",
        "required_field" to "This field is required",
        "invalid_phone" to "Invalid phone number",
        "invalid_date" to "Invalid date",
        "success" to "Success",
        "error" to "Error",
        "are_you_sure" to "Are you sure?",
        "cannot_undo" to "This action cannot be undone",
        
        // Actions
        "view_details" to "View Details",
        "add_new" to "Add New",
        "edit_info" to "Edit Information",
        "delete_item" to "Delete Item",
        "refresh" to "Refresh",
        "export" to "Export"
    )
    
    // Bisaya translations (Casual, easy to understand)
    private val bisayaStrings = mapOf(
        // Common
        "app_name" to "Blotter Management",
        "submit" to "Isumite",
        "cancel" to "Kanselahon",
        "save" to "Tipigi",
        "delete" to "Tangtangon",
        "edit" to "Usbon",
        "search" to "Pangitaa",
        "filter" to "Pilion",
        "back" to "Balik",
        "next" to "Sunod",
        "done" to "Human na",
        "yes" to "Oo",
        "no" to "Dili",
        "ok" to "Sige",
        "loading" to "Naghulat...",
        "add" to "Idugang",
        "view" to "Tan-awa",
        "update" to "I-update",
        "confirm" to "Siguruha",
        "close" to "Sirado",
        
        // Report Filing
        "file_report" to "Magreklamo",
        "new_report" to "Bag-ong Reklamo",
        "my_reports" to "Akong mga Reklamo",
        "all_reports" to "Tanan nga Reklamo",
        "complainant_info" to "Impormasyon sa Nagreklamo",
        "complainant_name" to "Ngalan sa Nagreklamo",
        "contact_number" to "Numero sa Telepono",
        "address" to "Adres / Puy-anan",
        "incident_details" to "Detalye sa Nahitabo",
        "incident_type" to "Unsa nga Klase sa Kaso",
        "incident_date" to "Petsa sa Nahitabo",
        "incident_time" to "Oras sa Nahitabo",
        "incident_location" to "Asa nahitabo",
        "description" to "Detalye",
        "what_happened" to "Unsa ang nahitabo?",
        "respondent_info" to "Impormasyon sa Gikasuhan",
        "respondent_name" to "Ngalan sa Gikasuhan",
        "respondent_address" to "Adres sa Gikasuhan",
        "case_number" to "Numero sa Kaso",
        "date_filed" to "Petsa sa Pagreklamo",
        
        // Evidence
        "evidence" to "Ebidensya",
        "add_evidence" to "Idugang og Ebidensya",
        "evidence_type" to "Klase sa Ebidensya",
        "evidence_description" to "Detalye sa Ebidensya",
        "location_found" to "Asa nakit-an",
        "collected_by" to "Kinuha ni",
        "take_photo" to "Kuhaa og Litrato",
        "select_photo" to "Pilia ang Litrato",
        "no_evidence" to "Walay ebidensya pa",
        
        // Witnesses
        "witness" to "Saksi",
        "witnesses" to "Mga Saksi",
        "add_witness" to "Idugang og Saksi",
        "witness_name" to "Ngalan sa Saksi",
        "witness_statement" to "Pahayag sa Saksi",
        "no_witnesses" to "Walay saksi pa",
        
        // Suspects
        "suspect" to "Suspetsado",
        "suspects" to "Mga Suspetsado",
        "add_suspect" to "Idugang og Suspetsado",
        "suspect_name" to "Ngalan sa Suspetsado",
        "suspect_alias" to "Alyas",
        "age" to "Edad",
        "gender" to "Sekso",
        "no_suspects" to "Walay suspetsado pa",
        
        // Status
        "status" to "Kahimtang",
        "pending" to "Naghulat pa",
        "assigned" to "Gi-assign na",
        "under_investigation" to "Gi-imbestigahan pa",
        "resolved" to "Nahuman na",
        "closed" to "Sirado na",
        
        // Notifications
        "notifications" to "Mga Pahibalo",
        "no_notifications" to "Walay bag-ong pahibalo",
        "mark_as_read" to "Markahi nga nabasa na",
        "mark_all_read" to "Markahi tanan nga nabasa na",
        "new_case_assigned" to "Bag-ong kaso gi-assign nimo",
        "status_updated" to "Gi-update ang kahimtang sa kaso",
        
        // Settings
        "settings" to "Mga Setting",
        "language" to "Pinulongan",
        "english" to "English",
        "bisaya" to "Binisaya",
        "change_language" to "Usba ang Pinulongan",
        "dark_mode" to "Dark Mode",
        "profile" to "Profile",
        "logout" to "Logout",
        
        // Dashboard
        "dashboard" to "Dashboard",
        "welcome" to "Maayong pag-abot",
        "total_reports" to "Kinatibuk-ang Reklamo",
        "pending_cases" to "Naghulat nga Kaso",
        "resolved_cases" to "Nahuman nga Kaso",
        "my_cases" to "Akong mga Kaso",
        
        // Messages
        "report_submitted" to "Nadawat na ang imong reklamo",
        "thank_you" to "Salamat sa pag-report",
        "we_will_contact" to "Kontakon namo ka sa dili madugay",
        "required_field" to "Kinahanglan ni",
        "invalid_phone" to "Sayop ang numero",
        "invalid_date" to "Sayop ang petsa",
        "success" to "Malampuson",
        "error" to "May Sayop",
        "are_you_sure" to "Sigurado ka ba?",
        "cannot_undo" to "Dili na ni ma-undo",
        
        // Actions
        "view_details" to "Tan-awa ang Detalye",
        "add_new" to "Idugang og Bag-o",
        "edit_info" to "Usba ang Impormasyon",
        "delete_item" to "Tangtangon",
        "refresh" to "I-refresh",
        "export" to "I-export"
    )
}
