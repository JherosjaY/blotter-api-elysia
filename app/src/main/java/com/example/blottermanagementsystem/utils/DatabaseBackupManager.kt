package com.example.blottermanagementsystem.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DatabaseBackupManager(private val context: Context) {
    
    private val databaseName = "blotter_database"
    private val backupDirectory = "BlotterBackups"
    
    /**
     * Create a backup of the database
     */
    suspend fun createBackup(): File? = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(databaseName)
            
            if (!dbFile.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Database not found", Toast.LENGTH_SHORT).show()
                }
                return@withContext null
            }
            
            // Create backup directory
            val backupDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                backupDirectory
            )
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            // Create backup filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "blotter_backup_$timestamp.zip"
            val backupFile = File(backupDir, backupFileName)
            
            // Create ZIP file
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
                // Add database file
                FileInputStream(dbFile).use { fis ->
                    val zipEntry = ZipEntry(databaseName)
                    zipOut.putNextEntry(zipEntry)
                    
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (fis.read(buffer).also { length = it } > 0) {
                        zipOut.write(buffer, 0, length)
                    }
                    zipOut.closeEntry()
                }
                
                // Add database WAL file if exists
                val walFile = File(dbFile.parent, "$databaseName-wal")
                if (walFile.exists()) {
                    FileInputStream(walFile).use { fis ->
                        val zipEntry = ZipEntry("$databaseName-wal")
                        zipOut.putNextEntry(zipEntry)
                        
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (fis.read(buffer).also { length = it } > 0) {
                            zipOut.write(buffer, 0, length)
                        }
                        zipOut.closeEntry()
                    }
                }
                
                // Add database SHM file if exists
                val shmFile = File(dbFile.parent, "$databaseName-shm")
                if (shmFile.exists()) {
                    FileInputStream(shmFile).use { fis ->
                        val zipEntry = ZipEntry("$databaseName-shm")
                        zipOut.putNextEntry(zipEntry)
                        
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (fis.read(buffer).also { length = it } > 0) {
                            zipOut.write(buffer, 0, length)
                        }
                        zipOut.closeEntry()
                    }
                }
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Backup created: $backupFileName",
                    Toast.LENGTH_LONG
                ).show()
            }
            
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Backup failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            null
        }
    }
    
    /**
     * Restore database from backup
     */
    suspend fun restoreBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(databaseName)
            
            // Close database connections first
            context.deleteDatabase(databaseName)
            
            // Extract ZIP file
            ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                
                while (entry != null) {
                    val filePath = File(dbFile.parent, entry.name)
                    
                    if (!entry.isDirectory) {
                        // Extract file
                        FileOutputStream(filePath).use { fos ->
                            val buffer = ByteArray(1024)
                            var length: Int
                            while (zipIn.read(buffer).also { length = it } > 0) {
                                fos.write(buffer, 0, length)
                            }
                        }
                    }
                    
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Database restored successfully. Please restart the app.",
                    Toast.LENGTH_LONG
                ).show()
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Restore failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
    }
    
    /**
     * Get list of available backups
     */
    fun getAvailableBackups(): List<File> {
        val backupDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            backupDirectory
        )
        
        if (!backupDir.exists()) {
            return emptyList()
        }
        
        return backupDir.listFiles { file ->
            file.isFile && file.extension == "zip" && file.name.startsWith("blotter_backup_")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * Delete old backups (keep only last N backups)
     */
    suspend fun cleanOldBackups(keepCount: Int = 5): Int = withContext(Dispatchers.IO) {
        val backups = getAvailableBackups()
        var deletedCount = 0
        
        if (backups.size > keepCount) {
            backups.drop(keepCount).forEach { file ->
                if (file.delete()) {
                    deletedCount++
                }
            }
        }
        
        deletedCount
    }
    
    /**
     * Get backup file size in MB
     */
    fun getBackupSize(file: File): String {
        val sizeInMB = file.length() / (1024.0 * 1024.0)
        return String.format("%.2f MB", sizeInMB)
    }
    
    /**
     * Export database to external storage for sharing
     */
    suspend fun exportToExternalStorage(): File? = withContext(Dispatchers.IO) {
        try {
            val backupFile = createBackup()
            
            if (backupFile != null) {
                // Copy to Downloads folder for easy access
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val externalFile = File(downloadsDir, backupFile.name)
                
                FileInputStream(backupFile).use { input ->
                    FileOutputStream(externalFile).use { output ->
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (input.read(buffer).also { length = it } > 0) {
                            output.write(buffer, 0, length)
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Backup exported to Downloads folder",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                externalFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Export failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            null
        }
    }
}
