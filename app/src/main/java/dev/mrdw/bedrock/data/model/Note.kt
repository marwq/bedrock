package dev.mrdw.bedrock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dev.mrdw.bedrock.data.Converters

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val color: String = "#FFFFFF",
    val emojiIcon: String? = null,
    val isPinned: Boolean = false,
    val folder: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isVoiceNote: Boolean = false,
    val voiceNotePath: String? = null
)
