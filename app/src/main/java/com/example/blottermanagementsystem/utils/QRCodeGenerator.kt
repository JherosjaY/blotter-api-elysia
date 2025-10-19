package com.example.blottermanagementsystem.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * QR Code Generator for Blotter Reports
 * Generates QR codes containing report IDs for quick access
 */
object QRCodeGenerator {
    
    /**
     * Generate QR code bitmap for a report ID
     * @param reportId The report ID to encode
     * @param size Size of the QR code in pixels (default 512)
     * @return Bitmap of the QR code
     */
    fun generateReportQRCode(reportId: Int, size: Int = 512): Bitmap? {
        return try {
            // Create QR code content with prefix for validation
            val content = "BLOTTER_REPORT:$reportId"
            
            // Configure QR code hints
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H) // High error correction
                put(EncodeHintType.MARGIN, 1) // Minimal margin
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }
            
            // Generate QR code matrix
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            // Convert to bitmap
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Parse QR code content to extract report ID
     * @param content The scanned QR code content
     * @return Report ID if valid, null otherwise
     */
    fun parseReportQRCode(content: String): Int? {
        return try {
            if (content.startsWith("BLOTTER_REPORT:")) {
                val reportId = content.removePrefix("BLOTTER_REPORT:").toIntOrNull()
                reportId
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Validate if QR code content is a valid blotter report QR
     * @param content The scanned QR code content
     * @return True if valid blotter report QR code
     */
    fun isValidReportQRCode(content: String): Boolean {
        return content.startsWith("BLOTTER_REPORT:") && parseReportQRCode(content) != null
    }
}
