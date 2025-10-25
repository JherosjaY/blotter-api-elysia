package com.example.blottermanagementsystem.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.components.DashboardTopBar
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.viewmodel.NotificationViewModel
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.utils.PreferencesManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    firstName: String = "Admin",
    onNavigateToUsers: () -> Unit,
    onNavigateToOfficers: () -> Unit,
    onNavigateToRecordsArchive: () -> Unit,
    onNavigateToQRScanner: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val userId = preferencesManager.userId
    
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    val notifications by notificationViewModel.getNotificationsByUser(userId).collectAsState(initial = emptyList())
    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }
    
    val activeReports = remember(allReports) { allReports.count { !it.isArchived } }
    val archivedReports = remember(allReports) { allReports.count { it.isArchived } }
    
    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    
    // Handle refresh
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refreshDashboard()
            kotlinx.coroutines.delay(1000) // Small delay for better UX
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Admin Dashboard",
                subtitle = "System Management",
                firstName = firstName,
                notificationCount = unreadCount,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val listState = LazyListOptimizer.rememberOptimizedLazyListState()
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(LazyListOptimizer.OPTIMAL_CONTENT_PADDING),
                verticalArrangement = Arrangement.spacedBy(LazyListOptimizer.OPTIMAL_ITEM_SPACING)
            ) {
            // Welcome Section
            item {
                Text(
                    text = "Welcome back, $firstName!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "System Overview & Management",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // System Statistics Section
            item {
                Text(
                    text = "System Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Statistics Cards Row 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Users",
                        value = dashboardStats.totalUsers.toString(),
                        icon = Icons.Default.Person,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Officers",
                        value = dashboardStats.totalOfficers.toString(),
                        icon = Icons.Default.Badge,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Statistics Cards Row 2
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Active Cases",
                        value = activeReports.toString(),
                        icon = Icons.Default.FolderOpen,
                        gradient = Brush.linearGradient(
                            colors = listOf(ElectricBlue, InfoBlue)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Archived",
                        value = archivedReports.toString(),
                        icon = Icons.Default.Archive,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Quick Actions Section
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Action Cards Row 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "User Management",
                        subtitle = "Manage user accounts",
                        icon = Icons.Default.People,
                        onClick = onNavigateToUsers,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ActionCard(
                        title = "Officer Management",
                        subtitle = "Manage officers",
                        icon = Icons.Default.Badge,
                        onClick = onNavigateToOfficers,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Action Cards Row 2
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Records Archive",
                        subtitle = "View all case records",
                        icon = Icons.Default.Folder,
                        onClick = onNavigateToRecordsArchive,
                        modifier = Modifier.weight(1f),
                        highlighted = true
                    )
                    
                    ActionCard(
                        title = "QR Scanner",
                        subtitle = "Scan case QR codes",
                        icon = Icons.Default.QrCodeScanner,
                        onClick = onNavigateToQRScanner,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = InfoBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Role",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "You have read-only access to case records. Officers handle case management.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
            
            // Pull-to-refresh indicator
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) ElectricBlue.copy(alpha = 0.1f) else Color(0xFF1e293b).copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (highlighted) 
                            Brush.radialGradient(
                                colors = listOf(
                                    ElectricBlue.copy(alpha = 0.3f),
                                    ElectricBlue.copy(alpha = 0.1f)
                                )
                            )
                        else 
                            Brush.radialGradient(
                                colors = listOf(
                                    ElectricBlue.copy(alpha = 0.2f),
                                    ElectricBlue.copy(alpha = 0.05f)
                                )
                            )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
