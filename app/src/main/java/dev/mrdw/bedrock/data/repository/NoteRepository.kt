package dev.mrdw.bedrock.data.repository

import dev.mrdw.bedrock.data.dao.NoteDao
import dev.mrdw.bedrock.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getPinnedNotes(): Flow<List<Note>> = noteDao.getPinnedNotes()

    fun getNotesByFolder(folder: String): Flow<List<Note>> = noteDao.getNotesByFolder(folder)

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    fun getAllFolders(): Flow<List<String>> = noteDao.getAllFolders()

    suspend fun getNoteById(noteId: Long): Note? = noteDao.getNoteById(noteId)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(noteId: Long) = noteDao.deleteNoteById(noteId)

    suspend fun updatePinStatus(noteId: Long, isPinned: Boolean) =
        noteDao.updatePinStatus(noteId, isPinned)
}
