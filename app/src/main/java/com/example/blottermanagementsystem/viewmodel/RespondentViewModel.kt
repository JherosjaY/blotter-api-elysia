package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.*
import com.example.blottermanagementsystem.data.repository.ApiRepository
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RespondentViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = BlotterDatabase.getDatabase(application)
    private val apiRepository = ApiRepository()
    private val repository = BlotterRepository(
        userDao = database.userDao(),
        blotterReportDao = database.blotterReportDao(),
        suspectDao = database.suspectDao(),
        witnessDao = database.witnessDao(),
        evidenceDao = database.evidenceDao(),
        hearingDao = database.hearingDao(),
        statusHistoryDao = database.statusHistoryDao(),
        resolutionDao = database.resolutionDao(),
        officerDao = database.officerDao(),
        activityLogDao = database.activityLogDao(),
        notificationDao = database.notificationDao(),
        statusDao = database.statusDao(),
        personDao = database.personDao(),
        respondentDao = database.respondentDao(),
        personHistoryDao = database.personHistoryDao(),
        smsNotificationDao = database.smsNotificationDao(),
        respondentStatementDao = database.respondentStatementDao(),
        summonsDao = database.summonsDao(),
        kpFormDao = database.kpFormDao(),
        mediationSessionDao = database.mediationSessionDao(),
        caseTimelineDao = database.caseTimelineDao(),
        caseTemplateDao = database.caseTemplateDao()
    )
    
    val respondentsNeedingAttention: Flow<List<Respondent>> = repository.getRespondentsNeedingAttention()
    
    private val _cooperationStats = MutableStateFlow(CooperationStats())
    val cooperationStats: StateFlow<CooperationStats> = _cooperationStats
    
    init {
        loadCooperationStats()
    }
    
    private fun loadCooperationStats() {
        viewModelScope.launch {
            val cooperated = repository.getCountByCooperationStatus("Appeared")
            val pending = repository.getCountByCooperationStatus("Notified")
            val noResponse = repository.getCountByCooperationStatus("No Response")
            val refused = repository.getCountByCooperationStatus("Refused")
            
            _cooperationStats.value = CooperationStats(
                cooperated = cooperated,
                pending = pending,
                noResponse = noResponse,
                refused = refused
            )
        }
    }
    
    fun getRespondentsByReportId(reportId: Int): Flow<List<Respondent>> {
        return repository.getRespondentsByReportId(reportId)
    }
    
    suspend fun getRespondentById(respondentId: Int): Respondent? {
        return repository.getRespondentById(respondentId)
    }
    
    suspend fun createRespondent(
        blotterReportId: Int,
        personId: Int,
        accusation: String,
        relationshipToComplainant: String?,
        contactNumber: String,
        hasEvidence: Boolean
    ): Long {
        val respondent = Respondent(
            blotterReportId = blotterReportId,
            personId = personId,
            accusation = accusation,
            relationshipToComplainant = relationshipToComplainant,
            contactNumber = contactNumber,
            hasEvidence = hasEvidence,
            status = "Accused",
            cooperationStatus = "Not Contacted"
        )
        
        // Try to sync to cloud first
        try {
            Log.d("RespondentViewModel", "📝 Adding respondent to cloud...")
            val result = apiRepository.createRespondent(respondent)
            if (result.isSuccess) {
                val cloudRespondent = result.getOrNull()
                if (cloudRespondent != null) {
                    val respondentId = repository.insertRespondent(cloudRespondent)
                    Log.d("RespondentViewModel", "✅ Respondent synced to cloud and local")
                    
                    // Log to person history
                    val history = PersonHistory(
                        personId = personId,
                        blotterReportId = blotterReportId,
                        activityType = PersonActivityType.ADDED_AS_RESPONDENT,
                        description = "Added as respondent: $accusation"
                    )
                    repository.insertHistory(history)
                    
                    return respondentId
                }
            }
        } catch (e: Exception) {
            Log.e("RespondentViewModel", "❌ Error syncing respondent: ${e.message}", e)
        }
        
        // Fallback: Save to local only
        val respondentId = repository.insertRespondent(respondent)
        
        // Log to person history
        val history = PersonHistory(
            personId = personId,
            blotterReportId = blotterReportId,
            activityType = PersonActivityType.ADDED_AS_RESPONDENT,
            description = "Added as respondent: $accusation"
        )
        repository.insertHistory(history)
        
        return respondentId
    }
    
    suspend fun sendInitialNotification(respondentId: Int) {
        val respondent = repository.getRespondentById(respondentId) ?: return
        
        val message = """
            BARANGAY NOTICE
            
            You have been named as respondent in Blotter Case.
            
            Accusation: ${respondent.accusation}
            
            You are required to appear at the Barangay Hall within 7 days to give your statement.
            
            Reply 'ACKNOWLEDGE' to confirm receipt.
        """.trimIndent()
        
        val notification = SmsNotification(
            respondentId = respondentId,
            blotterReportId = respondent.blotterReportId,
            messageType = SmsMessageType.INITIAL_NOTICE,
            messageContent = message,
            recipientNumber = respondent.contactNumber
        )
        
        repository.insertSmsNotification(notification)
        
        // Update respondent
        repository.updateRespondent(
            respondent.copy(
                notificationSent = true,
                notificationSentDate = System.currentTimeMillis(),
                cooperationStatus = "Notified"
            )
        )
    }
    
    suspend fun markAsAppeared(respondentId: Int, statement: String?) {
        val date = System.currentTimeMillis()
        repository.markAsAppeared(respondentId, date)
        
        if (statement != null) {
            repository.recordStatement(respondentId, statement, date)
            
            // Log to person history
            val respondent = repository.getRespondentById(respondentId)
            respondent?.let {
                val history = PersonHistory(
                    personId = it.personId,
                    blotterReportId = it.blotterReportId,
                    activityType = PersonActivityType.APPEARED_IN_PERSON,
                    description = "Appeared at barangay hall and gave statement"
                )
                repository.insertHistory(history)
            }
        }
        
        loadCooperationStats()
    }
    
    suspend fun elevateToSuspect(
        respondentId: Int,
        evidenceFound: String,
        investigationNotes: String
    ) {
        val respondent = repository.getRespondentById(respondentId) ?: return
        
        // Create suspect record
        // Get person details to populate suspect
        val person = repository.getPersonById(respondent.personId)
        val fullName = if (person != null) {
            "${person.firstName} ${person.lastName}"
        } else {
            "Unknown"
        }
        
        val suspect = Suspect(
            blotterReportId = respondent.blotterReportId,
            name = fullName,
            alias = null,
            age = null,
            gender = person?.gender,
            address = person?.address,
            description = evidenceFound,
            photoUri = person?.photoUri
        )
        repository.insertSuspect(suspect)
        
        // Update respondent status
        repository.updateRespondent(
            respondent.copy(
                status = "Elevated to Suspect",
                hasEvidence = true
            )
        )
        
        // Log to person history
        val history = PersonHistory(
            personId = respondent.personId,
            blotterReportId = respondent.blotterReportId,
            activityType = PersonActivityType.ELEVATED_TO_SUSPECT,
            description = "Elevated to suspect. Evidence: $evidenceFound"
        )
        repository.insertHistory(history)
    }
    
    suspend fun clearRespondent(respondentId: Int, reason: String) {
        val respondent = repository.getRespondentById(respondentId) ?: return
        
        repository.updateRespondent(
            respondent.copy(
                status = "Cleared",
                cooperationStatus = "Appeared",
                notes = reason
            )
        )
        
        // Log to person history
        val history = PersonHistory(
            personId = respondent.personId,
            blotterReportId = respondent.blotterReportId,
            activityType = PersonActivityType.CLEARED,
            description = "Cleared: $reason"
        )
        repository.insertHistory(history)
    }
    
    fun refreshStats() {
        loadCooperationStats()
    }
    
    // Respondent Statement Management
    suspend fun addRespondentStatement(statement: com.example.blottermanagementsystem.data.entity.RespondentStatement) {
        repository.insertRespondentStatement(statement)
    }
    
    fun getRespondentStatements(respondentId: Int) = repository.getStatementsByRespondentId(respondentId)
}

data class CooperationStats(
    val cooperated: Int = 0,
    val pending: Int = 0,
    val noResponse: Int = 0,
    val refused: Int = 0
)

