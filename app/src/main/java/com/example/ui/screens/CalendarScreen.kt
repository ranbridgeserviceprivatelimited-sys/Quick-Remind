package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.CalendarEventItem
import com.example.data.model.ReminderItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    events: List<CalendarEventItem>,
    reminders: List<ReminderItem>,
    onAddEventClick: () -> Unit
) {
    var selectedDayOffset by remember { mutableStateOf(0) } // 0 is today, 1 tomorrow, -1 yesterday
    val cal = Calendar.getInstance()

    // 7 days horizontal strip
    val daysList = remember {
        ( -2..5 ).map { offset ->
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, offset)
            offset to c.time
        }
    }

    val selectedDate = remember(selectedDayOffset) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, selectedDayOffset)
        c.time
    }

    val dayFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
    val selectedDateStr = dayFormat.format(selectedDate)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEventClick,
                containerColor = CyberViolet,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_event")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text("Integrated Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Unified view of events, deadlines, and reminders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(14.dp))

            // Date strip selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysList) { (offset, date) ->
                    val isSelected = selectedDayOffset == offset
                    val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                    val dayNum = SimpleDateFormat("dd", Locale.getDefault()).format(date)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CyberViolet else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .width(62.dp)
                            .clickable { selectedDayOffset = offset }
                            .testTag("calendar_day_$offset")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = dayNum,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (selectedDayOffset == 0) "Today — $selectedDateStr" else selectedDateStr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Scheduled Events
                if (events.isNotEmpty()) {
                    items(events) { event ->
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        val startStr = timeFormat.format(Date(event.startMillis))
                        val endStr = timeFormat.format(Date(event.endMillis))

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(40.dp)
                                        .clip(CircleShape)
                                        .background(CyberViolet)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(event.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "$startStr - $endStr • ${event.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyberViolet.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        event.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberVioletLight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Due Reminders on Calendar
                if (reminders.isNotEmpty()) {
                    items(reminders.take(3)) { rem ->
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        val dueStr = timeFormat.format(Date(rem.dueDateMillis))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rem.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("Due: $dueStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
