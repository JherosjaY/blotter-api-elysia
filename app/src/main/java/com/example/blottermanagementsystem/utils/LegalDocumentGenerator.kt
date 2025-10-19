package com.example.blottermanagementsystem.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.blottermanagementsystem.data.entity.KPForm
import com.example.blottermanagementsystem.data.entity.Summons
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class LegalDocumentGenerator(private val context: Context) {
    
    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 50
    
    /**
     * Generate KP Form PDF
     */
    fun generateKPFormPDF(
        kpForm: KPForm,
        complainantName: String,
        respondentName: String,
        caseNumber: String,
        barangayName: String = "Barangay Hall"
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var yPosition = (margin + 20).toFloat()
            
            // Header
            val headerPaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("REPUBLIC OF THE PHILIPPINES", pageWidth / 2f, yPosition, headerPaint)
            yPosition += 25f
            canvas.drawText(barangayName.uppercase(), pageWidth / 2f, yPosition, headerPaint)
            yPosition += 25f
            canvas.drawText("OFFICE OF THE LUPONG TAGAPAMAYAPA", pageWidth / 2f, yPosition, headerPaint)
            yPosition += 40f
            
            // Form Title
            val titlePaint = Paint().apply {
                textSize = 16f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(kpForm.formTitle.uppercase(), pageWidth / 2f, yPosition, titlePaint)
            yPosition += 20f
            canvas.drawText("(${kpForm.formType})", pageWidth / 2f, yPosition, titlePaint)
            yPosition += 40f
            
            // Body text
            val bodyPaint = Paint().apply {
                textSize = 12f
                textAlign = Paint.Align.LEFT
            }
            
            when (kpForm.formType) {
                "KP-7" -> generateNoticeOfHearing(canvas, bodyPaint, yPosition.toInt(), kpForm, complainantName, respondentName, caseNumber)
                "KP-10" -> generateSummonsForm(canvas, bodyPaint, yPosition.toInt(), kpForm, complainantName, respondentName, caseNumber)
                "KP-16" -> generateAmicableSettlement(canvas, bodyPaint, yPosition.toInt(), kpForm, complainantName, respondentName, caseNumber)
                "KP-18" -> generateCertificateToFileAction(canvas, bodyPaint, yPosition.toInt(), kpForm, complainantName, respondentName, caseNumber)
            }
            
            pdfDocument.finishPage(page)
            
            // Save to file
            val fileName = "${kpForm.formType}_${kpForm.formNumber}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Generate Summons PDF
     */
    fun generateSummonsPDF(
        summons: Summons,
        respondentName: String,
        respondentAddress: String,
        complainantName: String,
        caseNumber: String,
        barangayName: String = "Barangay Hall"
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var yPosition = (margin + 20).toFloat()
            
            // Header
            val headerPaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("REPUBLIC OF THE PHILIPPINES", pageWidth / 2f, yPosition, headerPaint)
            yPosition += 25f
            canvas.drawText(barangayName.uppercase(), pageWidth / 2f, yPosition, headerPaint)
            yPosition += 25f
            canvas.drawText("OFFICE OF THE LUPONG TAGAPAMAYAPA", pageWidth / 2f, yPosition, headerPaint)
            yPosition += 40f
            
            // Summons Title
            val titlePaint = Paint().apply {
                textSize = 16f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SUMMONS", pageWidth / 2f, yPosition, titlePaint)
            yPosition += 20f
            canvas.drawText("(${summons.summonsType})", pageWidth / 2f, yPosition, titlePaint)
            yPosition += 40f
            
            // Body
            val bodyPaint = Paint().apply {
                textSize = 12f
                textAlign = Paint.Align.LEFT
            }
            
            canvas.drawText("Summons No.: ${summons.summonsNumber}", margin.toFloat(), yPosition, bodyPaint)
            yPosition += 25f
            canvas.drawText("Case No.: $caseNumber", margin.toFloat(), yPosition, bodyPaint)
            yPosition += 25f
            canvas.drawText("Date Issued: ${formatDate(summons.issueDate)}", margin.toFloat(), yPosition, bodyPaint)
            yPosition += 40f
            
            canvas.drawText("TO: $respondentName", margin.toFloat(), yPosition, bodyPaint)
            yPosition += 20f
            canvas.drawText("     $respondentAddress", margin.toFloat(), yPosition, bodyPaint)
            yPosition += 40f
            
            // Main text
            val text = """
                You are hereby summoned to appear before the Lupong Tagapamayapa of this Barangay
                for the purpose of ${summons.purpose} in connection with the complaint filed against you
                by $complainantName.
                
                ${if (summons.hearingDate != null) {
                    "You are required to appear on ${formatDate(summons.hearingDate)} at ${summons.hearingTime}\nat ${summons.hearingVenue}."
                } else {
                    "You will be notified of the hearing schedule."
                }}
                
                Failure to appear without justifiable reason may result in the issuance of a
                Certificate to File Action, allowing the complainant to file the case in court.
            """.trimIndent()
            
            drawMultilineText(canvas, text, margin.toFloat(), yPosition, bodyPaint, pageWidth - 2 * margin)
            yPosition += 200f
            
            // Signature
            yPosition += 40f
            canvas.drawText("Issued by:", margin.toFloat(), yPosition, bodyPaint)
            yPosition += 40f
            canvas.drawText("_____________________________", margin.toFloat(), yPosition, bodyPaint)
            yPosition += 20f
            canvas.drawText(summons.issuedBy, margin.toFloat(), yPosition, bodyPaint)
            yPosition += 15f
            canvas.drawText(summons.issuedByPosition, margin.toFloat(), yPosition, bodyPaint)
            
            pdfDocument.finishPage(page)
            
            // Save
            val fileName = "SUMMONS_${summons.summonsNumber}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun generateNoticeOfHearing(
        canvas: android.graphics.Canvas,
        paint: Paint,
        startY: Int,
        kpForm: KPForm,
        complainant: String,
        respondent: String,
        caseNumber: String
    ) {
        var y = startY
        canvas.drawText("Case No.: $caseNumber", margin.toFloat(), y.toFloat(), paint)
        y += 25
        canvas.drawText("Complainant: $complainant", margin.toFloat(), y.toFloat(), paint)
        y += 20
        canvas.drawText("Respondent: $respondent", margin.toFloat(), y.toFloat(), paint)
        y += 40
        
        val text = """
            You are hereby notified that a hearing has been scheduled for the above-mentioned case.
            
            Date: ${kpForm.hearingDate?.let { formatDate(it) } ?: "TBA"}
            Time: ${kpForm.hearingTime ?: "TBA"}
            Venue: ${kpForm.hearingVenue ?: "Barangay Hall"}
            
            Your presence is required. Failure to appear may result in adverse consequences.
        """.trimIndent()
        
        drawMultilineText(canvas, text, margin.toFloat(), y.toFloat(), paint, pageWidth - 2 * margin)
    }
    
    private fun generateSummonsForm(
        canvas: android.graphics.Canvas,
        paint: Paint,
        startY: Int,
        kpForm: KPForm,
        complainant: String,
        respondent: String,
        caseNumber: String
    ) {
        var y = startY
        canvas.drawText("Case No.: $caseNumber", margin.toFloat(), y.toFloat(), paint)
        y += 25
        canvas.drawText("Complainant: $complainant", margin.toFloat(), y.toFloat(), paint)
        y += 20
        canvas.drawText("Respondent: $respondent", margin.toFloat(), y.toFloat(), paint)
        y += 40
        
        val text = """
            You are hereby summoned to appear before the Lupon for mediation/conciliation.
            
            This is in connection with the complaint filed against you. Your cooperation is
            expected in resolving this matter amicably.
        """.trimIndent()
        
        drawMultilineText(canvas, text, margin.toFloat(), y.toFloat(), paint, pageWidth - 2 * margin)
    }
    
    private fun generateAmicableSettlement(
        canvas: android.graphics.Canvas,
        paint: Paint,
        startY: Int,
        kpForm: KPForm,
        complainant: String,
        respondent: String,
        caseNumber: String
    ) {
        var y = startY
        canvas.drawText("Case No.: $caseNumber", margin.toFloat(), y.toFloat(), paint)
        y += 25
        canvas.drawText("Complainant: $complainant", margin.toFloat(), y.toFloat(), paint)
        y += 20
        canvas.drawText("Respondent: $respondent", margin.toFloat(), y.toFloat(), paint)
        y += 40
        
        val text = """
            AMICABLE SETTLEMENT
            
            The parties have agreed to settle this case amicably with the following terms:
            
            ${kpForm.settlementTerms ?: "Terms to be specified"}
            
            Both parties agree to abide by these terms and consider this matter resolved.
            
            Settled this ${kpForm.settlementDate?.let { formatDate(it) } ?: "date"}.
        """.trimIndent()
        
        drawMultilineText(canvas, text, margin.toFloat(), y.toFloat(), paint, pageWidth - 2 * margin)
    }
    
    private fun generateCertificateToFileAction(
        canvas: android.graphics.Canvas,
        paint: Paint,
        startY: Int,
        kpForm: KPForm,
        complainant: String,
        respondent: String,
        caseNumber: String
    ) {
        var y = startY
        canvas.drawText("Case No.: $caseNumber", margin.toFloat(), y.toFloat(), paint)
        y += 25
        canvas.drawText("Complainant: $complainant", margin.toFloat(), y.toFloat(), paint)
        y += 20
        canvas.drawText("Respondent: $respondent", margin.toFloat(), y.toFloat(), paint)
        y += 40
        
        val text = """
            CERTIFICATE TO FILE ACTION
            
            This is to certify that the case between the above-named parties has been brought
            before the Lupong Tagapamayapa for conciliation/mediation.
            
            Despite ${kpForm.attemptsMade} attempt(s) at settlement, the parties failed to reach
            an amicable agreement.
            
            Reason: ${kpForm.certificationReason ?: "Parties could not agree on terms"}
            
            This certification is issued to enable the complainant to file the appropriate action
            in court or with the proper government office.
            
            Issued this ${formatDate(kpForm.issueDate)}.
        """.trimIndent()
        
        drawMultilineText(canvas, text, margin.toFloat(), y.toFloat(), paint, pageWidth - 2 * margin)
    }
    
    private fun drawMultilineText(
        canvas: android.graphics.Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Int
    ) {
        var currentY = y
        val lines = text.split("\n")
        
        for (line in lines) {
            if (line.isBlank()) {
                currentY += 15
                continue
            }
            
            val words = line.split(" ")
            var currentLine = ""
            
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val width = paint.measureText(testLine)
                
                if (width > maxWidth && currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, x, currentY, paint)
                    currentY += 18
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, x, currentY, paint)
                currentY += 18
            }
        }
    }
    
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
