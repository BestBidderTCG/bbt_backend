package com.bestbidder.domain.card

object CardCatalog {
    val catalog: Map<String, List<String>> =
        mapOf(
            "Pokémon" to
                listOf(
                    "Destinos de Paldea",
                    "Brecha Paradójica",
                    "Pokémon 151",
                    "Evoluciones de Paldea",
                    "Surging Sparks",
                ),
            "Riftbound" to
                listOf(
                    "Riftbound: Origins",
                    "Spiritforged",
                    "Unleashed",
                ),
            "Magic" to
                listOf(
                    "Alpha / Beta / Unlimited",
                    "Ciclo de Urza (1998-1999)",
                    "Wilds of Eldraine",
                ),
        )

    fun isValid(
        category: String?,
        collection: String?,
    ): Boolean =
        when {
            category == null -> collection == null
            else -> catalog[category]?.let { collection == null || it.contains(collection) } ?: false
        }
}
