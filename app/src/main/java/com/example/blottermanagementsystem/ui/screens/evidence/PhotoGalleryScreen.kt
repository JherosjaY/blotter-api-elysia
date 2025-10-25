package com.example.blottermanagementsystem.ui.screens.evidence

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.blottermanagementsystem.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGalleryScreen(
    reportId: Int,
    photoUris: List<String>,
    onNavigateBack: () -> Unit,
    onAddPhoto: () -> Unit
) {
    var selectedPhoto by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Evidence Photos (${photoUris.size})",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                actions = {
                    IconButton(onClick = onAddPhoto) {
                        Icon(Icons.Default.Add, "Add Photo", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (photoUris.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Photo,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No photos yet",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAddPhoto,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Photos")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photoUris) { uri ->
                    PhotoThumbnail(
                        uri = uri,
                        onClick = { selectedPhoto = uri }
                    )
                }
            }
        }
        
        selectedPhoto?.let { uri ->
            PhotoViewerDialog(
                uri = uri,
                onDismiss = { selectedPhoto = null }
            )
        }
    }
}

@Composable
fun PhotoThumbnail(
    uri: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Evidence Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(
                    Icons.Default.ZoomIn,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp),
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

@Composable
fun PhotoViewerDialog(
    uri: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        text = {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Full Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                contentScale = ContentScale.Fit
            )
        }
    )
}
