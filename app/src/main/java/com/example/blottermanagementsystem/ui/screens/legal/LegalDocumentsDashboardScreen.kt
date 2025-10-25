package com.example.blottermanagementsystem.ui.screens.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.viewmodel.LegalDocumentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentsDashboardScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToSummons: (Int) -> Unit,
    onNavigateToKPForms: (Int) -> Unit,
    onNavigateToMediation: (Int) -> Unit,
    viewModel: LegalDocumentsViewModel = viewModel()
) {
    var stats by remember { mutableStateOf<com.example.blottermanagementsystem.viewmodel.DocumentStatistics?>(null) }
    
    LaunchedEffect(reportId) {
        stats = viewModel.getDocumentStatistics(reportId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Legal Documents", fontWeight = FontWeight.Bold)
                        Text("Case #$reportId", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val listState = LazyListOptimizer.rememberOptimizedLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statistics Cards
            item {
                Text(
                    "Document Statistics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Summons",
                        count = stats?.summonsIssued ?: 0,
                        icon = Icons.Default.Gavel,
                        color = WarningYellow,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "KP Forms",
                        count = stats?.kpFormsGenerated ?: 0,
                        icon = Icons.Default.Description,
                        color = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                StatCard(
                    title = "Mediation Sessions",
                    count = stats?.mediationAttempts ?: 0,
                    icon = Icons.Default.People,
                    color = SuccessGreen,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Document Management Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Document Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                DocumentActionCard(
                    title = "Summons Management",
                    description = "Issue and track summons to respondents",
                    icon = Icons.Default.Gavel,
                    color = WarningYellow,
                    onClick = { onNavigateToSummons(reportId) }
                )
            }
            
            item {
                DocumentActionCard(
                    title = "KP Forms Generator",
                    description = "Generate KP-7, KP-10, KP-16, KP-18 forms",
                    icon = Icons.Default.Description,
                    color = InfoBlue,
                    onClick = { onNavigateToKPForms(reportId) }
                )
            }
            
            item {
                DocumentActionCard(
                    title = "Mediation Sessions",
                    description = "Record and track mediation attempts",
                    icon = Icons.Default.People,
                    color = SuccessGreen,
                    onClick = { onNavigateToMediation(reportId) }
                )
            }
            
            // Quick Actions
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        text = "Issue Summons",
                        icon = Icons.Default.Send,
                        onClick = { onNavigateToSummons(reportId) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        text = "New KP Form",
                        icon = Icons.Default.Add,
                        onClick = { onNavigateToKPForms(reportId) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DocumentActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, fontSize = 12.sp)
        }
    }
}
