package com.udb.flowpad.ui

import androidx.compose.ui.res.stringResource
import com.udb.flowpad.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.udb.flowpad.data.TaskEntity

@Composable
fun TasksView(
    tasks: List<TaskEntity>,
    onAdd: (String) -> Unit,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onRename: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf<TaskEntity?>(null) }
    var draft by remember { mutableStateOf("") }
    editing?.let { task ->
        AlertDialog(onDismissRequest = { editing = null }, title = { Text(stringResource(R.string.text_31)) },
            text = { OutlinedTextField(value = draft, onValueChange = { draft = it }, label = { Text(stringResource(R.string.text_32)) },
                isError = draft.isBlank() || draft.length > 200,
                supportingText = { Text(stringResource(R.string.text_2)) }) },
            confirmButton = { TextButton(enabled = draft.isNotBlank() && draft.length <= 200,
                onClick = { onRename(task.id, draft); editing = null }) { Text(stringResource(R.string.text_12)) } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.text_6)) } })
    }
    var text by rememberSaveable { mutableStateOf("") }
    val submit = {
        if (text.isNotBlank() && text.length <= 200) {
            onAdd(text)
            text = ""
        }
    }

    Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                isError = text.isBlank() || text.length > 200,
                supportingText = { Text(stringResource(R.string.text_2)) },
                onValueChange = { text = it },
                placeholder = { Text(stringResource(R.string.text_33)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { submit() }, enabled = text.isNotBlank() && text.length <= 200) { Text(stringResource(R.string.text_34)) }
        }

        if (tasks.isNotEmpty()) {
            val done = tasks.count { it.completed }
            Spacer(Modifier.height(14.dp))
            Text(
                "$done de ${tasks.size} completadas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { done / tasks.size.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            Text(
                "Aún no hay tareas. Escribe una arriba y pulsa Añadir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        }

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(tasks, key = { it.id }) { task ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = task.completed, onCheckedChange = { onToggle(task.id, it) })
                    Text(
                        task.text,
                        modifier = Modifier.weight(1f).clickable { draft = task.text; editing = task },
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                        color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { onDelete(task.id) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.text_35),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
