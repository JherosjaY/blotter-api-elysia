package com.example.blottermanagementsystem

import android.app.Application
import com.example.blottermanagementsystem.utils.CloudinaryUploader

class BlotterApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Cloudinary
        CloudinaryUploader.initialize(this)
    }
}
