package com.example.blottermanagementsystem.ui.screens.qr

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedQRScreen(
    reportId: Int,
    caseNumber: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(reportId) {
        qrBitmap = QRCodeGenerator.generateReportQRCode(reportId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Case QR Code",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    Text(
                        caseNumber,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // QR Code
                    qrBitmap?.let { bitmap ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = androidx.compose.ui.graphics.Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .size(280.dp)
                                    .padding(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "Scan this QR code to quickly access case details",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Button(
                onClick = {
                    qrBitmap?.let { bitmap ->
                        scope.launch {
                            saveQRToGallery(context, bitmap, caseNumber)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Gallery")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = {
                    qrBitmap?.let { bitmap ->
                        scope.launch {
                            shareQRCode(context, bitmap, caseNumber)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share QR Code")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = InfoBlue.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "QR Code Uses",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Quick case access\n" +
                        "• Print on summons\n" +
                        "• Attach to legal documents\n" +
                        "• Share with involved parties\n" +
                        "• Evidence tracking",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Save QR Code to Gallery
suspend fun saveQRToGallery(context: android.content.Context, bitmap: Bitmap, caseNumber: String) {
    withContext(Dispatchers.IO) {
        try {
            val filename = "QR_${caseNumber}_${System.currentTimeMillis()}.png"
            var fos: OutputStream? = null
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (Scoped Storage)
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BlotterQR")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                // Android 9 and below
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val blotterDir = File(imagesDir, "BlotterQR")
                if (!blotterDir.exists()) {
                    blotterDir.mkdirs()
                }
                val image = File(blotterDir, filename)
                fos = FileOutputStream(image)
            }
            
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "QR Code saved to Gallery!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Share QR Code
suspend fun shareQRCode(context: android.content.Context, bitmap: Bitmap, caseNumber: String) {
    withContext(Dispatchers.IO) {
        try {
            // Save to cache directory
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "QR_${caseNumber}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            
            // Get URI using FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            withContext(Dispatchers.Main) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, "QR Code for Case: $caseNumber")
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Print QR Code
fun printQRCode(context: android.content.Context, bitmap: Bitmap, caseNumber: String) {
    try {
        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as PrintManager
        
        // Create a WebView to hold the QR code
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Create print adapter
                val printAdapter = webView.createPrintDocumentAdapter("QR_$caseNumber")
                
                // Print
                printManager.print(
                    "QR Code - $caseNumber",
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        
        // Convert bitmap to base64 and load in WebView
        val baos = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()
        val imageString = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
        
        val html = """
            <html>
            <head>
                <style>
                    body { 
                        text-align: center; 
                        padding: 20px;
                        font-family: Arial, sans-serif;
                    }
                    h1 { color: #1976D2; }
                    img { 
                        max-width: 400px; 
                        border: 2px solid #ddd;
                        padding: 10px;
                        margin: 20px auto;
                    }
                </style>
            </head>
            <body>
                <h1>Case QR Code</h1>
                <h2>$caseNumber</h2>
                <img src="data:image/png;base64,$imageString" />
                <p>Scan this QR code to access case details</p>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        
    } catch (e: Exception) {
        Toast.makeText(context, "Print failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
