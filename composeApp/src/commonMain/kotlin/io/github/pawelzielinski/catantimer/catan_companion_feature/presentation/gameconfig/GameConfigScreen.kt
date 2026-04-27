package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameconfig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import io.github.pawelzielinski.catantimer.core.presentation.components.DraggableItemsLazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.config_add_player
import catantimer.composeapp.generated.resources.config_cities_knights
import catantimer.composeapp.generated.resources.config_in_between_turns
import catantimer.composeapp.generated.resources.config_options
import catantimer.composeapp.generated.resources.config_player_count
import catantimer.composeapp.generated.resources.config_players
import catantimer.composeapp.generated.resources.config_remove_player
import catantimer.composeapp.generated.resources.config_seafarers
import catantimer.composeapp.generated.resources.config_start_game
import catantimer.composeapp.generated.resources.config_title
import catantimer.composeapp.generated.resources.config_turn_duration
import catantimer.composeapp.generated.resources.ic_close
import catantimer.composeapp.generated.resources.ic_minus
import catantimer.composeapp.generated.resources.ic_plus
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Player
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import io.github.pawelzielinski.catantimer.core.designsystem.components.CatanCheckbox
import io.github.pawelzielinski.catantimer.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.service.HapticService
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val MILLIS_PER_SECOND = 1_000L
private const val TURN_DURATION_STEP_MILLIS = 15 * MILLIS_PER_SECOND
private const val TURN_DURATION_MIN_MILLIS = 30 * MILLIS_PER_SECOND
private const val TURN_DURATION_MAX_MILLIS = 600 * MILLIS_PER_SECOND

private val PLAYER_COUNT_OPTIONS = listOf(3, 4, 5, 6)

@Composable
fun GameConfigScreenRoot(
    onNavigateBack: () -> Unit,
    onGameCreated: (Long) -> Unit,
    onAddPlayer: () -> Unit,
    viewModel: GameConfigViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            GameConfigEvent.NavigateBack -> onNavigateBack()
            is GameConfigEvent.NavigateToGameplay -> onGameCreated(event.gameId)
            GameConfigEvent.NavigateToPlayerSelection -> onAddPlayer()
        }
    }

    GameConfigScreen(state = uiState, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameConfigScreen(
    state: GameConfigState,
    onAction: (GameConfigAction) -> Unit,
    hapticService: HapticService = koinInject()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.config_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(GameConfigAction.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = CatanSpacing.md,
                end = CatanSpacing.md,
                top = CatanSpacing.md,
                bottom = CatanSpacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.md)
        ) {
            item {
                TurnDurationSection(
                    durationMillis = state.turnDurationMillis,
                    onDecrement = {
                        onAction(
                            GameConfigAction.TurnDurationChanged(
                                (state.turnDurationMillis - TURN_DURATION_STEP_MILLIS)
                                    .coerceAtLeast(TURN_DURATION_MIN_MILLIS)
                            )
                        )
                    },
                    onIncrement = {
                        onAction(
                            GameConfigAction.TurnDurationChanged(
                                (state.turnDurationMillis + TURN_DURATION_STEP_MILLIS)
                                    .coerceAtMost(TURN_DURATION_MAX_MILLIS)
                            )
                        )
                    }
                )
            }

            item {
                NumberOfPlayersSection(
                    selectedCount = state.numberOfPlayers,
                    onCountSelected = { onAction(GameConfigAction.PlayerCountSelected(it)) }
                )
            }

            item {
                PlayersHeader(onAddPlayer = { onAction(GameConfigAction.AddPlayerClick) })
            }

            item {
                val playerRowHeight = 48.dp
                val playersListHeight = playerRowHeight * state.selectedPlayers.size +
                    CatanSpacing.sm * (state.selectedPlayers.size - 1).coerceAtLeast(0)
                DraggableItemsLazyColumn(
                    items = state.selectedPlayers,
                    key = { it.id },
                    onOrderChanged = { onAction(GameConfigAction.PlayersReordered(it)) },
                    onDragStart = { hapticService.vibrateOnce() },
                    itemContent = { index, player, modifier ->
                        SelectedPlayerRow(
                            player = player,
                            index = index,
                            onRemove = { onAction(GameConfigAction.PlayerToggled(player)) },
                            modifier = modifier
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(playersListHeight),
                    verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
                )
            }

            item {
                GameOptionsSection(
                    showSpecialTurnRule = state.numberOfPlayers > 4,
                    specialTurnRuleEnabled = state.specialTurnRuleEnabled,
                    seafarersEnabled = state.expansions.contains(GameExpansion.SEAFARERS),
                    citiesAndKnightsEnabled = state.expansions.contains(GameExpansion.CITIES_AND_KNIGHTS),
                    onSpecialTurnRuleToggled = { onAction(GameConfigAction.SpecialTurnRuleToggled) },
                    onSeafarersToggled = { onAction(GameConfigAction.ExpansionToggled(GameExpansion.SEAFARERS)) },
                    onCitiesAndKnightsToggled = {
                        onAction(
                            GameConfigAction.ExpansionToggled(
                                GameExpansion.CITIES_AND_KNIGHTS
                            )
                        )
                    }
                )
            }

            state.validationError?.let { error ->
                item {
                    Text(
                        text = error.asStringComposable(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = CatanSpacing.xs)
                    )
                }
            }

            item {
                Button(
                    onClick = { onAction(GameConfigAction.StartGameClick) },
                    enabled = state.isValid,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.config_start_game))
                }
            }
        }
    }
}

