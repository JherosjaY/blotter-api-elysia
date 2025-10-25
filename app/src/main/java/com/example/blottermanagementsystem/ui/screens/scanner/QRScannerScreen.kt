package com.example.blottermanagementsystem.ui.screens.scanner

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.blottermanagementsystem.ui.theme.*
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

@Composable
fun QRScannerScreen(
    onCodeScanned: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { context ->
                    DecoratedBarcodeView(context).apply {
                        // Use default viewfinder size and position
                        
                        decodeContinuous(object : BarcodeCallback {
                            override fun barcodeResult(result: BarcodeResult?) {
                                result?.text?.let { code ->
                                    scannedCode = code
                                    onCodeScanned(code)
                                }
                            }
                        })
                        resume()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Top Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkNavy.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan QR Code",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            
            // Instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkNavy.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Position the QR code within the frame",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "The scanner will automatically detect the code",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    scannedCode?.let { code ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Scanned: $code",
                                fontSize = 14.sp,
                                color = SuccessGreen,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkNavy),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        text = "Camera permission required",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue
                        )
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}
