package com.listadecompras.app.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listadecompras.app.data.ShoppingRepository
import com.listadecompras.app.data.lineTotal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ListSummary(
    val id: Long,
    val name: String,
    val totalItems: Int,
    val checkedItems: Int,
    val total: Double,
)

data class ListsUiState(
    val loading: Boolean = true,
    val lists: List<ListSummary> = emptyList(),
)

class ListsViewModel(private val repository: ShoppingRepository) : ViewModel() {

    val state: StateFlow<ListsUiState> =
        combine(repository.observeLists(), repository.observeAllItems()) { lists, items ->
            val byList = items.groupBy { it.listId }
            ListsUiState(
                loading = false,
                lists = lists.map { list ->
                    val its = byList[list.id].orEmpty()
                    ListSummary(
                        id = list.id,
                        name = list.name,
                        totalItems = its.size,
                        checkedItems = its.count { it.checked },
                        total = its.sumOf { it.lineTotal() ?: 0.0 },
                    )
                },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListsUiState())

    fun createList(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch { onCreated(repository.createList(name)) }
    }

    fun renameList(id: Long, name: String) {
        viewModelScope.launch { repository.renameList(id, name) }
    }

    fun duplicateList(id: Long) {
        viewModelScope.launch { repository.duplicateList(id) }
    }

    fun deleteList(id: Long) {
        viewModelScope.launch { repository.deleteList(id) }
    }
}
