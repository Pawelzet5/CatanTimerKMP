package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameconfig

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player

class PlayersSelectionViewModel : ViewModel() {

    private val _pendingSelection = MutableStateFlow<List<Player>?>(null)
    val pendingSelection: StateFlow<List<Player>?> = _pendingSelection.asStateFlow()

    fun setSelectedPlayers(players: List<Player>) {
        _pendingSelection.value = players
    }

    fun clearSelection() {
        _pendingSelection.value = null
    }
}
