package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val gender: String? = null,
    val birthDate: Long? = null,
    val address: String? = null,
    val contactNumber: String? = null,
    val email: String? = null,
    val photoUri: String? = null,
    val personType: String, // "User", "Complainant", "Respondent", "Witness", "Officer", "Multiple"
    val dateAdded: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val notes: String? = null
)
