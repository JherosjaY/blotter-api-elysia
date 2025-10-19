package com.example.blottermanagementsystem.ui.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.data.entity.Officer
import com.example.blottermanagementsystem.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignOfficersDialog(
    caseNumber: String,
    availableOfficers: List<Officer>,
    currentlyAssignedIds: List<Int>,
    onDismiss: () -> Unit,
    onAssign: (List<Int>) -> Unit
) {
    var selectedOfficerIds by remember { mutableStateOf(currentlyAssignedIds.toSet()) }
    val maxOfficers = 2
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Assign Officers",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Case: $caseNumber",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f)),
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
                            tint = InfoBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Select up to $maxOfficers officers",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = InfoBlue
                            )
                            Text(
                                text = "${selectedOfficerIds.size}/$maxOfficers selected",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                // Officers List
                if (availableOfficers.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOff,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No available officers",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableOfficers) { officer ->
                            val isSelected = selectedOfficerIds.contains(officer.id)
                            val canSelect = selectedOfficerIds.size < maxOfficers || isSelected
                            
                            OfficerSelectionCard(
                                officer = officer,
                                isSelected = isSelected,
                                canSelect = canSelect,
                                onClick = {
                                    selectedOfficerIds = if (isSelected) {
                                        selectedOfficerIds - officer.id
                                    } else if (canSelect) {
                                        selectedOfficerIds + officer.id
                                    } else {
                                        selectedOfficerIds
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAssign(selectedOfficerIds.toList()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                ),
                enabled = selectedOfficerIds.isNotEmpty()
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Assign (${selectedOfficerIds.size})")
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

@Composable
private fun OfficerSelectionCard(
    officer: Officer,
    isSelected: Boolean,
    canSelect: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canSelect || isSelected, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ElectricBlue.copy(alpha = 0.2f) else SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) ElectricBlue.copy(alpha = 0.3f) else InfoBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isSelected) ElectricBlue else InfoBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Officer Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = officer.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) ElectricBlue else TextPrimary
                )
                Text(
                    text = officer.rank,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${officer.assignedCases} cases",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }
            
            // Selection Indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
            } else if (!canSelect) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Max reached",
                    tint = TextTertiary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Not selected",
                    tint = TextTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
