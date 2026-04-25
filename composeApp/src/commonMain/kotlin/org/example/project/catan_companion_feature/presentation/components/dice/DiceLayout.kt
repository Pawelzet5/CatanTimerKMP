package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.dice

import androidx.compose.ui.geometry.Offset

object DiceLayout {

    // Returns dot center positions as fractions of dice size (0f..1f)
    fun getPositions(value: Int): List<Offset> = when (value) {
        1 -> listOf(
            Offset(0.5f, 0.5f)
        )
        2 -> listOf(
            Offset(0.25f, 0.25f),
            Offset(0.75f, 0.75f)
        )
        3 -> listOf(
            Offset(0.25f, 0.25f),
            Offset(0.5f, 0.5f),
            Offset(0.75f, 0.75f)
        )
        4 -> listOf(
            Offset(0.25f, 0.25f),
            Offset(0.75f, 0.25f),
            Offset(0.25f, 0.75f),
            Offset(0.75f, 0.75f)
        )
        5 -> listOf(
            Offset(0.25f, 0.25f),
            Offset(0.75f, 0.25f),
            Offset(0.5f, 0.5f),
            Offset(0.25f, 0.75f),
            Offset(0.75f, 0.75f)
        )
        6 -> listOf(
            Offset(0.25f, 0.2f),
            Offset(0.75f, 0.2f),
            Offset(0.25f, 0.5f),
            Offset(0.75f, 0.5f),
            Offset(0.25f, 0.8f),
            Offset(0.75f, 0.8f)
        )
        else -> emptyList()
    }
}
