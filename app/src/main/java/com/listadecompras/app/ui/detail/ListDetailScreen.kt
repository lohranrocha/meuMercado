package com.listadecompras.app.ui.detail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.listadecompras.app.data.Item
import com.listadecompras.app.data.Suggestion
import com.listadecompras.app.data.Units
import com.listadecompras.app.data.lineTotal
import com.listadecompras.app.ui.components.ConfirmDialog
import com.listadecompras.app.ui.components.NameDialog
import com.listadecompras.app.ui.theme.SectionHeaderStyle
import com.listadecompras.app.util.Fmt
import com.listadecompras.app.util.QuickAddParser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    viewModel: ListDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var editing by remember { mutableStateOf<Item?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var cartExpanded by rememberSaveable { mutableStateOf(true) }

    // A lista foi apagada em outra tela: volta.
    LaunchedEffect(state.loading, state.exists) {
        if (!state.loading && !state.exists) onBack()
    }

    fun deleteWithUndo(item: Item) {
        viewModel.delete(item)
        scope.launch {
            val result = snackbar.showSnackbar(
                message = "\"${item.name}\" removido",
                actionLabel = "Desfazer",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.restore(item)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(state.listName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, viewModel.shareText())
                        }
                        context.startActivity(Intent.createChooser(send, "Compartilhar lista"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Compartilhar")
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Mais opções")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Renomear lista") },
                                onClick = { menuOpen = false; renaming = true },
                            )
                            DropdownMenuItem(
                                text = { Text("Desmarcar todos") },
                                enabled = state.checked.isNotEmpty(),
                                onClick = { menuOpen = false; viewModel.uncheckAll() },
                            )
                            DropdownMenuItem(
                                text = { Text("Remover comprados") },
                                enabled = state.checked.isNotEmpty(),
                                onClick = { menuOpen = false; confirmClear = true },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (state.totalItems > 0 && query.isBlank()) {
                TotalsBar(state)
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AddBar(
                value = query,
                onValueChange = viewModel::onQueryChange,
                onSubmit = viewModel::submitQuery,
            )

            if (query.isNotBlank()) {
                SuggestionsList(
                    query = query,
                    suggestions = suggestions,
                    onAddTyped = viewModel::submitQuery,
                    onPick = viewModel::addSuggestion,
                )
            } else if (!state.loading && state.totalItems == 0) {
                EmptyList()
            } else {
                ItemsList(
                    state = state,
                    cartExpanded = cartExpanded,
                    onToggleCart = { cartExpanded = !cartExpanded },
                    onToggle = viewModel::toggle,
                    onEdit = { editing = it },
                )
            }
        }
    }

    editing?.let { item ->
        ItemEditorSheet(
            item = item,
            onSave = { updated ->
                viewModel.update(updated)
                editing = null
            },
            onDelete = { toDelete ->
                editing = null
                deleteWithUndo(toDelete)
            },
            onDismiss = { editing = null },
        )
    }

    if (renaming) {
        NameDialog(
            title = "Renomear lista",
            initial = state.listName,
            confirmLabel = "Salvar",
            onConfirm = { viewModel.rename(it); renaming = false },
            onDismiss = { renaming = false },
        )
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "Remover comprados?",
            message = "Os ${state.checked.size} itens marcados serão apagados desta lista.",
            confirmLabel = "Remover",
            onConfirm = { viewModel.deleteChecked(); confirmClear = false },
            onDismiss = { confirmClear = false },
        )
    }
}

// ---------------------------------------------------------------------------
// Barra de adicionar
// ---------------------------------------------------------------------------

