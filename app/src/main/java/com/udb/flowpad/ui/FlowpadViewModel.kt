package com.udb.flowpad.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udb.flowpad.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FlowpadViewModel(app: Application, uid: String) : AndroidViewModel(app) {

    val message = MutableStateFlow<String?>(null)
    private val handler = kotlinx.coroutines.CoroutineExceptionHandler { _, _ ->
        message.value = getApplication<Application>().getString(com.udb.flowpad.R.string.label_55)
    }
    private fun valid(text: String): Boolean {
        if (text.isBlank() || text.length > 200) {
            message.value = getApplication<Application>().getString(com.udb.flowpad.R.string.label_56)
            return false
        }
        return true
    }
    private fun <T> Flow<T>.recover(): Flow<T> = retryWhen { _, _ ->
        message.value = getApplication<Application>().getString(com.udb.flowpad.R.string.label_57)
        kotlinx.coroutines.delay(3000)
        true
    }
    private val dao = FlowpadDatabase.get(app, uid).dao()

    val projects: StateFlow<List<ProjectEntity>> =
        dao.observeProjects().recover().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val activeId: Flow<Long?> =
        dao.observeSetting(KEY_ACTIVE).map { it?.toLongOrNull() }

    /** Proyecto activo; si el guardado ya no existe se usa el primero. */
    val activeProject: StateFlow<ProjectEntity?> =
        combine(projects, activeId) { list, id ->
            list.firstOrNull { it.id == id } ?: list.firstOrNull()
        }.recover().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** null = seguir el tema del sistema hasta que el usuario elija uno. */
    val darkMode: StateFlow<Boolean?> =
        dao.observeSetting(KEY_DARK).map { it?.toBooleanStrictOrNull() }
            .recover().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val activeProjectId: Flow<Long?> =
        activeProject.map { it?.id }.distinctUntilChanged()

    val cards: StateFlow<List<CardEntity>> =
        activeProjectId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else dao.observeCards(id)
        }.recover().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val tasks: StateFlow<List<TaskEntity>> =
        activeProjectId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else dao.observeTasks(id)
        }.recover().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val notes: StateFlow<List<NoteBlockEntity>> =
        activeProjectId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else dao.observeNotes(id)
        }.recover().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch(handler) { seedIfNeeded() }
    }

    /** Primer arranque: crea el "Proyecto UDB" de ejemplo del anteproyecto. */
    private suspend fun seedIfNeeded() {
        if (dao.getSetting(KEY_SEEDED) != null) return
        if (dao.projectCount() == 0) {
            val pid = dao.insertProject(
                ProjectEntity(
                    name = "Proyecto UDB",
                    description = DEFAULT_DESCRIPTION,
                    emoji = "🎓",
                    coverIndex = 0
                )
            )
            listOf(
                Triple("Preparar documentación", KanbanStatus.POR_HACER, 1L),
                Triple("Investigar requisitos del proyecto", KanbanStatus.POR_HACER, 2L),
                Triple("Desarrollar prototipo", KanbanStatus.EN_PROCESO, 1L),
                Triple("Definir objetivos", KanbanStatus.HECHO, 1L)
            ).forEach { (title, status, pos) ->
                dao.insertCard(CardEntity(projectId = pid, title = title, status = status.key, position = pos))
            }
            dao.insertTask(TaskEntity(projectId = pid, text = "Revisar instrucciones del docente"))
            dao.insertTask(TaskEntity(projectId = pid, text = "Subir avances al repositorio"))
            dao.insertNote(NoteBlockEntity(projectId = pid, type = NoteType.HEADING, content = "Primera fase", position = 1))
            dao.insertNote(NoteBlockEntity(projectId = pid, type = NoteType.TEXT, content = "Parte 1 del proyecto completada.", position = 2))
            dao.putSetting(SettingEntity(KEY_ACTIVE, pid.toString()))
        }
        dao.putSetting(SettingEntity(KEY_SEEDED, "1"))
    }

    // ---------------- Tema ----------------
    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch(handler) { dao.putSetting(SettingEntity(KEY_DARK, dark.toString())) }
    }

    // ---------------- Proyectos ----------------
    fun selectProject(id: Long) {
        viewModelScope.launch(handler) { dao.putSetting(SettingEntity(KEY_ACTIVE, id.toString())) }
    }

    fun createProject(name: String) {
        if (!valid(name)) return
        viewModelScope.launch(handler) {
            val count = dao.projectCount()
            val id = dao.insertProject(
                ProjectEntity(
                    name = name.trim(),
                    description = DEFAULT_DESCRIPTION,
                    emoji = ProjectEmojis[count % ProjectEmojis.size],
                    coverIndex = count % COVER_COUNT
                )
            )
            dao.putSetting(SettingEntity(KEY_ACTIVE, id.toString()))
        }
    }

    fun updateProject(id: Long, name: String, description: String, emoji: String, coverIndex: Int) {
        if (!valid(name)) return
        viewModelScope.launch(handler) { dao.updateProject(id, name.trim(), description.trim(), emoji, coverIndex) }
    }

    fun deleteProject(id: Long) {
        if (projects.value.size <= 1) return
        viewModelScope.launch(handler) {
            val next = projects.value.firstOrNull { it.id != id }
            dao.deleteProject(id)
            if (next != null) dao.putSetting(SettingEntity(KEY_ACTIVE, next.id.toString()))
        }
    }

    // ---------------- Tablero Kanban ----------------
    fun addCard(title: String, status: KanbanStatus) {
        if (!valid(title)) return
        val pid = activeProject.value?.id ?: return
        viewModelScope.launch(handler) {
            val pos = dao.maxCardPosition(pid, status.key) + 1
            dao.insertCard(CardEntity(projectId = pid, title = title.trim(), status = status.key, position = pos))
        }
    }

    fun renameCard(id: Long, title: String) {
        if (!valid(title)) return
        viewModelScope.launch(handler) { dao.renameCard(id, title.trim()) }
    }

    /** Mueve la tarjeta una columna a la izquierda (-1) o a la derecha (+1). */
    fun moveCard(card: CardEntity, delta: Int) {
        val newIndex = KanbanStatus.fromKey(card.status).ordinal + delta
        if (newIndex !in KanbanStatus.entries.indices) return
        val target = KanbanStatus.entries[newIndex]
        viewModelScope.launch(handler) {
            val pos = dao.maxCardPosition(card.projectId, target.key) + 1
            dao.moveCard(card.id, target.key, pos)
        }
    }

    fun deleteCard(id: Long) {
        viewModelScope.launch(handler) { dao.deleteCard(id) }
    }

    // ---------------- Tareas ----------------
    fun addTask(text: String) {
        if (!valid(text)) return
        val pid = activeProject.value?.id ?: return
        viewModelScope.launch(handler) { dao.insertTask(TaskEntity(projectId = pid, text = text.trim())) }
    }

    fun setTaskCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch(handler) { dao.setTaskCompleted(id, completed) }
    }

    fun renameTask(id: Long, text: String) {
        if (!valid(text)) return
        viewModelScope.launch(handler) { dao.renameTask(id, text.trim()) }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch(handler) { dao.deleteTask(id) }
    }

    // ---------------- Notas ----------------
    fun addNote(type: String) {
        val pid = activeProject.value?.id ?: return
        viewModelScope.launch(handler) {
            val pos = dao.maxNotePosition(pid) + 1
            dao.insertNote(NoteBlockEntity(projectId = pid, type = type, content = "", position = pos))
        }
    }

    fun updateNote(id: Long, content: String) {
        viewModelScope.launch(handler) { dao.setNoteContent(id, content) }
    }

    fun moveNote(block: NoteBlockEntity, up: Boolean) {
        val list = notes.value
        val index = list.indexOfFirst { it.id == block.id }
        val neighbor = list.getOrNull(if (up) index - 1 else index + 1) ?: return
        viewModelScope.launch(handler) {
            dao.swapNotes(block.id, block.position, neighbor.id, neighbor.position)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch(handler) { dao.deleteNote(id) }
    }

    private companion object {
        const val KEY_ACTIVE = "active_project"
        const val KEY_DARK = "dark_mode"
        const val KEY_SEEDED = "seeded"
    }
}
