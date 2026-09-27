package com.listadecompras.app.util

import com.listadecompras.app.data.Units

/**
 * Interpreta o que foi digitado na barra de adicionar, no estilo do Listonic:
 *   "leite"            → leite, 1 (sem quantidade explícita)
 *   "2 leite"          → leite, 2
 *   "1,5 kg carne"     → carne, 1,5 kg
 *   "banana 6"         → banana, 6
 *   "picanha 1.2kg"    → picanha, 1,2 kg
 */
data class QuickAdd(val name: String, val quantity: Double?, val unit: String?)

object QuickAddParser {
    private val unitPattern = Units.all.joinToString("|") { Regex.escape(it) }
    private val number = "(\\d+(?:[.,]\\d+)?)"

    private val leading = Regex("^$number\\s*($unitPattern)?\\s+(.+)$", RegexOption.IGNORE_CASE)
    private val trailing = Regex("^(.+?)\\s+$number\\s*($unitPattern)?$", RegexOption.IGNORE_CASE)

    fun parse(input: String): QuickAdd {
        val text = input.trim()
        leading.matchEntire(text)?.let { m ->
            val (qty, unit, name) = m.destructured
            return QuickAdd(name.trim(), Text.parseNumber(qty), canonicalUnit(unit))
        }
        trailing.matchEntire(text)?.let { m ->
            val (name, qty, unit) = m.destructured
            return QuickAdd(name.trim(), Text.parseNumber(qty), canonicalUnit(unit))
        }
        return QuickAdd(text, null, null)
    }

    private fun canonicalUnit(raw: String): String? {
        if (raw.isBlank()) return null
        return Units.all.firstOrNull { it.equals(raw, ignoreCase = true) }
    }
}
