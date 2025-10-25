package com.example.blottermanagementsystem.ui.screens.evidence

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Evidence
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.utils.rememberPaginationState
import com.example.blottermanagementsystem.utils.rememberOptimizedImageLoader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceListScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onAddEvidence: () -> Unit,
    isAdmin: Boolean = false,
    viewModel: DashboardViewModel = viewModel()
) {
    val evidenceList by viewModel.getEvidenceByReportId(reportId).collectAsState(initial = emptyList())
    val paginationState = rememberPaginationState(evidenceList, pageSize = 15)
    val imageLoader = rememberOptimizedImageLoader()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isAdmin) "Evidence (View Only)" else "Evidence",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                actions = {
                    if (!isAdmin) {
                        IconButton(onClick = onAddEvidence) {
                            Icon(Icons.Default.Add, "Add Evidence", tint = ElectricBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!isAdmin) {
                FloatingActionButton(
                    onClick = onAddEvidence,
                    containerColor = SuccessGreen
                ) {
                    Icon(Icons.Default.Add, "Add Evidence")
                }
            }
        }
    ) { padding ->
        if (evidenceList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Default.FolderOff,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (isAdmin) "No evidence added yet" else "No evidence added",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isAdmin) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onAddEvidence, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add First Evidence", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            val listState = LazyListOptimizer.rememberOptimizedLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(LazyListOptimizer.OPTIMAL_CONTENT_PADDING),
                verticalArrangement = Arrangement.spacedBy(LazyListOptimizer.OPTIMAL_ITEM_SPACING)
            ) {
                items(paginationState.visibleItems, key = { it.id }) { evidence ->
                    EvidenceCard(evidence)
                }
            }
        }
    }
}

@Composable
private fun EvidenceCard(evidence: Evidence) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Evidence #${evidence.id}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        evidence.evidenceType,
                        fontSize = 12.sp,
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Text(evidence.description, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            
            Divider(color = DividerColor)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Evidence Type:", fontSize = 11.sp, color = TextTertiary)
                    Text(evidence.evidenceType, fontSize = 13.sp, color = TextSecondary)
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Collected By:", fontSize = 11.sp, color = TextTertiary)
                    Text(evidence.collectedBy ?: "N/A", fontSize = 13.sp, color = TextSecondary)
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(dateFormat.format(Date(evidence.collectedDate)), fontSize = 12.sp, color = TextTertiary)
            }
            
            evidence.filePath?.let { path ->
                Divider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
                Text("File Path:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Text(path, fontSize = 12.sp, color = TextPrimary)
            }
        }
    }
}
