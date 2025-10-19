package com.example.blottermanagementsystem.ui.screens.legal

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blottermanagementsystem.utils.DateUtils
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.KPForm
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.LegalDocumentsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KPFormsScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    viewModel: LegalDocumentsViewModel = viewModel()
) {
    val kpForms by viewModel.getKPFormsByReportId(reportId).collectAsState(initial = emptyList())
    var showFormSelector by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KP Forms", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { showFormSelector = true }) {
                        Icon(Icons.Default.Add, "Generate Form", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFormSelector = true },
                containerColor = ElectricBlue
            ) {
                Icon(Icons.Default.Add, "Generate Form")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Form Type Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Available KP Forms",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• KP-7: Notice of Hearing\n• KP-10: Summons\n• KP-16: Amicable Settlement\n• KP-18: Certificate to File Action",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            if (kpForms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No KP forms generated yet", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(kpForms) { form ->
                        KPFormCard(form)
                    }
                }
            }
        }
    }
    
    if (showFormSelector) {
        FormSelectorDialog(
            reportId = reportId,
            onDismiss = { showFormSelector = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun KPFormCard(form: KPForm) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        form.formType,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Text(
                        form.formTitle,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                
                Surface(
                    color = when (form.status) {
                        "Issued" -> SuccessGreen.copy(alpha = 0.2f)
                        "Draft" -> WarningYellow.copy(alpha = 0.2f)
                        "Signed" -> InfoBlue.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        form.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = when (form.status) {
                            "Issued" -> SuccessGreen
                            "Draft" -> WarningYellow
                            "Signed" -> InfoBlue
                            else -> Color.Gray
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Form #: ${form.formNumber}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Text(
                "Issued by: ${form.issuedBy}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Text(
                "Date: ${DateUtils.formatDate(form.issueDate)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSelectorDialog(
    reportId: Int,
    onDismiss: () -> Unit,
    viewModel: LegalDocumentsViewModel
) {
    var selectedFormType by remember { mutableStateOf("KP-7") }
    var issuedBy by remember { mutableStateOf("") }
    var createdBy by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val formTypes = listOf(
        "KP-7" to "Notice of Hearing",
        "KP-10" to "Summons",
        "KP-16" to "Amicable Settlement",
        "KP-18" to "Certificate to File Action"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate KP Form", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select Form Type:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                
                formTypes.forEach { (type, title) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFormType == type,
                            onClick = { selectedFormType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(type, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(title, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
                
                OutlinedTextField(
                    value = issuedBy,
                    onValueChange = { issuedBy = it },
                    label = { Text("Issued By") },
                    placeholder = { Text("Punong Barangay Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = createdBy,
                    onValueChange = { createdBy = it },
                    label = { Text("Created By") },
                    placeholder = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        val formTitle = formTypes.find { it.first == selectedFormType }?.second ?: ""
                        viewModel.createKPForm(
                            blotterReportId = reportId,
                            formType = selectedFormType,
                            formTitle = formTitle,
                            issuedBy = issuedBy,
                            createdBy = createdBy
                        )
                        isLoading = false
                        onDismiss()
                    }
                },
                enabled = !isLoading && issuedBy.isNotBlank() && createdBy.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Generate")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

