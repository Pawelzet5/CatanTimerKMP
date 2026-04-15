package org.example.project.catan_companion_feature.presentation.gameconfig

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.config_add_player
import catantimer.composeapp.generated.resources.config_cities_knights
import catantimer.composeapp.generated.resources.config_in_between_turns
import catantimer.composeapp.generated.resources.config_options
import catantimer.composeapp.generated.resources.config_players
import catantimer.composeapp.generated.resources.config_seafarers
import catantimer.composeapp.generated.resources.config_start_game
import catantimer.composeapp.generated.resources.config_title
import catantimer.composeapp.generated.resources.config_turn_duration
import catantimer.composeapp.generated.resources.config_turn_duration_minutes
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.core.designsystem.CatanSpacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val TURN_DURATION_MIN_MINUTES = 1
private const val TURN_DURATION_MAX_MINUTES = 10
private const val MILLIS_PER_MINUTE = 60_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameConfigScreen(
    onNavigateBack: () -> Unit,
    onGameCreated: (Long) -> Unit,
    onAddPlayer: () -> Unit,
    viewModel: GameConfigViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToGameplay.collect { gameId ->
            onGameCreated(gameId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.config_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                    durationMillis = uiState.turnDurationMillis,
                    onDurationChanged = { viewModel.onTurnDurationChanged(it) }
                )
            }

            item {
                SectionLabel(stringResource(Res.string.config_players))
            }

            items(uiState.selectedPlayers, key = { it.id }) { player ->
                SelectedPlayerRow(
                    player = player,
                    onRemove = { viewModel.onPlayerToggled(player) }
                )
            }

            item {
                OutlinedButton(
                    onClick = onAddPlayer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.config_add_player))
                }
            }

            item {
                GameOptionsSection(
                    specialTurnRuleEnabled = uiState.specialTurnRuleEnabled,
                    seafarersEnabled = uiState.expansions.contains(GameExpansion.SEAFARERS),
                    citiesAndKnightsEnabled = uiState.expansions.contains(GameExpansion.CITIES_AND_KNIGHTS),
                    onSpecialTurnRuleToggled = { viewModel.onSpecialTurnRuleToggled() },
                    onSeafarersToggled = { viewModel.onExpansionToggled(GameExpansion.SEAFARERS) },
                    onCitiesAndKnightsToggled = { viewModel.onExpansionToggled(GameExpansion.CITIES_AND_KNIGHTS) }
                )
            }

            if (uiState.validationError != null) {
                item {
                    Text(
                        text = uiState.validationError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = CatanSpacing.xs)
                    )
                }
            }

            item {
                Button(
                    onClick = { viewModel.onStartGame() },
                    enabled = uiState.isValid,
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
    onDurationChanged: (Long) -> Unit
) {
    val minutes = (durationMillis / MILLIS_PER_MINUTE).toInt()
        .coerceIn(TURN_DURATION_MIN_MINUTES, TURN_DURATION_MAX_MINUTES)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(CatanSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.config_turn_duration),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(Res.string.config_turn_duration_minutes, minutes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Slider(
                value = minutes.toFloat(),
                onValueChange = { onDurationChanged(it.toLong() * MILLIS_PER_MINUTE) },
                valueRange = TURN_DURATION_MIN_MINUTES.toFloat()..TURN_DURATION_MAX_MINUTES.toFloat(),
                steps = TURN_DURATION_MAX_MINUTES - TURN_DURATION_MIN_MINUTES - 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = CatanSpacing.xs)
    )
}

@Composable
private fun SelectedPlayerRow(
    player: Player,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun GameOptionsSection(
    specialTurnRuleEnabled: Boolean,
    seafarersEnabled: Boolean,
    citiesAndKnightsEnabled: Boolean,
    onSpecialTurnRuleToggled: () -> Unit,
    onSeafarersToggled: () -> Unit,
    onCitiesAndKnightsToggled: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(CatanSpacing.md)) {
            Text(
                text = stringResource(Res.string.config_options),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OptionToggleRow(
                label = stringResource(Res.string.config_in_between_turns),
                checked = specialTurnRuleEnabled,
                onCheckedChange = { onSpecialTurnRuleToggled() }
            )
            OptionToggleRow(
                label = stringResource(Res.string.config_seafarers),
                checked = seafarersEnabled,
                onCheckedChange = { onSeafarersToggled() }
            )
            OptionToggleRow(
                label = stringResource(Res.string.config_cities_knights),
                checked = citiesAndKnightsEnabled,
                onCheckedChange = { onCitiesAndKnightsToggled() }
            )
        }
    }
}

@Composable
private fun OptionToggleRow(
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
