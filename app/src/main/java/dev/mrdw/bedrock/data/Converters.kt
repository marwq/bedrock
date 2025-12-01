package dev.mrdw.bedrock.data

import androidx.room.TypeConverter
import dev.mrdw.bedrock.data.model.Priority
import dev.mrdw.bedrock.data.model.RepeatInterval
import dev.mrdw.bedrock.data.model.Subtask
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList()
        else json.decodeFromString(ListSerializer(String.serializer()), value)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return json.encodeToString(ListSerializer(String.serializer()), list)
    }

    @TypeConverter
    fun fromSubtaskList(value: String): List<Subtask> {
        return if (value.isEmpty()) emptyList()
        else json.decodeFromString<List<Subtask>>(value)
    }

    @TypeConverter
    fun toSubtaskList(list: List<Subtask>): String {
        return json.encodeToString(list)
    }

    @TypeConverter
    fun fromPriority(value: String): Priority {
        return Priority.valueOf(value)
    }

    @TypeConverter
    fun toPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun fromRepeatInterval(value: String?): RepeatInterval? {
        return value?.let { RepeatInterval.valueOf(it) }
    }

    @TypeConverter
    fun toRepeatInterval(interval: RepeatInterval?): String? {
        return interval?.name
    }
}
