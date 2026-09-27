package com.listadecompras.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listadecompras.app.data.Catalog
import com.listadecompras.app.data.Category
import com.listadecompras.app.data.Item
import com.listadecompras.app.data.ItemHistory
import com.listadecompras.app.data.ShoppingRepository
import com.listadecompras.app.data.Suggestion
import com.listadecompras.app.data.categoryEnum
import com.listadecompras.app.data.lineTotal
import com.listadecompras.app.util.Fmt
import com.listadecompras.app.util.QuickAdd
import com.listadecompras.app.util.QuickAddParser
import com.listadecompras.app.util.Text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Section(val category: Category, val items: List<Item>)

data class DetailUiState(
    val loading: Boolean = true,
    val exists: Boolean = true,
    val listName: String = "",
    val sections: List<Section> = emptyList(),
    val checked: List<Item> = emptyList(),
    val totalItems: Int = 0,
    /** Soma de todos os itens com preço. */
    val total: Double = 0.0,
    /** Soma só do que já está no carrinho (marcado). */
    val cartTotal: Double = 0.0,
    /** Quantos itens ainda não têm preço (o total é parcial). */
    val itemsWithoutPrice: Int = 0,
)

class ListDetailViewModel(
    private val repository: ShoppingRepository,
    private val listId: Long,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val items = repository.observeItems(listId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<DetailUiState> =
        combine(repository.observeList(listId), items) { list, its ->
            val pending = its.filter { !it.checked }
            val done = its.filter { it.checked }.sortedByDescending { it.checkedAt ?: 0L }
            DetailUiState(
                loading = false,
                exists = list != null,
                listName = list?.name.orEmpty(),
                sections = pending
                    .groupBy { it.categoryEnum() }
                    .toSortedMap()
                    .map { (category, group) -> Section(category, group) },
                checked = done,
                totalItems = its.size,
                total = its.sumOf { it.lineTotal() ?: 0.0 },
                cartTotal = done.sumOf { it.lineTotal() ?: 0.0 },
                itemsWithoutPrice = its.count { it.price == null },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    val suggestions: StateFlow<List<Suggestion>> =
        combine(_query, repository.observeHistory(), items) { q, history, its ->
            buildSuggestions(q, history, its)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---------- Adicionar ----------

    fun onQueryChange(value: String) {
        _query.value = value
    }

    /** Adiciona exatamente o que foi digitado ("2 leite", "carne 1,5kg"…). */
    fun submitQuery() {
        val text = _query.value
        if (text.isBlank()) return
        _query.value = ""
        viewModelScope.launch { repository.addItem(listId, QuickAddParser.parse(text)) }
    }

    /** Adiciona uma sugestão, aproveitando a quantidade digitada, se houver. */
    fun addSuggestion(suggestion: Suggestion) {
        val parsed = QuickAddParser.parse(_query.value)
        _query.value = ""
        viewModelScope.launch {
            repository.addItem(
                listId,
                QuickAdd(suggestion.name, parsed.quantity, parsed.unit),
                suggestion,
            )
        }
    }

    // ---------- Itens ----------

    fun toggle(item: Item) {
        viewModelScope.launch { repository.setChecked(item, !item.checked) }
    }

    fun update(item: Item) {
        viewModelScope.launch { repository.updateItem(item) }
    }

    fun delete(item: Item) {
        viewModelScope.launch { repository.deleteItem(item) }
    }

    fun restore(item: Item) {
        viewModelScope.launch { repository.restoreItem(item) }
    }

    // ---------- Lista ----------

    fun rename(name: String) {
        viewModelScope.launch { repository.renameList(listId, name) }
    }

    fun uncheckAll() {
        viewModelScope.launch { repository.uncheckAll(listId) }
    }

    fun deleteChecked() {
        viewModelScope.launch { repository.deleteChecked(listId) }
    }

    /** Texto para compartilhar a lista por WhatsApp, e-mail etc. */
    fun shareText(): String {
        val s = state.value
        return buildString {
            appendLine("🛒 ${s.listName}")
            s.sections.forEach { section ->
                appendLine()
                appendLine("${section.category.emoji} ${section.category.label}")
                section.items.forEach { appendLine("▫️ ${describe(it)}") }
            }
            if (s.checked.isNotEmpty()) {
                appendLine()
                appendLine("✅ Já no carrinho")
                s.checked.forEach { appendLine("▪️ ${describe(it)}") }
            }
            if (s.total > 0) {
                appendLine()
                append("Total estimado: ${Fmt.money(s.total)}")
            }
        }.trim()
    }

    private fun describe(item: Item): String {
        val qty = Fmt.quantityWithUnit(item.quantity, item.unit)
        val price = item.lineTotal()?.let { " — ${Fmt.money(it)}" }.orEmpty()
        val note = if (item.note.isNotBlank()) " (${item.note})" else ""
        return "${item.name}, $qty$price$note"
    }
}

private const val MAX_SUGGESTIONS = 8

private fun buildSuggestions(
    query: String,
    history: List<ItemHistory>,
    items: List<Item>,
): List<Suggestion> {
    val q = Text.normalize(QuickAddParser.parse(query).name)
    if (q.isEmpty()) return emptyList()

    val alreadyInList = items.filter { !it.checked }.map { Text.normalize(it.name) }.toSet()

    // 0 = igual, 1 = começa com, 2 = alguma palavra começa com, 3 = contém
    fun score(key: String): Int? = when {
        key == q -> 0
        key.startsWith(q) -> 1
        key.split(' ', '-').any { it.startsWith(q) } -> 2
        q.length >= 3 && key.contains(q) -> 3
        else -> null
    }

    data class Candidate(val suggestion: Suggestion, val score: Int, val uses: Int)

    val candidates = LinkedHashMap<String, Candidate>()

    history.forEach { h ->
        if (h.nameKey in alreadyInList) return@forEach
        val sc = score(h.nameKey) ?: return@forEach
        candidates[h.nameKey] = Candidate(
            Suggestion(h.name, Category.fromName(h.category), h.unit, h.lastPrice, fromHistory = true),
            sc,
            h.useCount,
        )
    }
    Catalog.products.forEach { p ->
        val key = Text.normalize(p.name)
        if (key in alreadyInList || key in candidates) return@forEach
        val sc = score(key) ?: return@forEach
        candidates[key] = Candidate(
            Suggestion(p.name, p.category, p.unit, null, fromHistory = false),
            sc,
            0,
        )
    }

    return candidates.values
        .sortedWith(
            compareBy<Candidate> { it.score }
                .thenByDescending { it.uses }
                .thenBy { it.suggestion.name.length }
        )
        .take(MAX_SUGGESTIONS)
        .map { it.suggestion }
}
