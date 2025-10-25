package com.example.blottermanagementsystem.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.data.entity.Officer
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Export all cases to Excel
     */
    fun exportCasesToExcel(
        context: Context,
        reports: List<BlotterReport>,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Blotter Reports")
            
            // Create header style
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.LIGHT_BLUE.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                val font = workbook.createFont().apply {
                    bold = true
                    color = IndexedColors.WHITE.index
                }
                setFont(font)
                borderBottom = BorderStyle.THIN
                borderTop = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
            }
            
            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = listOf(
                "Case Number",
                "Incident Type",
                "Date Filed",
                "Incident Date",
                "Location",
                "Complainant",
                "Status",
                "Priority",
                "Assigned Officer",
                "Description"
            )
            
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).apply {
                    setCellValue(header)
                    setCellStyle(headerStyle)
                }
            }
            
            // Add data rows
            reports.forEachIndexed { index, report ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(report.caseNumber)
                row.createCell(1).setCellValue(report.incidentType)
                row.createCell(2).setCellValue(dateFormat.format(Date(report.dateFiled)))
                row.createCell(3).setCellValue(dateFormat.format(Date(report.incidentDate)))
                row.createCell(4).setCellValue(report.incidentLocation)
                row.createCell(5).setCellValue(report.complainantName)
                row.createCell(6).setCellValue(report.status)
                row.createCell(7).setCellValue("Normal")
                row.createCell(8).setCellValue(report.assignedOfficer ?: "Unassigned")
                row.createCell(9).setCellValue(report.narrative)
            }
            
            // Set column widths manually (autoSizeColumn doesn't work on Android)
            headers.indices.forEach { sheet.setColumnWidth(it, 4000) }
            
            // Save to file
            val fileName = "BlotterReports_${System.currentTimeMillis()}.xlsx"
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            
            // Get URI for sharing
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            onSuccess(uri)
            
        } catch (e: Exception) {
            onError("Failed to export: ${e.message}")
        }
    }
    
    /**
     * Export statistics to Excel
     */
    fun exportStatisticsToExcel(
        context: Context,
        totalCases: Int,
        activeCases: Int,
        resolvedCases: Int,
        pendingCases: Int,
        casesByType: Map<String, Int>,
        casesByStatus: Map<String, Int>,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val workbook = XSSFWorkbook()
            
            // Sheet 1: Overview
            val overviewSheet = workbook.createSheet("Overview")
            val headerStyle = createHeaderStyle(workbook)
            
            var rowNum = 0
            overviewSheet.createRow(rowNum++).apply {
                createCell(0).apply {
                    setCellValue("Statistic")
                    setCellStyle(headerStyle)
                }
                createCell(1).apply {
                    setCellValue("Count")
                    setCellStyle(headerStyle)
                }
            }
            
            overviewSheet.createRow(rowNum++).apply {
                createCell(0).setCellValue("Total Cases")
                createCell(1).setCellValue(totalCases.toDouble())
            }
            overviewSheet.createRow(rowNum++).apply {
                createCell(0).setCellValue("Active Cases")
                createCell(1).setCellValue(activeCases.toDouble())
            }
            overviewSheet.createRow(rowNum++).apply {
                createCell(0).setCellValue("Resolved Cases")
                createCell(1).setCellValue(resolvedCases.toDouble())
            }
            overviewSheet.createRow(rowNum++).apply {
                createCell(0).setCellValue("Pending Cases")
                createCell(1).setCellValue(pendingCases.toDouble())
            }
            
            // Set column widths manually
            overviewSheet.setColumnWidth(0, 6000)
            overviewSheet.setColumnWidth(1, 4000)
            
            // Sheet 2: Cases by Type
            val typeSheet = workbook.createSheet("Cases by Type")
            rowNum = 0
            typeSheet.createRow(rowNum++).apply {
                createCell(0).apply {
                    setCellValue("Incident Type")
                    setCellStyle(headerStyle)
                }
                createCell(1).apply {
                    setCellValue("Count")
                    setCellStyle(headerStyle)
                }
            }
            
            casesByType.forEach { (type, count) ->
                typeSheet.createRow(rowNum++).apply {
                    createCell(0).setCellValue(type)
                    createCell(1).setCellValue(count.toDouble())
                }
            }
            
            // Set column widths manually
            typeSheet.setColumnWidth(0, 6000)
            typeSheet.setColumnWidth(1, 4000)
            
            // Sheet 3: Cases by Status
            val statusSheet = workbook.createSheet("Cases by Status")
            rowNum = 0
            statusSheet.createRow(rowNum++).apply {
                createCell(0).apply {
                    setCellValue("Status")
                    setCellStyle(headerStyle)
                }
                createCell(1).apply {
                    setCellValue("Count")
                    setCellStyle(headerStyle)
                }
            }
            
            casesByStatus.forEach { (status, count) ->
                statusSheet.createRow(rowNum++).apply {
                    createCell(0).setCellValue(status)
                    createCell(1).setCellValue(count.toDouble())
                }
            }
            
            // Set column widths manually
            statusSheet.setColumnWidth(0, 6000)
            statusSheet.setColumnWidth(1, 4000)
            
            // Save to file
            val fileName = "Statistics_${System.currentTimeMillis()}.xlsx"
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            onSuccess(uri)
            
        } catch (e: Exception) {
            onError("Failed to export statistics: ${e.message}")
        }
    }
    
    /**
     * Export officer performance to Excel
     */
    fun exportOfficerPerformanceToExcel(
        context: Context,
        officers: List<Officer>,
        officerCaseCounts: Map<Int, Int>,
        officerResolvedCounts: Map<Int, Int>,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Officer Performance")
            val headerStyle = createHeaderStyle(workbook)
            
            // Header
            val headerRow = sheet.createRow(0)
            val headers = listOf(
                "Officer Name",
                "Badge Number",
                "Rank",
                "Total Cases Assigned",
                "Cases Resolved",
                "Resolution Rate (%)",
                "Status"
            )
            
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).apply {
                    setCellValue(header)
                    setCellStyle(headerStyle)
                }
            }
            
            // Data rows
            officers.forEachIndexed { index, officer ->
                val row = sheet.createRow(index + 1)
                val totalCases = officerCaseCounts[officer.id] ?: 0
                val resolvedCases = officerResolvedCounts[officer.id] ?: 0
                val resolutionRate = if (totalCases > 0) {
                    (resolvedCases.toDouble() / totalCases * 100)
                } else 0.0
                
                row.createCell(0).setCellValue(officer.name)
                row.createCell(1).setCellValue(officer.badgeNumber)
                row.createCell(2).setCellValue(officer.rank)
                row.createCell(3).setCellValue(totalCases.toDouble())
                row.createCell(4).setCellValue(resolvedCases.toDouble())
                row.createCell(5).setCellValue(String.format("%.2f", resolutionRate))
                row.createCell(6).setCellValue(if (officer.isAvailable) "Available" else "Unavailable")
            }
            
            // Set column widths manually (autoSizeColumn doesn't work on Android)
            headers.indices.forEach { sheet.setColumnWidth(it, 4000) }
            
            // Save to file
            val fileName = "OfficerPerformance_${System.currentTimeMillis()}.xlsx"
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            onSuccess(uri)
            
        } catch (e: Exception) {
            onError("Failed to export officer performance: ${e.message}")
        }
    }
    
    /**
     * Share exported file
     */
    fun shareExportedFile(context: Context, uri: Uri, fileName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Excel File"))
    }
    
    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
            }
            setFont(font)
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.CENTER
        }
    }
}
