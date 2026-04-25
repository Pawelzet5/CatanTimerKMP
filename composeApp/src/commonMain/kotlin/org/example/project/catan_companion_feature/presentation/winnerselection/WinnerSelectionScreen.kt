package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.winnerselection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.end_game_confirm
import catantimer.composeapp.generated.resources.end_game_no_winner
import catantimer.composeapp.generated.resources.end_game_select_winner
import catantimer.composeapp.generated.resources.end_game_who_won
import catantimer.composeapp.generated.resources.ic_close
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.GamePlayer
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay.GameplayAction
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay.GameplayEvent
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay.GameplayState
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay.GameplayViewModel
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import io.github.pawelzielinski.catantimer.core.designsystem.components.CatanButton
import io.github.pawelzielinski.catantimer.core.presentation.ObserveAsEvents
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
                CatanButton(
                    text = stringResource(Res.string.end_game_confirm),
                    onClick = { onAction(GameplayAction.ConfirmWinnerClick(selectedWinnerId)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = CatanSpacing.md,
                vertical = CatanSpacing.md
            ),
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
        ) {
            item {
                Text(
                    text = stringResource(Res.string.end_game_who_won),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = CatanSpacing.xs)
                )
            }

            items(players, key = { it.playerId }) { player ->
                RadioItem(
                    text = player.playerName,
                    selected = selectedWinnerId == player.playerId,
                    onSelect = { selectedWinnerId = player.playerId }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = CatanSpacing.xs))
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
private fun NoWinnerRadioItem(
    selected: Boolean,
    onSelect: () -> Unit
) {
    RadioItem(
        text = stringResource(Res.string.end_game_no_winner),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun RadioItem(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = backgroundColor, shape = shape)
            .then(
                if (selected) Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = shape
                ) else Modifier
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = CatanSpacing.sm, vertical = CatanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}
