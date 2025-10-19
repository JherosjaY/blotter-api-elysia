package com.example.blottermanagementsystem.ui.screens.witness

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
import com.example.blottermanagementsystem.data.entity.Witness
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WitnessListScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onAddWitness: () -> Unit,
    isAdmin: Boolean = false,
    viewModel: DashboardViewModel = viewModel()
) {
    val witnesses by viewModel.getWitnessesByReportId(reportId).collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isAdmin) "Witnesses (View Only)" else "Witnesses",
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
                        IconButton(onClick = onAddWitness) {
                            Icon(Icons.Default.Add, "Add Witness", tint = ElectricBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!isAdmin) {
                FloatingActionButton(
                    onClick = onAddWitness,
                    containerColor = ElectricBlue
                ) {
                    Icon(Icons.Default.Add, "Add Witness")
                }
            }
        }
    ) { padding ->
        if (witnesses.isEmpty()) {
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
                        if (isAdmin) "No witnesses added yet" else "No witnesses added",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isAdmin) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onAddWitness, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add First Witness", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(witnesses) { witness ->
                    WitnessCard(witness)
                }
            }
        }
    }
}

@Composable
private fun WitnessCard(witness: Witness) {
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
                Icon(Icons.Default.Person, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(witness.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(witness.contactNumber ?: "N/A", fontSize = 14.sp, color = TextSecondary)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(witness.address ?: "N/A", fontSize = 14.sp, color = TextSecondary)
            }
            
            witness.statement?.let { stmt ->
                Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                Text("Statement:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Text(stmt, fontSize = 13.sp, color = TextPrimary)
            }
        }
    }
}
