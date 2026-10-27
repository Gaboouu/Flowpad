package com.udb.flowpad.ui

import androidx.compose.ui.res.stringResource
import com.udb.flowpad.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.flowpad.data.ProjectEmojis
import com.udb.flowpad.data.ProjectEntity
import com.udb.flowpad.ui.theme.Covers

@Composable
fun NewProjectDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.text_1)) },
        text = {
            OutlinedTextField(
                value = name,
                isError = name.isBlank() || name.length > 200,
                supportingText = { Text(stringResource(R.string.text_2)) },
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.text_3)) },
                placeholder = { Text(stringResource(R.string.text_4)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && name.length <= 200, onClick = { onCreate(name) }) { Text(stringResource(R.string.text_5)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.text_6)) } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProjectDialog(
    project: ProjectEntity,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, emoji: String, cover: Int) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(project.name) }
    var description by remember { mutableStateOf(project.description) }
    var emoji by remember { mutableStateOf(project.emoji) }
    var cover by remember { mutableStateOf(project.coverIndex) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.text_7)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                isError = name.isBlank() || name.length > 200,
                supportingText = { Text(stringResource(R.string.text_2)) },
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.text_3)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.text_8)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.text_9), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectEmojis.forEach { e ->
                        val selected = e == emoji
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(shape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .then(
                                    if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                                    else Modifier
                                )
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) { Text(e, fontSize = 22.sp) }
                    }
                }

                Text(stringResource(R.string.text_10), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Covers.forEachIndexed { i, c ->
                        val selected = i == cover
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            Modifier
                                .size(width = 56.dp, height = 36.dp)
                                .clip(shape)
                                .background(c.brush)
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, shape)
                                    else Modifier
                                )
                                .clickable { cover = i }
                        )
                    }
                }

                if (canDelete) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text(stringResource(R.string.text_11), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && name.length <= 200,
                onClick = { onSave(name, description, emoji, cover) }
            ) { Text(stringResource(R.string.text_12)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.text_6)) } }
    )

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("¿Eliminar «${project.name}»?") },
            text = { Text(stringResource(R.string.text_13)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text(stringResource(R.string.text_14), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.text_6)) } }
        )
    }
}
