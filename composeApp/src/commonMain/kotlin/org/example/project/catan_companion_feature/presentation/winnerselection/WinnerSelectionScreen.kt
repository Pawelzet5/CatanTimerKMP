package org.example.project.catan_companion_feature.presentation.winnerselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.end_game_confirm
import catantimer.composeapp.generated.resources.end_game_no_winner
import catantimer.composeapp.generated.resources.end_game_select_winner
import catantimer.composeapp.generated.resources.ic_close
import org.example.project.catan_companion_feature.domain.dataclass.GamePlayer
import org.example.project.catan_companion_feature.presentation.gameplay.GameplayAction
import org.example.project.catan_companion_feature.presentation.gameplay.GameplayEvent
import org.example.project.catan_companion_feature.presentation.gameplay.GameplayState
import org.example.project.catan_companion_feature.presentation.gameplay.GameplayViewModel
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WinnerSelectionScreenRoot(
    gameId: Long,
    onGameFinished: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameplayViewModel = koinViewModel { parametersOf(gameId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is GameplayEvent.NavigateToGameSummary -> onGameFinished(event.gameId)
            else -> Unit
        }
    }

    WinnerSelectionScreen(
        state = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinnerSelectionScreen(
    state: GameplayState,
    onAction: (GameplayAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val players = state.game?.players?.sortedBy { it.orderIndex } ?: emptyList()
    var selectedWinnerId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.end_game_select_winner),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CatanSpacing.md)
            ) {
                Button(
                    onClick = { onAction(GameplayAction.ConfirmWinnerClick(selectedWinnerId)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.end_game_confirm),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = CatanSpacing.sm)
        ) {
            items(players, key = { it.playerId }) { player ->
                PlayerRadioItem(
                    player = player,
                    selected = selectedWinnerId == player.playerId,
                    onSelect = { selectedWinnerId = player.playerId }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = CatanSpacing.sm))
            }
            item {
                NoWinnerRadioItem(
                    selected = selectedWinnerId == null,
                    onSelect = { selectedWinnerId = null }
                )
            }
        }
    }
}

@Composable
private fun PlayerRadioItem(
    player: GamePlayer,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CatanSpacing.md, vertical = CatanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.md)
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Text(
            text = player.playerName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NoWinnerRadioItem(
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CatanSpacing.md, vertical = CatanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.md)
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Column {
            Text(
                text = stringResource(Res.string.end_game_no_winner),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(CatanSpacing.xs))
        }
    }
}
