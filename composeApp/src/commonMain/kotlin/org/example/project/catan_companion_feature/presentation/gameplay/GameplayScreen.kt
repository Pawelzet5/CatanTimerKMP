package org.example.project.catan_companion_feature.presentation.gameplay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.gameplay_continue
import catantimer.composeapp.generated.resources.gameplay_player_turn
import catantimer.composeapp.generated.resources.gameplay_turn
import org.example.project.catan_companion_feature.domain.enums.DiceType
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.presentation.components.dice.DiceRow
import org.example.project.catan_companion_feature.presentation.components.dice.EventDiceRow
import org.example.project.catan_companion_feature.presentation.components.gameplay.BarbarianTracker
import org.example.project.core.designsystem.CatanSpacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplayScreen(
    gameId: Long,
    onNavigateToSummary: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameplayViewModel = koinViewModel { parametersOf(gameId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GameplayAppBar(
                uiState = uiState,
                onMenuClick = viewModel::onShowSettingsSheet
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TurnNavigatorBar(
                uiState = uiState,
                onPrevious = viewModel::onNavigateToPreviousTurn,
                onNext = viewModel::onNavigateToNextTurn,
                onJumpToCurrent = viewModel::onJumpToCurrentTurn
            )

            when (uiState.phase) {
                GameplayPhase.DICE_SELECTION -> DiceSelectionContent(
                    uiState = uiState,
                    onDiceSelected = viewModel::onDiceSelected,
                    onEventDiceSelected = { event ->
                        val dice = uiState.pendingDiceEdit
                        viewModel.onDiceSelected(dice?.red ?: 0, dice?.yellow ?: 0, event)
                    },
                    onContinue = viewModel::onContinueFromDice
                )
                GameplayPhase.EVENT -> { /* PR 14b */ }
                GameplayPhase.MAIN_TIMER -> { /* PR 14b */ }
                GameplayPhase.IN_BETWEEN_TIMER -> { /* PR 14b */ }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameplayAppBar(
    uiState: GameplayUiState,
    onMenuClick: () -> Unit
) {
    val turn = uiState.displayedTurn
    val turnNumber = turn?.number ?: 1
    val playerName = turn?.playerName.orEmpty()
    val hasCitiesAndKnights = uiState.game?.expansions?.contains(GameExpansion.CITIES_AND_KNIGHTS) == true

    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(Res.string.gameplay_turn, turnNumber),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(Res.string.gameplay_player_turn, playerName),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        actions = {
            if (hasCitiesAndKnights) {
                uiState.barbarianState?.let { state ->
                    BarbarianTracker(state = state)
                }
            }
            IconButton(onClick = onMenuClick) {
                Text(text = "⋮", fontSize = 20.sp)
            }
        }
    )
}

@Composable
private fun TurnNavigatorBar(
    uiState: GameplayUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToCurrent: () -> Unit
) {
    val turnNumber = uiState.displayedTurn?.number ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CatanSpacing.md, vertical = CatanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Text(text = "‹", fontSize = 24.sp)
        }

        if (!uiState.isViewingLatest) {
            TextButton(onClick = onJumpToCurrent) {
                Text(
                    text = stringResource(Res.string.gameplay_turn, turnNumber),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        } else {
            Text(
                text = stringResource(Res.string.gameplay_turn, turnNumber),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onNext) {
            Text(text = "›", fontSize = 24.sp)
        }
    }
}

@Composable
private fun DiceSelectionContent(
    uiState: GameplayUiState,
    onDiceSelected: (Int, Int, EventDiceType?) -> Unit,
    onEventDiceSelected: (EventDiceType) -> Unit,
    onContinue: () -> Unit
) {
    val hasCitiesAndKnights = uiState.game?.expansions?.contains(GameExpansion.CITIES_AND_KNIGHTS) == true
    val pendingDice = uiState.pendingDiceEdit
    val isContinueEnabled = if (hasCitiesAndKnights) {
        pendingDice != null && pendingDice.red > 0 && pendingDice.yellow > 0 && pendingDice.event != null
    } else {
        pendingDice != null && pendingDice.red > 0 && pendingDice.yellow > 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CatanSpacing.md),
        verticalArrangement = Arrangement.spacedBy(CatanSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.gameplay_turn, uiState.currentTurn?.number ?: 1),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        DiceRow(
            selectedValue = pendingDice?.red,
            diceType = DiceType.RedDice,
            onValueSelected = { red ->
                onDiceSelected(red, pendingDice?.yellow ?: 0, pendingDice?.event)
            }
        )

        DiceRow(
            selectedValue = pendingDice?.yellow,
            diceType = DiceType.YellowDice,
            onValueSelected = { yellow ->
                onDiceSelected(pendingDice?.red ?: 0, yellow, pendingDice?.event)
            }
        )

        if (hasCitiesAndKnights) {
            EventDiceRow(
                selectedType = pendingDice?.event,
                onTypeSelected = onEventDiceSelected
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            enabled = isContinueEnabled && uiState.isViewingLatest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.gameplay_continue))
        }
    }
}
