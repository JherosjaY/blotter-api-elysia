package com.example.blottermanagementsystem.ui.screens.timeline

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.CaseTimeline
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseTimelineScreen(
    reportId: Int,
    caseNumber: String,
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val timelineEvents by viewModel.getCaseTimeline(reportId).collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Case Timeline",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            caseNumber,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0f172a)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0f172a),
                            Color(0xFF1e293b)
                        )
                    )
                )
        ) {
            // Background blur circle
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 100.dp, y = (-50).dp)
                    .blur(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            if (timelineEvents.isEmpty()) {
                // Show progress stepper for empty timeline
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    item {
                        Text(
                            "Case Progress Stages",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                
                    item {
                        CaseProgressStepper(currentStage = "Pending")
                    }
                
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1e293b).copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    InfoBlue.copy(alpha = 0.3f),
                                                    InfoBlue.copy(alpha = 0.1f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = InfoBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    "Timeline events will appear here as the case progresses through each stage.",
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(timelineEvents) { index, event ->
                        AnimatedTimelineItem(
                            event = event,
                            index = index,
                            isLast = index == timelineEvents.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedTimelineItem(
    event: CaseTimeline,
    index: Int,
    isLast: Boolean
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + 
                slideInHorizontally(animationSpec = tween(500)) { it / 2 }
    ) {
        TimelineEventItem(event = event, isLast = isLast)
    }
}

@Composable
fun TimelineEventItem(event: CaseTimeline, isLast: Boolean) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    val (icon, color) = getEventIconAndColor(event.eventType)
    
    // Removed pulsing animation for performance
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Timeline line and icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp)
        ) {
            // Animated Icon with glow effect
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Removed glow animation for performance
                
                // Main icon circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.3f),
                                    color.copy(alpha = 0.15f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Animated connecting line
            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(3.dp)
                        .height(80.dp)
                        .padding(vertical = 4.dp)
                ) {
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.5f),
                                color.copy(alpha = 0.2f)
                            )
                        ),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Event content card with gradient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1e293b).copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Colored top bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    color,
                                    color.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Event title with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            event.eventTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Event description
                    Text(
                        event.eventDescription,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Divider(color = DividerColor.copy(alpha = 0.3f))
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Metadata row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Performed by with role badge
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    event.performedBy,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Role badge
                            Surface(
                                color = getRoleColor(event.performedByRole).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    event.performedByRole,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getRoleColor(event.performedByRole),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        // Timestamp with icon
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    dateFormat.format(Date(event.timestamp)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    timeFormat.format(Date(event.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CaseProgressStepper(currentStage: String) {
    val stages = listOf(
        StageInfo("Pending", "Awaiting review", Icons.Default.HourglassEmpty, WarningOrange),
        StageInfo("Investigation", "In progress", Icons.Default.Search, InfoBlue),
        StageInfo("Hearing", "Scheduled", Icons.Default.Event, ElectricBlue),
        StageInfo("Resolved", "Completed", Icons.Default.CheckCircle, SuccessGreen)
    )
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Horizontal stepper
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stages.forEachIndexed { index, stage ->
                val isActive = stage.name == currentStage
                val isPast = stages.indexOfFirst { it.name == currentStage } > index
                
                // Stage item
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive || isPast) {
                                    Brush.radialGradient(
                                        colors = listOf(
                                            stage.color.copy(alpha = 0.4f),
                                            stage.color.copy(alpha = 0.2f)
                                        )
                                    )
                                } else {
                                    Brush.radialGradient(
                                        colors = listOf(
                                            DividerColor.copy(alpha = 0.3f),
                                            DividerColor.copy(alpha = 0.1f)
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPast) Icons.Default.CheckCircle else stage.icon,
                            contentDescription = null,
                            tint = if (isActive || isPast) stage.color else DividerColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Stage name
                    Text(
                        stage.name,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive || isPast) stage.color else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    // Description
                    Text(
                        stage.description,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Connecting line (except last item)
                if (index < stages.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .height(3.dp)
                            .offset(y = (-20).dp)
                            .background(
                                if (isPast) stage.color.copy(alpha = 0.5f) 
                                else DividerColor.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

data class StageInfo(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun getEventIconAndColor(eventType: String): Pair<ImageVector, Color> {
    return when (eventType) {
        "STATUS_CHANGE" -> Icons.Default.Update to InfoBlue
        "OFFICER_ASSIGNED" -> Icons.Default.PersonAdd to ElectricBlue
        "HEARING_SCHEDULED" -> Icons.Default.Event to WarningOrange
        "EVIDENCE_ADDED" -> Icons.Default.Description to SuccessGreen
        "RESPONDENT_ADDED" -> Icons.Default.PersonAdd to ErrorRed
        "WITNESS_ADDED" -> Icons.Default.Visibility to InfoBlue
        "SUSPECT_ADDED" -> Icons.Default.Warning to WarningOrange
        "RESOLUTION_ADDED" -> Icons.Default.CheckCircle to SuccessGreen
        "CASE_CREATED" -> Icons.Default.Add to ElectricBlue
        "CASE_UPDATED" -> Icons.Default.Edit to InfoBlue
        else -> Icons.Default.Circle to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun getRoleColor(role: String): Color {
    return when (role) {
        "Admin" -> ErrorRed
        "Officer" -> ElectricBlue
        "User" -> SuccessGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
