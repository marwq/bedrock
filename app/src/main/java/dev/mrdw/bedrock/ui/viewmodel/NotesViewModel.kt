package dev.mrdw.bedrock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mrdw.bedrock.data.AppDatabase
import dev.mrdw.bedrock.data.model.Note
import dev.mrdw.bedrock.data.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow<NoteFilter>(NoteFilter.All)
    val selectedFilter: StateFlow<NoteFilter> = _selectedFilter.asStateFlow()

    val notes: StateFlow<List<Note>>
    val folders: StateFlow<List<String>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao())

        notes = combine(
            repository.getAllNotes(),
            _searchQuery,
            _selectedFilter
        ) { allNotes, query, filter ->
            var filteredNotes = allNotes

            if (query.isNotBlank()) {
                filteredNotes = filteredNotes.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
                }
            }

            when (filter) {
                is NoteFilter.All -> filteredNotes
                is NoteFilter.Pinned -> filteredNotes.filter { it.isPinned }
                is NoteFilter.Folder -> filteredNotes.filter { it.folder == filter.folderName }
                is NoteFilter.Tag -> filteredNotes.filter { filter.tagName in it.tags }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        folders = repository.getAllFolders()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: NoteFilter) {
        _selectedFilter.value = filter
    }

    fun togglePinStatus(noteId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.updatePinStatus(noteId, !isPinned)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}

sealed class NoteFilter {
    data object All : NoteFilter()
    data object Pinned : NoteFilter()
    data class Folder(val folderName: String) : NoteFilter()
    data class Tag(val tagName: String) : NoteFilter()
}
