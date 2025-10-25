package com.example.blottermanagementsystem.ui.screens.voice

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTextScreen(
    onNavigateBack: () -> Unit,
    onTextGenerated: (String) -> Unit
) {
    var transcribedText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            transcribedText = matches?.firstOrNull() ?: ""
        }
        isListening = false
    }
    
    fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to describe the incident...")
        }
        isListening = true
        speechRecognizerLauncher.launch(intent)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Voice-to-Text",
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
            // Info Card
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = InfoBlue
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Dictate Your Report",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Speak clearly to describe the incident",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Microphone Button
            FloatingActionButton(
                onClick = { startVoiceRecognition() },
                modifier = Modifier.size(120.dp),
                containerColor = if (isListening) ErrorRed else ElectricBlue
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Record",
                    modifier = Modifier.size(64.dp),
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                if (isListening) "Listening..." else "Tap to speak",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isListening) ErrorRed else ElectricBlue
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Transcribed Text
            if (transcribedText.isNotEmpty()) {
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
                            Text(
                                "Transcribed Text",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { transcribedText = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = ErrorRed
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            transcribedText,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { onTextGenerated(transcribedText) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use This Text")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Tips Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Tips for Better Recognition",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Speak clearly and at a moderate pace\n" +
                        "• Minimize background noise\n" +
                        "• Use proper punctuation words\n" +
                        "• Pause briefly between sentences",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
