package com.example.blottermanagementsystem.ui.screens.person

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.PersonHistory
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.PersonViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonHistoryScreen(
    personId: Int,
    onNavigateBack: () -> Unit,
    isOfficer: Boolean = false,
    isAdmin: Boolean = false,
    personViewModel: PersonViewModel = viewModel()
) {
    // PRIVACY: Only Officers and Admins can view person history
    // Regular users CANNOT see history for privacy protection
    if (!isOfficer && !isAdmin) {
        // Redirect back - unauthorized access
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }
    
    val person by personViewModel.getPersonByIdFlow(personId).collectAsState(initial = null)
    val history by personViewModel.getHistoryByPersonId(personId).collectAsState(initial = emptyList())
    
    var involvement by remember { mutableStateOf<com.example.blottermanagementsystem.viewmodel.PersonInvolvementSummary?>(null) }
    
    LaunchedEffect(personId) {
        involvement = personViewModel.getPersonInvolvementSummary(personId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Person History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        person?.let {
                            Text(
                                text = "${it.firstName} ${it.lastName}",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Person Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                person?.let {
                                    Text(
                                        text = "${it.firstName} ${it.lastName}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = it.personType,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (it.contactNumber != null) {
                                        Text(
                                            text = it.contactNumber,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Involvement Summary
            item {
                involvement?.let { summary ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Involvement Summary",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SummaryItem("Complainant", summary.asComplainant, SuccessGreen)
                                SummaryItem("Respondent", summary.asRespondent, WarningOrange)
                                SummaryItem("Witness", summary.asWitness, ElectricBlue)
                                SummaryItem("Suspect", summary.asSuspect, ErrorRed)
                            }
                        }
                    }
                }
            }
            
            // Activity Timeline
            item {
                Text(
                    text = "Activity Timeline",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
            }
            
            if (history.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📋",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No activity history",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(history) { activity ->
                    HistoryItem(activity)
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun HistoryItem(activity: PersonHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon based on activity type
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (activity.activityType) {
                            "FILED_REPORT" -> SuccessGreen.copy(alpha = 0.2f)
                            "ADDED_AS_RESPONDENT" -> WarningOrange.copy(alpha = 0.2f)
                            "ELEVATED_TO_SUSPECT" -> ErrorRed.copy(alpha = 0.2f)
                            "CLEARED" -> SuccessGreen.copy(alpha = 0.2f)
                            else -> ElectricBlue.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (activity.activityType) {
                        "FILED_REPORT" -> Icons.Default.Description
                        "ADDED_AS_RESPONDENT" -> Icons.Default.Warning
                        "ELEVATED_TO_SUSPECT" -> Icons.Default.Error
                        "CLEARED" -> Icons.Default.CheckCircle
                        "APPEARED_IN_PERSON" -> Icons.Default.Person
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when (activity.activityType) {
                        "FILED_REPORT" -> SuccessGreen
                        "ADDED_AS_RESPONDENT" -> WarningOrange
                        "ELEVATED_TO_SUSPECT" -> ErrorRed
                        "CLEARED" -> SuccessGreen
                        else -> ElectricBlue
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                        .format(Date(activity.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
