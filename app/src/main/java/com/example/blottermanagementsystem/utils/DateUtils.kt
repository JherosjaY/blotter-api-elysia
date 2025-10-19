package com.example.blottermanagementsystem.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
