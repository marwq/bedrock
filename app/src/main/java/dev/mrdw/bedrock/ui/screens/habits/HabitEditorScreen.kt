package dev.mrdw.bedrock.ui.screens.habits

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mrdw.bedrock.ui.viewmodel.HabitEditorViewModel
import kotlinx.coroutines.launch

data class HabitTemplate(
    val name: String,
    val description: String,
    val emoji: String,
    val category: String
)

private val habitTemplates = listOf(
    // Morning
    HabitTemplate("Morning Workout", "Start your day with exercise", "🏃", "Morning"),
    HabitTemplate("Drink Water", "2 glasses of water in the morning", "💧", "Morning"),
    HabitTemplate("Meditation", "10 minutes of mindfulness", "🧘", "Morning"),
    HabitTemplate("Make Bed", "Keep your space tidy", "🛏️", "Morning"),
    HabitTemplate("Read", "30 minutes of reading", "📚", "Morning"),

    // Productivity
    HabitTemplate("Deep Work", "2 hours of focused work", "💻", "Productivity"),
    HabitTemplate("Learn Something New", "Study or practice a skill", "🎓", "Productivity"),
    HabitTemplate("Journal", "Write down your thoughts", "📝", "Productivity"),

    // Health
    HabitTemplate("Exercise", "30 minutes of physical activity", "💪", "Health"),
    HabitTemplate("Healthy Meal", "Eat nutritious food", "🥗", "Health"),
    HabitTemplate("No Sugar", "Avoid sugary foods", "🚫", "Health"),
    HabitTemplate("Walk 10K Steps", "Stay active throughout the day", "👟", "Health"),

    // Evening
    HabitTemplate("No Screen Time", "1 hour before bed", "📱", "Evening"),
    HabitTemplate("Gratitude", "Write 3 things you're grateful for", "🙏", "Evening"),
    HabitTemplate("Early Sleep", "In bed by 10 PM", "😴", "Evening"),
    HabitTemplate("Skincare Routine", "Take care of your skin", "✨", "Evening"),

    // Lifestyle
    HabitTemplate("Practice Language", "15 minutes of language learning", "🗣️", "Lifestyle"),
    HabitTemplate("Call Family", "Stay connected with loved ones", "📞", "Lifestyle"),
    HabitTemplate("Creative Time", "Draw, write, or create", "🎨", "Lifestyle"),
    HabitTemplate("No Social Media", "Digital detox", "🔕", "Lifestyle")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditorScreen(
    habitId: Long?,
    duplicateFromId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToDuplicate: (Long) -> Unit,
    viewModel: HabitEditorViewModel = viewModel()
) {
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val emojiIcon by viewModel.emojiIcon.collectAsState()
    val timeHour by viewModel.timeHour.collectAsState()
    val timeMinute by viewModel.timeMinute.collectAsState()

    var showEmojiPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTemplates by remember { mutableStateOf(habitId == null && duplicateFromId == null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(habitId, duplicateFromId) {
        when {
            habitId != null -> viewModel.loadHabit(habitId)
            duplicateFromId != null -> viewModel.loadHabitForDuplication(duplicateFromId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (habitId == null && duplicateFromId == null) {
                            TextButton(onClick = { showTemplates = !showTemplates }) {
                                Icon(
                                    if (showTemplates) Icons.Default.Close else Icons.Default.Lightbulb,
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showTemplates) "Custom" else "Templates")
                            }
                        }

                        // Delete Button (only for existing habits)
                        if (habitId != null) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (viewModel.deleteHabit()) {
                                            onNavigateBack()
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Duplicate Button (only for existing habits)
                        if (habitId != null) {
                            IconButton(
                                onClick = {
                                    habitId?.let { onNavigateToDuplicate(it) }
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    "Duplicate",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Save Button (only for new habits)
                        if (habitId == null || duplicateFromId != null) {
                            AnimatedVisibility(
                                visible = !showTemplates && name.isNotBlank(),
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            if (viewModel.saveHabit()) {
                                                // If duplicate, pop twice to get back to main screen
                                                if (duplicateFromId != null) {
                                                    onNavigateBack()
                                                    onNavigateBack()
                                                } else {
                                                    onNavigateBack()
                                                }
                                            } else {
                                                snackbarHostState.showSnackbar("Please enter a habit name")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

            // Templates Section
            AnimatedVisibility(
                visible = showTemplates,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        "Choose a habit",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val groupedTemplates = habitTemplates.groupBy { it.category }
                    groupedTemplates.forEach { (category, templates) ->
                        Text(
                            category,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        templates.forEach { template ->
                            TemplateItem(
                                template = template,
                                onClick = {
                                    viewModel.applyTemplate(template.name, template.description, template.emoji)
                                    showTemplates = false
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Custom Habit Form
            AnimatedVisibility(
                visible = !showTemplates,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Emoji + Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { showEmojiPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emojiIcon ?: "+",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        BasicTextField(
                            value = name,
                            onValueChange = { viewModel.updateName(it) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 8.dp),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (name.isEmpty()) {
                                        Text(
                                            "Habit name",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    // Time Picker
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Time",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Text(
                                String.format("%02d:%02d", timeHour, timeMinute),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Description
                    BasicTextField(
                        value = description,
                        onValueChange = { viewModel.updateDescription(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box {
                                if (description.isEmpty()) {
                                    Text(
                                        "Description (optional)",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }
        }
    }

    // Emoji Picker Dialog
    if (showEmojiPicker) {
        AlertDialog(
            onDismissRequest = { showEmojiPicker = false },
            title = {
                Text(
                    "Choose Icon",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(emojiList) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.updateEmojiIcon(emoji)
                                    showEmojiPicker = false
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmojiPicker = false }) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = timeHour,
            initialMinute = timeMinute,
            onConfirm = { hour, minute ->
                viewModel.updateTime(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Set Time",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Time Display
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hour
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            String.format("%02d", selectedHour),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        ":",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Minute
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            String.format("%02d", selectedMinute),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Hour Slider
                Column {
                    Text(
                        "Hour",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = selectedHour.toFloat(),
                        onValueChange = { selectedHour = it.toInt() },
                        valueRange = 0f..23f,
                        steps = 22
                    )
                }

                // Minute Slider
                Column {
                    Text(
                        "Minute",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = selectedMinute.toFloat(),
                        onValueChange = { selectedMinute = it.toInt() },
                        valueRange = 0f..59f,
                        steps = 58
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHour, selectedMinute) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Set Time", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun TemplateItem(
    template: HabitTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(text = template.emoji, style = MaterialTheme.typography.titleLarge)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

private val emojiList = listOf(
    "💪", "🏃", "🧘", "📚", "✍️", "💧", "🥗", "😴",
    "🎯", "🔥", "⭐", "✨", "💻", "🎨", "🎵", "📱",
    "🚫", "✅", "📝", "🙏", "🌟", "💡", "🎓", "👟"
)
