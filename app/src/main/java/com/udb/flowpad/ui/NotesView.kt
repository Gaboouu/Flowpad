package com.udb.flowpad.ui

import androidx.compose.ui.res.stringResource
import com.udb.flowpad.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.udb.flowpad.data.NoteBlockEntity
import com.udb.flowpad.data.NoteType

@Composable
fun NotesView(
    blocks: List<NoteBlockEntity>,
    onChange: (Long, String) -> Unit,
    onMove: (NoteBlockEntity, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (blocks.isEmpty()) {
        Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "Aún no hay notas.\nUsa el botón «Bloque» para añadir texto o encabezados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(blocks, key = { _, b -> b.id }) { index, block ->
            NoteBlockItem(
                block = block,
                canUp = index > 0,
                canDown = index < blocks.lastIndex,
                onChange = { onChange(block.id, it) },
                onUp = { onMove(block, true) },
                onDown = { onMove(block, false) },
                onDelete = { onDelete(block.id) }
            )
        }
    }
}

@Composable
private fun NoteBlockItem(
    block: NoteBlockEntity,
    canUp: Boolean,
    canDown: Boolean,
    onChange: (String) -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onDelete: () -> Unit
) {
    // El texto se mantiene localmente para que el cursor no salte al guardar en la base de datos.
    var text by remember(block.id) { mutableStateOf(block.content) }
    var menu by remember { mutableStateOf(false) }
    val heading = block.type == NoteType.HEADING

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            TextField(
                value = text,
                onValueChange = {
                    text = it
                    onChange(it)
                },
                modifier = Modifier.weight(1f),
                textStyle = if (heading) {
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                placeholder = { Text(if (heading) stringResource(R.string.label_53) else stringResource(R.string.label_54)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.text_38))
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.text_36)) },
                        enabled = canUp,
                        onClick = { menu = false; onUp() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.text_37)) },
                        enabled = canDown,
                        onClick = { menu = false; onDown() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.text_14), color = MaterialTheme.colorScheme.error) },
                        onClick = { menu = false; onDelete() }
                    )
                }
            }
        }
    }
}
