package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onVoiceClick: () -> Unit,
    onScanClick: () -> Unit,
    onMorningPlanClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val currentDateFormatted = dateFormat.format(Date()).uppercase()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = currentDateFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SleekTextMuted
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.primaryContainer.let {
                            if (it == SleekNavyContainer) SleekNavyDark else MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { isSearchExpanded = !isSearchExpanded },
                    modifier = Modifier.size(36.dp).testTag("topbar_search_button")
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onMorningPlanClick,
                    modifier = Modifier.size(36.dp).testTag("topbar_plan_button")
                ) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = "Daily Plan",
                        tint = SleekTaskLabel,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onScanClick,
                    modifier = Modifier.size(36.dp).testTag("topbar_scan_button")
                ) {
                    Icon(
                        Icons.Default.DocumentScanner,
                        contentDescription = "Scan Image",
                        tint = SleekNavySecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Sleek User Avatar Monogram
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SleekNavyContainer)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { onSettingsClick() }
                        .testTag("topbar_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RK",
                        color = SleekNavySecondary,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        if (isSearchExpanded) {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search memories, projects, tasks, STM32...", color = SleekTextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("topbar_search_input"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekNavyPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekNavyPrimary,
                    unfocusedBorderColor = SleekBorderLight,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
