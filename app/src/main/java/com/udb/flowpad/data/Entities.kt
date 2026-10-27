package com.udb.flowpad.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Estados del tablero Kanban: todo -> doing -> done. */
enum class KanbanStatus(val key: String, val label: String) {
    POR_HACER("todo", "Por hacer"),
    EN_PROCESO("doing", "En proceso"),
    HECHO("done", "Hecho");

    companion object {
        fun fromKey(key: String): KanbanStatus =
            entries.firstOrNull { it.key == key } ?: POR_HACER
    }
}

object NoteType {
    const val TEXT = "text"
    const val HEADING = "heading"
}

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val emoji: String,
    val coverIndex: Int
)

@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val status: String,
    val position: Long
)

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val text: String,
    val completed: Boolean = false
)

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class NoteBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val type: String,
    val content: String,
    val position: Long
)

/** Configuraciones del usuario (tema, proyecto activo...). */
@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val name: String,
    val value: String
)
