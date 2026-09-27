package com.listadecompras.app.data

/**
 * Seções do mercado. A ordem do enum é a ordem em que aparecem na lista,
 * pensada para o caminho típico dentro de um supermercado.
 */
enum class Category(val label: String, val emoji: String) {
    HORTIFRUTI("Hortifrúti", "🥬"),
    PADARIA("Padaria", "🥖"),
    CARNES("Carnes e peixes", "🥩"),
    FRIOS_LATICINIOS("Frios e laticínios", "🧀"),
    MATINAIS("Matinais", "☕"),
    MERCEARIA("Mercearia", "🥫"),
    DOCES_SNACKS("Doces e snacks", "🍫"),
    BEBIDAS("Bebidas", "🥤"),
    CONGELADOS("Congelados", "🧊"),
    LIMPEZA("Limpeza", "🧽"),
    HIGIENE("Higiene e beleza", "🧴"),
    BEBE("Bebê", "🍼"),
    PET("Pet", "🐾"),
    CASA("Casa e utilidades", "🏠"),
    OUTROS("Outros", "🛒");

    companion object {
        fun fromName(name: String?): Category =
            entries.firstOrNull { it.name == name } ?: OUTROS
    }
}

/** Unidades de medida oferecidas no editor de item. */
object Units {
    val all = listOf("un", "kg", "g", "L", "ml", "pct", "cx", "dz")

    /**
     * Para gramas e mililitros o preço digitado é por kg / por litro,
     * que é como o mercado mostra na etiqueta.
     */
    fun priceFactor(unit: String): Double = when (unit) {
        "g", "ml" -> 0.001
        else -> 1.0
    }

    /** Texto exibido ao lado do campo de preço: "por kg", "por un" etc. */
    fun priceLabel(unit: String): String = when (unit) {
        "g" -> "kg"
        "ml" -> "L"
        else -> unit
    }

    /** Unidades em que faz sentido somar +1 quando o item é adicionado de novo. */
    fun isCountable(unit: String): Boolean = unit in setOf("un", "pct", "cx", "dz")
}
