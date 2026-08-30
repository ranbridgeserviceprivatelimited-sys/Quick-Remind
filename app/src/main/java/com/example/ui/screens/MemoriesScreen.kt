package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.components.KnowledgeGraphView
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MemoriesScreen(
    memories: List<MemoryItem>,
    searchQuery: String,
    selectedCategory: MemoryCategory?,
    selectedImportance: MemoryImportance?,
    onCategorySelect: (MemoryCategory?) -> Unit,
    onImportanceSelect: (MemoryImportance?) -> Unit,
    onAddMemoryClick: () -> Unit,
    onDeleteMemory: (MemoryItem) -> Unit,
    onTogglePermanent: (MemoryItem) -> Unit,
    onSpeakMemory: (String) -> Unit
) {
    var viewMode by remember { mutableStateOf("LIST") } // "LIST" or "GRAPH"
    var showOnlyPermanent by remember { mutableStateOf(false) }

    val filteredMemories = memories.filter { mem ->
        val matchesSearch = searchQuery.isBlank() ||
                mem.title.contains(searchQuery, ignoreCase = true) ||
                mem.content.contains(searchQuery, ignoreCase = true) ||
                mem.tags.any { it.contains(searchQuery, ignoreCase = true) }
        val matchesCat = selectedCategory == null || mem.category == selectedCategory
        val matchesImp = selectedImportance == null || mem.importance == selectedImportance
        val matchesPerm = !showOnlyPermanent || mem.isPermanent
        matchesSearch && matchesCat && matchesImp && matchesPerm
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMemoryClick,
                containerColor = CyberViolet,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_memory")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Store Memory")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // View Switcher & Permanent Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TabRow(
                    selectedTabIndex = if (viewMode == "LIST") 0 else 1,
                    modifier = Modifier.width(220.dp).clip(RoundedCornerShape(12.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = viewMode == "LIST",
                        onClick = { viewMode = "LIST" },
                        text = { Text("Memory Vault") },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = viewMode == "GRAPH",
                        onClick = { viewMode = "GRAPH" },
                        text = { Text("Graph") },
                        icon = { Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                FilterChip(
                    selected = showOnlyPermanent,
                    onClick = { showOnlyPermanent = !showOnlyPermanent },
                    label = { Text("Permanent") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }

            Spacer(Modifier.height(10.dp))

            if (viewMode == "GRAPH") {
                KnowledgeGraphView(memories = memories, modifier = Modifier.fillMaxSize())
            } else {
                // Category filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelect(null) },
                            label = { Text("All (${memories.size})") }
                        )
                    }
                    items(MemoryCategory.values()) { cat ->
                        val count = memories.count { it.category == cat }
                        if (count > 0 || cat == MemoryCategory.PROJECT || cat == MemoryCategory.IDEA) {
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { onCategorySelect(if (selectedCategory == cat) null else cat) },
                                label = { Text("${cat.name} ($count)") }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    if (filteredMemories.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(8.dp))
                                    Text("No memories found matching criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(filteredMemories) { memory ->
                            MemoryCard(
                                memory = memory,
                                onSpeak = { onSpeakMemory(memory.content) },
                                onDelete = { onDeleteMemory(memory) },
                                onTogglePermanent = { onTogglePermanent(memory) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryCard(
    memory: MemoryItem,
    onSpeak: () -> Unit,
    onDelete: () -> Unit,
    onTogglePermanent: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(memory.createdAtMillis))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth().testTag("memory_card_${memory.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when(memory.category) {
                            MemoryCategory.PROJECT -> CyberViolet.copy(alpha = 0.2f)
                            MemoryCategory.IDEA -> CyberAmber.copy(alpha = 0.2f)
                            MemoryCategory.WORK -> CyberCyan.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = memory.category.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when(memory.category) {
                                MemoryCategory.PROJECT -> CyberVioletLight
                                MemoryCategory.IDEA -> CyberAmber
                                MemoryCategory.WORK -> CyberCyan
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (memory.importance == MemoryImportance.HIGH) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = CyberRose.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "HIGH IMPORTANCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberRose,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Speak Memory", tint = CyberCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Forget this", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = memory.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = memory.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!memory.connectedTopic.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = CyberVioletLight, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Linked Topic: ${memory.connectedTopic}", style = MaterialTheme.typography.labelSmall, color = CyberVioletLight, fontWeight = FontWeight.Medium)
                }
            }

            if (memory.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    memory.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (memory.isPermanent) Icons.Default.Lock else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (memory.isPermanent) CyberCyan else CyberAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (memory.isPermanent) "Permanent Memory" else "Short-term",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (memory.isPermanent) CyberCyan else CyberAmber
                    )
                }
            }
        }
    }
}
