package dev.mrdw.bedrock.ui.screens.habits

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mrdw.bedrock.data.model.Habit
import dev.mrdw.bedrock.ui.viewmodel.HabitsViewModel
import dev.mrdw.bedrock.ui.viewmodel.HabitWithCompletion
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    onNavigateToHabitEditor: (Long?) -> Unit,
    viewModel: HabitsViewModel = viewModel()
) {
    val habitsWithCompletions by viewModel.habitsWithCompletions.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val daysOfWeek = remember { viewModel.getDaysOfWeek() }
    val today = LocalDate.now()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToHabitEditor(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add Habit", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Habits",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Week Calendar
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(daysOfWeek) { date ->
                        DayItem(
                            date = date,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            onClick = { viewModel.selectDate(date) }
                        )
                    }
                }
            }

            // Habits List
            if (habitsWithCompletions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Text(
                            "No habits yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "Tap + to create your first habit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habitsWithCompletions, key = { it.habit.id }) { habitWithCompletion ->
                        HabitItem(
                            habitWithCompletion = habitWithCompletion,
                            selectedDate = selectedDate,
                            canEdit = selectedDate >= today,
                            onToggleCompletion = {
                                viewModel.toggleHabitCompletion(habitWithCompletion.habit.id)
                            },
                            onDelete = { viewModel.deleteHabit(habitWithCompletion.habit) },
                            onEdit = { onNavigateToHabitEditor(habitWithCompletion.habit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val dayNumber = date.dayOfMonth

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else if (isToday) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (isSelected) Color.White
            else if (isToday) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (isSelected) Color.White
            else if (isToday) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HabitItem(
    habitWithCompletion: HabitWithCompletion,
    selectedDate: LocalDate,
    canEdit: Boolean,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (habitWithCompletion.isCompleted) 1.0f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (habitWithCompletion.isCompleted) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (habitWithCompletion.isCompleted)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = habitWithCompletion.habit.emojiIcon ?: "✓",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                // Habit Name & Time
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = habitWithCompletion.habit.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val now = LocalDate.now()
                        val currentTime = java.time.LocalTime.now()
                        val habitTime = java.time.LocalTime.of(
                            habitWithCompletion.habit.timeHour,
                            habitWithCompletion.habit.timeMinute
                        )

                        val timeText = if (selectedDate == now && habitTime.isAfter(currentTime)) {
                            val minutesUntil = java.time.Duration.between(currentTime, habitTime).toMinutes()
                            if (minutesUntil < 60) {
                                "through $minutesUntil min"
                            } else {
                                String.format("%02d:%02d", habitWithCompletion.habit.timeHour, habitWithCompletion.habit.timeMinute)
                            }
                        } else {
                            String.format("%02d:%02d", habitWithCompletion.habit.timeHour, habitWithCompletion.habit.timeMinute)
                        }

                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (habitWithCompletion.habit.description.isNotBlank()) {
                        Text(
                            text = habitWithCompletion.habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }

            // Completion Checkbox
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (habitWithCompletion.isCompleted)
                            MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (habitWithCompletion.isCompleted)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = canEdit, onClick = onToggleCompletion),
                contentAlignment = Alignment.Center
            ) {
                if (habitWithCompletion.isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
