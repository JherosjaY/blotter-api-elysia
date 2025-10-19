package com.example.blottermanagementsystem.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.blottermanagementsystem.data.entity.BlotterReport
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {
    
    private val pageWidth = 595f
    private val pageHeight = 842f
    private val margin = 40f
    private val contentWidth = pageWidth - (margin * 2)
    
    fun exportReportToPdf(report: BlotterReport): File? {
        return try {
            val pdfDocument = PdfDocument()
            var pageNumber = 1
            
            // Start first page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            var yPos = margin
            
            val paint = Paint()
            val lineHeight = 18f
            
            // Centered header
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("BLOTTER REPORT", pageWidth / 2, yPos, paint)
            yPos += 10f
            
            // Decorative line
            paint.strokeWidth = 2f
            paint.color = Color.parseColor("#1E88E5")
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)
            yPos += 25f
            
            // Reset paint for content
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.BLACK
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            
            // Report Details
            canvas.drawText("Case Number: ${report.caseNumber}", 50f, yPos, paint)
            yPos += lineHeight
            
            canvas.drawText("Status: ${report.status}", 50f, yPos, paint)
            yPos += lineHeight
            
            canvas.drawText("Date Filed: ${formatDate(report.dateFiled)}", 50f, yPos, paint)
            yPos += lineHeight * 2
            
            // Complainant Section - Table format
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.parseColor("#1E88E5")
            canvas.drawText("COMPLAINANT INFORMATION", margin, yPos, paint)
            yPos += lineHeight
            
            // Draw table
            paint.color = Color.LTGRAY
            paint.style = Paint.Style.STROKE
            val tableWidth = contentWidth
            val rowHeight = 18f
            val labelWidth = tableWidth * 0.3f
            
            // Table rows
            val complainantData = listOf(
                "Name" to report.complainantName,
                "Contact" to report.complainantContact,
                "Address" to report.complainantAddress
            )
            
            complainantData.forEachIndexed { index, (label, value) ->
                val rowY = yPos + (index * rowHeight)
                // Row border
                canvas.drawRect(margin, rowY, pageWidth - margin, rowY + rowHeight, paint)
                // Vertical separator
                canvas.drawLine(margin + labelWidth, rowY, margin + labelWidth, rowY + rowHeight, paint)
                
                // Label
                paint.style = Paint.Style.FILL
                paint.color = Color.DKGRAY
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(label, margin + 5f, rowY + 13f, paint)
                
                // Value
                paint.color = Color.BLACK
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(value, margin + labelWidth + 5f, rowY + 13f, paint)
                
                paint.style = Paint.Style.STROKE
                paint.color = Color.LTGRAY
            }
            
            yPos += complainantData.size * rowHeight + lineHeight
            
            // Incident Section - Table format
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.parseColor("#1E88E5")
            paint.style = Paint.Style.FILL
            canvas.drawText("INCIDENT DETAILS", margin, yPos, paint)
            yPos += lineHeight
            
            // Incident table
            val incidentData = listOf(
                "Type" to report.incidentType,
                "Date" to formatDate(report.incidentDate),
                "Time" to report.incidentTime,
                "Location" to report.incidentLocation
            )
            
            paint.color = Color.LTGRAY
            paint.style = Paint.Style.STROKE
            
            incidentData.forEachIndexed { index, (label, value) ->
                val rowY = yPos + (index * rowHeight)
                canvas.drawRect(margin, rowY, pageWidth - margin, rowY + rowHeight, paint)
                canvas.drawLine(margin + labelWidth, rowY, margin + labelWidth, rowY + rowHeight, paint)
                
                paint.style = Paint.Style.FILL
                paint.color = Color.DKGRAY
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(label, margin + 5f, rowY + 13f, paint)
                
                paint.color = Color.BLACK
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(value, margin + labelWidth + 5f, rowY + 13f, paint)
                
                paint.style = Paint.Style.STROKE
                paint.color = Color.LTGRAY
            }
            
            yPos += incidentData.size * rowHeight + lineHeight
            
            // Narrative Section
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.parseColor("#1E88E5")
            paint.style = Paint.Style.FILL
            canvas.drawText("NARRATIVE", margin, yPos, paint)
            yPos += lineHeight
            
            // Narrative box with border
            paint.color = Color.LTGRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            
            val narrativeStartY = yPos
            val maxWidth = contentWidth - 10f
            val words = report.narrative.split(" ")
            var line = ""
            var narrativeLines = 0
            
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            
            // Calculate narrative height
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth) {
                    narrativeLines++
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) narrativeLines++
            
            val narrativeHeight = (narrativeLines * lineHeight) + 10f
            canvas.drawRect(margin, narrativeStartY, pageWidth - margin, narrativeStartY + narrativeHeight, paint)
            
            // Draw narrative text
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            yPos += 15f
            line = ""
            
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth) {
                    canvas.drawText(line, margin + 5f, yPos, paint)
                    yPos += lineHeight
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, margin + 5f, yPos, paint)
            }
            
            yPos = narrativeStartY + narrativeHeight + lineHeight
            
            // Respondent Section - Table format
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.parseColor("#1E88E5")
            paint.style = Paint.Style.FILL
            canvas.drawText("RESPONDENT INFORMATION", margin, yPos, paint)
            yPos += lineHeight
            
            // Respondent table
            val respondentData = listOf(
                "Name" to report.respondentName,
                "Address" to report.respondentAddress
            )
            
            paint.color = Color.LTGRAY
            paint.style = Paint.Style.STROKE
            
            respondentData.forEachIndexed { index, (label, value) ->
                val rowY = yPos + (index * rowHeight)
                canvas.drawRect(margin, rowY, pageWidth - margin, rowY + rowHeight, paint)
                canvas.drawLine(margin + labelWidth, rowY, margin + labelWidth, rowY + rowHeight, paint)
                
                paint.style = Paint.Style.FILL
                paint.color = Color.DKGRAY
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(label, margin + 5f, rowY + 13f, paint)
                
                paint.color = Color.BLACK
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(value, margin + labelWidth + 5f, rowY + 13f, paint)
                
                paint.style = Paint.Style.STROKE
                paint.color = Color.LTGRAY
            }
            
            yPos += respondentData.size * rowHeight + lineHeight
            
            // Footer - centered
            paint.textSize = 9f
            paint.color = Color.GRAY
            paint.textAlign = Paint.Align.CENTER
            paint.style = Paint.Style.FILL
            val footerText = "Generated on ${SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date())}"
            canvas.drawText(footerText, pageWidth / 2, pageHeight - 20f, paint)
            
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
    
    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
