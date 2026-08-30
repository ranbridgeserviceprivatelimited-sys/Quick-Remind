package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MemoryItem
import com.example.ui.theme.*

data class GraphNode(
    val id: String,
    val label: String,
    val type: String, // "CENTRAL", "PROJECT", "PERSON", "HARDWARE", "TASK"
    val xRatio: Float,
    val yRatio: Float,
    val color: Color
)

data class GraphEdge(
    val fromId: String,
    val toId: String
)

@Composable
fun KnowledgeGraphView(
    memories: List<MemoryItem>,
    modifier: Modifier = Modifier
) {
    var selectedNode by remember { mutableStateOf<GraphNode?>(null) }

    val nodes = remember {
        listOf(
            GraphNode("flight", "Flight Controller", "PROJECT", 0.5f, 0.45f, CyberViolet),
            GraphNode("stm32", "STM32 MCU", "HARDWARE", 0.22f, 0.25f, CyberCyan),
            GraphNode("mpu", "MPU6000 IMU", "HARDWARE", 0.78f, 0.22f, CyberEmerald),
            GraphNode("ravi", "Ravi (Mentor)", "PERSON", 0.20f, 0.70f, CyberAmber),
            GraphNode("gps", "GPS Module", "HARDWARE", 0.50f, 0.85f, CyberRose),
            GraphNode("cdac", "C-DAC Review", "PROJECT", 0.82f, 0.65f, CyberVioletLight)
        )
    }

    val edges = remember {
        listOf(
            GraphEdge("flight", "stm32"),
            GraphEdge("flight", "mpu"),
            GraphEdge("flight", "gps"),
            GraphEdge("flight", "ravi"),
            GraphEdge("ravi", "gps"),
            GraphEdge("flight", "cdac")
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hub, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Connected Knowledge Graph", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = CircleShape
            ) {
                Text(
                    "6 Linked Entities",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Quick Remind automatically correlates related memories, hardware specs, team members, and tasks.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IndigoDeepBg)
        ) {
            // Draw connecting links
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                edges.forEach { edge ->
                    val fromNode = nodes.find { it.id == edge.fromId }
                    val toNode = nodes.find { it.id == edge.toId }
                    if (fromNode != null && toNode != null) {
                        val start = Offset(fromNode.xRatio * w, fromNode.yRatio * h)
                        val end = Offset(toNode.xRatio * w, toNode.yRatio * h)
                        drawLine(
                            brush = Brush.linearGradient(listOf(fromNode.color.copy(alpha = 0.6f), toNode.color.copy(alpha = 0.6f))),
                            start = start,
                            end = end,
                            strokeWidth = 2.5f
                        )
                    }
                }
            }

            // Draw clickable nodes
            nodes.forEach { node ->
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val xPos = (maxWidth.value * node.xRatio) - 45f
                    val yPos = (maxHeight.value * node.yRatio) - 20f

                    Surface(
                        color = node.color,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .offset(x = xPos.dp, y = yPos.dp)
                            .clickable { selectedNode = node }
                            .testTag("graph_node_${node.id}")
                    ) {
                        Text(
                            text = node.label,
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        if (selectedNode != null) {
            Spacer(Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = selectedNode!!.color)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("${selectedNode!!.label} [${selectedNode!!.type}]", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            when(selectedNode!!.id) {
                                "stm32" -> "STM32F405 microcontroller running at 168MHz. High priority memory linked to Flight Controller PCB."
                                "gps" -> "U-Blox NEO-M8N configured at 115200 baud with Ravi."
                                "ravi" -> "Mentor & collaborator for GPS integration and lab demo."
                                "mpu" -> "MPU6000 IMU connected via SPI at 10MHz."
                                "cdac" -> "Upcoming evaluation and milestone 2 review."
                                else -> "Central project entity with 5 active connections."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
