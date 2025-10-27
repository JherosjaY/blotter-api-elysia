package com.example.blottermanagementsystem.ui.screens.reports

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.utils.rememberCached
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.viewmodel.RespondentViewModel
import com.example.blottermanagementsystem.viewmodel.PersonViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: Int,
    isEditable: Boolean = false,
    isAdmin: Boolean = false,
    isOfficer: Boolean = false,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit = {},
    onExportPDF: () -> Unit = {},
    onNavigateToPersonHistory: (Int) -> Unit = {},
    onNavigateToRespondentList: (Int) -> Unit = {},
    onNavigateToSuspectList: (Int) -> Unit = {},
    onNavigateToWitnessList: (Int) -> Unit = {},
    onNavigateToEvidenceList: (Int) -> Unit = {},
    onNavigateToAddHearing: (Int) -> Unit = {},
    onNavigateToAddResolution: (Int) -> Unit = {},
    onNavigateToLegalDocuments: (Int) -> Unit = {},
    onNavigateToCaseTimeline: (Int, String) -> Unit = { _, _ -> },
    viewModel: DashboardViewModel = viewModel(),
    respondentViewModel: RespondentViewModel = viewModel(),
    personViewModel: PersonViewModel = viewModel()
) {
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    val report = allReports.find { it.id == reportId }
    
    // Fetch respondents for this report
    val respondents by respondentViewModel.getRespondentsByReportId(reportId).collectAsState(initial = emptyList())
    
    // For officer assignment
    val allOfficers by viewModel.allOfficers.collectAsState()
    var showAssignDialog by remember { mutableStateOf(false) }
    var showUpdateStatusDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var assignedOfficerIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val currentUserId = preferencesManager.userId ?: 0
    
    // Load assigned officers
    LaunchedEffect(reportId) {
        assignedOfficerIds = viewModel.getAssignedOfficerIds(reportId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Report Details",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        report?.let {
                            Text(
                                text = "Case #${it.caseNumber}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricBlue
                        )
                    }
                },
                actions = {
                    // Delete button (only for pending reports)
                    if (isEditable) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Report",
                                tint = ErrorRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (report == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📄",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Report Not Found",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "The requested report could not be found.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val listState = LazyListOptimizer.rememberOptimizedLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = true
            ) {
                // Status Badge
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = report.status)
                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(Date(report.dateFiled)),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Complainant Information
                item {
                    DetailCard(
                        title = "Complainant Information",
                        icon = Icons.Default.Person
                    ) {
                        DetailRow("Name", report.complainantName)
                        DetailRow("Address", report.complainantAddress)
                        DetailRow("Contact", report.complainantContact)
                    }
                }
                
                // Respondent Information
                if (respondents.isNotEmpty()) {
                    items(respondents.size) { index ->
                        val respondent = respondents[index]
                        
                        RespondentCard(
                            respondent = respondent,
                            index = index,
                            totalCount = respondents.size,
                            personViewModel = personViewModel,
                            onNavigateToPersonHistory = onNavigateToPersonHistory,
                            isOfficer = isOfficer,
                            isAdmin = isAdmin
                        )
                    }
                }
                
                // Incident Details
                item {
                    DetailCard(
                        title = "Incident Details",
                        icon = Icons.Default.Info
                    ) {
                        DetailRow("Type", report.incidentType)
                        DetailRow("Date", SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(Date(report.incidentDate)))
                        DetailRow("Time", report.incidentTime)
                        DetailRow("Location", report.incidentLocation)
                    }
                }
                
                // Narrative
                item {
                    DetailCard(
                        title = "Narrative",
                        icon = Icons.Default.Edit
                    ) {
                        Text(
                            text = report.narrative,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
                
                // Respondent Information
                if (report.respondentName.isNotBlank()) {
                    item {
                        DetailCard(
                            title = "Respondent Information",
                            icon = Icons.Default.AccountCircle
                        ) {
                            DetailRow("Name", report.respondentName)
                            if (report.respondentAddress.isNotBlank()) {
                                DetailRow("Address", report.respondentAddress)
                            }
                        }
                    }
                }
                
                // Assigned Officer
                if (report.assignedOfficer.isNotBlank()) {
                    item {
                        DetailCard(
                            title = "Assigned Officer",
                            icon = Icons.Default.Star
                        ) {
                            DetailRow("Officer", report.assignedOfficer)
                        }
                    }
                }
                
                // QR Code Section
                item {
                    var showQRCode by remember { mutableStateOf(false) }
                    
                    DetailCard(
                        title = "Quick Access QR Code",
                        icon = Icons.Default.QrCode
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Scan this QR code to quickly open this report",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (showQRCode) {
                                // Generate and display QR code
                                val qrBitmap = remember(reportId) {
                                    com.example.blottermanagementsystem.utils.QRCodeGenerator.generateReportQRCode(reportId)
                                }
                                
                                qrBitmap?.let { bitmap ->
                                    androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "QR Code for Case #${report.caseNumber}",
                                        modifier = Modifier
                                            .size(200.dp)
                                            .padding(8.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        "Case #${report.caseNumber}",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedButton(
                                onClick = { showQRCode = !showQRCode },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ElectricBlue
                                )
                            ) {
                                Icon(
                                    imageVector = if (showQRCode) Icons.Default.VisibilityOff else Icons.Default.QrCode,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (showQRCode) "Hide QR Code" else "Show QR Code")
                            }
                        }
                    }
                }
                
                // Actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportPDF,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ElectricBlue
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export PDF")
                        }
                        
                        if (isEditable) {
                            Button(
                                onClick = { onNavigateToEdit(reportId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBlue
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit")
                            }
                        }
                    }
                }
                
                // Case Timeline Button (All roles)
                if (report != null) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { onNavigateToCaseTimeline(reportId, report.caseNumber) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ElectricBlue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Case Timeline")
                        }
                    }
                }
                
                // Assign Officers Button (Admin only)
                if (isAdmin && report != null) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAssignDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = InfoBlue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentInd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (assignedOfficerIds.isEmpty()) "Assign Officers" 
                                else "Reassign Officers (${assignedOfficerIds.size})"
                            )
                        }
                    }
                }
                
                // Start Investigation Button (Officer only, when status is "Assigned")
                if (isOfficer && report != null && report.status == "Assigned") {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.updateReportStatus(
                                        reportId = reportId,
                                        newStatus = "Under Investigation",
                                        notes = "Investigation started by officer",
                                        userId = currentUserId
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = InfoBlue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Investigation")
                        }
                    }
                }
                
                // Update Status Button (Officer only, when already investigating)
                if (isOfficer && report != null && report.status == "Under Investigation") {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showUpdateStatusDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningOrange
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Update,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update Case Status")
                        }
                    }
                }
                
                // Case Management Section (Officers/Clerk ONLY - Admin VIEW ONLY)
                if (report != null && (isAdmin || isOfficer || isEditable)) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isAdmin) "Case Overview (View Only)" else "Case Management",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            if (isAdmin) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = "View Only",
                                    tint = InfoBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Admin Warning Banner
                    if (isAdmin) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = InfoBlue.copy(alpha = 0.15f)
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        null,
                                        tint = InfoBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            "Administrator View",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InfoBlue
                                        )
                                        Text(
                                            "You can view case details. Officers handle investigations.",
                                            fontSize = 11.sp,
                                            color = InfoBlue.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    
                    // Management Buttons Grid (Officers & Clerks can ADD, Admin can only VIEW)
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: Respondents & Suspects
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onNavigateToRespondentList(reportId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = ElectricBlue
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            if (isAdmin) Icons.Default.Visibility else Icons.Default.Person,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (isAdmin) "View Respondents" else "Respondents",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                
                                OutlinedButton(
                                    onClick = { onNavigateToSuspectList(reportId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = DangerRed
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            if (isAdmin) Icons.Default.Visibility else Icons.Default.PersonSearch,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (isAdmin) "View Suspects" else "Suspects",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            // Row 2: Witnesses & Evidence
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onNavigateToWitnessList(reportId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = SuccessGreen
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (isAdmin) "View Witnesses" else "Witnesses",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                
                                OutlinedButton(
                                    onClick = { onNavigateToEvidenceList(reportId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = WarningYellow
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            if (isAdmin) Icons.Default.Visibility else Icons.Default.Description,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (isAdmin) "View Evidence" else "Evidence",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            // Row 3: Hearings & Resolution (View only for Admin)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onNavigateToAddHearing(reportId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = InfoBlue
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            if (isAdmin) Icons.Default.Visibility else Icons.Default.Event,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (isAdmin) "View Hearings" else "Hearings",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                
                                OutlinedButton(
                                    onClick = { onNavigateToAddResolution(reportId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = SuccessGreen
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            if (isAdmin) Icons.Default.Visibility else Icons.Default.Gavel,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (isAdmin) "View Resolution" else "Resolution",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            // Row 4: Legal Documents
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onNavigateToLegalDocuments(reportId) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarningYellow
                                )
                            ) {
                                Icon(
                                    if (isAdmin) Icons.Default.Visibility else Icons.Default.Description,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isAdmin) "View Legal Documents & KP Forms" else "Legal Documents & KP Forms",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // User-Friendly Section (Regular Users ONLY)
                if (report != null && !isAdmin && !isOfficer && !isEditable) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Case Status & Updates",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Status Timeline Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Current Status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = InfoBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Current Status",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            report.status,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (report.status) {
                                                "Pending" -> WarningYellow
                                                "Under Investigation" -> InfoBlue
                                                "Resolved" -> SuccessGreen
                                                else -> androidx.compose.ui.graphics.Color.White
                                            }
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = DividerColor)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Case Number
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Tag,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Case Number",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            report.caseNumber,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Filed Date
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Filed On",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                                .format(Date(report.dateFiled)),
                                            fontSize = 14.sp,
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // What's Next Card
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = InfoBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "What's Next?",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InfoBlue
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        when (report.status) {
                                            "Pending" -> "Your case is being reviewed. You'll be notified of any updates."
                                            "Under Investigation" -> "An officer is investigating your case. Please be available for any follow-up."
                                            "For Mediation" -> "A mediation session will be scheduled. You'll receive a notification."
                                            "Resolved" -> "Your case has been resolved. You can view the resolution details."
                                            else -> "We'll keep you updated on your case progress."
                                        },
                                        fontSize = 13.sp,
                                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    // My Upcoming Hearings Card
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        val hearings by viewModel.getHearingsByReportId(reportId).collectAsState(initial = emptyList())
                        val upcomingHearings = hearings.filter { it.status != "Completed" && it.status != "Cancelled" }
                        
                        if (upcomingHearings.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = null,
                                            tint = WarningYellow,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "📅 Upcoming Hearing",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarningYellow
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = WarningYellow.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    upcomingHearings.take(2).forEach { hearing ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = WarningYellow,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    "${hearing.hearingDate} at ${hearing.hearingTime}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = androidx.compose.ui.graphics.Color.White
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "📍 ${hearing.location}",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    hearing.purpose,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = WarningYellow.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Info,
                                                contentDescription = null,
                                                tint = WarningYellow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Please arrive 15 minutes early. Bring a valid ID.",
                                                fontSize = 12.sp,
                                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Case Progress Timeline
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Timeline,
                                        contentDescription = null,
                                        tint = InfoBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Case Progress",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Timeline Steps
                                val steps = listOf(
                                    Triple("Filed", "Report submitted", report.status != "Pending"),
                                    Triple("Under Review", "Being investigated", report.status in listOf("Under Investigation", "For Mediation", "For Lupon", "Mediation Ongoing", "Settled", "Resolved")),
                                    Triple("In Process", "Mediation/Hearing", report.status in listOf("For Mediation", "For Lupon", "Mediation Ongoing", "Settled", "Resolved")),
                                    Triple("Resolved", "Case closed", report.status == "Resolved")
                                )
                                
                                steps.forEachIndexed { index, (title, desc, isCompleted) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Timeline indicator
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isCompleted) SuccessGreen else DividerColor,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isCompleted) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = androidx.compose.ui.graphics.Color.White,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = androidx.compose.ui.graphics.Color.White,
                                                            modifier = Modifier.size(8.dp)
                                                        ) {}
                                                    }
                                                }
                                            }
                                            
                                            if (index < steps.size - 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(2.dp)
                                                        .height(40.dp)
                                                        .background(if (isCompleted) SuccessGreen else DividerColor)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(16.dp))
                                        
                                        // Step info
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                title,
                                                fontSize = 14.sp,
                                                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCompleted) androidx.compose.ui.graphics.Color.White else TextSecondary
                                            )
                                            Text(
                                                desc,
                                                fontSize = 12.sp,
                                                color = TextTertiary
                                            )
                                            if (index < steps.size - 1) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Action Required Alert (if applicable)
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        val hearings by viewModel.getHearingsByReportId(reportId).collectAsState(initial = emptyList())
                        val hasUpcomingHearing = hearings.any { it.status == "Scheduled" }
                        
                        if (hasUpcomingHearing || report.status == "For Mediation") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.NotificationImportant,
                                        contentDescription = null,
                                        tint = DangerRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "⚠️ Action Required",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DangerRed
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            when {
                                                hasUpcomingHearing -> "You have an upcoming hearing. Please prepare necessary documents and arrive on time."
                                                report.status == "For Mediation" -> "Your case is scheduled for mediation. You'll receive a notification with the schedule."
                                                else -> "Please check your notifications for important updates."
                                            },
                                            fontSize = 13.sp,
                                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Need Help Card
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Help,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Need Help?",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "If you have questions about your case, please contact the barangay office or the assigned officer.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Report?", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        "Are you sure you want to delete this report? This action cannot be undone.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                report?.let {
                                    val currentUserName = "${preferencesManager.firstName} ${preferencesManager.lastName}"
                                    viewModel.deleteReport(it, currentUserName)
                                }
                                showDeleteDialog = false
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = CardBackground,
                shape = RoundedCornerShape(16.dp)
            )
        }
        
        // Assign Officers Dialog
        if (showAssignDialog && report != null) {
            com.example.blottermanagementsystem.ui.components.AssignOfficersDialog(
                caseNumber = report.caseNumber,
                availableOfficers = allOfficers,
                currentlyAssignedIds = assignedOfficerIds,
                onDismiss = { showAssignDialog = false },
                onAssign = { selectedIds ->
                    scope.launch {
                        viewModel.assignOfficersToCase(reportId, selectedIds)
                        assignedOfficerIds = selectedIds
                        showAssignDialog = false
                    }
                }
            )
        }
        
        // Update Status Dialog
        if (showUpdateStatusDialog && report != null) {
            com.example.blottermanagementsystem.ui.components.UpdateStatusDialog(
                currentStatus = report.status,
                onDismiss = { showUpdateStatusDialog = false },
                onUpdateStatus = { newStatus, notes ->
                    scope.launch {
                        viewModel.updateReportStatus(reportId, newStatus, notes, currentUserId)
                        showUpdateStatusDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "Pending" -> WarningOrange.copy(alpha = 0.2f) to WarningOrange
        "Under Investigation" -> InfoBlue.copy(alpha = 0.2f) to InfoBlue
        "Resolved" -> SuccessGreen.copy(alpha = 0.2f) to SuccessGreen
        "Closed" -> TextSecondary.copy(alpha = 0.2f) to TextSecondary
        else -> ElectricBlue.copy(alpha = 0.2f) to ElectricBlue
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = status,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun RespondentCard(
    respondent: com.example.blottermanagementsystem.data.entity.Respondent,
    index: Int,
    totalCount: Int,
    personViewModel: PersonViewModel,
    onNavigateToPersonHistory: (Int) -> Unit,
    isOfficer: Boolean = false,
    isAdmin: Boolean = false
) {
    val person by personViewModel.getPersonByIdFlow(respondent.personId).collectAsState(initial = null)
    
    DetailCard(
        title = if (totalCount > 1) "Respondent ${index + 1}" else "Respondent Information",
        icon = Icons.Default.Warning
    ) {
        person?.let {
            DetailRow("Name", "${it.firstName} ${it.lastName}")
            DetailRow("Accusation", respondent.accusation)
            DetailRow("Relationship", respondent.relationshipToComplainant ?: "Unknown")
            DetailRow("Contact", respondent.contactNumber)
            DetailRow("Status", respondent.status)
            
            // Cooperation Status with color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cooperation",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = when (respondent.cooperationStatus) {
                        "Appeared" -> SuccessGreen
                        "Notified" -> WarningOrange
                        "No Response" -> ErrorRed
                        "Refused" -> ErrorRed
                        else -> TextSecondary
                    }.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = respondent.cooperationStatus,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (respondent.cooperationStatus) {
                            "Appeared" -> SuccessGreen
                            "Notified" -> WarningOrange
                            "No Response" -> ErrorRed
                            "Refused" -> ErrorRed
                            else -> TextSecondary
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            // View Person History Button - ONLY for Officer/Admin
            if (isOfficer || isAdmin) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onNavigateToPersonHistory(it.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricBlue
                    )
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Person History")
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}
