package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gamesummary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.end_game_back_home
import catantimer.composeapp.generated.resources.end_game_duration
import catantimer.composeapp.generated.resources.end_game_ended
import catantimer.composeapp.generated.resources.end_game_started
import catantimer.composeapp.generated.resources.end_game_summary
import catantimer.composeapp.generated.resources.end_game_winner_label
import catantimer.composeapp.generated.resources.ic_dice
import catantimer.composeapp.generated.resources.ic_person
import catantimer.composeapp.generated.resources.ic_statistics
import catantimer.composeapp.generated.resources.ic_timer
import catantimer.composeapp.generated.resources.ic_winner
import catantimer.composeapp.generated.resources.stats_avg_turn_time
import catantimer.composeapp.generated.resources.stats_barbarian_attacks
import catantimer.composeapp.generated.resources.stats_player_avg
import catantimer.composeapp.generated.resources.stats_dice_distribution
import catantimer.composeapp.generated.resources.stats_game_stats
import catantimer.composeapp.generated.resources.stats_thief_rolled
import catantimer.composeapp.generated.resources.stats_title
import catantimer.composeapp.generated.resources.stats_total_turns
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.GameStatistics
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.EventDiceType
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.charts.DiceStatisticsChart
import io.github.pawelzielinski.catantimer.core.designsystem.CatanBgSecondary
import io.github.pawelzielinski.catantimer.core.designsystem.CatanBrown
import io.github.pawelzielinski.catantimer.core.designsystem.CatanOrange
import io.github.pawelzielinski.catantimer.core.designsystem.CatanOrangeSubtle
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import io.github.pawelzielinski.catantimer.core.designsystem.components.CatanButton
import io.github.pawelzielinski.catantimer.core.presentation.ObserveAsEvents
import io.github.pawelzielinski.catantimer.core.util.formatEpochMillis
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GameSummaryScreenRoot(
    gameId: Long,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    viewModel: GameSummaryViewModel = koinViewModel { parametersOf(gameId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            GameSummaryEvent.NavigateBack -> onNavigateBack()
            GameSummaryEvent.NavigateHome -> onNavigateHome()
        }
    }

    GameSummaryScreen(state = uiState, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSummaryScreen(
    state: GameSummaryState,
    onAction: (GameSummaryAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.end_game_summary),
                        style = MaterialTheme.typography.titleLarge
                    )
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
                    text = stringResource(Res.string.end_game_back_home),
                    onClick = { onAction(GameSummaryAction.HomeClick) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    horizontal = CatanSpacing.md,
                    vertical = CatanSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(CatanSpacing.md)
            ) {
                state.game?.let { game ->
                    game.winnerId?.let { winnerId ->
                        val winnerName = game.players.find { it.playerId == winnerId }?.playerName
                        if (winnerName != null) {
                            item { TrophyBanner(winnerName = winnerName) }
                        }
                    }

                    item { DurationCard(game = game) }

                    state.statistics?.let { stats ->
                        item { GameStatsCard(game = game, statistics = stats) }
                        item { DiceChartCard(statistics = stats) }
                        item { PlayerStatsCard(game = game, statistics = stats) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophyBanner(winnerName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(CatanBrown, CatanOrange)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(CatanSpacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.xs)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_winner),
                contentDescription = null,
                tint = CatanBgSecondary,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = stringResource(Res.string.end_game_winner_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = CatanOrangeSubtle.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = winnerName,
                style = MaterialTheme.typography.headlineSmall,
                color = CatanBgSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DurationCard(game: Game) {
    val finishedAt = game.finishedAt

    ElevatedSectionCard {
        SectionLabel(
            text = stringResource(Res.string.end_game_duration),
            icon = painterResource(Res.drawable.ic_timer)
        )
        Spacer(modifier = Modifier.height(CatanSpacing.sm))

        if (finishedAt != null) {
            val durationMillis = finishedAt - game.startedAt
            Text(
                text = stringResource(Res.string.end_game_started, formatEpochMillis(game.startedAt)) +
                    " · " +
                    stringResource(Res.string.end_game_ended, formatEpochMillis(finishedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(CatanSpacing.xs))
            Text(
                text = formatDurationMillis(durationMillis),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GameStatsCard(game: Game, statistics: GameStatistics) {
    val thiefRolled = statistics.diceDistribution.counts[7] ?: 0
    val barbarianAttacks = statistics.eventDiceDistribution[EventDiceType.BARBARIANS] ?: 0
    val hasCitiesAndKnights = GameExpansion.CITIES_AND_KNIGHTS in game.expansions

    ElevatedSectionCard {
        SectionLabel(
            text = stringResource(Res.string.stats_game_stats),
            icon = painterResource(Res.drawable.ic_statistics)
        )
        Spacer(modifier = Modifier.height(CatanSpacing.sm))

        StatRow(
            label = stringResource(Res.string.stats_total_turns),
            value = statistics.totalTurns.toString()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = CatanSpacing.xs))
        StatRow(
            label = stringResource(Res.string.stats_avg_turn_time),
            value = formatDurationMillis(statistics.averageTurnDurationMillis)
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = CatanSpacing.xs))
        StatRow(
            label = stringResource(Res.string.stats_thief_rolled),
            value = "${thiefRolled}×"
        )
        if (hasCitiesAndKnights) {
            HorizontalDivider(modifier = Modifier.padding(vertical = CatanSpacing.xs))
            StatRow(
                label = stringResource(Res.string.stats_barbarian_attacks),
                value = "${barbarianAttacks}×"
            )
        }
    }
}

@Composable
private fun DiceChartCard(statistics: GameStatistics) {
    ElevatedSectionCard {
        SectionLabel(
            text = stringResource(Res.string.stats_dice_distribution),
            icon = painterResource(Res.drawable.ic_dice)
        )
        Spacer(modifier = Modifier.height(CatanSpacing.md))
        DiceStatisticsChart(
            distribution = statistics.diceDistribution,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PlayerStatsCard(game: Game, statistics: GameStatistics) {
    val players = game.players.sortedBy { it.orderIndex }

    ElevatedSectionCard {
        SectionLabel(
            text = stringResource(Res.string.stats_title),
            icon = painterResource(Res.drawable.ic_person)
        )
        Spacer(modifier = Modifier.height(CatanSpacing.sm))

        players.forEachIndexed { index, player ->
            val avgMillis = statistics.playerAverageTurnDurations[player.playerId] ?: 0L
            val isWinner = player.playerId == game.winnerId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isWinner) Modifier.background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) else Modifier
                    )
                    .padding(
                        vertical = CatanSpacing.xs,
                        horizontal = if (isWinner) CatanSpacing.sm else 0.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.playerName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.xs)
                    ) {
                        Text(
                            text = player.playerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isWinner) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_winner),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(Res.string.stats_player_avg, formatDurationMillis(avgMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (index < players.lastIndex) {
                Spacer(modifier = Modifier.height(CatanSpacing.xs))
            }
        }
    }
}

@Composable
private fun ElevatedSectionCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(CatanSpacing.md)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SectionLabel(text: String, icon: Painter) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.xs)
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CatanSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatDurationMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

