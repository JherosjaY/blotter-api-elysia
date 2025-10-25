package com.example.blottermanagementsystem.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Hearing
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HearingCalendarScreen(
    onNavigateBack: () -> Unit,
    onHearingClick: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val allHearings by viewModel.getAllHearings().collectAsState(initial = emptyList())
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val coroutineScope = rememberCoroutineScope()
    
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.SUNDAY
    )
    
    // Group hearings by date
    val hearingsByDate = remember(allHearings) {
        allHearings.mapNotNull { hearing ->
            try {
                val date = LocalDate.parse(hearing.hearingDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                hearing to date
            } catch (e: Exception) {
                null
            }
        }.groupBy({ it.second }, { it.first })
    }
    
    val selectedDateHearings = selectedDate?.let { hearingsByDate[it] } ?: emptyList()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Hearing Calendar",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Calendar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month/Year header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val previousMonth = state.firstVisibleMonth.yearMonth.minusMonths(1)
                            if (previousMonth >= startMonth) {
                                coroutineScope.launch {
                                    state.animateScrollToMonth(previousMonth)
                                }
                            }
                        }) {
                            Icon(Icons.Default.ChevronLeft, "Previous", tint = ElectricBlue)
                        }
                        
                        Text(
                            state.firstVisibleMonth.yearMonth.format(
                                DateTimeFormatter.ofPattern("MMMM yyyy")
                            ),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = {
                            val nextMonth = state.firstVisibleMonth.yearMonth.plusMonths(1)
                            if (nextMonth <= endMonth) {
                                coroutineScope.launch {
                                    state.animateScrollToMonth(nextMonth)
                                }
                            }
                        }) {
                            Icon(Icons.Default.ChevronRight, "Next", tint = ElectricBlue)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Day of week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DayOfWeek.values().forEach { dayOfWeek ->
                            Text(
                                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Calendar grid
                    HorizontalCalendar(
                        state = state,
                        dayContent = { day ->
                            Day(
                                day = day,
                                isSelected = selectedDate == day.date,
                                hasHearings = hearingsByDate.containsKey(day.date),
                                hearingCount = hearingsByDate[day.date]?.size ?: 0,
                                onClick = { selectedDate = if (selectedDate == day.date) null else day.date }
                            )
                        }
                    )
                }
            }
            
            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("Pending", WarningOrange)
                LegendItem("Completed", SuccessGreen)
                LegendItem("Cancelled", ErrorRed)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selected date hearings
            if (selectedDate != null) {
                Text(
                    "Hearings on ${selectedDate!!.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedDateHearings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hearings scheduled",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedDateHearings) { hearing ->
                            HearingCard(hearing = hearing, onClick = { onHearingClick(hearing.id) })
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Select a date to view hearings",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    hasHearings: Boolean,
    hearingCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> ElectricBlue
                    hasHearings -> ElectricBlue.copy(alpha = 0.2f)
                    else -> androidx.compose.ui.graphics.Color.Transparent
                }
            )
            .clickable(enabled = day.position == DayPosition.MonthDate) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 14.sp,
                color = when {
                    day.position != DayPosition.MonthDate -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    isSelected -> androidx.compose.ui.graphics.Color.White
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (hasHearings && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue)
                )
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HearingCard(hearing: Hearing, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (hearing.status) {
                            "Completed" -> SuccessGreen
                            "Cancelled" -> ErrorRed
                            else -> WarningOrange
                        }
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    hearing.purpose,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    hearing.location,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                hearing.hearingTime,
                fontSize = 12.sp,
                color = ElectricBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
