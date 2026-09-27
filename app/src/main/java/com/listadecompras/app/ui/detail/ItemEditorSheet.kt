package com.listadecompras.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.listadecompras.app.data.Category
import com.listadecompras.app.data.Item
import com.listadecompras.app.data.Units
import com.listadecompras.app.data.categoryEnum
import com.listadecompras.app.util.Fmt
import com.listadecompras.app.util.Text as TextUtil

/** Painel inferior para editar todos os detalhes de um item. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorSheet(
    item: Item,
    onSave: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(item.name) }
    var quantityText by remember { mutableStateOf(Fmt.quantityInput(item.quantity)) }
    var unit by remember { mutableStateOf(item.unit) }
    var priceText by remember { mutableStateOf(Fmt.moneyInput(item.price)) }
    var category by remember { mutableStateOf(item.categoryEnum()) }
    var note by remember { mutableStateOf(item.note) }

    val quantity = TextUtil.parseNumber(quantityText)
    val price = TextUtil.parseNumber(priceText)
    val quantityValid = quantity != null && quantity > 0
    val priceValid = priceText.isBlank() || price != null
    val canSave = name.isNotBlank() && quantityValid && priceValid

    fun step(): Double = when (unit) {
        "g", "ml" -> 100.0
        "kg", "L" -> 0.5
        else -> 1.0
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))
            FieldLabel("Quantidade")
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(
                    onClick = {
                        val q = (quantity ?: 0.0) - step()
                        if (q > 0) quantityText = Fmt.quantityInput(q)
                    },
                ) { Text("−", style = MaterialTheme.typography.titleLarge) }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    singleLine = true,
                    isError = !quantityValid,
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(110.dp),
                )

                FilledTonalIconButton(
                    onClick = { quantityText = Fmt.quantityInput((quantity ?: 0.0) + step()) },
                ) { Text("+", style = MaterialTheme.typography.titleLarge) }
            }

            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(Units.all) { u ->
                    FilterChip(
                        selected = u == unit,
                        onClick = { unit = u },
                        label = { Text(u) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            FieldLabel("Preço")
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                singleLine = true,
                isError = !priceValid,
                placeholder = { Text("0,00") },
                prefix = { Text("R$ ") },
                suffix = { Text("por ${Units.priceLabel(unit)}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            if (quantityValid && price != null && price > 0) {
                val lineTotal = price * quantity!! * Units.priceFactor(unit)
                Text(
                    text = "Total deste item: ${Fmt.money(lineTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            FieldLabel("Seção do mercado")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(Category.entries) { c ->
                    FilterChip(
                        selected = c == category,
                        onClick = { category = c },
                        label = { Text("${c.emoji} ${c.label}") },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Observação (marca, tamanho…)") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { onDelete(item) }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.weight(1f))
                Button(
                    enabled = canSave,
                    onClick = {
                        onSave(
                            item.copy(
                                name = name.trim(),
                                quantity = quantity ?: item.quantity,
                                unit = unit,
                                price = price?.takeIf { it > 0 },
                                category = category.name,
                                note = note,
                            )
                        )
                    },
                ) { Text("Salvar") }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
