package com.example.blottermanagementsystem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.blottermanagementsystem.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateStatusDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onUpdateStatus: (String, String) -> Unit // status, notes
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var notes by remember { mutableStateOf("") }
    
    val statuses = listOf(
        "Pending" to WarningOrange,
        "Under Investigation" to InfoBlue,
        "Resolved" to SuccessGreen
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Update,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Update Case Status",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Current Status
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Current: $currentStatus",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // Status Options
                Text(
                    text = "Select New Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                statuses.forEach { (status, color) ->
                    val isSelected = selectedStatus == status
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = status },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) color.copy(alpha = 0.2f) else SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (status) {
                                        "Pending" -> Icons.Default.Schedule
                                        "Under Investigation" -> Icons.Default.Search
                                        "Resolved" -> Icons.Default.CheckCircle
                                        else -> Icons.Default.Circle
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) color else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = status,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else TextPrimary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                
                // Notes
                Text(
                    text = "Add Notes (Optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Enter investigation notes or findings...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdateStatus(selectedStatus, notes) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                ),
                enabled = selectedStatus != currentStatus
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ElectricBlue)
            }
        },
        containerColor = CardBackground
    )
}
