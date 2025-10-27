package com.example.blottermanagementsystem.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.blottermanagementsystem.data.dao.*
import com.example.blottermanagementsystem.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        User::class,
        BlotterReport::class,
        Suspect::class,
        Witness::class,
        Evidence::class,
        Hearing::class,
        StatusHistory::class,
        Resolution::class,
        Officer::class,
        ActivityLog::class,
        Notification::class,
        Status::class,
        Person::class,
        Respondent::class,
        PersonHistory::class,
        SmsNotification::class,
        RespondentStatement::class,
        Summons::class,
        KPForm::class,
        MediationSession::class,
        CaseTimeline::class,
        CaseTemplate::class
    ],
    version = 8,
    exportSchema = false
)
abstract class BlotterDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun blotterReportDao(): BlotterReportDao
    abstract fun suspectDao(): SuspectDao
    abstract fun witnessDao(): WitnessDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun hearingDao(): HearingDao
    abstract fun statusHistoryDao(): StatusHistoryDao
    abstract fun resolutionDao(): ResolutionDao
    abstract fun officerDao(): OfficerDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun notificationDao(): NotificationDao
    abstract fun statusDao(): StatusDao
    abstract fun personDao(): PersonDao
    abstract fun respondentDao(): RespondentDao
    abstract fun personHistoryDao(): PersonHistoryDao
    abstract fun smsNotificationDao(): SmsNotificationDao
    abstract fun respondentStatementDao(): RespondentStatementDao
    abstract fun summonsDao(): SummonsDao
    abstract fun kpFormDao(): KPFormDao
    abstract fun mediationSessionDao(): MediationSessionDao
    abstract fun caseTimelineDao(): CaseTimelineDao
    abstract fun caseTemplateDao(): CaseTemplateDao
    
    companion object {
        @Volatile
        private var INSTANCE: BlotterDatabase? = null
        
        // Migration from version 1 to 2 - Add evidence fields
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE blotter_reports ADD COLUMN imageUris TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE blotter_reports ADD COLUMN videoUris TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE blotter_reports ADD COLUMN videoDurations TEXT NOT NULL DEFAULT ''"
                )
            }
        }
        
        // Migration from version 3 to 4 - Add assignedOfficerIds field
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE blotter_reports ADD COLUMN assignedOfficerIds TEXT NOT NULL DEFAULT ''"
                )
            }
        }
        
        // Migration from version 5 to 6 - Add userId to officers table
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE officers ADD COLUMN userId INTEGER"
                )
            }
        }
        
        // Migration from version 6 to 7 - Add photo/video fields to evidence table
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE evidence ADD COLUMN locationFound TEXT"
                )
                database.execSQL(
                    "ALTER TABLE evidence ADD COLUMN chainOfCustodyNotes TEXT"
                )
                database.execSQL(
                    "ALTER TABLE evidence ADD COLUMN photoUris TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE evidence ADD COLUMN videoUris TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE evidence ADD COLUMN capturedBy TEXT"
                )
                database.execSQL(
                    "ALTER TABLE evidence ADD COLUMN captureTimestamp INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
        
        // Migration from version 7 to 8 - Add case timeline and templates
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create case_timeline table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS case_timeline (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        blotterReportId INTEGER NOT NULL,
                        eventType TEXT NOT NULL,
                        eventTitle TEXT NOT NULL,
                        eventDescription TEXT NOT NULL,
                        performedBy TEXT NOT NULL,
                        performedByRole TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        metadata TEXT,
                        FOREIGN KEY(blotterReportId) REFERENCES blotter_reports(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_case_timeline_blotterReportId ON case_timeline(blotterReportId)")
                
                // Create case_templates table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS case_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        templateName TEXT NOT NULL,
                        incidentType TEXT NOT NULL,
                        descriptionTemplate TEXT NOT NULL,
                        commonQuestions TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        usageCount INTEGER NOT NULL DEFAULT 0,
                        createdBy TEXT NOT NULL,
                        createdDate INTEGER NOT NULL
                    )
                """)
            }
        }
        
        fun getDatabase(context: Context): BlotterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlotterDatabase::class.java,
                    "blotter_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_3_4, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration() // For version 3 with new tables
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }
        
        private suspend fun populateDatabase(database: BlotterDatabase) {
            val userDao = database.userDao()
            val statusDao = database.statusDao()
            
            // Insert default statuses
            val statuses = listOf(
                Status(name = "Pending", description = "Case is pending review", color = "#FFA726"),
                Status(name = "Under Investigation", description = "Case is being investigated", color = "#42A5F5"),
                Status(name = "For Mediation", description = "Case scheduled for mediation", color = "#9C27B0"),
                Status(name = "For Lupon", description = "Case referred to Lupon Tagapamayapa", color = "#673AB7"),
                Status(name = "Mediation Ongoing", description = "Mediation in progress", color = "#3F51B5"),
                Status(name = "Settled", description = "Case settled amicably", color = "#4CAF50"),
                Status(name = "Referred to Court", description = "Case referred to court/prosecutor", color = "#FF5722"),
                Status(name = "Resolved", description = "Case has been resolved", color = "#66BB6A"),
                Status(name = "Archived", description = "Case has been archived", color = "#78909C"),
                Status(name = "Closed", description = "Case is closed", color = "#EF5350")
            )
            statuses.forEach { statusDao.insertStatus(it) }
            
            // Insert default admin user (local + will sync to cloud on first login)
            val adminPassword = hashPassword("admin123")
            val adminUser = User(
                firstName = "System",
                lastName = "Administrator",
                username = "admin",
                password = adminPassword,
                role = "Admin",
                profileCompleted = true,
                badgeNumber = "ADMIN001",
                rank = "Administrator",
                dutyStatus = "Active"
            )
            userDao.insertUser(adminUser)
        }
        
        private fun hashPassword(password: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray())
            return hash.fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}
