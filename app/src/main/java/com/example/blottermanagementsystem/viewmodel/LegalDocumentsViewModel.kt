package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.*
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LegalDocumentsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = BlotterDatabase.getDatabase(application)
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
    
    // Summons Management
    fun getSummonsByReportId(reportId: Int) = repository.getSummonsByReportId(reportId)
    fun getSummonsByRespondentId(respondentId: Int) = repository.getSummonsByRespondentId(respondentId)
    fun getPendingSummons() = repository.getPendingSummons()
    fun getUncompliedSummons() = repository.getUncompliedSummons()
    
    // Respondent Management (for summons)
    fun getRespondentsByReportId(reportId: Int) = repository.getRespondentsByReportId(reportId)
    
    suspend fun createSummons(
        blotterReportId: Int,
        respondentId: Int,
        summonsType: String,
        hearingDate: Long?,
        hearingTime: String?,
        purpose: String,
        issuedBy: String
    ): Long {
        val summonsNumber = generateSummonsNumber()
        val summons = Summons(
            blotterReportId = blotterReportId,
            respondentId = respondentId,
            summonsNumber = summonsNumber,
            summonsType = summonsType,
            hearingDate = hearingDate,
            hearingTime = hearingTime,
            purpose = purpose,
            issuedBy = issuedBy
        )
        return repository.insertSummons(summons)
    }
    
    suspend fun updateSummonsDelivery(
        summonsId: Int,
        status: String,
        receivedBy: String?,
        relation: String?,
        notes: String?
    ) {
        val summons = repository.getSummonsById(summonsId)
        summons?.let {
            repository.updateSummons(
                it.copy(
                    deliveryStatus = status,
                    receivedBy = receivedBy,
                    receivedByRelation = relation,
                    deliveryNotes = notes,
                    receivedDate = if (status == "Served") System.currentTimeMillis() else null
                )
            )
        }
    }
    
    suspend fun markSummonsComplied(summonsId: Int, notes: String?) {
        repository.markSummonsAsComplied(summonsId, System.currentTimeMillis(), notes)
    }
    
    // KP Forms Management
    fun getKPFormsByReportId(reportId: Int) = repository.getKPFormsByReportId(reportId)
    fun getKPFormsByType(formType: String) = repository.getKPFormsByType(formType)
    
    suspend fun createKPForm(
        blotterReportId: Int,
        formType: String,
        formTitle: String,
        issuedBy: String,
        createdBy: String
    ): Long {
        val formNumber = generateKPFormNumber(formType)
        val kpForm = KPForm(
            blotterReportId = blotterReportId,
            formType = formType,
            formNumber = formNumber,
            formTitle = formTitle,
            issuedBy = issuedBy,
            createdBy = createdBy
        )
        return repository.insertKPForm(kpForm)
    }
    
    suspend fun createNoticeOfHearing(
        blotterReportId: Int,
        hearingDate: Long,
        hearingTime: String,
        venue: String,
        issuedBy: String,
        createdBy: String
    ): Long {
        val formNumber = generateKPFormNumber("KP-7")
        val kpForm = KPForm(
            blotterReportId = blotterReportId,
            formType = "KP-7",
            formNumber = formNumber,
            formTitle = "Notice of Hearing",
            hearingDate = hearingDate,
            hearingTime = hearingTime,
            hearingVenue = venue,
            issuedBy = issuedBy,
            createdBy = createdBy
        )
        return repository.insertKPForm(kpForm)
    }
    
    suspend fun createAmicableSettlement(
        blotterReportId: Int,
        settlementTerms: String,
        issuedBy: String,
        createdBy: String
    ): Long {
        val formNumber = generateKPFormNumber("KP-16")
        val kpForm = KPForm(
            blotterReportId = blotterReportId,
            formType = "KP-16",
            formNumber = formNumber,
            formTitle = "Amicable Settlement",
            settlementTerms = settlementTerms,
            settlementDate = System.currentTimeMillis(),
            issuedBy = issuedBy,
            createdBy = createdBy,
            status = "Draft"
        )
        return repository.insertKPForm(kpForm)
    }
    
    suspend fun createCertificateToFileAction(
        blotterReportId: Int,
        reason: String,
        attemptsMade: Int,
        issuedBy: String,
        createdBy: String
    ): Long {
        val formNumber = generateKPFormNumber("KP-18")
        val kpForm = KPForm(
            blotterReportId = blotterReportId,
            formType = "KP-18",
            formNumber = formNumber,
            formTitle = "Certificate to File Action",
            certificationReason = reason,
            attemptsMade = attemptsMade,
            lastAttemptDate = System.currentTimeMillis(),
            issuedBy = issuedBy,
            createdBy = createdBy
        )
        return repository.insertKPForm(kpForm)
    }
    
    suspend fun updateKPFormStatus(formId: Int, status: String) {
        repository.updateFormStatus(formId, status, System.currentTimeMillis())
    }
    
    // Mediation Sessions Management
    fun getMediationSessionsByReportId(reportId: Int) = repository.getMediationSessionsByReportId(reportId)
    fun getUpcomingMediationSessions() = repository.getUpcomingMediationSessions()
    
    suspend fun createMediationSession(
        blotterReportId: Int,
        sessionDate: Long,
        sessionTime: String,
        mediatorName: String,
        sessionType: String,
        recordedBy: String
    ): Long {
        val sessionCount = repository.getMediationSessionCount(blotterReportId)
        val session = MediationSession(
            blotterReportId = blotterReportId,
            sessionNumber = sessionCount + 1,
            sessionDate = sessionDate,
            sessionTime = sessionTime,
            mediatorName = mediatorName,
            sessionType = sessionType,
            discussionSummary = "",
            outcome = "Ongoing",
            recordedBy = recordedBy
        )
        return repository.insertMediationSession(session)
    }
    
    suspend fun updateMediationOutcome(
        sessionId: Int,
        outcome: String,
        discussionSummary: String,
        settlementTerms: String?,
        reasonForFailure: String?
    ) {
        val session = repository.getMediationSessionById(sessionId)
        session?.let {
            repository.updateMediationSession(
                it.copy(
                    outcome = outcome,
                    discussionSummary = discussionSummary,
                    settlementTerms = settlementTerms,
                    reasonForFailure = reasonForFailure
                )
            )
        }
    }
    
    suspend fun scheduleNextMediationSession(
        sessionId: Int,
        nextDate: Long,
        nextTime: String
    ) {
        val session = repository.getMediationSessionById(sessionId)
        session?.let {
            repository.updateMediationSession(
                it.copy(
                    nextSessionScheduled = true,
                    nextSessionDate = nextDate,
                    nextSessionTime = nextTime
                )
            )
        }
    }
    
    // Helper functions
    private fun generateSummonsNumber(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "SUM-$dateStr-$random"
    }
    
    private fun generateKPFormNumber(formType: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "$formType-$dateStr-$random"
    }
    
    // Statistics
    suspend fun getDocumentStatistics(reportId: Int): DocumentStatistics {
        val summonsCount = repository.getSummonsCountByReport(reportId)
        val kpFormsCount = repository.getKPFormsCountByReport(reportId)
        val mediationCount = repository.getMediationSessionCount(reportId)
        
        return DocumentStatistics(
            summonsIssued = summonsCount,
            kpFormsGenerated = kpFormsCount,
            mediationAttempts = mediationCount
        )
    }
}

data class DocumentStatistics(
    val summonsIssued: Int = 0,
    val kpFormsGenerated: Int = 0,
    val mediationAttempts: Int = 0
)
