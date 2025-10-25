package com.example.blottermanagementsystem.ui.screens.suspect

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Suspect
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.utils.rememberPaginationState
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuspectListScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onAddSuspect: () -> Unit,
    isAdmin: Boolean = false,
    viewModel: DashboardViewModel = viewModel()
) {
    val suspects by viewModel.getSuspectsByReportId(reportId).collectAsState(initial = emptyList())
    val paginationState = rememberPaginationState(suspects, pageSize = 20)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isAdmin) "Suspects (View Only)" else "Suspects",
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
                        IconButton(onClick = onAddSuspect) {
                            Icon(Icons.Default.Add, "Add Suspect", tint = ElectricBlue)
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
                    onClick = onAddSuspect,
                    containerColor = ErrorRed
                ) {
                    Icon(Icons.Default.Add, "Add Suspect")
                }
            }
        }
    ) { padding ->
        if (suspects.isEmpty()) {
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
                        Icons.Default.PersonOff,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (isAdmin) "No suspects added yet" else "No suspects added",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isAdmin) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onAddSuspect, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add First Suspect", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                items(paginationState.visibleItems) { suspect ->
                    SuspectCard(suspect)
                }
            }
        }
    }
}

@Composable
private fun SuspectCard(suspect: Suspect) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(suspect.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                suspect.alias?.let { alias ->
                    Spacer(Modifier.width(4.dp))
                    Text("\"$alias\"", fontSize = 14.sp, color = TextSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                suspect.age?.let { age ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$age yrs", fontSize = 14.sp, color = TextSecondary)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(suspect.gender ?: "N/A", fontSize = 14.sp, color = TextSecondary)
                }
            }
            
            suspect.address?.let { addr ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(addr, fontSize = 14.sp, color = TextSecondary)
                }
            }
            
            suspect.description?.let { desc ->
                Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                Text("Description:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Text(desc, fontSize = 13.sp, color = TextPrimary)
            }
        }
    }
}
