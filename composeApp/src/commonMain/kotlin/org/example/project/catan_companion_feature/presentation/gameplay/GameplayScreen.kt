package org.example.project.catan_companion_feature.presentation.gameplay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.config_cities_knights
import catantimer.composeapp.generated.resources.config_in_between_turns
import catantimer.composeapp.generated.resources.config_seafarers
import catantimer.composeapp.generated.resources.confirm_end_game_message
import catantimer.composeapp.generated.resources.gameplay_active_player
import catantimer.composeapp.generated.resources.gameplay_continue
import catantimer.composeapp.generated.resources.gameplay_next_turn
import catantimer.composeapp.generated.resources.gameplay_player_turn
import catantimer.composeapp.generated.resources.gameplay_turn
import catantimer.composeapp.generated.resources.historical_edit_message
import catantimer.composeapp.generated.resources.historical_edit_title
import catantimer.composeapp.generated.resources.ic_menu
import catantimer.composeapp.generated.resources.menu_end_game
import catantimer.composeapp.generated.resources.menu_open
import catantimer.composeapp.generated.resources.menu_settings
import catantimer.composeapp.generated.resources.menu_statistics
import catantimer.composeapp.generated.resources.settings_cancel
import catantimer.composeapp.generated.resources.settings_new_game
import catantimer.composeapp.generated.resources.settings_save
import catantimer.composeapp.generated.resources.settings_warning_message
import catantimer.composeapp.generated.resources.settings_warning_title
import org.example.project.catan_companion_feature.domain.dataclass.BarbarianState
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.enums.DiceType
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.core.presentation.ObserveAsEvents
import org.example.project.catan_companion_feature.presentation.components.ConfirmationDialog
import org.example.project.catan_companion_feature.presentation.components.PlayerAvatar
import org.example.project.catan_companion_feature.presentation.components.PlayerAvatarSize
import org.example.project.catan_companion_feature.presentation.components.dice.Dice
import org.example.project.catan_companion_feature.presentation.components.dice.DiceRow
import org.example.project.catan_companion_feature.presentation.components.dice.EventDiceRow
import org.example.project.catan_companion_feature.presentation.components.gameplay.BarbarianTracker
import org.example.project.catan_companion_feature.presentation.components.gameplay.EventPhaseContent
import org.example.project.catan_companion_feature.presentation.components.gameplay.StatisticsPopup
import org.example.project.catan_companion_feature.presentation.components.timer.GameTimer
import org.example.project.catan_companion_feature.presentation.components.timer.TimerControls
import org.example.project.core.designsystem.CatanDiceRedBackground
import org.example.project.core.designsystem.CatanDiceRedDot
import org.example.project.core.designsystem.CatanDiceYellowBackground
import org.example.project.core.designsystem.CatanDiceYellowDot
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.designsystem.catanColors
import org.example.project.core.designsystem.components.CatanButton
import org.example.project.core.designsystem.components.CatanOutlinedButton
import org.jetbrains.compose.resources.painterResource
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
            is GameplayEvent.NavigateToGameSummary -> onNavigateToSummary(event.gameId)
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
    Scaffold { paddingValues ->
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

            uiState.displayedTurn?.let { turn ->
                TimerPlayerIndicator(turn = turn, uiState = uiState)
            }

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
                        onContinue = { onAction(GameplayAction.ContinueFromDiceClick) },
                        onMenuClick = { onAction(GameplayAction.MenuClick) },
                    )
                    GameplayPhase.EVENT -> {
                        val turn = uiState.displayedTurn ?: return@Column
                        val game = uiState.game ?: return@Column
                        val eventStep = uiState.eventStep ?: return@Column
                        EventPhaseContent(
                            turn = turn,
                            game = game,
                            eventStep = eventStep,
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
                        onInBetweenTurn = { onAction(GameplayAction.InBetweenTurnClick) },
                        onMenuClick = { onAction(GameplayAction.MenuClick) },
                    )
                    GameplayPhase.IN_BETWEEN_TIMER -> TimerPhaseContent(
                        uiState = uiState,
                        onStartStop = { onAction(GameplayAction.TimerToggleClick) },
                        onAddTime = { onAction(GameplayAction.AddTimeClick) },
                        onReset = { onAction(GameplayAction.ResetTimerClick) },
                        onNextTurn = { onAction(GameplayAction.NextTurnClick) },
                        onInBetweenTurn = {},
                        onMenuClick = { onAction(GameplayAction.MenuClick) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TurnNavigatorBar(
    uiState: GameplayState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToCurrent: () -> Unit
) {
    val turn = uiState.displayedTurn
    val turnNumber = turn?.number ?: 1
    val playerName = turn?.playerName.orEmpty()
    val canGoBack = turnNumber > 1

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = CatanSpacing.lg, vertical = CatanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TurnNavButton(icon = Icons.AutoMirrored.Filled.ArrowBack, enabled = canGoBack, onClick = onPrevious)
            TurnNavInfo(
                turnNumber = turnNumber,
                playerName = playerName,
                isViewingLatest = uiState.isViewingLatest,
                onJumpToCurrent = onJumpToCurrent,
            )
            if (!uiState.isViewingLatest) {
                TurnNavButton(icon = Icons.AutoMirrored.Filled.ArrowForward, enabled = true, onClick = onNext)
            } else {
                Spacer(Modifier.size(36.dp))
            }
        }
        HorizontalDivider(color = MaterialTheme.catanColors.borderAccent)
    }
}

@Composable
private fun TurnNavButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .then(if (!enabled) Modifier.alpha(0.3f) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun TurnNavInfo(
    turnNumber: Int,
    playerName: String,
    isViewingLatest: Boolean,
    onJumpToCurrent: () -> Unit,
) {
    val labelColor = if (isViewingLatest) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.primary
    }
    val clickableModifier = if (!isViewingLatest) {
        Modifier.clickable(onClick = onJumpToCurrent)
    } else {
        Modifier
    }

    Column(
        modifier = clickableModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.gameplay_turn, turnNumber),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
        )
        Text(
            text = stringResource(Res.string.gameplay_player_turn, playerName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DiceSelectionContent(
    uiState: GameplayState,
    onDiceSelected: (Int, Int, EventDiceType?) -> Unit,
    onEventDiceSelected: (EventDiceType) -> Unit,
    onContinue: () -> Unit,
    onMenuClick: () -> Unit,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_menu),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(CatanSpacing.xs))
                Text(stringResource(Res.string.menu_open), style = MaterialTheme.typography.labelMedium)
            }
        }

        CatanButton(
            text = stringResource(Res.string.gameplay_continue),
            onClick = onContinue,
            enabled = isContinueEnabled && uiState.isViewingLatest,
            modifier = Modifier.fillMaxWidth()
        )
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
                playerCount = game?.players?.size ?: 0,
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
    playerCount: Int,
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

        if (playerCount > 4) {
            SettingsToggleRow(
                label = stringResource(Res.string.config_in_between_turns),
                checked = specialTurnRuleEnabled,
                onCheckedChange = onSpecialTurnRuleChanged
            )
        }
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

        CatanButton(
            text = stringResource(Res.string.settings_save),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )
        CatanOutlinedButton(
            text = stringResource(Res.string.settings_new_game),
            onClick = onStartNewGame,
            modifier = Modifier.fillMaxWidth()
        )
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
    onInBetweenTurn: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val showInBetweenButton = uiState.phase == GameplayPhase.MAIN_TIMER
        && uiState.game?.specialTurnRuleEnabled == true
        && uiState.isViewingLatest
    val hasCitiesAndKnights = uiState.game?.expansions?.contains(GameExpansion.CITIES_AND_KNIGHTS) == true

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = CatanSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            GameTimer(
                remainingMillis = uiState.timerState.remainingMillis,
                totalMillis = uiState.game?.turnDurationMillis ?: 0L,
            )
            Spacer(Modifier.height(CatanSpacing.xl))
            if (uiState.isViewingLatest) {
                TimerControls(
                    isRunning = uiState.timerState.isRunning,
                    onStartStop = onStartStop,
                    onAddTime = onAddTime,
                    onReset = onReset,
                )
            }
        }
        TimerBottomActions(
            hasCitiesAndKnights = hasCitiesAndKnights,
            barbarianState = uiState.barbarianState,
            isViewingLatest = uiState.isViewingLatest,
            onMenuClick = onMenuClick,
            onNextTurn = if (showInBetweenButton) onInBetweenTurn else onNextTurn,
        )
    }
}

