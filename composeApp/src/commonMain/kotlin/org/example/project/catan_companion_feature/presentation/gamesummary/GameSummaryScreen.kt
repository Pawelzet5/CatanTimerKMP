package org.example.project.catan_companion_feature.presentation.gamesummary

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.end_game_back_home
import catantimer.composeapp.generated.resources.end_game_duration
import catantimer.composeapp.generated.resources.end_game_summary
import catantimer.composeapp.generated.resources.end_game_winner
import catantimer.composeapp.generated.resources.ic_close
import catantimer.composeapp.generated.resources.ic_winner
import catantimer.composeapp.generated.resources.stats_avg_turn_time
import catantimer.composeapp.generated.resources.stats_dice_distribution
import catantimer.composeapp.generated.resources.stats_title
import catantimer.composeapp.generated.resources.stats_total_turns
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameStatistics
import org.example.project.catan_companion_feature.presentation.components.charts.DiceStatisticsChart
import org.example.project.core.designsystem.CatanBrown
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.presentation.ObserveAsEvents
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
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(GameSummaryAction.BackClick) }) {
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
                    onClick = { onAction(GameSummaryAction.HomeClick) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.end_game_back_home),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
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
                            item { WinnerCard(winnerName = winnerName) }
                        }
                    }

                    state.statistics?.let { stats ->
                        item { StatsCard(game = game, statistics = stats) }
                        item {
                            DiceChartCard(statistics = stats)
                        }
                        item {
                            PlayerStatsCard(game = game, statistics = stats)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WinnerCard(winnerName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(CatanBrown, MaterialTheme.colorScheme.primary)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(CatanSpacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_winner),
                contentDescription = null,
                tint = Color(0xFFFFF7ED),
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = stringResource(Res.string.end_game_winner, winnerName),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFFFF7ED),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatsCard(game: Game, statistics: GameStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(CatanSpacing.md)) {
            SectionLabel(stringResource(Res.string.stats_total_turns))
            Spacer(modifier = Modifier.height(CatanSpacing.sm))

            StatRow(
                label = stringResource(Res.string.stats_total_turns),
                value = statistics.totalTurns.toString()
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = CatanSpacing.xs))

            val durationMillis = game.finishedAt?.let { it - game.startedAt }
            if (durationMillis != null) {
                StatRow(
                    label = stringResource(Res.string.end_game_duration),
                    value = formatDurationMillis(durationMillis)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = CatanSpacing.xs))
            }

            StatRow(
                label = stringResource(Res.string.stats_avg_turn_time),
                value = formatDurationMillis(statistics.averageTurnDurationMillis)
            )
        }
    }
}

@Composable
private fun DiceChartCard(statistics: GameStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(CatanSpacing.md)) {
            SectionLabel(stringResource(Res.string.stats_dice_distribution))
            Spacer(modifier = Modifier.height(CatanSpacing.md))
            DiceStatisticsChart(
                distribution = statistics.diceDistribution,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PlayerStatsCard(game: Game, statistics: GameStatistics) {
    val players = game.players.sortedBy { it.orderIndex }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(CatanSpacing.md)) {
            SectionLabel(stringResource(Res.string.stats_title))
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
                        .padding(vertical = CatanSpacing.xs, horizontal = if (isWinner) CatanSpacing.sm else 0.dp),
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
                        Text(
                            text = player.playerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatDurationMillis(avgMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isWinner) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_winner),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (index < players.lastIndex) {
                    Spacer(modifier = Modifier.height(CatanSpacing.xs))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
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
