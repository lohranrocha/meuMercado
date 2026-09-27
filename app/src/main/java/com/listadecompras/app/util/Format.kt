package com.listadecompras.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.Normalizer
import java.util.Locale

object Fmt {
    private val ptBR: Locale = Locale.forLanguageTag("pt-BR")
    private val money: NumberFormat = NumberFormat.getCurrencyInstance(ptBR)
    private val qty = DecimalFormat("#,##0.###", DecimalFormatSymbols(ptBR))
    private val plain = DecimalFormat("0.##", DecimalFormatSymbols(ptBR))

    /** R$ 12,90 */
    fun money(value: Double): String = money.format(value).replace(' ', ' ')

    /** 2, 1,5, 0,25 */
    fun quantity(value: Double): String = qty.format(value)

    /** "2 un", "1,5 kg" */
    fun quantityWithUnit(value: Double, unit: String): String = "${quantity(value)} $unit"

    /** Valor para preencher um campo de texto editável: "12,9" → "12,90" para dinheiro. */
    fun moneyInput(value: Double?): String =
        value?.let { String.format(ptBR, "%.2f", it) } ?: ""

    fun quantityInput(value: Double): String = plain.format(value)
}

object Text {
    private val accents = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    private val spaces = "\\s+".toRegex()

    /** "  Pão  Francês " → "pao frances" (chave de busca e de histórico). */
    fun normalize(s: String): String =
        Normalizer.normalize(s.trim().lowercase(), Normalizer.Form.NFD)
            .replace(accents, "")
            .replace(spaces, " ")

    /** Primeira letra maiúscula, resto como o usuário digitou. */
    fun capitalizeFirst(s: String): String {
        val t = s.trim().replace(spaces, " ")
        return if (t.isEmpty()) t else t.substring(0, 1).uppercase() + t.substring(1)
    }

    /** Aceita "1,5", "1.5", "R$ 12,90", "1.234,56". Retorna nulo se inválido. */
    fun parseNumber(input: String): Double? {
        var s = input.trim().replace("R$", "").replace(" ", "")
        if (s.isEmpty()) return null
        if (s.contains(',')) {
            // Formato brasileiro: ponto é milhar, vírgula é decimal.
            s = s.replace(".", "").replace(',', '.')
        }
        return s.toDoubleOrNull()?.takeIf { it >= 0 }
    }
}
