package com.example.blottermanagementsystem.ui.screens.reports

import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.blottermanagementsystem.ui.components.FullscreenLoadingDialog
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReportScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onNavigateToCamera: () -> Unit = {},
    savedStateHandle: androidx.lifecycle.SavedStateHandle? = null,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }
    
    // Get the report to edit
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    val report = allReports.find { it.id == reportId }
    
    // State variables - will be populated when report loads
    var complainantName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contactError by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }
    var incidentType by remember { mutableStateOf("") }
    var incidentLocation by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var respondentName by remember { mutableStateOf("") }
    var respondentAddress by remember { mutableStateOf("") }
    
    // Evidence states
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var videoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var videoDurations by remember { mutableStateOf<Map<Uri, Long>>(emptyMap()) }
    
    // Load report data when available
    LaunchedEffect(report) {
        report?.let {
            complainantName = it.complainantName
            contactNumber = it.complainantContact
            address = it.complainantAddress
            incidentType = it.incidentType
            incidentLocation = it.incidentLocation
            description = it.narrative
            respondentName = it.respondentName
            respondentAddress = it.respondentAddress
            
            // Load existing evidences
            imageUris = if (it.imageUris.isNotBlank()) {
                it.imageUris.split(",").filter { uri -> uri.isNotBlank() }.map { uri -> Uri.parse(uri) }
            } else emptyList()
            
            videoUris = if (it.videoUris.isNotBlank()) {
                it.videoUris.split(",").filter { uri -> uri.isNotBlank() }.map { uri -> Uri.parse(uri) }
            } else emptyList()
            
            videoDurations = if (it.videoDurations.isNotBlank()) {
                val durationMap = mutableMapOf<Uri, Long>()
                it.videoDurations.split(",").forEach { entry ->
                    if (entry.isNotBlank() && entry.contains(":")) {
                        val parts = entry.split(":", limit = 2)
                        if (parts.size == 2) {
                            try {
                                val uri = Uri.parse(parts[0])
                                val duration = parts[1].toLongOrNull() ?: 0L
                                durationMap[uri] = duration
                            } catch (e: Exception) {
                                // Skip invalid entries
                            }
                        }
                    }
                }
                durationMap
            } else emptyMap()
        }
    }
    
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Phone validation function
    fun validatePhoneNumber(phone: String): Boolean {
        val pattern = "^09\\d{9}$".toRegex()
        return pattern.matches(phone)
    }
    
    // Helper function to get video duration
    fun getVideoDuration(uri: Uri): Long {
        return try {
            val retriever = MediaMetadataRetriever()
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
            try {
                val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
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
    
    // Video Picker (max 5 videos)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newUris = uris.filter { it !in videoUris }
        val availableSlots = 5 - videoUris.size
        val urisToAdd = newUris.take(availableSlots)
        videoUris = videoUris + urisToAdd
        
        // Get durations for new videos
        urisToAdd.forEach { uri ->
            val duration = getVideoDuration(uri)
            videoDurations = videoDurations + (uri to duration)
        }
    }
    
    // Fullscreen loading dialog
    FullscreenLoadingDialog(
        message = "Updating Report...",
        show = isLoading
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Report") },
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
            // Case Number (Read-only)
            Text(
                "Case Number: ${report?.caseNumber}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
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
                onValueChange = { complainantName = it },
                label = { Text("Complainant Name") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { 
                    contactNumber = it
                    contactError = if (it.isNotBlank() && !validatePhoneNumber(it)) {
                        "Format: 09XXXXXXXXX (11 digits)"
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
                            text = "Format: 09XXXXXXXXX",
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
            
            OutlinedTextField(
                value = incidentType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Incident Type") },
                leadingIcon = { Icon(Icons.Default.Info, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = incidentLocation,
                onValueChange = { incidentLocation = it },
                label = { Text("Incident Location") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Narrative/Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                placeholder = { Text("Describe the incident in detail...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Respondent Information (Optional)
            Text(
                "Respondent Information (Optional)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )
            
            OutlinedTextField(
                value = respondentName,
                onValueChange = { respondentName = it },
                label = { Text("Respondent Name") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = WarningOrange) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = respondentAddress,
                onValueChange = { respondentAddress = it },
                label = { Text("Respondent Address") },
                leadingIcon = { Icon(Icons.Default.Home, null, tint = WarningOrange) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Evidence Section
            Text(
                "Evidence (Optional)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )
            
            // Evidence buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    enabled = imageUris.size < 5,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Gallery", fontSize = 11.sp, maxLines = 1)
                }
                
                OutlinedButton(
                    onClick = {
                        // Request camera permission first
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = imageUris.size < 5,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Camera", fontSize = 11.sp, maxLines = 1)
                }
                
                OutlinedButton(
                    onClick = { videoPickerLauncher.launch("video/*") },
                    modifier = Modifier.weight(1f),
                    enabled = videoUris.size < 5,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Videocam, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Videos", fontSize = 11.sp, maxLines = 1)
                }
            }
            
            // Display selected images with remove button
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
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Remove button
                            IconButton(
                                onClick = { imageUris = imageUris - uri },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(ErrorRed, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Display selected videos with remove button
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
                            val thumbnail = getVideoThumbnail(context, uri)
                            if (thumbnail != null) {
                                Image(
                                    bitmap = thumbnail.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(CardBackground, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Videocam, null, tint = ElectricBlue)
                                }
                            }
                            
                            // Play icon overlay
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(32.dp)
                            )
                            
                            // Remove button
                            IconButton(
                                onClick = {
                                    videoUris = videoUris - uri
                                    videoDurations = videoDurations - uri
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(ErrorRed, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            // Duration label
                            videoDurations[uri]?.let { duration ->
                                val seconds = (duration / 1000) % 60
                                val minutes = (duration / 1000) / 60
                                Text(
                                    text = String.format("%d:%02d", minutes, seconds),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Total count indicator
            if (imageUris.isNotEmpty() || videoUris.isNotEmpty()) {
                Text(
                    text = "Photos: ${imageUris.size}/5  •  Videos: ${videoUris.size}/5",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Update Button
            Button(
                onClick = {
                    if (validatePhoneNumber(contactNumber)) {
                        scope.launch {
                            isLoading = true
                            try {
                                // Convert evidence URIs to JSON strings
                                val imageUrisJson = imageUris.joinToString(",") { it.toString() }
                                val videoUrisJson = videoUris.joinToString(",") { it.toString() }
                                val videoDurationsJson = videoDurations.entries.joinToString(",") { 
                                    "${it.key}:${it.value}" 
                                }
                                
                                // Update the report
                                report?.let {
                                    val updatedReport = it.copy(
                                        complainantName = complainantName,
                                        complainantContact = contactNumber,
                                        complainantAddress = address,
                                        incidentLocation = incidentLocation,
                                        narrative = description,
                                        respondentName = respondentName.ifBlank { "N/A" },
                                        respondentAddress = respondentAddress.ifBlank { "N/A" },
                                        imageUris = imageUrisJson,
                                        videoUris = videoUrisJson,
                                        videoDurations = videoDurationsJson
                                    )
                                    val currentUserName = "${preferencesManager.firstName} ${preferencesManager.lastName}"
                                    viewModel.updateReport(updatedReport, currentUserName, report?.status)
                                }
                                
                                // Show loading for 2 seconds
                                delay(2000)
                                isLoading = false
                                showSuccessDialog = true
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Failed to update report"
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
                enabled = complainantName.isNotBlank() && 
                         contactNumber.isNotBlank() && 
                         validatePhoneNumber(contactNumber) &&
                         address.isNotBlank() &&
                         incidentLocation.isNotBlank() &&
                         description.isNotBlank()
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Report Updated!",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        text = "Case Number: ${report?.caseNumber}",
                        fontSize = 14.sp,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your blotter report has been successfully updated.",
                        fontSize = 14.sp,
                        color = TextSecondary
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
}
