package dev.mrdw.bedrock.ui.screens.notes

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mrdw.bedrock.ui.viewmodel.NoteEditorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: NoteEditorViewModel = viewModel()
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val emojiIcon by viewModel.emojiIcon.collectAsState()
    val isPinned by viewModel.isPinned.collectAsState()

    var showEmojiPicker by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    var showTagInput by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(noteId) {
        noteId?.let { viewModel.loadNote(it) }
    }

    // Auto-save when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                viewModel.saveNote()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Custom Top Bar
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

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { viewModel.togglePinned() }) {
                        Icon(
                            if (isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                            "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Emoji + Title Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Emoji Icon
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

                    // Title Field (No borders)
                    BasicTextField(
                        value = title,
                        onValueChange = { viewModel.updateTitle(it) },
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
                                if (title.isEmpty()) {
                                    Text(
                                        "Title",
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

                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    thickness = 1.dp
                )

                // Content Field (No borders)
                BasicTextField(
                    value = content,
                    onValueChange = { viewModel.updateContent(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 28.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (content.isEmpty()) {
                                Text(
                                    "Start typing...",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Tags Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tags",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        IconButton(
                            onClick = { showTagInput = !showTagInput },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (showTagInput) Icons.Default.Close else Icons.Default.Add,
                                "Toggle Tag Input",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showTagInput,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = newTag,
                                onValueChange = { newTag = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (newTag.isEmpty()) {
                                            Text(
                                                "Enter tag name",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            Button(
                                onClick = {
                                    viewModel.addTag(newTag)
                                    newTag = ""
                                    showTagInput = false
                                },
                                enabled = newTag.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Add")
                            }
                        }
                    }

                    if (tags.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(tags) { tag ->
                                FilterChip(
                                    selected = false,
                                    onClick = { },
                                    label = {
                                        Text(
                                            "#$tag",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            "Remove",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.removeTag(tag) }
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }
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
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmojiPicker = false }) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.updateEmojiIcon(null)
                    showEmojiPicker = false
                }) {
                    Text("Remove")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

private val emojiList = listOf(
    "📝", "📌", "💡", "📚", "🎯", "⭐", "❤️", "🔥",
    "✨", "📖", "🎨", "🎵", "🏃", "💼", "🏠", "🍕",
    "🎮", "✈️", "🌟", "💻", "📱", "🎬", "🎪", "🎭"
)
