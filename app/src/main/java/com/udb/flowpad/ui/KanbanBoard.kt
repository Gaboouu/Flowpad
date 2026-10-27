package com.udb.flowpad.ui

import androidx.compose.ui.res.stringResource
import com.udb.flowpad.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.flowpad.data.CardEntity
import com.udb.flowpad.data.KanbanStatus
import kotlinx.coroutines.launch

private data class CardDialogState(val card: CardEntity?, val status: KanbanStatus)

/**
 * Tablero Kanban: Por hacer -> En proceso -> Hecho.
 * Una columna por "página" (se desliza de lado) y chips arriba para saltar entre ellas.
 */
@Composable
fun KanbanBoard(
    cards: List<CardEntity>,
    onAdd: (String, KanbanStatus) -> Unit,
    onRename: (Long, String) -> Unit,
    onMove: (CardEntity, Int) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { KanbanStatus.entries.size })
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf<CardDialogState?>(null) }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KanbanStatus.entries.forEachIndexed { index, status ->
                val count = cards.count { it.status == status.key }
                FilterChip(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    label = { Text("${status.label} · $count", fontSize = 12.sp, maxLines = 1) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 36.dp, bottom = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val status = KanbanStatus.entries[page]
            KanbanColumn(
                status = status,
                cards = cards.filter { it.status == status.key },
                onAdd = { dialog = CardDialogState(null, status) },
                onEdit = { dialog = CardDialogState(it, status) },
                onMove = onMove,
                onDelete = onDelete,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }

    dialog?.let { state ->
        CardDialog(
            state = state,
            onDismiss = { dialog = null },
            onConfirm = { title ->
                if (state.card == null) onAdd(title, state.status) else onRename(state.card.id, title)
                dialog = null
            }
        )
    }
}

@Composable
private fun KanbanColumn(
    status: KanbanStatus,
    cards: List<CardEntity>,
    onAdd: () -> Unit,
    onEdit: (CardEntity) -> Unit,
    onMove: (CardEntity, Int) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(status.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.background) {
                Text(
                    "${cards.size}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (cards.isEmpty()) {
                item {
                    Text(
                        "Sin tarjetas todavía",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp, horizontal = 4.dp)
                    )
                }
            }
            items(cards, key = { it.id }) { card ->
                val index = KanbanStatus.fromKey(card.status).ordinal
                KanbanCard(
                    card = card,
                    canLeft = index > 0,
                    canRight = index < KanbanStatus.entries.lastIndex,
                    onEdit = { onEdit(card) },
                    onMove = { delta -> onMove(card, delta) },
                    onDelete = { onDelete(card.id) }
                )
            }
        }

        TextButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.text_15))
        }
    }
}

@Composable
private fun KanbanCard(
    card: CardEntity,
    canLeft: Boolean,
    canRight: Boolean,
    onEdit: () -> Unit,
    onMove: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .clickable(onClick = onEdit)
                .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 4.dp)
        ) {
            Text(card.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMove(-1) }, enabled = canLeft, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.text_17))
                }
                IconButton(onClick = { onMove(1) }, enabled = canRight, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.text_18))
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.text_19),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CardDialog(
    state: CardDialogState,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(state.card?.title ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (state.card == null) "Nueva tarjeta · ${state.status.label}" else stringResource(R.string.label_47))
        },
        text = {
            OutlinedTextField(
                value = title,
                isError = title.isBlank() || title.length > 200,
                supportingText = { Text(stringResource(R.string.text_2)) },
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.text_16)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank() && title.length <= 200, onClick = { onConfirm(title) }) {
                Text(if (state.card == null) stringResource(R.string.label_48) else stringResource(R.string.label_49))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.text_6)) } }
    )
}