@Composable
private fun TurnDurationSection(
    durationMillis: Long,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    val totalSeconds = (durationMillis / MILLIS_PER_SECOND).toInt()
    val displayText = "${(totalSeconds / 60).toString().padStart(2, '0')}:${(totalSeconds % 60).toString().padStart(2, '0')}"

    Column(verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)) {
        FormLabel(stringResource(Res.string.config_turn_duration))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = CatanSpacing.md, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperButton(
                painter = painterResource(Res.drawable.ic_minus),
                contentDescription = "Decrease duration",
                enabled = durationMillis > TURN_DURATION_MIN_MILLIS,
                onClick = onDecrement
            )
            Text(
                text = displayText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            StepperButton(
                painter = painterResource(Res.drawable.ic_plus),
                contentDescription = "Increase duration",
                enabled = durationMillis < TURN_DURATION_MAX_MILLIS,
                onClick = onIncrement
            )
        }
    }
}

@Composable
private fun StepperButton(
    painter: Painter,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = CircleShape
    val accentColor = if (enabled)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                color = if (enabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
            .border(.5.dp, accentColor, shape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = accentColor
        )
    }
}

@Composable
private fun NumberOfPlayersSection(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)) {
        FormLabel(stringResource(Res.string.config_player_count))
        Row(horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm)) {
            PLAYER_COUNT_OPTIONS.forEach { count ->
                PlayerCountChip(
                    count = count,
                    isActive = count == selectedCount,
                    onClick = { onCountSelected(count) }
                )
            }
        }
    }
}

@Composable
private fun PlayerCountChip(
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isActive,
        onClick = onClick,
        label = {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.secondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isActive,
            borderColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.5.dp,
            selectedBorderWidth = 1.5.dp
        )
    )
}

@Composable
private fun PlayersHeader(onAddPlayer: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FormLabel(stringResource(Res.string.config_players))
        OutlinedButton(
            onClick = onAddPlayer,
            contentPadding = PaddingValues(
                horizontal = CatanSpacing.sm,
                vertical = CatanSpacing.xs
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_plus),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(CatanSpacing.xs))
            Text(
                text = stringResource(Res.string.config_add_player),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SelectedPlayerRow(
    player: Player,
    index: Int,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = CatanSpacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = "${index + 1}. ${player.name}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = stringResource(Res.string.config_remove_player),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun GameOptionsSection(
    showSpecialTurnRule: Boolean,
    specialTurnRuleEnabled: Boolean,
    seafarersEnabled: Boolean,
    citiesAndKnightsEnabled: Boolean,
    onSpecialTurnRuleToggled: () -> Unit,
    onSeafarersToggled: () -> Unit,
    onCitiesAndKnightsToggled: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)) {
        FormLabel(stringResource(Res.string.config_options))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(CatanSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.xs)
        ) {
            OptionCheckboxRow(
                label = stringResource(Res.string.config_seafarers),
                checked = seafarersEnabled,
                onToggle = onSeafarersToggled
            )
            OptionCheckboxRow(
                label = stringResource(Res.string.config_cities_knights),
                checked = citiesAndKnightsEnabled,
                onToggle = onCitiesAndKnightsToggled
            )
            AnimatedVisibility(showSpecialTurnRule) {
                OptionCheckboxRow(
                    label = stringResource(Res.string.config_in_between_turns),
                    checked = specialTurnRuleEnabled,
                    onToggle = onSpecialTurnRuleToggled
                )
            }
        }
    }
}

@Composable
private fun OptionCheckboxRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = CatanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
    ) {
        CatanCheckbox(
            checked = checked,
            onCheckedChange = onToggle
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FormLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
