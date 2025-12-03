package dev.mrdw.bedrock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mrdw.bedrock.data.AppDatabase
import dev.mrdw.bedrock.data.model.Note
import dev.mrdw.bedrock.data.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository

    private val _noteId = MutableStateFlow<Long?>(null)
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _emojiIcon = MutableStateFlow<String?>(null)
    val emojiIcon: StateFlow<String?> = _emojiIcon.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao())

        // Autosave functionality
        viewModelScope.launch {
            combine(_title, _content, _tags, _emojiIcon, _isPinned) { _, _, _, _, _ ->
                Unit
            }.debounce(1000) // Wait 1 second after last change
                .collect {
                    if (_noteId.value != null) {
                        autoSave()
                    }
                }
        }
    }

    private suspend fun autoSave() {
        if (_noteId.value == null) return
        if (_title.value.isBlank() && _content.value.isBlank()) return

        try {
            val note = Note(
                id = _noteId.value ?: 0,
                title = _title.value.ifBlank { "Untitled" },
                content = _content.value,
                tags = _tags.value,
                color = "#FFFFFF",
                emojiIcon = _emojiIcon.value,
                folder = null,
                isPinned = _isPinned.value,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateNote(note)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadNote(noteId: Long) {
        _noteId.value = noteId
        viewModelScope.launch {
            repository.getNoteById(noteId)?.let { note ->
                _title.value = note.title
                _content.value = note.content
                _tags.value = note.tags
                _emojiIcon.value = note.emojiIcon
                _isPinned.value = note.isPinned
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun addTag(tag: String) {
        val cleanTag = tag.trim().removePrefix("#")
        if (cleanTag.isNotBlank() && cleanTag !in _tags.value) {
            _tags.value = _tags.value + cleanTag
        }
    }

    fun removeTag(tag: String) {
        _tags.value = _tags.value - tag
    }

    fun updateEmojiIcon(emoji: String?) {
        _emojiIcon.value = emoji
    }

    fun togglePinned() {
        _isPinned.value = !_isPinned.value
    }

    suspend fun saveNote(): Boolean {
        return try {
            if (_title.value.isBlank() && _content.value.isBlank()) {
                return false
            }

            val note = Note(
                id = _noteId.value ?: 0,
                title = _title.value.ifBlank { "Untitled" },
                content = _content.value,
                tags = _tags.value,
                color = "#FFFFFF",
                emojiIcon = _emojiIcon.value,
                folder = null,
                isPinned = _isPinned.value,
                updatedAt = System.currentTimeMillis()
            )

            if (_noteId.value == null) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
