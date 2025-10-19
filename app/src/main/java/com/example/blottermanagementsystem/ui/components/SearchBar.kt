package com.example.blottermanagementsystem.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blottermanagementsystem.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(placeholder, color = TextSecondary) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = ElectricBlue
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextSecondary
                    )
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElectricBlue,
            unfocusedBorderColor = DividerColor,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = ElectricBlue
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithFilter(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    filterOptions: List<String>,
    onFilterSelected: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(placeholder, color = TextSecondary) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ElectricBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ElectricBlue
                ),
                singleLine = true
            )
            
            // Filter Button
            Box {
                OutlinedButton(
                    onClick = { showFilterMenu = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (selectedFilter != "All") ElectricBlue else TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    option,
                                    color = if (option == selectedFilter) ElectricBlue else TextPrimary
                                )
                            },
                            onClick = {
                                onFilterSelected(option)
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (option == selectedFilter) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = ElectricBlue
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Show active filter
        if (selectedFilter != "All") {
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = { onFilterSelected("All") },
                label = { Text("Filter: $selectedFilter") },
                leadingIcon = {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove filter",
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = ElectricBlue.copy(alpha = 0.1f),
                    labelColor = ElectricBlue,
                    leadingIconContentColor = ElectricBlue,
                    trailingIconContentColor = ElectricBlue
                )
            )
        }
    }
}
