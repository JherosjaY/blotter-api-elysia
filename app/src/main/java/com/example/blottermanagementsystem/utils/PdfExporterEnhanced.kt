package com.example.blottermanagementsystem.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import com.example.blottermanagementsystem.data.entity.BlotterReport
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporterEnhanced(private val context: Context) {
    
    private val pageWidth = 595f
    private val pageHeight = 842f
    private val margin = 40f
    private val contentWidth = pageWidth - (margin * 2)
    
    fun exportReportToPdf(
        report: BlotterReport,
        imageUris: List<Uri> = emptyList(),
        videoUris: List<Uri> = emptyList(),
        videoDurations: Map<Uri, Long> = emptyMap()
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            var pageNumber = 1
            
            // Start first page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            var yPos = margin
            
            // Header with centered title
            yPos = drawHeader(canvas, yPos)
            yPos += 20f
            
            // Case information
            yPos = drawInfoBox(canvas, yPos, "CASE INFORMATION", listOf(
                "Case Number" to report.caseNumber,
                "Status" to report.status,
                "Date Filed" to formatDate(report.dateFiled)
            ))
            yPos += 15f
            
            // Complainant information
            yPos = drawInfoBox(canvas, yPos, "COMPLAINANT INFORMATION", listOf(
                "Name" to report.complainantName,
                "Contact" to report.complainantContact,
                "Address" to report.complainantAddress
            ))
            yPos += 15f
            
            // Incident details
            yPos = drawInfoBox(canvas, yPos, "INCIDENT DETAILS", listOf(
                "Type" to report.incidentType,
                "Date" to formatDate(report.incidentDate),
                "Time" to report.incidentTime,
                "Location" to report.incidentLocation
            ))
            yPos += 15f
            
            // Narrative
            yPos = drawNarrative(canvas, yPos, report.narrative)
            yPos += 15f
            
            // Respondent information
            yPos = drawInfoBox(canvas, yPos, "RESPONDENT INFORMATION", listOf(
                "Name" to report.respondentName,
                "Address" to report.respondentAddress
            ))
            yPos += 20f
            
            // Evidence section
            if (imageUris.isNotEmpty() || videoUris.isNotEmpty()) {
                // Check if we need a new page
                if (yPos > pageHeight - 250f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    yPos = margin
                }
                
                yPos = drawEvidenceSection(canvas, yPos, imageUris, videoUris, videoDurations)
            }
            
            // Footer
            drawFooter(canvas)
            
            pdfDocument.finishPage(page)
            
            // Save PDF
            val fileName = "Report_${report.caseNumber}_${System.currentTimeMillis()}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun drawHeader(canvas: android.graphics.Canvas, startY: Float): Float {
        var y = startY
        val paint = Paint()
        
        // Title - centered and bold
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        
        canvas.drawText("BLOTTER REPORT", pageWidth / 2, y, paint)
        y += 10f
        
        // Decorative line under title
        paint.strokeWidth = 2f
        paint.color = Color.parseColor("#1E88E5") // Blue color
        canvas.drawLine(margin, y, pageWidth - margin, y, paint)
        y += 15f
        
        return y
    }
    
    private fun drawInfoBox(canvas: android.graphics.Canvas, startY: Float, title: String, data: List<Pair<String, String>>): Float {
        var y = startY
        val paint = Paint()
        
        // Section title
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#1E88E5")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(title, margin, y, paint)
        y += 15f
        
        // Draw table
        val rowHeight = 20f
        val labelWidth = contentWidth * 0.35f
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        
        // Draw table border
        val tableRect = RectF(margin, y, pageWidth - margin, y + (data.size * rowHeight))
        canvas.drawRect(tableRect, paint)
        
        // Draw rows
        paint.textSize = 10f
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.DEFAULT
        
        data.forEachIndexed { index, (label, value) ->
            val rowY = y + (index * rowHeight)
            
            // Draw row separator
            if (index > 0) {
                paint.color = Color.LTGRAY
                canvas.drawLine(margin, rowY, pageWidth - margin, rowY, paint)
            }
            
            // Draw vertical separator
            canvas.drawLine(margin + labelWidth, rowY, margin + labelWidth, rowY + rowHeight, paint)
            
            // Label (bold)
            paint.color = Color.DKGRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(label, margin + 5f, rowY + 14f, paint)
            
            // Value
            paint.color = Color.BLACK
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(value, margin + labelWidth + 5f, rowY + 14f, paint)
        }
        
        y += data.size * rowHeight + 5f
        return y
    }
    
    private fun drawNarrative(canvas: android.graphics.Canvas, startY: Float, narrative: String): Float {
        var y = startY
        val paint = Paint()
        
        // Section title
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#1E88E5")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("NARRATIVE", margin, y, paint)
        y += 15f
        
        // Narrative box
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.BLACK
        
        val maxWidth = contentWidth - 10f
        val lineHeight = 15f
        val words = narrative.split(" ")
        var line = ""
        
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(line, margin + 5f, y, paint)
                y += lineHeight
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, margin + 5f, y, paint)
            y += lineHeight
        }
        
        return y
    }
    
    private fun drawEvidenceSection(
        canvas: android.graphics.Canvas,
        startY: Float,
        imageUris: List<Uri>,
        videoUris: List<Uri>,
        videoDurations: Map<Uri, Long>
    ): Float {
        var y = startY
        val paint = Paint()
        
        // Section title
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#1E88E5")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("EVIDENCE", margin, y, paint)
        y += 15f
        
        val thumbnailSize = 80f
        val spacing = 10f
        var xPos = margin
        
        // Draw images
        if (imageUris.isNotEmpty()) {
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.BLACK
            canvas.drawText("Photos (${imageUris.size}):", margin, y, paint)
            y += 15f
            
            imageUris.forEachIndexed { index, uri ->
                if (xPos + thumbnailSize > pageWidth - margin) {
                    xPos = margin
                    y += thumbnailSize + spacing + 15f
                }
                
                try {
                    val bitmap = loadBitmapFromUri(uri, thumbnailSize.toInt())
                    bitmap?.let {
                        val destRect = RectF(xPos, y, xPos + thumbnailSize, y + thumbnailSize)
                        canvas.drawBitmap(it, null, destRect, paint)
                        
                        // Border
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 1f
                        paint.color = Color.LTGRAY
                        canvas.drawRect(destRect, paint)
                        paint.style = Paint.Style.FILL
                        paint.color = Color.BLACK
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                xPos += thumbnailSize + spacing
            }
            
            y += thumbnailSize + spacing + 10f
            xPos = margin
        }
        
        // Draw videos
        if (videoUris.isNotEmpty()) {
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.BLACK
            canvas.drawText("Videos (${videoUris.size}):", margin, y, paint)
            y += 15f
            
            videoUris.forEach { uri ->
                if (xPos + thumbnailSize > pageWidth - margin) {
                    xPos = margin
                    y += thumbnailSize + spacing + 15f
                }
                
                try {
                    val thumbnail = getVideoThumbnail(uri)
                    thumbnail?.let {
                        val destRect = RectF(xPos, y, xPos + thumbnailSize, y + thumbnailSize)
                        canvas.drawBitmap(it, null, destRect, paint)
                        
                        // Border
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 1f
                        paint.color = Color.LTGRAY
                        canvas.drawRect(destRect, paint)
                        paint.style = Paint.Style.FILL
                        
                        // Play icon overlay
                        paint.color = Color.argb(180, 0, 0, 0)
                        val playIconRect = RectF(
                            xPos + thumbnailSize/2 - 15f,
                            y + thumbnailSize/2 - 15f,
                            xPos + thumbnailSize/2 + 15f,
                            y + thumbnailSize/2 + 15f
                        )
                        canvas.drawCircle(xPos + thumbnailSize/2, y + thumbnailSize/2, 15f, paint)
                        
                        // Play triangle
                        paint.color = Color.WHITE
                        val path = Path()
                        path.moveTo(xPos + thumbnailSize/2 - 5f, y + thumbnailSize/2 - 8f)
                        path.lineTo(xPos + thumbnailSize/2 - 5f, y + thumbnailSize/2 + 8f)
                        path.lineTo(xPos + thumbnailSize/2 + 8f, y + thumbnailSize/2)
                        path.close()
                        canvas.drawPath(path, paint)
                        
                        // Duration label
                        videoDurations[uri]?.let { duration ->
                            val seconds = (duration / 1000) % 60
                            val minutes = (duration / 1000) / 60
                            val durationText = String.format("%d:%02d", minutes, seconds)
                            
                            paint.color = Color.argb(200, 0, 0, 0)
                            val textBounds = Rect()
                            paint.textSize = 8f
                            paint.getTextBounds(durationText, 0, durationText.length, textBounds)
                            val textWidth = textBounds.width()
                            
                            canvas.drawRect(
                                xPos + thumbnailSize - textWidth - 8f,
                                y + thumbnailSize - 15f,
                                xPos + thumbnailSize - 2f,
                                y + thumbnailSize - 2f,
                                paint
                            )
                            
                            paint.color = Color.WHITE
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            canvas.drawText(durationText, xPos + thumbnailSize - textWidth - 5f, y + thumbnailSize - 5f, paint)
                        }
                        
                        paint.color = Color.BLACK
                        paint.typeface = Typeface.DEFAULT
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                xPos += thumbnailSize + spacing
            }
            
            y += thumbnailSize + spacing
        }
        
        return y
    }
    
    private fun drawFooter(canvas: android.graphics.Canvas) {
        val paint = Paint()
        paint.textSize = 9f
        paint.color = Color.GRAY
        paint.textAlign = Paint.Align.CENTER
        
        val footerText = "Generated on ${SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date())}"
        canvas.drawText(footerText, pageWidth / 2, pageHeight - 20f, paint)
    }
    
    private fun loadBitmapFromUri(uri: Uri, targetSize: Int): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false
            
            val inputStream2 = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
            inputStream2?.close()
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun getVideoThumbnail(uri: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
