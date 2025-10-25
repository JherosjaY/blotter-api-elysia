package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "case_templates")
data class CaseTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val templateName: String,
    val incidentType: String,
    val descriptionTemplate: String,
    val commonQuestions: String, // JSON array of questions
    val isActive: Boolean = true,
    val usageCount: Int = 0,
    val createdBy: String,
    val createdDate: Long = System.currentTimeMillis()
)
