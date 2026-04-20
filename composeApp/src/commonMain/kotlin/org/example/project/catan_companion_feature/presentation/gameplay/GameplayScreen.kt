package org.example.project.catan_companion_feature.presentation.gameplay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.config_cities_knights
import catantimer.composeapp.generated.resources.config_in_between_turns
import catantimer.composeapp.generated.resources.config_seafarers
import catantimer.composeapp.generated.resources.confirm_end_game_message
import catantimer.composeapp.generated.resources.gameplay_continue
import catantimer.composeapp.generated.resources.gameplay_in_between
import catantimer.composeapp.generated.resources.gameplay_next_turn
import catantimer.composeapp.generated.resources.gameplay_player_turn
import catantimer.composeapp.generated.resources.gameplay_turn
import catantimer.composeapp.generated.resources.historical_edit_message
import catantimer.composeapp.generated.resources.historical_edit_title
import catantimer.composeapp.generated.resources.menu_end_game
import catantimer.composeapp.generated.resources.menu_settings
import catantimer.composeapp.generated.resources.menu_statistics
import catantimer.composeapp.generated.resources.settings_cancel
import catantimer.composeapp.generated.resources.settings_new_game
import catantimer.composeapp.generated.resources.settings_save
import catantimer.composeapp.generated.resources.settings_warning_message
import catantimer.composeapp.generated.resources.settings_warning_title
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.enums.DiceType
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.core.presentation.ObserveAsEvents
import org.example.project.catan_companion_feature.presentation.components.ConfirmationDialog
import org.example.project.catan_companion_feature.presentation.components.dice.DiceRow
import org.example.project.catan_companion_feature.presentation.components.dice.EventDiceRow
import org.example.project.catan_companion_feature.presentation.components.gameplay.BarbarianTracker
import org.example.project.catan_companion_feature.presentation.components.gameplay.EventPhaseContent
import org.example.project.catan_companion_feature.presentation.components.gameplay.StatisticsPopup
import org.example.project.catan_companion_feature.presentation.components.timer.GameTimer
import org.example.project.catan_companion_feature.presentation.components.timer.TimerControls
import org.example.project.core.designsystem.CatanSpacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GameplayScreenRoot(
    gameId: Long,
    onNavigateToSummary: (Long) -> Unit,
    onNavigateToWinnerSelection: (Long) -> Unit,
    onNavigateToGameConfig: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameplayViewModel = koinViewModel { parametersOf(gameId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is GameplayEvent.NavigateToWinnerSelection -> onNavigateToWinnerSelection(event.gameId)
            GameplayEvent.NavigateToGameConfig -> onNavigateToGameConfig()
        }
    }

    GameplayScreen(
        uiState = uiState,
        onAction = viewModel::onAction
    )

    if (uiState.showSettingsSheet) {
        SettingsBottomSheet(
            game = uiState.game,
            onDismiss = { viewModel.onAction(GameplayAction.DismissSettingsSheetClick) },
            onViewStatistics = { viewModel.onAction(GameplayAction.ViewStatisticsClick) },
            onSaveSettings = { expansions, specialTurnRuleEnabled ->
                viewModel.onAction(GameplayAction.SaveSettingsClick(expansions, specialTurnRuleEnabled))
            },
            onStartNewGame = { viewModel.onAction(GameplayAction.StartNewGameClick) },
            onEndGame = { viewModel.onAction(GameplayAction.EndGameClick) }
        )
    }

    if (uiState.showStatisticsPopup) {
        val distribution = uiState.diceDistribution
        if (distribution != null) {
            StatisticsPopup(
                distribution = distribution,
                onDismiss = { viewModel.onAction(GameplayAction.DismissStatisticsPopupClick) }
            )
        }
    }

    if (uiState.showEndGameConfirm) {
        ConfirmationDialog(
            title = stringResource(Res.string.menu_end_game),
            message = stringResource(Res.string.confirm_end_game_message),
            onConfirm = { viewModel.onAction(GameplayAction.ConfirmEndGameClick) },
            onDismiss = { viewModel.onAction(GameplayAction.DismissEndGameConfirmClick) }
        )
    }

    if (uiState.showHistoricalEditConfirm) {
        val displayedTurnNumber = uiState.displayedTurn?.number ?: 0
        val currentTurnNumber = uiState.currentTurn?.number ?: 0
        ConfirmationDialog(
            title = stringResource(Res.string.historical_edit_title),
            message = stringResource(Res.string.historical_edit_message, displayedTurnNumber, currentTurnNumber),
            onConfirm = { viewModel.onAction(GameplayAction.ConfirmHistoricalEditClick) },
            onDismiss = { viewModel.onAction(GameplayAction.DismissHistoricalEditConfirmClick) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplayScreen(
    uiState: GameplayState,
    onAction: (GameplayAction) -> Unit
) {
    Scaffold(
        topBar = {
            GameplayAppBar(
                uiState = uiState,
                onMenuClick = { onAction(GameplayAction.MenuClick) }
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
                onPrevious = { onAction(GameplayAction.PreviousClick) },
                onNext = { onAction(GameplayAction.NextClick) },
                onJumpToCurrent = { onAction(GameplayAction.JumpToCurrentClick) }
            )

            if (!uiState.isViewingLatest) {
                val turn = uiState.displayedTurn ?: return@Column
                HistoricalDiceContent(
                    turn = turn,
                    game = uiState.game,
                    pendingDiceEdit = uiState.pendingDiceEdit,
                    onDiceSelected = { red, yellow, event -> onAction(GameplayAction.DiceSelected(red, yellow, event)) },
                    onSaveChanges = { onAction(GameplayAction.SaveHistoricalEditClick) }
                )
            } else {
                when (uiState.phase) {
                    GameplayPhase.DICE_SELECTION -> DiceSelectionContent(
                        uiState = uiState,
                        onDiceSelected = { red, yellow, event -> onAction(GameplayAction.DiceSelected(red, yellow, event)) },
                        onEventDiceSelected = { event ->
                            val dice = uiState.pendingDiceEdit
                            onAction(GameplayAction.DiceSelected(dice?.red ?: 0, dice?.yellow ?: 0, event))
                        },
                        onContinue = { onAction(GameplayAction.ContinueFromDiceClick) }
                    )
                    GameplayPhase.EVENT -> {
                        val turn = uiState.displayedTurn ?: return@Column
                        val game = uiState.game ?: return@Column
                        EventPhaseContent(
                            turn = turn,
                            game = game,
                            barbarianState = uiState.barbarianState,
                            onContinue = { onAction(GameplayAction.ContinueFromEventClick) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    GameplayPhase.MAIN_TIMER -> TimerPhaseContent(
                        uiState = uiState,
                        onStartStop = { onAction(GameplayAction.TimerToggleClick) },
                        onAddTime = { onAction(GameplayAction.AddTimeClick) },
                        onReset = { onAction(GameplayAction.ResetTimerClick) },
                        onNextTurn = { onAction(GameplayAction.NextTurnClick) },
                        onInBetweenTurn = { onAction(GameplayAction.InBetweenTurnClick) }
                    )
                    GameplayPhase.IN_BETWEEN_TIMER -> TimerPhaseContent(
                        uiState = uiState,
                        onStartStop = { onAction(GameplayAction.TimerToggleClick) },
                        onAddTime = { onAction(GameplayAction.AddTimeClick) },
                        onReset = { onAction(GameplayAction.ResetTimerClick) },
                        onNextTurn = { onAction(GameplayAction.NextTurnClick) },
                        onInBetweenTurn = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameplayAppBar(
    uiState: GameplayState,
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
    uiState: GameplayState,
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
    uiState: GameplayState,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsBottomSheet(
    game: Game?,
    onDismiss: () -> Unit,
    onViewStatistics: () -> Unit,
    onSaveSettings: (Set<GameExpansion>, Boolean) -> Unit,
    onStartNewGame: () -> Unit,
    onEndGame: () -> Unit
) {
    var showSettingsExpanded by remember { mutableStateOf(false) }
    var pendingExpansions by remember(game) { mutableStateOf(game?.expansions ?: emptySet()) }
    var pendingSpecialTurnRule by remember(game) { mutableStateOf(game?.specialTurnRuleEnabled ?: false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        if (showSettingsExpanded) {
            GameSettingsContent(
                expansions = pendingExpansions,
                specialTurnRuleEnabled = pendingSpecialTurnRule,
                onExpansionsChanged = { pendingExpansions = it },
                onSpecialTurnRuleChanged = { pendingSpecialTurnRule = it },
                onSave = { onSaveSettings(pendingExpansions, pendingSpecialTurnRule) },
                onStartNewGame = onStartNewGame,
                onCancel = { showSettingsExpanded = false }
            )
        } else {
            SettingsMenuContent(
                onViewStatistics = onViewStatistics,
                onChangeSettings = { showSettingsExpanded = true },
                onEndGame = onEndGame
            )
        }
    }
}

@Composable
private fun SettingsMenuContent(
    onViewStatistics: () -> Unit,
    onChangeSettings: () -> Unit,
    onEndGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = CatanSpacing.lg)
    ) {
        SettingsMenuItem(
            label = stringResource(Res.string.menu_statistics),
            onClick = onViewStatistics
        )
        HorizontalDivider()
        SettingsMenuItem(
            label = stringResource(Res.string.menu_settings),
            onClick = onChangeSettings
        )
        HorizontalDivider()
        SettingsMenuItem(
            label = stringResource(Res.string.menu_end_game),
            onClick = onEndGame,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun SettingsMenuItem(
    label: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = CatanSpacing.md, vertical = CatanSpacing.md)
    )
}

@Composable
private fun GameSettingsContent(
    expansions: Set<GameExpansion>,
    specialTurnRuleEnabled: Boolean,
    onExpansionsChanged: (Set<GameExpansion>) -> Unit,
    onSpecialTurnRuleChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
    onStartNewGame: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CatanSpacing.md)
            .padding(bottom = CatanSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
    ) {
        Text(
            text = stringResource(Res.string.menu_settings),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(Res.string.settings_warning_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = stringResource(Res.string.settings_warning_message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(CatanSpacing.xs))

        SettingsToggleRow(
            label = stringResource(Res.string.config_in_between_turns),
            checked = specialTurnRuleEnabled,
            onCheckedChange = onSpecialTurnRuleChanged
        )
        SettingsToggleRow(
            label = stringResource(Res.string.config_seafarers),
            checked = expansions.contains(GameExpansion.SEAFARERS),
            onCheckedChange = { enabled ->
                val updated = if (enabled) expansions + GameExpansion.SEAFARERS
                              else expansions - GameExpansion.SEAFARERS
                onExpansionsChanged(updated)
            }
        )
        SettingsToggleRow(
            label = stringResource(Res.string.config_cities_knights),
            checked = expansions.contains(GameExpansion.CITIES_AND_KNIGHTS),
            onCheckedChange = { enabled ->
                val updated = if (enabled) expansions + GameExpansion.CITIES_AND_KNIGHTS
                              else expansions - GameExpansion.CITIES_AND_KNIGHTS
                onExpansionsChanged(updated)
            }
        )

        Spacer(modifier = Modifier.height(CatanSpacing.xs))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.settings_save))
        }
        OutlinedButton(
            onClick = onStartNewGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.settings_new_game))
        }
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.settings_cancel))
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CatanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun TimerPhaseContent(
    uiState: GameplayState,
    onStartStop: () -> Unit,
    onAddTime: () -> Unit,
    onReset: () -> Unit,
    onNextTurn: () -> Unit,
    onInBetweenTurn: () -> Unit
) {
    val turn = uiState.displayedTurn
    val playerName = when (uiState.phase) {
        GameplayPhase.IN_BETWEEN_TIMER -> turn?.secondaryPlayerName.orEmpty()
        else -> turn?.playerName.orEmpty()
    }
    val showInBetweenButton = uiState.phase == GameplayPhase.MAIN_TIMER
        && uiState.game?.specialTurnRuleEnabled == true
        && uiState.isViewingLatest
    val secondaryPlayerName = turn?.secondaryPlayerName.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CatanSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = playerName,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(CatanSpacing.lg))

        GameTimer(remainingMillis = uiState.timerState.remainingMillis)

        Spacer(modifier = Modifier.height(CatanSpacing.md))

        if (uiState.isViewingLatest) {
            TimerControls(
                isRunning = uiState.timerState.isRunning,
                onStartStop = onStartStop,
                onAddTime = onAddTime,
                onReset = onReset
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (showInBetweenButton) {
            Button(
                onClick = onInBetweenTurn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.gameplay_in_between, secondaryPlayerName))
            }
            Spacer(modifier = Modifier.height(CatanSpacing.sm))
        }

        Button(
            onClick = onNextTurn,
            enabled = uiState.isViewingLatest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.gameplay_next_turn))
        }
    }
}