@Composable
private fun TimerPlayerIndicator(turn: Turn, uiState: GameplayState) {
    val playerName = when (uiState.phase) {
        GameplayPhase.IN_BETWEEN_TIMER -> turn.secondaryPlayerName.orEmpty()
        else -> turn.playerName
    }
    val playerColorIndex = uiState.game?.players
        ?.find { it.playerId == turn.playerId }
        ?.orderIndex ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = CatanSpacing.md, vertical = CatanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm),
    ) {
        PlayerAvatar(name = playerName, colorIndex = playerColorIndex, size = PlayerAvatarSize.Medium)
        Column(modifier = Modifier.weight(1f)) {
            Text(playerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = stringResource(Res.string.gameplay_active_player),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        turn.redDice?.let { red ->
            turn.yellowDice?.let { yellow ->
                TimerDiceRollBadge(redValue = red, yellowValue = yellow)
            }
        }
    }
}

@Composable
private fun TimerDiceRollBadge(redValue: Int, yellowValue: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.xs),
    ) {
        Dice(value = redValue, isSelected = false, backgroundColor = CatanDiceRedBackground, dotColor = CatanDiceRedDot, onClick = {}, size = 32.dp)
        Dice(value = yellowValue, isSelected = false, backgroundColor = CatanDiceYellowBackground, dotColor = CatanDiceYellowDot, onClick = {}, size = 32.dp)
        Text(
            text = "=${redValue + yellowValue}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun TimerBottomActions(
    hasCitiesAndKnights: Boolean,
    barbarianState: BarbarianState?,
    isViewingLatest: Boolean,
    onMenuClick: () -> Unit,
    onNextTurn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CatanSpacing.md)
            .padding(bottom = CatanSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasCitiesAndKnights && barbarianState != null) {
                BarbarianTracker(state = barbarianState)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_menu),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(CatanSpacing.xs))
                Text(stringResource(Res.string.menu_open), style = MaterialTheme.typography.labelMedium)
            }
        }
        CatanButton(
            text = stringResource(Res.string.gameplay_next_turn),
            onClick = onNextTurn,
            enabled = isViewingLatest,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
