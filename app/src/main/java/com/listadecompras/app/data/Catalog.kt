package com.listadecompras.app.data

import com.listadecompras.app.util.Text

/**
 * Catálogo embutido de produtos comuns em supermercados brasileiros.
 * Serve para categorizar automaticamente o que é digitado e para sugerir
 * itens antes de o usuário ter histórico próprio.
 */
object Catalog {

    data class Product(val name: String, val category: Category, val unit: String)

    private val raw: Map<Category, String> = mapOf(
        Category.HORTIFRUTI to """
            Banana, Maçã, Laranja, Limão, Mamão, Melancia, Melão, Abacaxi, Uva, Morango, Manga,
            Pera, Kiwi, Abacate, Maracujá, Goiaba, Tangerina, Coco, Tomate, Cebola, Alho, Batata,
            Batata-doce, Cenoura, Beterraba, Mandioca, Abóbora, Abobrinha, Berinjela, Chuchu,
            Pepino, Pimentão, Brócolis, Couve-flor, Couve, Repolho, Alface, Rúcula, Agrião,
            Espinafre, Cheiro-verde, Salsinha, Cebolinha, Coentro, Hortelã, Manjericão, Gengibre,
            Quiabo, Vagem, Milho verde, Cogumelo, Ovos
        """,
        Category.PADARIA to """
            Pão francês, Pão de forma, Pão integral, Pão de queijo, Pão de hambúrguer,
            Pão de cachorro-quente, Bisnaguinha, Torrada, Bolo, Croissant, Sonho, Rosca, Broa,
            Tortilha, Pão sírio
        """,
        Category.CARNES to """
            Carne moída, Patinho, Alcatra, Picanha, Contrafilé, Fraldinha, Maminha, Acém, Músculo,
            Costela, Cupim, Filé mignon, Frango, Peito de frango, Coxa de frango, Sobrecoxa,
            Asa de frango, Filé de frango, Linguiça, Linguiça toscana, Calabresa, Bacon,
            Carne de porco, Lombo, Bisteca, Pernil, Costelinha de porco, Peixe, Tilápia,
            Salmão, Merluza, Sardinha fresca, Camarão, Bacalhau, Hambúrguer, Almôndega
        """,
        Category.FRIOS_LATICINIOS to """
            Leite, Leite desnatado, Leite sem lactose, Queijo mussarela, Queijo prato,
            Queijo minas, Queijo parmesão, Queijo coalho, Requeijão, Cream cheese, Presunto,
            Peito de peru, Mortadela, Salame, Salsicha, Manteiga, Margarina, Iogurte,
            Iogurte natural, Bebida láctea, Creme de leite fresco, Nata, Ricota, Coalhada
        """,
        Category.MATINAIS to """
            Café, Café em cápsula, Café solúvel, Filtro de café, Achocolatado, Cereal, Granola,
            Aveia, Chá, Mel, Geleia, Adoçante, Leite em pó, Tapioca, Cuscuz, Farinha de milho,
            Pasta de amendoim
        """,
        Category.MERCEARIA to """
            Arroz, Arroz integral, Feijão, Feijão preto, Macarrão, Macarrão instantâneo,
            Lasanha, Farinha de trigo, Farinha de mandioca, Farofa, Fubá, Amido de milho,
            Açúcar, Açúcar mascavo, Sal, Óleo, Azeite, Vinagre, Molho de tomate, Extrato de tomate,
            Maionese, Ketchup, Mostarda, Molho shoyu, Caldo de galinha, Tempero pronto,
            Pimenta-do-reino, Orégano, Colorau, Canela, Fermento, Gelatina, Leite condensado,
            Creme de leite, Milho em conserva, Ervilha, Seleta de legumes, Atum, Sardinha,
            Azeitona, Palmito, Grão-de-bico, Lentilha, Pipoca, Batata palha, Castanha, Uva-passa,
            Coco ralado
        """,
        Category.DOCES_SNACKS to """
            Chocolate, Bombom, Biscoito, Biscoito recheado, Bolacha água e sal, Cookie,
            Salgadinho, Batata chips, Amendoim, Bala, Chiclete, Paçoca, Doce de leite,
            Barra de cereal, Sorvete de pote, Wafer
        """,
        Category.BEBIDAS to """
            Água, Água com gás, Refrigerante, Coca-Cola, Guaraná, Suco, Suco de caixinha,
            Suco em pó, Cerveja, Vinho, Cachaça, Vodka, Energético, Isotônico, Chá gelado,
            Água de coco, Leite de coco
        """,
        Category.CONGELADOS to """
            Pizza congelada, Lasanha congelada, Batata frita congelada, Nuggets, Polpa de fruta,
            Sorvete, Açaí, Gelo, Pão de queijo congelado, Legumes congelados, Hambúrguer congelado,
            Empanado
        """,
        Category.LIMPEZA to """
            Detergente, Sabão em pó, Sabão líquido, Amaciante, Água sanitária, Desinfetante,
            Multiuso, Limpa vidros, Esponja, Palha de aço, Pano de chão, Pano de prato,
            Saco de lixo, Lustra-móveis, Álcool, Álcool em gel, Removedor, Tira-manchas,
            Sabão em barra, Vassoura, Rodo, Luva de limpeza, Inseticida, Desengordurante
        """,
        Category.HIGIENE to """
            Papel higiênico, Sabonete, Sabonete líquido, Shampoo, Condicionador, Creme dental,
            Escova de dente, Fio dental, Enxaguante bucal, Desodorante, Absorvente,
            Aparelho de barbear, Espuma de barbear, Algodão, Cotonete, Hidratante, Protetor solar,
            Lenço umedecido, Lenço de papel, Curativo, Creme de cabelo
        """,
        Category.BEBE to """
            Fralda, Lenço umedecido infantil, Pomada para assadura, Papinha, Leite infantil,
            Shampoo infantil
        """,
        Category.PET to """
            Ração, Ração de gato, Ração de cachorro, Areia de gato, Petisco pet, Sachê
        """,
        Category.CASA to """
            Papel toalha, Papel alumínio, Filme plástico, Guardanapo, Copo descartável,
            Prato descartável, Pilha, Lâmpada, Vela, Fósforo, Isqueiro, Carvão, Saco para freezer
        """,
    )

    /** Seções em que o normal é comprar por peso. */
    private val byWeight = setOf(Category.CARNES)
    private val weightExceptions = setOf("hamburguer", "almondega", "bacalhau")

    val products: List<Product> = raw.flatMap { (category, names) ->
        names.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { name ->
                val key = Text.normalize(name)
                val unit = if (category in byWeight && key !in weightExceptions) "kg" else "un"
                Product(name, category, unit)
            }
    }

    private val byKey: Map<String, Product> = products.associateBy { Text.normalize(it.name) }

    fun find(name: String): Product? = byKey[Text.normalize(name)]

    /**
     * Adivinha a seção de um nome livre: primeiro pelo nome exato, depois pela
     * primeira palavra ("Leite Italac integral" → Leite → Frios e laticínios).
     */
    fun guessCategory(name: String): Category? {
        val key = Text.normalize(name)
        byKey[key]?.let { return it.category }
        // Procura o produto de catálogo mais longo contido no começo do nome.
        return products
            .asSequence()
            .map { Text.normalize(it.name) to it }
            .filter { (k, _) -> key.startsWith("$k ") }
            .maxByOrNull { (k, _) -> k.length }
            ?.second?.category
            ?: byKey[key.substringBefore(' ')]?.category
    }
}
