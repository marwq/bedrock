package dev.mrdw.bedrock.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.mrdw.bedrock.data.dao.NoteDao
import dev.mrdw.bedrock.data.dao.ReminderDao
import dev.mrdw.bedrock.data.dao.TaskDao
import dev.mrdw.bedrock.data.dao.HabitDao
import dev.mrdw.bedrock.data.model.Note
import dev.mrdw.bedrock.data.model.Reminder
import dev.mrdw.bedrock.data.model.Task
import dev.mrdw.bedrock.data.model.Habit
import dev.mrdw.bedrock.data.model.HabitCompletion

@Database(
    entities = [Note::class, Task::class, Reminder::class, Habit::class, HabitCompletion::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bedrock_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
