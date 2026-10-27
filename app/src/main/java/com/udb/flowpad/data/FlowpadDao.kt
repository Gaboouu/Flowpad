package com.udb.flowpad.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FlowpadDao {

    // ---------- Proyectos ----------
    @Query("SELECT * FROM projects ORDER BY id")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun projectCount(): Int

    @Insert
    suspend fun insertProject(project: ProjectEntity): Long

    @Query("UPDATE projects SET name = :name, description = :description, emoji = :emoji, coverIndex = :coverIndex WHERE id = :id")
    suspend fun updateProject(id: Long, name: String, description: String, emoji: String, coverIndex: Int)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    // ---------- Tarjetas Kanban ----------
    @Query("SELECT * FROM cards WHERE projectId = :projectId ORDER BY position, id")
    fun observeCards(projectId: Long): Flow<List<CardEntity>>

    @Query("SELECT COALESCE(MAX(position), 0) FROM cards WHERE projectId = :projectId AND status = :status")
    suspend fun maxCardPosition(projectId: Long, status: String): Long

    @Insert
    suspend fun insertCard(card: CardEntity): Long

    @Query("UPDATE cards SET title = :title WHERE id = :id")
    suspend fun renameCard(id: Long, title: String)

    @Query("UPDATE cards SET status = :status, position = :position WHERE id = :id")
    suspend fun moveCard(id: Long, status: String, position: Long)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCard(id: Long)

    // ---------- Tareas ----------
    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY id")
    fun observeTasks(projectId: Long): Flow<List<TaskEntity>>

    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    @Query("UPDATE tasks SET completed = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("UPDATE tasks SET text = :text WHERE id = :id")
    suspend fun renameTask(id: Long, text: String)

    // ---------- Notas ----------
    @Query("SELECT * FROM notes WHERE projectId = :projectId ORDER BY position, id")
    fun observeNotes(projectId: Long): Flow<List<NoteBlockEntity>>

    @Query("SELECT COALESCE(MAX(position), 0) FROM notes WHERE projectId = :projectId")
    suspend fun maxNotePosition(projectId: Long): Long

    @Insert
    suspend fun insertNote(note: NoteBlockEntity): Long

    @Query("UPDATE notes SET content = :content WHERE id = :id")
    suspend fun setNoteContent(id: Long, content: String)

    @Query("UPDATE notes SET position = :position WHERE id = :id")
    suspend fun setNotePosition(id: Long, position: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Transaction
    suspend fun swapNotes(firstId: Long, firstPosition: Long, secondId: Long, secondPosition: Long) {
        setNotePosition(firstId, secondPosition)
        setNotePosition(secondId, firstPosition)
    }

    // ---------- Configuración ----------
    @Query("SELECT value FROM settings WHERE name = :name")
    fun observeSetting(name: String): Flow<String?>

    @Query("SELECT value FROM settings WHERE name = :name")
    suspend fun getSetting(name: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putSetting(setting: SettingEntity)
}
