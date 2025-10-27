package com.example.blottermanagementsystem.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Audio Recorder Utility
 * Records and plays audio files for blotter reports
 */
class AudioRecorder(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioFile: File? = null
    private var _isRecording = false
    private var _isPlaying = false
    
    /**
     * Start recording audio
     * @return File path of the recording, or null if failed
     */
    fun startRecording(): String? {
        return try {
            // Create audio file
            val audioFile = createAudioFile()
            currentAudioFile = audioFile
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile.absolutePath)
                
                try {
                    prepare()
                    start()
                    _isRecording = true
                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                }
            }
            
            audioFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Stop recording audio
     * @return File path of the recorded audio, or null if failed
     */
    fun stopRecording(): String? {
        return try {
            if (_isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                _isRecording = false
                currentAudioFile?.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Play audio file
     * @param filePath Path to the audio file
     * @param onCompletion Callback when playback completes
     */
    fun playAudio(
        filePath: String,
        onCompletion: () -> Unit = {}
    ): Boolean {
        return try {
            stopPlaying() // Stop any current playback
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    _isPlaying = false
                    onCompletion()
                }
                start()
                _isPlaying = true
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Stop playing audio
     */
    fun stopPlaying() {
        try {
            if (_isPlaying) {
                mediaPlayer?.apply {
                    stop()
                    release()
                }
                mediaPlayer = null
                _isPlaying = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Pause audio playback
     */
    fun pausePlaying() {
        try {
            if (_isPlaying) {
                mediaPlayer?.pause()
                _isPlaying = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Resume audio playback
     */
    fun resumePlaying() {
        try {
            if (!_isPlaying && mediaPlayer != null) {
                mediaPlayer?.start()
                _isPlaying = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Get current playback position in milliseconds
     */
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Get total duration of audio in milliseconds
     */
    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Seek to position in milliseconds
     */
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = _isRecording
    
    /**
     * Check if currently playing
     */
    fun isPlaying(): Boolean = _isPlaying
    
    /**
     * Delete audio file
     */
    fun deleteAudio(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get audio file size in bytes
     */
    fun getFileSize(filePath: String): Long {
        return try {
            File(filePath).length()
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Format file size to human-readable string
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
    
    /**
     * Format duration to MM:SS
     */
    fun formatDuration(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    
    /**
     * Create audio file in app's internal storage
     */
    private fun createAudioFile(): File {
        val audioDir = File(context.filesDir, "audio_recordings")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "AUDIO_$timestamp.m4a"
        
        return File(audioDir, fileName)
    }
    
    /**
     * Get all audio recordings
     */
    fun getAllRecordings(): List<File> {
        val audioDir = File(context.filesDir, "audio_recordings")
        return if (audioDir.exists()) {
            audioDir.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * Release all resources
     * Call this when done using the recorder
     */
    fun release() {
        try {
            stopRecording()
            stopPlaying()
            mediaRecorder?.release()
            mediaPlayer?.release()
            mediaRecorder = null
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