@Composable
private fun AddBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Adicionar item (ex.: 2 leite)") },
        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Limpar")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
        // Mantém o teclado aberto para adicionar vários itens em sequência.
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SuggestionsList(
    query: String,
    suggestions: List<Suggestion>,
    onAddTyped: () -> Unit,
    onPick: (Suggestion) -> Unit,
) {
    val parsed = remember(query) { QuickAddParser.parse(query) }
    val exactMatch = suggestions.any { it.name.equals(parsed.name.trim(), ignoreCase = true) }
    val qtyHint = parsed.quantity?.let { q ->
        " · ${Fmt.quantity(q)}${parsed.unit?.let { " $it" }.orEmpty()}"
    }.orEmpty()

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (!exactMatch && parsed.name.isNotBlank()) {
            item(key = "typed") {
                SuggestionRow(
                    title = "Adicionar \"${parsed.name.trim()}\"",
                    subtitle = "Novo item$qtyHint",
                    emoji = "➕",
                    onClick = onAddTyped,
                )
            }
        }
        items(suggestions, key = { "s-" + it.name }) { s ->
            SuggestionRow(
                title = s.name,
                subtitle = buildString {
                    append(s.category.label)
                    append(qtyHint)
                    s.lastPrice?.let { append(" · último: ${Fmt.money(it)}") }
                },
                emoji = s.category.emoji,
                onClick = { onPick(s) },
            )
        }
    }
}

@Composable
private fun SuggestionRow(title: String, subtitle: String, emoji: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(40.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.Filled.Add,
            contentDescription = "Adicionar",
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

// ---------------------------------------------------------------------------
// Itens
// ---------------------------------------------------------------------------

@Composable
private fun ItemsList(
    state: DetailUiState,
    cartExpanded: Boolean,
    onToggleCart: () -> Unit,
    onToggle: (Item) -> Unit,
    onEdit: (Item) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.sections, key = { it.category.name }) { section ->
            Column {
                SectionHeader("${section.category.emoji}  ${section.category.label.uppercase()}")
                SectionCard(section.items, onToggle, onEdit)
            }
        }

        if (state.sections.isEmpty() && state.checked.isNotEmpty()) {
            item(key = "all-done") {
                Text(
                    "🎉 Tudo no carrinho!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                )
            }
        }

        if (state.checked.isNotEmpty()) {
            item(key = "cart") {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggleCart),
                    ) {
                        SectionHeader(
                            "✅  NO CARRINHO (${state.checked.size})",
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            if (cartExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (cartExpanded) "Recolher" else "Expandir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    AnimatedVisibility(visible = cartExpanded) {
                        SectionCard(state.checked, onToggle, onEdit)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = SectionHeaderStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun SectionCard(items: List<Item>, onToggle: (Item) -> Unit, onEdit: (Item) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 56.dp),
                    )
                }
                ItemRow(item, onToggle = { onToggle(item) }, onEdit = { onEdit(item) })
            }
        }
    }
}

@Composable
private fun ItemRow(item: Item, onToggle: () -> Unit, onEdit: () -> Unit) {
    val total = item.lineTotal()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 6.dp)
            .alpha(if (item.checked) 0.55f else 1f),
    ) {
        Checkbox(checked = item.checked, onCheckedChange = { onToggle() })
        Column(
            Modifier
                .weight(1f)
                .padding(start = 4.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val details = buildList {
                add(Fmt.quantityWithUnit(item.quantity, item.unit))
                if (item.note.isNotBlank()) add(item.note)
            }.joinToString(" · ")
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (total != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Fmt.money(total),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (item.quantity != 1.0 || item.unit != "un") {
                    Text(
                        text = "${Fmt.money(item.price ?: 0.0)}/${Units.priceLabel(item.unit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyList() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        Text("📝", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text("Lista vazia", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Digite na barra acima. Dá para já informar a quantidade: \"2 leite\", \"carne 1,5 kg\".",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------------------------
// Totais
// ---------------------------------------------------------------------------

@Composable
private fun TotalsBar(state: DetailUiState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "No carrinho",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        Fmt.money(state.cartTotal),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total da lista",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        Fmt.money(state.total),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (state.itemsWithoutPrice > 0) {
                val n = state.itemsWithoutPrice
                Text(
                    text = if (n == 1) "1 item sem preço" else "$n itens sem preço",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
