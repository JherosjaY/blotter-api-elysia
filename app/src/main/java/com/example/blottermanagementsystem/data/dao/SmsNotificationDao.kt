package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.SmsNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsNotificationDao {
    @Query("SELECT * FROM sms_notifications WHERE respondentId = :respondentId ORDER BY sentDate DESC")
    fun getNotificationsByRespondentId(respondentId: Int): Flow<List<SmsNotification>>
    
    @Query("SELECT * FROM sms_notifications WHERE blotterReportId = :reportId ORDER BY sentDate DESC")
    fun getNotificationsByReportId(reportId: Int): Flow<List<SmsNotification>>
    
    @Query("SELECT * FROM sms_notifications WHERE deliveryStatus = :status ORDER BY sentDate DESC")
    fun getNotificationsByStatus(status: String): Flow<List<SmsNotification>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SmsNotification): Long
    
    @Update
    suspend fun updateNotification(notification: SmsNotification)
    
    @Query("UPDATE sms_notifications SET deliveryStatus = :status WHERE id = :notificationId")
    suspend fun updateDeliveryStatus(notificationId: Int, status: String)
    
    @Query("UPDATE sms_notifications SET respondentReply = :reply, replyDate = :date WHERE id = :notificationId")
    suspend fun recordReply(notificationId: Int, reply: String, date: Long)
    
    @Query("SELECT COUNT(*) FROM sms_notifications WHERE deliveryStatus = 'Failed'")
    suspend fun getFailedNotificationCount(): Int
}
