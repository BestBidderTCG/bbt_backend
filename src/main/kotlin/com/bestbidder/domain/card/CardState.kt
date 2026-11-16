package com.bestbidder.domain.card

/**
 * Representa los estados físicos/conservación posibles de una carta.
 */
enum class CardState {
    NEW, // Impecable / Como nueva
    NEAR_MINT, // Casi nueva
    GOOD, // Usada / Buen estado
    LIGHT_PLAYED, // Con marcas ligeras de uso
    POOR, // Deteriorada / Mal estado
}
