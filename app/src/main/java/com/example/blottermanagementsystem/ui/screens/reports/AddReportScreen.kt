package com.example.blottermanagementsystem.ui.screens.reports

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import java.io.File
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.data.entity.Person
import com.example.blottermanagementsystem.data.entity.PersonActivityType
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.viewmodel.PersonViewModel
import com.example.blottermanagementsystem.viewmodel.RespondentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun getVideoThumbnail(context: android.content.Context, uri: Uri): android.graphics.Bitmap? {
    return remember(uri) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddReportScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onNavigateToCamera: () -> Unit = {},
    savedStateHandle: androidx.lifecycle.SavedStateHandle? = null,
    viewModel: DashboardViewModel = viewModel(),
    personViewModel: PersonViewModel = viewModel(),
    respondentViewModel: RespondentViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }
    val currentUserId = preferencesManager.userId
    
    var caseNumber by remember { mutableStateOf("BLT-${System.currentTimeMillis()}") }
    
    // Complainant Info
    var complainantName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contactError by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }
    
    // Incident Details
    var incidentType by remember { mutableStateOf("") }
    var showIncidentTypeDropdown by remember { mutableStateOf(false) }
    val incidentTypes = listOf(
        "Theft",
        "Assault",
        "Robbery",
        "Vandalism",
        "Fraud",
        "Harassment",
        "Threatening",
        "Bullying",
        "Trespassing",
        "Domestic Violence",
        "Physical Injury",
        "Verbal Abuse",
        "Property Damage",
        "Noise Complaint",
        "Boundary Dispute",
        "Illegal Gambling",
        "Public Disturbance",
        "Child Abuse",
        "Neglect",
        "Scam/Estafa",
        "Cyberbullying",
        "Stalking",
        "Illegal Drugs",
        "Drunk and Disorderly",
        "Animal Complaint",
        "Other"
    )
    
    var incidentDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Only allow dates up to today
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )
    
    var incidentTime by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    var incidentLocation by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Respondent Info
    var respondentName by remember { mutableStateOf("") }
    var respondentAlias by remember { mutableStateOf("") }
    var respondentAddress by remember { mutableStateOf("") }
    var respondentContact by remember { mutableStateOf("") }
    var respondentContactError by remember { mutableStateOf<String?>(null) }
    var accusation by remember { mutableStateOf("") }
    var relationshipToComplainant by remember { mutableStateOf("") }
    
    // Error states for required fields
    var showErrors by remember { mutableStateOf(false) }
    var showRelationshipDropdown by remember { mutableStateOf(false) }
    val relationships = listOf("Neighbor", "Relative", "Friend", "Stranger", "Co-worker", "Other")
    var hasEvidenceNow by remember { mutableStateOf(false) }
    
    // BringIntoViewRequesters for scrolling to error fields
    val incidentTypeRequester = remember { BringIntoViewRequester() }
    val incidentDateRequester = remember { BringIntoViewRequester() }
    val incidentTimeRequester = remember { BringIntoViewRequester() }
    val incidentLocationRequester = remember { BringIntoViewRequester() }
    val descriptionRequester = remember { BringIntoViewRequester() }
    val respondentNameRequester = remember { BringIntoViewRequester() }
    val accusationRequester = remember { BringIntoViewRequester() }
    val relationshipRequester = remember { BringIntoViewRequester() }
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var showPersonSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by personViewModel.searchResults.collectAsState()
    
    // Evidence (max 5 total - images + videos combined)
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var videoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var videoDurations by remember { mutableStateOf<Map<Uri, Long>>(emptyMap()) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var showMediaDialog by remember { mutableStateOf(false) }
    var isVideoMedia by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Helper function to get video duration
    fun getVideoDuration(uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
            duration
        } catch (e: Exception) {
            0L
        }
    }
    // Image Picker (max 5 photos)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newUris = uris.filter { it !in imageUris }
        val availableSlots = 5 - imageUris.size
        val urisToAdd = newUris.take(availableSlots)
        imageUris = imageUris + urisToAdd
    }
    
    // Camera Launcher - uses device's native camera app
    val tempPhotoUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri.value?.let { uri ->
                if (imageUris.size < 5 && uri !in imageUris) {
                    imageUris = imageUris + uri
                }
            }
        }
    }
    
    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch camera
            try {
                val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    photoFile
                )
                tempPhotoUri.value = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Error: ${e.message}")
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required")
            }
        }
    }
    
    // Video Picker (max 5 videos, check 2-min duration)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (it !in videoUris) { // Prevent duplicates
                val totalVideos = videoUris.size + 1
                if (totalVideos <= 5) {
                    val duration = getVideoDuration(it)
                    val durationInMinutes = duration / 1000 / 60
                    
                    if (duration <= 120000) { // 2 minutes = 120,000 ms
                        videoUris = videoUris + it
                        videoDurations = videoDurations + (it to duration)
                        errorMessage = null
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Video duration must be 2 minutes or less (selected: ${durationInMinutes}min)",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Maximum 5 videos allowed",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }
    
    // Phone validation function - Supports both 09 and +63 formats
    fun validatePhoneNumber(phone: String): Boolean {
        val pattern09 = "^09\\d{9}$".toRegex()  // 09XXXXXXXXX (11 digits)
        val pattern63 = "^\\+639\\d{9}$".toRegex()  // +639XXXXXXXXX (13 chars)
        val pattern639 = "^639\\d{9}$".toRegex()  // 639XXXXXXXXX (12 digits)
        return pattern09.matches(phone) || pattern63.matches(phone) || pattern639.matches(phone)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Blotter Report") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Case Number (Auto-generated)
            OutlinedTextField(
                value = caseNumber,
                onValueChange = { },
                label = { Text("Case Number") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = DividerColor,
                    disabledLabelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Complainant Information
            Text(
                "Complainant Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )
            
            OutlinedTextField(
                value = complainantName,
                onValueChange = { 
                    complainantName = it
                    if (showErrors) showErrors = false
                },
                label = { Text("Complainant Name") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                isError = showErrors && complainantName.isBlank(),
                supportingText = {
                    if (showErrors && complainantName.isBlank()) {
                        Text("Required", color = ErrorRed, fontSize = 12.sp)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue,
                    errorBorderColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { 
                    contactNumber = it
                    contactError = if (it.isNotBlank() && !validatePhoneNumber(it)) {
                        when {
                            !it.startsWith("09") -> "Must start with 09"
                            it.length < 11 -> "Need ${11 - it.length} more digit(s)"
                            it.length > 11 -> "Too long (max 11 digits)"
                            else -> "Invalid format"
                        }
                    } else null
                },
                label = { Text("Contact Number") },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = ElectricBlue) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                placeholder = { Text("09XXXXXXXXX") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue,
                    errorBorderColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                leadingIcon = { Icon(Icons.Default.Home, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Incident Details
            Text(
                "Incident Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )
            
            // Incident Type Dropdown
            ExposedDropdownMenuBox(
                expanded = showIncidentTypeDropdown,
                onExpandedChange = { showIncidentTypeDropdown = it },
                modifier = Modifier.bringIntoViewRequester(incidentTypeRequester)
            ) {
                OutlinedTextField(
                    value = incidentType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Incident Type") },
                    leadingIcon = { Icon(Icons.Default.Info, null, tint = ElectricBlue) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showIncidentTypeDropdown) },
                    isError = showErrors && incidentType.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = if (showErrors && incidentType.isBlank()) ErrorRed else DividerColor,
                        errorBorderColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showIncidentTypeDropdown,
                    onDismissRequest = { showIncidentTypeDropdown = false }
                ) {
                    incidentTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                incidentType = type
                                showIncidentTypeDropdown = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Picker
                Box(
                    modifier = Modifier
                        .weight(1.12f)
                        .clickable { showDatePicker = true }
                        .bringIntoViewRequester(incidentDateRequester)
                ) {
                    OutlinedTextField(
                        value = incidentDate,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Date") },
                        leadingIcon = { Icon(Icons.Default.DateRange, null, tint = ElectricBlue) },
                        isError = showErrors && incidentDate.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Select date") },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = if (showErrors && incidentDate.isBlank()) ErrorRed else DividerColor,
                            disabledLabelColor = TextPrimary,
                            disabledTextColor = TextPrimary,
                            disabledPlaceholderColor = TextSecondary,
                            disabledLeadingIconColor = ElectricBlue,
                            errorBorderColor = ErrorRed
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Time Picker
                Box(
                    modifier = Modifier
                        .weight(0.88f)
                        .clickable { showTimePicker = true }
                        .bringIntoViewRequester(incidentTimeRequester)
                ) {
                    OutlinedTextField(
                        value = incidentTime,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        label = { Text("Time") },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = ElectricBlue
                            )
                        },
                        isError = showErrors && incidentTime.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Select time") },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = if (showErrors && incidentTime.isBlank()) ErrorRed else DividerColor,
                            disabledLabelColor = TextPrimary,
                            disabledTextColor = TextPrimary,
                            disabledPlaceholderColor = TextSecondary,
                            disabledLeadingIconColor = ElectricBlue,
                            errorBorderColor = ErrorRed
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            // Date Picker Dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                                incidentDate = sdf.format(Date(millis))
                            }
                            showDatePicker = false
                        }) {
                            Text("OK", color = ElectricBlue)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = ElectricBlue)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            // Time Picker Dialog
            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val hour = timePickerState.hour
                            val minute = timePickerState.minute
                            // Convert to 12-hour format with AM/PM
                            val period = if (hour < 12) "AM" else "PM"
                            val hour12 = when {
                                hour == 0 -> 12
                                hour > 12 -> hour - 12
                                else -> hour
                            }
                            incidentTime = String.format("%02d:%02d %s", hour12, minute, period)
                            showTimePicker = false
                        }) {
                            Text("OK", color = ElectricBlue)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel", color = ElectricBlue)
                        }
                    },
                    text = {
                        TimePicker(state = timePickerState)
                    }
                )
            }
            
            OutlinedTextField(
                value = incidentLocation,
                onValueChange = { incidentLocation = it },
                label = { Text("Incident Location") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = ElectricBlue) },
                isError = showErrors && incidentLocation.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(incidentLocationRequester),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = if (showErrors && incidentLocation.isBlank()) ErrorRed else DividerColor,
                    cursorColor = ElectricBlue,
                    errorBorderColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description/Narrative") },
                isError = showErrors && description.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .bringIntoViewRequester(descriptionRequester),
                maxLines = 6,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                placeholder = { Text("Describe the incident in detail...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = if (showErrors && description.isBlank()) ErrorRed else DividerColor,
                    cursorColor = ElectricBlue,
                    errorBorderColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Respondent Information
            Text(
                "Respondent Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )
            
            OutlinedTextField(
                value = respondentName,
                onValueChange = { 
                    respondentName = it
                    if (showErrors) showErrors = false
                },
                label = { Text("Respondent Name") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = WarningOrange) },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(respondentNameRequester),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                placeholder = { Text("Full name or leave blank if unknown") },
                isError = showErrors && respondentName.isBlank() && respondentAlias.isBlank(),
                supportingText = {
                    if (showErrors && respondentName.isBlank() && respondentAlias.isBlank()) {
                        Text("Enter name or alias", color = ErrorRed, fontSize = 12.sp)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarningOrange,
                    unfocusedBorderColor = if (showErrors && respondentName.isBlank() && respondentAlias.isBlank()) ErrorRed else DividerColor,
                    cursorColor = WarningOrange,
                    errorBorderColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = respondentAlias,
                onValueChange = { 
                    respondentAlias = it
                    if (showErrors) showErrors = false
                },
                label = { Text("Alias/Nickname (if real name unknown)") },
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = WarningOrange) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                placeholder = { Text("e.g., 'Boy Bato', 'Toto'") },
                isError = showErrors && respondentName.isBlank() && respondentAlias.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarningOrange,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = WarningOrange,
                    errorBorderColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = respondentAddress,
                onValueChange = { respondentAddress = it },
                label = { Text("Respondent Address (Optional)") },
                leadingIcon = { Icon(Icons.Default.Home, null, tint = WarningOrange) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarningOrange,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = WarningOrange
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = respondentContact,
                onValueChange = { 
                    respondentContact = it
                    respondentContactError = if (it.isNotBlank() && !validatePhoneNumber(it)) {
                        when {
                            !it.startsWith("09") -> "Must start with 09"
                            it.length < 11 -> "Need ${11 - it.length} more digit(s)"
                            it.length > 11 -> "Too long (max 11 digits)"
                            else -> "Invalid format"
                        }
                    } else null
                },
                label = { Text("Respondent Contact (Optional)") },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = WarningOrange) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("09XXXXXXXXX") },
                isError = respondentContactError != null,
                supportingText = {
                    if (respondentContactError != null) {
                        Text(
                            text = respondentContactError!!,
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
                    cursorColor = ElectricBlue,
                    errorBorderColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // Accusation
            OutlinedTextField(
                value = accusation,
                onValueChange = { newValue ->
                    // Capitalize first letter
                    accusation = newValue.replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                    }
                },
                label = { Text("Accusation") },
                leadingIcon = { Icon(Icons.Default.Warning, null, tint = WarningOrange) },
                isError = showErrors && accusation.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(accusationRequester),
                placeholder = { Text("What are they accused of?") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarningOrange,
                    unfocusedBorderColor = if (showErrors && accusation.isBlank()) ErrorRed else DividerColor,
                    cursorColor = WarningOrange,
                    errorBorderColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Relationship Dropdown
            ExposedDropdownMenuBox(
                expanded = showRelationshipDropdown,
                onExpandedChange = { showRelationshipDropdown = it },
                modifier = Modifier.bringIntoViewRequester(relationshipRequester)
            ) {
                OutlinedTextField(
                    value = relationshipToComplainant,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Relationship to Complainant") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRelationshipDropdown) },
                    isError = showErrors && relationshipToComplainant.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = if (showErrors && relationshipToComplainant.isBlank()) ErrorRed else DividerColor,
                        errorBorderColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showRelationshipDropdown,
                    onDismissRequest = { showRelationshipDropdown = false }
                ) {
                    relationships.forEach { relationship ->
                        DropdownMenuItem(
                            text = { Text(relationship) },
                            onClick = {
                                relationshipToComplainant = relationship
                                showRelationshipDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Evidence Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Do you have evidence now?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "If yes, upload photos/videos below",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hasEvidenceNow,
                        onCheckedChange = { hasEvidenceNow = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SuccessGreen,
                            checkedTrackColor = SuccessGreen.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            
            // Evidence Upload (only show if hasEvidenceNow is true)
            if (hasEvidenceNow) {
                Text(
                    "Evidence (Optional)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
            
            Text(
                "Upload max 5 photos and 5 videos (videos: max 2 min each)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Image Thumbnails
            if (imageUris.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(imageUris) { uri ->
                        Box(
                            modifier = Modifier.size(100.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Evidence Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, ElectricBlue, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMediaUri = uri
                                        isVideoMedia = false
                                        showMediaDialog = true
                                    },
                                contentScale = ContentScale.Crop
                            )
                            // Remove button (small X, no background)
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = ErrorRed,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clickable { imageUris = imageUris.filter { it != uri } }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Video Thumbnails with duration
            if (videoUris.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(videoUris) { uri ->
                        Box(
                            modifier = Modifier.size(100.dp)
                        ) {
                            // Video thumbnail
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, ElectricBlue, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMediaUri = uri
                                        isVideoMedia = true
                                        showMediaDialog = true
                                    }
                            ) {
                                // Thumbnail image from video
                                val thumbnail = getVideoThumbnail(context, uri)
                                if (thumbnail != null) {
                                    Image(
                                        bitmap = thumbnail.asImageBitmap(),
                                        contentDescription = "Video Thumbnail",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Fallback to black background if thumbnail fails
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black)
                                    )
                                }
                                // Play icon overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                // Duration label
                                videoDurations[uri]?.let { duration ->
                                    val seconds = (duration / 1000) % 60
                                    val minutes = (duration / 1000) / 60
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = String.format("%d:%02d", minutes, seconds),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            // Remove button (small X, no background)
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = ErrorRed,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clickable { 
                                        videoUris = videoUris.filter { it != uri }
                                        videoDurations = videoDurations - uri
                                    }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Upload Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        errorMessage = null
                        imagePickerLauncher.launch("image/*") 
                    },
                    modifier = Modifier.weight(1f),
                    enabled = imageUris.size < 5,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Gallery", fontSize = 11.sp, maxLines = 1)
                }
                
                // Camera Button - launches native camera app
                OutlinedButton(
                    onClick = { 
                        errorMessage = null
                        // Request camera permission first
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = imageUris.size < 5,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Camera", fontSize = 11.sp, maxLines = 1)
                }
                
                OutlinedButton(
                    onClick = { 
                        errorMessage = null
                        videoPickerLauncher.launch("video/*") 
                    },
                    modifier = Modifier.weight(1f),
                    enabled = videoUris.size < 5,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Videos", fontSize = 11.sp, maxLines = 1)
                }
            }
            
            // Total count indicator
            if (imageUris.isNotEmpty() || videoUris.isNotEmpty()) {
                Text(
                    text = "Photos: ${imageUris.size}/5  •  Videos: ${videoUris.size}/5",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            } // End of if (hasEvidenceNow)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    // Validate required fields
                    val hasRequiredFields = complainantName.isNotBlank() &&
                            contactNumber.isNotBlank() &&
                            validatePhoneNumber(contactNumber) &&
                            address.isNotBlank() &&
                            incidentType.isNotBlank() &&
                            incidentDate.isNotBlank() &&
                            incidentTime.isNotBlank() &&
                            incidentLocation.isNotBlank() &&
                            description.isNotBlank() &&
                            (respondentName.isNotBlank() || respondentAlias.isNotBlank()) &&
                            accusation.isNotBlank() &&
                            relationshipToComplainant.isNotBlank()
                    
                    if (!hasRequiredFields) {
                        showErrors = true
                        scope.launch {
                            // Scroll to first empty required field
                            when {
                                incidentType.isBlank() -> incidentTypeRequester.bringIntoView()
                                incidentDate.isBlank() -> incidentDateRequester.bringIntoView()
                                incidentTime.isBlank() -> incidentTimeRequester.bringIntoView()
                                incidentLocation.isBlank() -> incidentLocationRequester.bringIntoView()
                                description.isBlank() -> descriptionRequester.bringIntoView()
                                respondentName.isBlank() && respondentAlias.isBlank() -> respondentNameRequester.bringIntoView()
                                accusation.isBlank() -> accusationRequester.bringIntoView()
                                relationshipToComplainant.isBlank() -> relationshipRequester.bringIntoView()
                            }
                        }
                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // Validate respondent contact if provided
                    if (respondentContact.isNotBlank() && !validatePhoneNumber(respondentContact)) {
                        Toast.makeText(context, "Invalid respondent contact number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    if (validatePhoneNumber(contactNumber)) {
                        isLoading = true
                        scope.launch {
                            try {
                                // Step 1: Create/Find Person for Complainant
                                val nameParts = complainantName.split(" ", limit = 2)
                                val firstName = nameParts.getOrNull(0) ?: complainantName
                                val lastName = nameParts.getOrNull(1) ?: ""
                                
                                val complainantPersonId = personViewModel.findOrCreatePerson(
                                    firstName = firstName,
                                    lastName = lastName,
                                    contactNumber = contactNumber,
                                    address = address,
                                    personType = "Complainant"
                                ).toInt()
                                
                                // Step 2: Create/Find Person for Respondent
                                var respondentPersonId: Int? = null
                                val finalRespondentName = if (respondentName.isNotBlank()) {
                                    respondentName
                                } else {
                                    respondentAlias // Use alias if no real name
                                }
                                
                                if (finalRespondentName.isNotBlank()) {
                                    val respNameParts = finalRespondentName.split(" ", limit = 2)
                                    val respFirstName = respNameParts.getOrNull(0) ?: finalRespondentName
                                    val respLastName = respNameParts.getOrNull(1) ?: ""
                                    
                                    respondentPersonId = personViewModel.findOrCreatePerson(
                                        firstName = respFirstName,
                                        lastName = respLastName,
                                        contactNumber = respondentContact.ifBlank { "N/A" },
                                        address = respondentAddress.ifBlank { "Unknown" },
                                        personType = "Respondent"
                                    ).toInt()
                                }
                                
                                // Step 3: Convert evidence URIs to JSON strings
                                val imageUrisJson = imageUris.joinToString(",") { it.toString() }
                                val videoUrisJson = videoUris.joinToString(",") { it.toString() }
                                val videoDurationsJson = videoDurations.entries.joinToString(",") { 
                                    "${it.key}:${it.value}" 
                                }
                                
                                // Step 4: Create Blotter Report
                                val report = BlotterReport(
                                    caseNumber = caseNumber,
                                    complainantName = complainantName,
                                    complainantContact = contactNumber,
                                    complainantAddress = address,
                                    incidentType = incidentType,
                                    incidentDate = System.currentTimeMillis(),
                                    incidentTime = incidentTime,
                                    incidentLocation = incidentLocation,
                                    narrative = description,
                                    respondentName = respondentName.ifBlank { "N/A" },
                                    respondentAddress = respondentAddress.ifBlank { "N/A" },
                                    status = "Pending", // Always pending when filed, admin will assign to officer
                                    assignedOfficer = "",
                                    dateFiled = System.currentTimeMillis(),
                                    userId = currentUserId,
                                    imageUris = imageUrisJson,
                                    videoUris = videoUrisJson,
                                    videoDurations = videoDurationsJson
                                )
                                
                                val currentUserName = "${preferencesManager.firstName} ${preferencesManager.lastName}"
                                val reportId = viewModel.insertReport(report, currentUserName, currentUserId).toInt()
                                
                                // Step 5: Log Complainant Activity
                                personViewModel.logPersonActivity(
                                    personId = complainantPersonId,
                                    blotterReportId = reportId,
                                    activityType = PersonActivityType.FILED_REPORT,
                                    description = "Filed report: $incidentType"
                                )
                                
                                // Step 6: Create Respondent Record (if provided)
                                if (respondentPersonId != null && respondentName.isNotBlank()) {
                                    val respondentId = respondentViewModel.createRespondent(
                                        blotterReportId = reportId,
                                        personId = respondentPersonId,
                                        accusation = accusation.ifBlank { incidentType },
                                        relationshipToComplainant = relationshipToComplainant.ifBlank { "Unknown" },
                                        contactNumber = respondentContact.ifBlank { "N/A" },
                                        hasEvidence = hasEvidenceNow
                                    )
                                    
                                    // Step 7: Send SMS Notification (optional)
                                    if (respondentContact.isNotBlank() && validatePhoneNumber(respondentContact)) {
                                        try {
                                            respondentViewModel.sendInitialNotification(respondentId.toInt())
                                        } catch (e: Exception) {
                                            // SMS failed, but continue
                                        }
                                    }
                                }
                                
                                isLoading = false
                                showSuccessDialog = true
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Error: ${e.message}"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        } // End Column
    } // End Scaffold
    
    // Dialogs outside Scaffold but inside AddReportScreen
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50), // Green color
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Report Saved!",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Case Number: $caseNumber",
                        fontSize = 14.sp,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your blotter report has been successfully filed and is now pending review.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSaveSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue
                    )
                ) {
                    Text("Done")
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Media Viewer Dialog (Fullscreen)
    if (showMediaDialog && selectedMediaUri != null) {
        Dialog(
            onDismissRequest = { showMediaDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                if (isVideoMedia) {
                    // Video Player
                    val exoPlayer = remember {
                        ExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(selectedMediaUri!!))
                            prepare()
                            playWhenReady = true
                        }
                    }
                    
                    DisposableEffect(Unit) {
                        onDispose {
                            exoPlayer.release()
                        }
                    }
                    
                    AndroidView(
                        factory = { ctx ->
                            androidx.media3.ui.PlayerView(ctx).apply {
                                player = exoPlayer
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                useController = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Full image view
                    Image(
                        painter = rememberAsyncImagePainter(selectedMediaUri),
                        contentDescription = "Image Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showMediaDialog = false },
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Close button
                IconButton(
                    onClick = { showMediaDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
