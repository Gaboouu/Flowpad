package com.udb.flowpad.ui

import androidx.compose.ui.res.stringResource
import com.udb.flowpad.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.udb.flowpad.data.NoteType
import com.udb.flowpad.data.ProjectEntity
import com.udb.flowpad.ui.theme.FlowpadTheme
import com.udb.flowpad.ui.theme.coverAt
import kotlinx.coroutines.launch

@Composable
fun FlowpadApp() { AuthGate() }

@Composable
fun SignedInApp(vm: FlowpadViewModel, email: String, onLogout: () -> Unit) {
    val darkSetting by vm.darkMode.collectAsStateWithLifecycle()
    val dark = darkSetting ?: isSystemInDarkTheme()
    FlowpadTheme(dark = dark) { MainScreen(vm, dark, email, onLogout) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(vm: FlowpadViewModel, dark: Boolean, email: String, onLogout: () -> Unit) {
    val projects by vm.projects.collectAsStateWithLifecycle()
    val project by vm.activeProject.collectAsStateWithLifecycle()
    val cards by vm.cards.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableStateOf(0) }
    var showNewProject by remember { mutableStateOf(false) }
    var showEditProject by remember { mutableStateOf(false) }

    var query by rememberSaveable { mutableStateOf("") }
    var deletion by remember { mutableStateOf<(() -> Unit)?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val message by vm.message.collectAsStateWithLifecycle()
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); vm.message.value = null } }
    val current = project
    if (deletion != null) AlertDialog(onDismissRequest = { deletion = null },
        title = { Text(stringResource(R.string.text_20)) }, text = { Text(stringResource(R.string.text_21)) },
        confirmButton = { TextButton(onClick = { deletion?.invoke(); deletion = null }) { Text(stringResource(R.string.text_14)) } },
        dismissButton = { TextButton(onClick = { deletion = null }) { Text(stringResource(R.string.text_6)) } })

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProjectsDrawer(
                projects = projects.filter { it.name.contains(query, true) },
                activeId = current?.id,
                onSelect = {
                    vm.selectProject(it)
                    scope.launch { drawerState.close() }
                },
                onNew = {
                    scope.launch { drawerState.close() }
                    showNewProject = true
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(MaterialTheme.colorScheme.onBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "F",
                                    color = MaterialTheme.colorScheme.background,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.text_22), fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.text_30))
                        }
                    },
                    actions = {
                        TextButton(onClick = onLogout) { Text(stringResource(R.string.text_23)) }
                        IconButton(onClick = { vm.setDarkMode(!dark) }) {
                            Text(if (dark) stringResource(R.string.label_50) else stringResource(R.string.label_51), fontSize = 20.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                if (tab == 2 && current != null) {
                    NotesFab(onAdd = { vm.addNote(it) })
                }
            }
        ) { padding ->
            if (current == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .imePadding()
                ) {
                    Text(email, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(value = query, onValueChange = { query = it },
                        label = { Text(stringResource(R.string.text_24)) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                    ProjectHeader(current, onEdit = { showEditProject = true })

                    val pending = tasks.count { !it.completed }
                    TabRow(
                        selectedTabIndex = tab,
                        containerColor = Color.Transparent
                    ) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.text_25)) })
                        Tab(
                            selected = tab == 1,
                            onClick = { tab = 1 },
                            text = { Text(if (tasks.isEmpty()) stringResource(R.string.label_52) else "Tareas ($pending)") }
                        )
                        Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(R.string.text_26)) })
                    }

                    when (tab) {
                        0 -> KanbanBoard(
                            cards = cards.filter { it.title.contains(query, true) },
                            onAdd = { title, status -> vm.addCard(title, status) },
                            onRename = { id, title -> vm.renameCard(id, title) },
                            onMove = { card, delta -> vm.moveCard(card, delta) },
                            onDelete = { id -> deletion = { vm.deleteCard(id) } },
                            modifier = Modifier.weight(1f)
                        )
                        1 -> TasksView(
                            tasks = tasks.filter { it.text.contains(query, true) },
                            onAdd = { vm.addTask(it) },
                            onToggle = { id, done -> vm.setTaskCompleted(id, done) },
                            onRename = { id, text -> vm.renameTask(id, text) },
                            onDelete = { id -> deletion = { vm.deleteTask(id) } },
                            modifier = Modifier.weight(1f)
                        )
                        else -> NotesView(
                            blocks = notes.filter { it.content.contains(query, true) },
                            onChange = { id, text -> vm.updateNote(id, text) },
                            onMove = { block, up -> vm.moveNote(block, up) },
                            onDelete = { id -> deletion = { vm.deleteNote(id) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showNewProject) {
        NewProjectDialog(
            onDismiss = { showNewProject = false },
            onCreate = {
                vm.createProject(it)
                tab = 0
                showNewProject = false
            }
        )
    }

    if (showEditProject && current != null) {
        EditProjectDialog(
            project = current,
            canDelete = projects.size > 1,
            onDismiss = { showEditProject = false },
            onSave = { name, description, emoji, cover ->
                vm.updateProject(current.id, name, description, emoji, cover)
                showEditProject = false
            },
            onDelete = {
                vm.deleteProject(current.id)
                showEditProject = false
            }
        )
    }
}

@Composable
private fun ProjectHeader(project: ProjectEntity, onEdit: () -> Unit) {
    val cover = coverAt(project.coverIndex)
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cover.brush)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Text(project.emoji, fontSize = 28.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    project.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    project.description,
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.text_7), tint = Color.White)
            }
        }
    }
}

@Composable
private fun ProjectsDrawer(
    projects: List<ProjectEntity>,
    activeId: Long?,
    onSelect: (Long) -> Unit,
    onNew: () -> Unit
) {
    ModalDrawerSheet {
        Text(
            "MIS PROYECTOS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 28.dp, top = 28.dp, bottom = 12.dp)
        )
        LazyColumn(Modifier.weight(1f)) {
            items(projects, key = { it.id }) { p ->
                NavigationDrawerItem(
                    label = { Text(p.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = { Text(p.emoji, fontSize = 20.sp) },
                    selected = p.id == activeId,
                    onClick = { onSelect(p.id) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
        Button(
            onClick = onNew,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.text_1))
        }
    }
}

@Composable
private fun NotesFab(onAdd: (String) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Box {
        ExtendedFloatingActionButton(
            onClick = { menu = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.text_27)) }
        )
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.text_28)) },
                onClick = { menu = false; onAdd(NoteType.TEXT) }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.text_29)) },
                onClick = { menu = false; onAdd(NoteType.HEADING) }
            )
        }
    }
}
