package com.example.blottermanagementsystem.data.repository

import com.example.blottermanagementsystem.data.dao.*
import com.example.blottermanagementsystem.data.entity.*
import kotlinx.coroutines.flow.Flow

class BlotterRepository(
    private val userDao: UserDao,
    private val blotterReportDao: BlotterReportDao,
    private val suspectDao: SuspectDao,
    private val witnessDao: WitnessDao,
    private val evidenceDao: EvidenceDao,
    private val hearingDao: HearingDao,
    private val statusHistoryDao: StatusHistoryDao,
    private val resolutionDao: ResolutionDao,
    private val officerDao: OfficerDao,
    private val activityLogDao: ActivityLogDao,
    private val notificationDao: NotificationDao,
    private val statusDao: StatusDao,
    private val personDao: PersonDao,
    private val respondentDao: RespondentDao,
    private val personHistoryDao: PersonHistoryDao,
    private val smsNotificationDao: SmsNotificationDao,
    private val respondentStatementDao: RespondentStatementDao,
    private val summonsDao: SummonsDao,
    private val kpFormDao: KPFormDao,
    private val mediationSessionDao: MediationSessionDao,
    private val caseTimelineDao: CaseTimelineDao,
    private val caseTemplateDao: CaseTemplateDao
) {
    
    // User operations
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun getAllUsersSync(): List<User> = userDao.getAllUsersSync()
    suspend fun getUserById(userId: Int) = userDao.getUserById(userId)
    suspend fun getUserByUsername(username: String) = userDao.getUserByUsername(username)
    fun getUsersByRole(role: String) = userDao.getUsersByRole(role)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    suspend fun getUserCountByRole(role: String) = userDao.getUserCountByRole(role)
    suspend fun getTotalUserCount() = userDao.getTotalUserCount()
    
    // Blotter Report operations
    fun getAllActiveReports(): Flow<List<BlotterReport>> = blotterReportDao.getAllActiveReports()
    fun getAllArchivedReports(): Flow<List<BlotterReport>> = blotterReportDao.getAllArchivedReports()
    suspend fun getAllReportsSync(): List<BlotterReport> = blotterReportDao.getAllReportsSync()
    suspend fun getReportById(reportId: Int) = blotterReportDao.getReportById(reportId)
    suspend fun getReportByCaseNumber(caseNumber: String) = blotterReportDao.getReportByCaseNumber(caseNumber)
    fun getReportsByOfficer(officerName: String) = blotterReportDao.getReportsByOfficer(officerName)
    fun getReportsByOfficerId(officerId: Int) = blotterReportDao.getReportsByOfficerId(officerId)
    fun getReportsByStatus(status: String) = blotterReportDao.getReportsByStatus(status)
    fun getReportsByUser(userId: Int) = blotterReportDao.getReportsByUser(userId)
    suspend fun insertReport(report: BlotterReport) = blotterReportDao.insertReport(report)
    suspend fun updateReport(report: BlotterReport) = blotterReportDao.updateReport(report)
    suspend fun deleteReport(report: BlotterReport) = blotterReportDao.deleteReport(report)
    suspend fun getActiveReportCount() = blotterReportDao.getActiveReportCount()
    suspend fun getReportCountByStatus(status: String) = blotterReportDao.getReportCountByStatus(status)
    suspend fun getArchivedReportCount() = blotterReportDao.getArchivedReportCount()
    
    // Suspect operations
    fun getSuspectsByReportId(reportId: Int) = suspectDao.getSuspectsByReportId(reportId)
    suspend fun getSuspectById(suspectId: Int) = suspectDao.getSuspectById(suspectId)
    suspend fun insertSuspect(suspect: Suspect) = suspectDao.insertSuspect(suspect)
    suspend fun updateSuspect(suspect: Suspect) = suspectDao.updateSuspect(suspect)
    suspend fun deleteSuspect(suspect: Suspect) = suspectDao.deleteSuspect(suspect)
    suspend fun deleteSuspectsByReportId(reportId: Int) = suspectDao.deleteSuspectsByReportId(reportId)
    
    // Witness operations
    fun getWitnessesByReportId(reportId: Int) = witnessDao.getWitnessesByReportId(reportId)
    suspend fun getWitnessById(witnessId: Int) = witnessDao.getWitnessById(witnessId)
    suspend fun insertWitness(witness: Witness) = witnessDao.insertWitness(witness)
    suspend fun updateWitness(witness: Witness) = witnessDao.updateWitness(witness)
    suspend fun deleteWitness(witness: Witness) = witnessDao.deleteWitness(witness)
    suspend fun deleteWitnessesByReportId(reportId: Int) = witnessDao.deleteWitnessesByReportId(reportId)
    
    // Evidence operations
    fun getEvidenceByReportId(reportId: Int) = evidenceDao.getEvidenceByReportId(reportId)
    suspend fun getEvidenceById(evidenceId: Int) = evidenceDao.getEvidenceById(evidenceId)
    suspend fun insertEvidence(evidence: Evidence) = evidenceDao.insertEvidence(evidence)
    suspend fun updateEvidence(evidence: Evidence) = evidenceDao.updateEvidence(evidence)
    suspend fun deleteEvidence(evidence: Evidence) = evidenceDao.deleteEvidence(evidence)
    suspend fun deleteEvidenceByReportId(reportId: Int) = evidenceDao.deleteEvidenceByReportId(reportId)
    
    // Hearing operations
    fun getHearingsByReportId(reportId: Int) = hearingDao.getHearingsByReportId(reportId)
    suspend fun getHearingById(hearingId: Int) = hearingDao.getHearingById(hearingId)
    fun getHearingsByStatus(status: String) = hearingDao.getHearingsByStatus(status)
    fun getAllHearings() = hearingDao.getAllHearings()
    suspend fun insertHearing(hearing: Hearing) = hearingDao.insertHearing(hearing)
    suspend fun updateHearing(hearing: Hearing) = hearingDao.updateHearing(hearing)
    suspend fun deleteHearing(hearing: Hearing) = hearingDao.deleteHearing(hearing)
    suspend fun deleteHearingsByReportId(reportId: Int) = hearingDao.deleteHearingsByReportId(reportId)
    
    // Status History operations
    fun getStatusHistoryByReportId(reportId: Int) = statusHistoryDao.getStatusHistoryByReportId(reportId)
    suspend fun insertStatusHistory(statusHistory: StatusHistory) = statusHistoryDao.insertStatusHistory(statusHistory)
    suspend fun deleteStatusHistoryByReportId(reportId: Int) = statusHistoryDao.deleteStatusHistoryByReportId(reportId)
    
    // Resolution operations
    fun getResolutionByReportId(reportId: Int) = resolutionDao.getResolutionByReportId(reportId)
    suspend fun getResolutionById(resolutionId: Int) = resolutionDao.getResolutionById(resolutionId)
    suspend fun insertResolution(resolution: Resolution) = resolutionDao.insertResolution(resolution)
    suspend fun updateResolution(resolution: Resolution) = resolutionDao.updateResolution(resolution)
    suspend fun deleteResolution(resolution: Resolution) = resolutionDao.deleteResolution(resolution)
    
    // Officer operations
    fun getAllOfficers() = officerDao.getAllOfficers()
    suspend fun getAllOfficersSync(): List<Officer> = officerDao.getAllOfficersSync()
    suspend fun getOfficerById(officerId: Int) = officerDao.getOfficerById(officerId)
    suspend fun getOfficerByBadgeNumber(badgeNumber: String) = officerDao.getOfficerByBadgeNumber(badgeNumber)
    suspend fun getOfficerByName(fullName: String) = officerDao.getOfficerByName(fullName)
    suspend fun getOfficerByUserId(userId: Int) = officerDao.getOfficerByUserId(userId)
    suspend fun insertOfficer(officer: Officer) = officerDao.insertOfficer(officer)
    suspend fun updateOfficer(officer: Officer) = officerDao.updateOfficer(officer)
    suspend fun deleteOfficer(officer: Officer) = officerDao.deleteOfficer(officer)
    suspend fun getOfficerCount() = officerDao.getOfficerCount()
    
    // Activity Log operations
    fun getRecentActivityLogs() = activityLogDao.getRecentActivityLogs()
    fun getActivityLogsByCaseId(caseId: Int) = activityLogDao.getActivityLogsByCaseId(caseId)
    fun getActivityLogsByUser(username: String) = activityLogDao.getActivityLogsByUser(username)
    suspend fun insertActivityLog(activityLog: ActivityLog) = activityLogDao.insertActivityLog(activityLog)
    
    // Notification operations
    fun getNotificationsByUserId(userId: Int) = notificationDao.getNotificationsByUserId(userId)
    fun getUnreadNotificationsByUserId(userId: Int) = notificationDao.getUnreadNotificationsByUserId(userId)
    fun getUnreadNotificationCount(userId: Int) = notificationDao.getUnreadNotificationCount(userId)
    suspend fun insertNotification(notification: Notification) = notificationDao.insertNotification(notification)
    suspend fun updateNotification(notification: Notification) = notificationDao.updateNotification(notification)
    suspend fun markAsRead(notificationId: Int) = notificationDao.markAsRead(notificationId)
    suspend fun markAllAsRead(userId: Int) = notificationDao.markAllAsRead(userId)
    suspend fun deleteNotification(notification: Notification) = notificationDao.deleteNotification(notification)
    suspend fun deleteNotificationById(notificationId: Int) = notificationDao.deleteNotificationById(notificationId)
    suspend fun deleteAllNotificationsByUserId(userId: Int) = notificationDao.deleteAllNotificationsByUserId(userId)
    
    // Status operations
    fun getAllStatuses() = statusDao.getAllStatuses()
    suspend fun getStatusById(statusId: Int) = statusDao.getStatusById(statusId)
    suspend fun getStatusByName(name: String) = statusDao.getStatusByName(name)
    suspend fun insertStatus(status: Status) = statusDao.insertStatus(status)
    
    // Person operations
    fun getAllActivePerson() = personDao.getAllActivePerson()
    suspend fun getPersonById(personId: Int) = personDao.getPersonById(personId)
    fun getPersonByIdFlow(personId: Int) = personDao.getPersonByIdFlow(personId)
    suspend fun getPersonByContactNumber(contactNumber: String) = personDao.getPersonByContactNumber(contactNumber)
    fun searchPerson(searchQuery: String) = personDao.searchPerson(searchQuery)
    fun getPersonByType(personType: String) = personDao.getPersonByType(personType)
    suspend fun insertPerson(person: Person) = personDao.insertPerson(person)
    suspend fun updatePerson(person: Person) = personDao.updatePerson(person)
    suspend fun deletePerson(person: Person) = personDao.deletePerson(person)
    suspend fun deactivatePerson(personId: Int) = personDao.deactivatePerson(personId)
    suspend fun getActivePersonCount() = personDao.getActivePersonCount()
    suspend fun findExistingPerson(firstName: String, lastName: String, contactNumber: String) = 
        personDao.findExistingPerson(firstName, lastName, contactNumber)
    
    // Respondent operations
    fun getRespondentsByReportId(reportId: Int) = respondentDao.getRespondentsByReportId(reportId)
    suspend fun getRespondentById(respondentId: Int) = respondentDao.getRespondentById(respondentId)
    fun getRespondentsByPersonId(personId: Int) = respondentDao.getRespondentsByPersonId(personId)
    fun getRespondentsByCooperationStatus(status: String) = respondentDao.getRespondentsByCooperationStatus(status)
    fun getRespondentsNeedingAttention() = respondentDao.getRespondentsNeedingAttention()
    suspend fun insertRespondent(respondent: Respondent) = respondentDao.insertRespondent(respondent)
    suspend fun updateRespondent(respondent: Respondent) = respondentDao.updateRespondent(respondent)
    suspend fun deleteRespondent(respondent: Respondent) = respondentDao.deleteRespondent(respondent)
    suspend fun updateCooperationStatus(respondentId: Int, status: String) = 
        respondentDao.updateCooperationStatus(respondentId, status)
    suspend fun markAsAppeared(respondentId: Int, date: Long) = respondentDao.markAsAppeared(respondentId, date)
    suspend fun recordStatement(respondentId: Int, statement: String, date: Long) = 
        respondentDao.recordStatement(respondentId, statement, date)
    suspend fun getCountByCooperationStatus(status: String) = respondentDao.getCountByCooperationStatus(status)
    
    // Person History operations
    fun getHistoryByPersonId(personId: Int) = personHistoryDao.getHistoryByPersonId(personId)
    fun getHistoryByReportId(reportId: Int) = personHistoryDao.getHistoryByReportId(reportId)
    fun getHistoryByActivityType(activityType: String) = personHistoryDao.getHistoryByActivityType(activityType)
    suspend fun insertHistory(history: PersonHistory) = personHistoryDao.insertHistory(history)
    suspend fun deleteHistoryByPersonId(personId: Int) = personHistoryDao.deleteHistoryByPersonId(personId)
    suspend fun getHistoryCountByPersonId(personId: Int) = personHistoryDao.getHistoryCountByPersonId(personId)
    suspend fun getHistoryCountByPersonIdAndType(personId: Int, activityType: String) = 
        personHistoryDao.getHistoryCountByPersonIdAndType(personId, activityType)
    
    // SMS Notification operations
    fun getNotificationsByRespondentId(respondentId: Int) = smsNotificationDao.getNotificationsByRespondentId(respondentId)
    fun getNotificationsByReportId(reportId: Int) = smsNotificationDao.getNotificationsByReportId(reportId)
    fun getNotificationsByStatus(status: String) = smsNotificationDao.getNotificationsByStatus(status)
    suspend fun insertSmsNotification(notification: SmsNotification) = smsNotificationDao.insertNotification(notification)
    suspend fun updateSmsNotification(notification: SmsNotification) = smsNotificationDao.updateNotification(notification)
    suspend fun updateDeliveryStatus(notificationId: Int, status: String) = 
        smsNotificationDao.updateDeliveryStatus(notificationId, status)
    suspend fun recordReply(notificationId: Int, reply: String, date: Long) = 
        smsNotificationDao.recordReply(notificationId, reply, date)
    suspend fun getFailedNotificationCount() = smsNotificationDao.getFailedNotificationCount()
    suspend fun deleteSmsNotification(notificationId: Int) = smsNotificationDao.deleteNotification(notificationId)
    
    // Respondent Statement operations
    fun getStatementsByRespondentId(respondentId: Int) = respondentStatementDao.getStatementsByRespondentId(respondentId)
    fun getStatementsByReportId(reportId: Int) = respondentStatementDao.getStatementsByReportId(reportId)
    fun getUnverifiedStatements() = respondentStatementDao.getUnverifiedStatements()
    suspend fun insertRespondentStatement(statement: RespondentStatement) = respondentStatementDao.insertStatement(statement)
    suspend fun updateRespondentStatement(statement: RespondentStatement) = respondentStatementDao.updateStatement(statement)
    suspend fun verifyStatement(statementId: Int, verifiedBy: String, date: Long) = 
        respondentStatementDao.verifyStatement(statementId, verifiedBy, date)
    suspend fun addOfficerNotes(statementId: Int, notes: String) = 
        respondentStatementDao.addOfficerNotes(statementId, notes)
    suspend fun updateStatus(status: Status) = statusDao.updateStatus(status)
    suspend fun deleteStatus(status: Status) = statusDao.deleteStatus(status)
    
    // Summons operations
    fun getSummonsByReportId(reportId: Int) = summonsDao.getSummonsByReportId(reportId)
    fun getSummonsByRespondentId(respondentId: Int) = summonsDao.getSummonsByRespondentId(respondentId)
    suspend fun getSummonsById(summonsId: Int) = summonsDao.getSummonsById(summonsId)
    fun getSummonsByStatus(status: String) = summonsDao.getSummonsByStatus(status)
    fun getPendingSummons() = summonsDao.getPendingSummons()
    fun getUncompliedSummons() = summonsDao.getUncompliedSummons()
    suspend fun insertSummons(summons: Summons) = summonsDao.insertSummons(summons)
    suspend fun updateSummons(summons: Summons) = summonsDao.updateSummons(summons)
    suspend fun deleteSummons(summons: Summons) = summonsDao.deleteSummons(summons)
    suspend fun updateDeliveryStatus(summonsId: Int, status: String, notes: String?, date: Long) = 
        summonsDao.updateDeliveryStatus(summonsId, status, notes, date)
    suspend fun markSummonsAsComplied(summonsId: Int, date: Long, notes: String?) = 
        summonsDao.markAsComplied(summonsId, date, notes)
    suspend fun getSummonsCountByReport(reportId: Int) = summonsDao.getSummonsCountByReport(reportId)
    suspend fun getPendingSummonsCount() = summonsDao.getPendingSummonsCount()
    
    // KP Form operations
    fun getKPFormsByReportId(reportId: Int) = kpFormDao.getKPFormsByReportId(reportId)
    suspend fun getKPFormById(formId: Int) = kpFormDao.getKPFormById(formId)
    fun getKPFormsByType(formType: String) = kpFormDao.getKPFormsByType(formType)
    fun getKPFormsByStatus(status: String) = kpFormDao.getKPFormsByStatus(status)
    suspend fun getLatestKPFormByType(reportId: Int, formType: String) = 
        kpFormDao.getLatestKPFormByType(reportId, formType)
    suspend fun insertKPForm(kpForm: KPForm) = kpFormDao.insertKPForm(kpForm)
    suspend fun updateKPForm(kpForm: KPForm) = kpFormDao.updateKPForm(kpForm)
    suspend fun deleteKPForm(kpForm: KPForm) = kpFormDao.deleteKPForm(kpForm)
    suspend fun updateFormStatus(formId: Int, status: String, date: Long) = 
        kpFormDao.updateFormStatus(formId, status, date)
    suspend fun updateDocumentPath(formId: Int, path: String) = 
        kpFormDao.updateDocumentPath(formId, path)
    suspend fun getKPFormsCountByReport(reportId: Int) = kpFormDao.getKPFormsCountByReport(reportId)
    suspend fun getIssuedFormsCountByType(formType: String) = kpFormDao.getIssuedFormsCountByType(formType)
    
    // Mediation Session operations
    fun getMediationSessionsByReportId(reportId: Int) = mediationSessionDao.getMediationSessionsByReportId(reportId)
    suspend fun getMediationSessionById(sessionId: Int) = mediationSessionDao.getMediationSessionById(sessionId)
    fun getMediationSessionsByOutcome(outcome: String) = mediationSessionDao.getMediationSessionsByOutcome(outcome)
    fun getUpcomingMediationSessions() = mediationSessionDao.getUpcomingMediationSessions()
    suspend fun getLatestMediationSession(reportId: Int) = mediationSessionDao.getLatestMediationSession(reportId)
    suspend fun insertMediationSession(session: MediationSession) = mediationSessionDao.insertMediationSession(session)
    suspend fun updateMediationSession(session: MediationSession) = mediationSessionDao.updateMediationSession(session)
    suspend fun deleteMediationSession(session: MediationSession) = mediationSessionDao.deleteMediationSession(session)
    suspend fun updateSessionOutcome(sessionId: Int, outcome: String, terms: String?) = 
        mediationSessionDao.updateSessionOutcome(sessionId, outcome, terms)
    suspend fun getMediationSessionCount(reportId: Int) = mediationSessionDao.getMediationSessionCount(reportId)
    suspend fun getSuccessfulMediationCount() = mediationSessionDao.getSuccessfulMediationCount()
    suspend fun getFailedMediationCount() = mediationSessionDao.getFailedMediationCount()
    
    // Case Timeline operations
    fun getTimelineByReportId(reportId: Int) = caseTimelineDao.getTimelineByReportId(reportId)
    suspend fun getTimelineByReportIdSync(reportId: Int) = caseTimelineDao.getTimelineByReportIdSync(reportId)
    suspend fun insertTimelineEvent(event: CaseTimeline) = caseTimelineDao.insertTimelineEvent(event)
    suspend fun insertTimelineEvents(events: List<CaseTimeline>) = caseTimelineDao.insertTimelineEvents(events)
    suspend fun deleteTimelineEvent(event: CaseTimeline) = caseTimelineDao.deleteTimelineEvent(event)
    suspend fun deleteTimelineByReportId(reportId: Int) = caseTimelineDao.deleteTimelineByReportId(reportId)
    suspend fun getTimelineEventCount(reportId: Int) = caseTimelineDao.getTimelineEventCount(reportId)
    
    // Case Template operations
    fun getAllActiveTemplates() = caseTemplateDao.getAllActiveTemplates()
    suspend fun getAllActiveTemplatesSync() = caseTemplateDao.getAllActiveTemplatesSync()
    suspend fun getTemplateById(templateId: Int) = caseTemplateDao.getTemplateById(templateId)
    suspend fun insertTemplate(template: CaseTemplate) = caseTemplateDao.insertTemplate(template)
    suspend fun updateTemplate(template: CaseTemplate) = caseTemplateDao.updateTemplate(template)
    suspend fun deleteTemplate(template: CaseTemplate) = caseTemplateDao.deleteTemplate(template)
    suspend fun incrementTemplateUsage(templateId: Int) = caseTemplateDao.incrementUsageCount(templateId)
}
