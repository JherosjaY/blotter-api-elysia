package com.example.blottermanagementsystem.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Officer
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.AdminViewModel
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.utils.rememberPaginationState
import com.example.blottermanagementsystem.utils.rememberDebouncedState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val officers by viewModel.getAllOfficers().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var searchQuery by rememberDebouncedState("", delayMillis = 300)
    var showAddDialog by remember { mutableStateOf(false) }
    var showCredentialsDialog by remember { mutableStateOf(false) }
    var generatedUsername by remember { mutableStateOf("") }
    var generatedPassword by remember { mutableStateOf("") }
    var officerName by remember { mutableStateOf("") }
    
    val filteredOfficers = remember(officers, searchQuery) {
        officers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.badgeNumber.contains(searchQuery, ignoreCase = true)
        }
    }
    val paginationState = rememberPaginationState(filteredOfficers, pageSize = 20)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Officer Management",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredOfficers.size} officers",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Officer",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search officers") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Officer List
            if (filteredOfficers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "👮",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No officers found",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val listState = LazyListOptimizer.rememberOptimizedLazyListState()
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(LazyListOptimizer.OPTIMAL_CONTENT_PADDING),
                    verticalArrangement = Arrangement.spacedBy(LazyListOptimizer.OPTIMAL_ITEM_SPACING)
                ) {
                    items(paginationState.visibleItems, key = { it.id }) { officer ->
                        OfficerCard(
                            officer = officer,
                            onDeactivate = {
                                scope.launch {
                                    // Toggle availability
                                    val updatedOfficer = officer.copy(isAvailable = !officer.isAvailable)
                                    viewModel.updateOfficer(updatedOfficer)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    // Delete officer and their user account
                                    viewModel.deleteOfficerById(officer.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add Officer Dialog
    if (showAddDialog) {
        AddOfficerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { firstName, lastName, badgeNumber, rank, contactNumber ->
                scope.launch {
                    val fullName = "$firstName $lastName"
                    
                    // Generate username and password
                    val username = "off.${badgeNumber.lowercase()}"
                    val tempPassword = badgeNumber
                    
                    // Create User account FIRST to get the user ID
                    val userId = viewModel.createOfficerAccount(
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        tempPassword = tempPassword,
                        badgeNumber = badgeNumber
                    )
                    
                    // Create Officer record with linked user ID
                    val newOfficer = Officer(
                        userId = userId, // Link to User table
                        name = fullName,
                        badgeNumber = badgeNumber,
                        rank = rank,
                        contactNumber = contactNumber,
                        assignedCases = 0,
                        isAvailable = true
                    )
                    viewModel.addOfficer(newOfficer)
                    
                    // Show credentials dialog
                    officerName = fullName
                    generatedUsername = username
                    generatedPassword = tempPassword
                    showAddDialog = false
                    showCredentialsDialog = true
                }
            }
        )
    }
    
    // Credentials Dialog
    if (showCredentialsDialog) {
        AlertDialog(
            onDismissRequest = { showCredentialsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Officer Account Created!",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Account created for $officerName",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Divider(color = DividerColor)
                    
                    // Credentials Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Login Credentials",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                            
                            // Username
                            Column {
                                Text(
                                    text = "Username",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = generatedUsername,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Password
                            Column {
                                Text(
                                    text = "Password (Badge Number)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = generatedPassword,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Password is same as badge number",
                                    fontSize = 10.sp,
                                    color = TextTertiary,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                    
                    // Important Note
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Important",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningOrange
                                )
                                Text(
                                    text = "Please provide these credentials to the officer. They will be required to change their username and password on first login.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCredentialsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue
                    )
                ) {
                    Text("Got it!")
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun OfficerCard(
    officer: Officer,
    onDeactivate: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Row: Avatar, Name, Badge, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    InfoBlue.copy(alpha = 0.3f),
                                    InfoBlue.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = InfoBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Name and Badge
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = officer.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Badge: ${officer.badgeNumber}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                // More Options Menu (moved here)
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Deactivate") },
                            onClick = {
                                showMenu = false
                                onDeactivate()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Block, null, tint = WarningOrange)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = ErrorRed) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = ErrorRed)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Row: Rank, Status, Cases
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge
                Surface(
                    color = InfoBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = officer.rank,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InfoBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Cases Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${officer.assignedCases}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Text(
                        text = "Cases",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Delete Officer?",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Are you sure you want to permanently delete this officer account?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Officer: ${officer.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Badge: ${officer.badgeNumber}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "⚠️ This action cannot be undone!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDelete()
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = CardBackground
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOfficerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit // firstName, lastName, badgeNumber, rank, contactNumber
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var rank by remember { mutableStateOf("") }
    var classLevel by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contactError by remember { mutableStateOf<String?>(null) }
    var showRankDropdown by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    
    // Phone validation function
    fun validatePhoneNumber(phone: String): Boolean {
        val pattern09 = "^09\\d{9}$".toRegex()
        return pattern09.matches(phone)
    }
    
    // Philippine PNP Ranks (up to Lieutenant)
    val ranks = listOf(
        "Patrolman" to "PTLM",
        "Patrolwoman" to "PTLW",
        "Police Officer I" to "PO1",
        "Police Officer II" to "PO2",
        "Police Officer III" to "PO3",
        "Senior Police Officer I" to "SPO1",
        "Senior Police Officer II" to "SPO2",
        "Senior Police Officer III" to "SPO3",
        "Senior Police Officer IV" to "SPO4",
        "Police Inspector" to "PINSP",
        "Lieutenant" to "LT"
    )
    
    val classLevels = listOf("Class I", "Class II", "Class III")
    
    // Auto-generate badge number
    val badgeNumber = remember {
        "BDG-${System.currentTimeMillis().toString().takeLast(6)}"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Officer",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    placeholder = { Text("e.g., Jeremy") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor
                    )
                )
                
                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    placeholder = { Text("e.g., Ranola") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor
                    )
                )
                
                // Auto-generated Badge Number (Read-only)
                OutlinedTextField(
                    value = badgeNumber,
                    onValueChange = {},
                    label = { Text("Badge Number") },
                    readOnly = true,
                    enabled = false,
                    leadingIcon = {
                        Icon(Icons.Default.Shield, null, tint = ElectricBlue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = ElectricBlue.copy(alpha = 0.5f),
                        disabledTextColor = TextPrimary,
                        disabledLabelColor = TextSecondary
                    )
                )
                
                // Rank Dropdown
                ExposedDropdownMenuBox(
                    expanded = showRankDropdown,
                    onExpandedChange = { showRankDropdown = it }
                ) {
                    OutlinedTextField(
                        value = if (rank.isNotEmpty()) ranks.find { it.first == rank }?.first ?: rank else "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rank") },
                        placeholder = { Text("Select rank") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRankDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showRankDropdown,
                        onDismissRequest = { showRankDropdown = false }
                    ) {
                        ranks.forEach { (rankName, _) ->
                            DropdownMenuItem(
                                text = { Text(rankName) },
                                onClick = {
                                    rank = rankName
                                    showRankDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Class Level Dropdown
                ExposedDropdownMenuBox(
                    expanded = showClassDropdown,
                    onExpandedChange = { showClassDropdown = it }
                ) {
                    OutlinedTextField(
                        value = classLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class Level") },
                        placeholder = { Text("Select class") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showClassDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showClassDropdown,
                        onDismissRequest = { showClassDropdown = false }
                    ) {
                        classLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    classLevel = level
                                    showClassDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Contact Number (Numeric keyboard)
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() } && it.length <= 11) {
                            contactNumber = it
                            contactError = if (it.isNotBlank() && !validatePhoneNumber(it)) {
                                when {
                                    !it.startsWith("09") -> "Must start with 09"
                                    it.length < 11 -> "Need ${11 - it.length} more digit(s)"
                                    it.length > 11 -> "Too long (max 11 digits)"
                                    else -> "Invalid format"
                                }
                            } else null
                        }
                    },
                    label = { Text("Contact Number") },
                    placeholder = { Text("09XXXXXXXXX") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, null, tint = ElectricBlue)
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = contactError != null,
                    supportingText = {
                        if (contactError != null) {
                            Text(
                                text = contactError!!,
                                color = ErrorRed,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "Format: 09XXXXXXXXX (11 digits)",
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor,
                        errorBorderColor = ErrorRed
                    )
                )
                
                // Preview of full rank
                if (rank.isNotEmpty() && classLevel.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ElectricBlue.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Full Rank Preview:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val abbreviation = ranks.find { it.first == rank }?.second ?: ""
                            Text(
                                text = "$abbreviation. $firstName $classLevel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank() && rank.isNotBlank() && classLevel.isNotBlank()) {
                        val abbreviation = ranks.find { it.first == rank }?.second ?: ""
                        val fullRank = "$abbreviation. $firstName $classLevel"
                        onConfirm(firstName, lastName, badgeNumber, fullRank, contactNumber)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                ),
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && rank.isNotBlank() && classLevel.isNotBlank()
            ) {
                Text("Add Officer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = CardBackground
    )
}
