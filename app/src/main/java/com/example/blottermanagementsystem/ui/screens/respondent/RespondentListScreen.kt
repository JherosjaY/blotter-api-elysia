package com.example.blottermanagementsystem.ui.screens.respondent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Respondent
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.utils.rememberPaginationState
import com.example.blottermanagementsystem.viewmodel.RespondentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespondentListScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToAddRespondent: () -> Unit,
    onNavigateToRespondentDetail: (Int) -> Unit,
    isAdmin: Boolean = false,
    viewModel: RespondentViewModel = viewModel()
) {
    val respondents by viewModel.getRespondentsByReportId(reportId).collectAsState(initial = emptyList())
    val paginationState = rememberPaginationState(respondents, pageSize = 20)
    val cooperationStats by viewModel.cooperationStats.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Respondents",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Case #$reportId",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                actions = {
                    if (!isAdmin) {
                        IconButton(onClick = onNavigateToAddRespondent) {
                            Icon(Icons.Default.Add, "Add Respondent", tint = ElectricBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Respondent List
            if (respondents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No respondents added yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToAddRespondent,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Respondent")
                        }
                    }
                }
            } else {
                val listState = LazyListOptimizer.rememberOptimizedLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LazyListOptimizer.OPTIMAL_CONTENT_PADDING),
                    verticalArrangement = Arrangement.spacedBy(LazyListOptimizer.OPTIMAL_ITEM_SPACING)
                ) {
                    items(paginationState.visibleItems, key = { it.id }) { respondent ->
                        RespondentCard(
                            respondent = respondent,
                            onClick = { onNavigateToRespondentDetail(respondent.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RespondentCard(
    respondent: Respondent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = when (respondent.cooperationStatus) {
                            "Appeared" -> SuccessGreen
                            "Notified" -> WarningYellow
                            "No Response" -> Color.Gray
                            "Refused" -> DangerRed
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Respondent #${respondent.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    respondent.accusation,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(respondent.status, getStatusColor(respondent.status))
                    StatusChip(respondent.cooperationStatus, getCooperationColor(respondent.cooperationStatus))
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun StatusChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Accused" -> WarningYellow
        "Cleared" -> SuccessGreen
        "Elevated to Suspect" -> DangerRed
        else -> Color.Gray
    }
}

fun getCooperationColor(status: String): Color {
    return when (status) {
        "Appeared" -> SuccessGreen
        "Notified" -> WarningYellow
        "No Response" -> Color.Gray
        "Refused" -> DangerRed
        else -> Color.Gray
    }
}
