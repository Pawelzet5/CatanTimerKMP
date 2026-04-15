package org.example.project.catan_companion_feature.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.games_completed
import catantimer.composeapp.generated.resources.games_in_progress
import catantimer.composeapp.generated.resources.games_winner
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.enums.GameStatus
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.util.formatEpochMillis
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameListItem(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerNames = game.players
        .sortedBy { it.orderIndex }
        .joinToString(", ") { it.playerName }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = CatanSpacing.md, vertical = CatanSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.xs),
        ) {
            Text(
                text = playerNames,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val statusLabel = when (game.status) {
                GameStatus.IN_PROGRESS -> stringResource(Res.string.games_in_progress)
                GameStatus.COMPLETED -> stringResource(Res.string.games_completed)
            }
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (game.status == GameStatus.COMPLETED) {
                val winnerName = game.players.find { it.playerId == game.winnerId }?.playerName
                if (winnerName != null) {
                    Text(
                        text = stringResource(Res.string.games_winner, winnerName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            text = formatEpochMillis(game.startedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Top).padding(start = CatanSpacing.sm),
        )
    }
}
