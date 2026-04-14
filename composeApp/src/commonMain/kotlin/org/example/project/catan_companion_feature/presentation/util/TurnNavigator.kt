package org.example.project.catan_companion_feature.presentation.util

import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.session.GameSession

class TurnNavigator(
    private val turns: List<Turn>,
    private val selectedIndex: Int = turns.lastIndex.coerceAtLeast(0)
) {
    val selectedTurn: Turn? get() = turns.getOrNull(selectedIndex)
    val isViewingLatest: Boolean get() = selectedIndex == turns.lastIndex
    val hasPrevious: Boolean get() = selectedIndex > 0
    val hasNext: Boolean get() = selectedIndex < turns.lastIndex

    fun selectPrevious(): TurnNavigator {
        if (!hasPrevious) return this
        return TurnNavigator(turns, selectedIndex - 1)
    }

    fun selectNext(): TurnNavigator {
        if (!hasNext) return this
        return TurnNavigator(turns, selectedIndex + 1)
    }

    fun selectLatest(): TurnNavigator {
        return TurnNavigator(turns, turns.lastIndex.coerceAtLeast(0))
    }

    companion object {
        fun from(session: GameSession): TurnNavigator {
            val allTurns = session.recentTurns + session.latestTurn
            return TurnNavigator(allTurns)
        }
    }
}
