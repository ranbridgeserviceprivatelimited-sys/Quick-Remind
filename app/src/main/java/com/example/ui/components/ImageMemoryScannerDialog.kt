package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ImageMemoryScannerDialog(
    isOpen: Boolean,
    isScanning: Boolean,
    onDismiss: () -> Unit,
    onScanBitmap: (Bitmap) -> Unit
) {
    if (!isOpen) return

    // Helper to generate sample image bitmap for emulator testing
    fun createSampleWhiteboardBitmap(type: String): Bitmap {
        val bitmap = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgPaint = Paint().apply {
            color = if (type == "WHITEBOARD") AndroidColor.parseColor("#F8FAFC") else AndroidColor.parseColor("#1E293B")
        }
        canvas.drawRect(0f, 0f, 600f, 400f, bgPaint)

        val textPaint = Paint().apply {
            color = if (type == "WHITEBOARD") AndroidColor.parseColor("#0F172A") else AndroidColor.parseColor("#F1F5F9")
            textSize = 24f
            isAntiAlias = true
        }

        if (type == "WHITEBOARD") {
            canvas.drawText("STM32 Flight Controller Hardware Notes:", 30f, 60f, textPaint)
            canvas.drawText("- Microcontroller: STM32F405 @ 168MHz", 30f, 110f, textPaint)
            canvas.drawText("- IMU Sensor: MPU6000 over SPI (CS=PB12)", 30f, 160f, textPaint)
            canvas.drawText("- Barometer: DPS310 over I2C1", 30f, 210f, textPaint)
            canvas.drawText("- GPS UART Baud: 115200", 30f, 260f, textPaint)
            canvas.drawText("TODO: Test gyro filter frequency response", 30f, 320f, textPaint)
        } else {
            canvas.drawText("AI Agriculture Drone Spec Sheet:", 30f, 60f, textPaint)
            canvas.drawText("- Multispectral sensor 5 bands (NIR/Red/Green)", 30f, 110f, textPaint)
            canvas.drawText("- Autonomous waypoint navigation", 30f, 160f, textPaint)
            canvas.drawText("- Target market: Precision nitrogen spraying", 30f, 210f, textPaint)
            canvas.drawText("- Milestone deadline: September 2026", 30f, 270f, textPaint)
        }
        return bitmap
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("📸 Visual Memory Scanner", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Quick Remind uses Gemini Multimodal Vision to extract facts, tasks, schematics, and text from whiteboards, receipts, documents, or sketches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isScanning) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(10.dp))
                            Text("Extracting knowledge & text from image...", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    Text("Select a sample or capture:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onScanBitmap(createSampleWhiteboardBitmap("WHITEBOARD")) }
                            .testTag("scan_whiteboard_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Scan Hardware Whiteboard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("STM32 pinouts, IMU SPI specs, Baro I2C", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onScanBitmap(createSampleWhiteboardBitmap("DRONE_SPEC")) }
                            .testTag("scan_drone_spec_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Scan Drone Spec Sheet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Multispectral camera & agriculture plan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
