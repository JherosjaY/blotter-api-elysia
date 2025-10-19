package com.example.blottermanagementsystem.ui.screens.evidence

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.blottermanagementsystem.data.entity.Evidence
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEvidenceScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit = {},
    onSaveSuccess: () -> Unit,
    caseNumber: String = "",
    viewModel: DashboardViewModel = viewModel(),
    savedStateHandle: androidx.lifecycle.SavedStateHandle? = null
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val currentUserName = "${preferencesManager.firstName} ${preferencesManager.lastName}"
    val userRole = preferencesManager.role
    val scope = rememberCoroutineScope()
    
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Physical") }
    var location by remember { mutableStateOf("") }
    var collectedBy by remember { mutableStateOf(preferencesManager.firstName ?: "") }
    var chainOfCustody by remember { mutableStateOf("") }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf<Uri?>(null) }
    
    val evidenceTypes = listOf("Physical", "Digital", "Documentary", "Testimonial", "Photographic", "Video", "Other")
    
    // Listen for captured image from camera
    LaunchedEffect(Unit) {
        savedStateHandle?.getStateFlow("captured_image_uri", "")?.collect { uriString ->
            if (uriString.isNotEmpty()) {
                val uri = Uri.parse(uriString)
                if (!photoUris.contains(uri)) {
                    photoUris = photoUris + uri
                }
                // Clear the saved state
                savedStateHandle.set("captured_image_uri", "")
            }
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        photoUris = photoUris + uris
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add Evidence",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = ElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
            )
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
            // Info Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = InfoBlue.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = InfoBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "Evidence Documentation",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InfoBlue
                        )
                        Text(
                            "Properly document all evidence for chain of custody",
                            fontSize = 12.sp,
                            color = InfoBlue.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Photo/Video Section
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Photos/Videos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    // Photo Grid
                    if (photoUris.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(photoUris) { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            1.dp,
                                            DividerColor,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { showFullImage = uri }
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Evidence photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { photoUris = photoUris - uri },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                ErrorRed.copy(alpha = 0.9f),
                                                RoundedCornerShape(4.dp)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            "Remove",
                                            tint = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Add Photo Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToCamera() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ElectricBlue
                            )
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Take Photo")
                        }
                        
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ElectricBlue
                            )
                        ) {
                            Icon(
                                Icons.Default.Image,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Gallery")
                        }
                    }
                }
            }
            
            // Evidence Type
            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Evidence Type *") },
                    leadingIcon = {
                        Icon(Icons.Default.Category, null, tint = ElectricBlue)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    evidenceTypes.forEach { evidenceType ->
                        DropdownMenuItem(
                            text = { Text(evidenceType) },
                            onClick = {
                                type = evidenceType
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Evidence Description *") },
                placeholder = { Text("Detailed description of the evidence") },
                leadingIcon = {
                    Icon(Icons.Default.Description, null, tint = ElectricBlue)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Location Found
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location Found *") },
                placeholder = { Text("Where was this evidence found?") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, null, tint = ElectricBlue)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Collected By
            OutlinedTextField(
                value = collectedBy,
                onValueChange = { collectedBy = it },
                label = { Text("Collected By *") },
                placeholder = { Text("Officer name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, null, tint = ElectricBlue)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Chain of Custody Notes
            OutlinedTextField(
                value = chainOfCustody,
                onValueChange = { chainOfCustody = it },
                label = { Text("Chain of Custody Notes") },
                placeholder = { Text("Additional custody information (optional)") },
                leadingIcon = {
                    Icon(Icons.Default.Link, null, tint = ElectricBlue)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Save Button
            Button(
                onClick = {
                    if (description.isBlank() || location.isBlank() || collectedBy.isBlank()) {
                        return@Button
                    }
                    
                    isLoading = true
                    scope.launch {
                        val evidence = Evidence(
                            blotterReportId = reportId,
                            evidenceType = type,
                            description = description,
                            locationFound = location,
                            collectedBy = collectedBy,
                            chainOfCustodyNotes = chainOfCustody.ifBlank { null },
                            photoUris = photoUris.joinToString(",") { it.toString() },
                            capturedBy = preferencesManager.firstName,
                            captureTimestamp = System.currentTimeMillis()
                        )
                        
                        viewModel.addEvidence(evidence, currentUserName, caseNumber, userRole)
                        isLoading = false
                        onSaveSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = androidx.compose.ui.graphics.Color.White
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Save Evidence",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
    
    // Full Image Dialog
    showFullImage?.let { uri ->
        AlertDialog(
            onDismissRequest = { showFullImage = null },
            confirmButton = {
                TextButton(onClick = { showFullImage = null }) {
                    Text("Close", color = ElectricBlue)
                }
            },
            text = {
                AsyncImage(
                    model = uri,
                    contentDescription = "Full size evidence photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            },
            containerColor = CardBackground
        )
    }
}
