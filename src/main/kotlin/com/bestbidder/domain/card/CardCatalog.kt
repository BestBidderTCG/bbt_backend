package com.bestbidder.domain.card

object CardCatalog {
    val catalog: Map<String, List<String>> =
        mapOf(
            "Pokemon" to
                listOf(
                    "Destinos de paldea",
                    "Brecha Paradojica", "Pokemon 151",
                    "Evoluciones de Paldea", "Surging Sparks",
                ),
            "Riftbound" to
                listOf(
                    "Riftbound: Origins",
                    "Spiritforged",
                    "Unleashed",
                ),
            "Magic" to
                listOf(
                    "Alpha / Beta / Unlimited (1993)",
                    "Ciclo de Urza (Saga, Legado y Destino, 1998-1999)",
                    "Wilds of Eldraine",
                ),
        )

    fun isValid(
        category: String,
        collection: String?,
    ): Boolean =
        catalog[category]?.let { validCollections ->
            collection == null || validCollections.contains(collection)
        } ?: false
}
