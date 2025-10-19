package com.example.blottermanagementsystem.ui.screens.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.blottermanagementsystem.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                context = context,
                lifecycleOwner = lifecycleOwner,
                lensFacing = lensFacing,
                flashMode = flashMode,
                onImageCaptured = onImageCaptured
            )
            
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(DarkNavy.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                            else -> ImageCapture.FLASH_MODE_OFF
                        }
                    },
                    modifier = Modifier
                        .background(DarkNavy.copy(alpha = 0.7f), CircleShape)
                ) {
                    Text(
                        text = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> "⚡"
                            ImageCapture.FLASH_MODE_AUTO -> "✨"
                            else -> "🔦"
                        },
                        fontSize = 24.sp
                    )
                }
            }
            
            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flip Camera
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(DarkNavy.copy(alpha = 0.7f), CircleShape)
                ) {
                    Text(
                        text = "🔄",
                        fontSize = 32.sp
                    )
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
                Text(
                    text = "📷",
                    fontSize = 80.sp
                )
                Text(
                    text = "Camera permission required",
                    color = TextPrimary
                )
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    lensFacing: Int,
    flashMode: Int,
    onImageCaptured: (Uri) -> Unit
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    
    LaunchedEffect(lensFacing, flashMode) {
        val cameraProvider = cameraProviderFuture.get()
        
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        
        imageCapture = ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build()
        
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        // Capture Button
        IconButton(
            onClick = {
                val photoFile = File(
                    context.cacheDir,
                    SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                        .format(System.currentTimeMillis()) + ".jpg"
                )
                
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                
                imageCapture?.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            onImageCaptured(Uri.fromFile(photoFile))
                        }
                        
                        override fun onError(exc: ImageCaptureException) {
                            exc.printStackTrace()
                        }
                    }
                )
            },
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .background(ElectricBlue, CircleShape)
                .border(4.dp, TextPrimary, CircleShape)
        ) {
            Text(
                text = "📸",
                fontSize = 40.sp
            )
        }
    }
}
