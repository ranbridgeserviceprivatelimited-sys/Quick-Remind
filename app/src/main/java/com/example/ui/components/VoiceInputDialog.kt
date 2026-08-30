package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun VoiceInputDialog(
    isOpen: Boolean,
    isProcessing: Boolean,
    feedbackText: String?,
    onDismiss: () -> Unit,
    onProcessCommand: (String) -> Unit
) {
    if (!isOpen) return

    var transcribedText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Pulsing animation for mic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micPulse"
    )

    // Speech to text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val spokenMatches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            spokenMatches?.firstOrNull()?.let { spoken ->
                transcribedText = spoken
                onProcessCommand(spoken)
            }
        }
    }

    val samplePhrases = listOf(
        "\"Repu morning 8 ki college ki vellali ani remind cheyyi\"",
        "\"Remember STM32 microcontroller is used in flight controller\"",
        "\"Next Monday 9 AM project meeting with C-DAC\"",
        "\"Future idea: AI based agriculture drone with multispectral camera\"",
        "\"What microcontroller am I using in flight controller?\""
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        confirmButton = {
            Button(
                onClick = {
                    if (transcribedText.isNotBlank()) {
                        onProcessCommand(transcribedText)
                    }
                },
                enabled = transcribedText.isNotBlank() && !isProcessing,
                modifier = Modifier.testTag("submit_voice_button")
            ) {
                Text(if (isProcessing) "AI Thinking..." else "Process Command")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("🎙️ Tell Quick Remind", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Speak naturally in English, Telugu, or Tanglish (Telugu+English mixed). Quick Remind will automatically detect memories, reminders, or questions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Large Glowing Mic Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(if (isProcessing) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(SleekNavyPrimary.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                    )

                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell Quick Remind anything...")
                                }
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                // Direct text fallback
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SleekNavyPrimary)
                            .testTag("record_mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record voice",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                if (isProcessing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("AI is understanding & remembering...", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Or Type Directly
                OutlinedTextField(
                    value = transcribedText,
                    onValueChange = { transcribedText = it },
                    label = { Text("Or Type Command / Speech Text") },
                    placeholder = { Text("e.g. Repu 8 ki college undi...") },
                    modifier = Modifier.fillMaxWidth().testTag("voice_text_input"),
                    trailingIcon = {
                        if (transcribedText.isNotEmpty()) {
                            IconButton(onClick = { transcribedText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )

                if (!feedbackText.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(feedbackText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Sample clickables
                Text("Try saying or tapping one:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                samplePhrases.forEach { sample ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clean = sample.replace("\"", "")
                                transcribedText = clean
                                onProcessCommand(clean)
                            }
                    ) {
                        Text(
                            text = sample,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    )
}
