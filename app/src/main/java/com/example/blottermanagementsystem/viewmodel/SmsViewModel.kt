package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.SmsNotification
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import android.util.Log
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SmsViewModel(application: Application) : AndroidViewModel(application) {
    
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
    
    private val _failedNotificationCount = MutableStateFlow(0)
    val failedNotificationCount: StateFlow<Int> = _failedNotificationCount
    
    init {
        loadFailedNotificationCount()
    }
    
    private fun loadFailedNotificationCount() {
        viewModelScope.launch {
            _failedNotificationCount.value = repository.getFailedNotificationCount()
        }
    }
    
    fun getNotificationsByReportId(reportId: Int): Flow<List<SmsNotification>> {
        return repository.getNotificationsByReportId(reportId)
    }
    
    fun getNotificationsByRespondentId(respondentId: Int): Flow<List<SmsNotification>> {
        return repository.getNotificationsByRespondentId(respondentId)
    }
    
    fun getNotificationsByStatus(status: String): Flow<List<SmsNotification>> {
        return repository.getNotificationsByStatus(status)
    }
    
    fun getAllNotifications(): Flow<List<SmsNotification>> {
        // Get all notifications by combining different statuses
        return combine(
            repository.getNotificationsByStatus("Pending"),
            repository.getNotificationsByStatus("Sent"),
            repository.getNotificationsByStatus("Delivered"),
            repository.getNotificationsByStatus("Failed"),
            repository.getNotificationsByStatus("Replied")
        ) { pending, sent, delivered, failed, replied ->
            (pending + sent + delivered + failed + replied).sortedByDescending { it.sentDate }
        }
    }
    
    suspend fun sendNotification(notification: SmsNotification): Long {
        // Save to local Room database
        val id = repository.insertSmsNotification(notification)
        loadFailedNotificationCount()
        return id
    }
    
    suspend fun updateDeliveryStatus(notificationId: Int, status: String) {
        repository.updateDeliveryStatus(notificationId, status)
        loadFailedNotificationCount()
    }
    
    suspend fun recordReply(notificationId: Int, reply: String) {
        val date = System.currentTimeMillis()
        repository.recordReply(notificationId, reply, date)
        repository.updateDeliveryStatus(notificationId, "Replied")
    }
    
    suspend fun retryFailedNotification(notificationId: Int) {
        repository.updateDeliveryStatus(notificationId, "Pending")
        loadFailedNotificationCount()
    }
    
    fun resendSms(notificationId: Int) {
        viewModelScope.launch {
            repository.updateDeliveryStatus(notificationId, "Pending")
            loadFailedNotificationCount()
        }
    }
    
    fun deleteSms(notificationId: Int) {
        viewModelScope.launch {
            repository.deleteSmsNotification(notificationId)
            loadFailedNotificationCount()
        }
    }
    
    fun refreshFailedCount() {
        loadFailedNotificationCount()
    }
}

