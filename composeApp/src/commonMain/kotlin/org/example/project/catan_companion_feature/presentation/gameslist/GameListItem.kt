package org.example.project.catan_companion_feature.presentation.gameslist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.config_cities_knights
import catantimer.composeapp.generated.resources.config_seafarers
import catantimer.composeapp.generated.resources.games_abandoned
import catantimer.composeapp.generated.resources.games_badge_active
import catantimer.composeapp.generated.resources.games_badge_done
import catantimer.composeapp.generated.resources.ic_dice
import catantimer.composeapp.generated.resources.ic_winner
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.enums.GameStatus
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.designsystem.catanColors
import org.example.project.core.util.formatDuration
import org.example.project.core.util.formatEpochMillis
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameListItem(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInProgress = game.status == GameStatus.IN_PROGRESS
    val hasWinner = game.status == GameStatus.COMPLETED && game.winnerId != null
    val catanColors = MaterialTheme.catanColors
    val playerNames = game.players.sortedBy { it.orderIndex }.joinToString(", ") { it.playerName }
    val subtitle = buildSubtitle(game)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics(mergeDescendants = true) {}
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isInProgress) {
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(catanColors.infoIcon)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = if (isInProgress) 13.dp else CatanSpacing.md,
                    end = CatanSpacing.md,
                    top = CatanSpacing.sm + 4.dp,
                    bottom = CatanSpacing.sm + 4.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm + 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isInProgress -> catanColors.infoContainer
                            hasWinner -> catanColors.successContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val statusIconDescription = when {
                    isInProgress -> stringResource(Res.string.games_badge_active)
                    hasWinner -> stringResource(Res.string.games_badge_done)
                    else -> stringResource(Res.string.games_abandoned)
                }
                Icon(
                    painter = painterResource(if (isInProgress) Res.drawable.ic_dice else Res.drawable.ic_winner),
                    contentDescription = statusIconDescription,
                    modifier = Modifier.size(20.dp),
                    tint = when {
                        isInProgress -> catanColors.infoIcon
                        hasWinner -> catanColors.successIcon
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = playerNames,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            GameStatusBadge(isInProgress = isInProgress, hasWinner = hasWinner)
        }
    }
}

@Composable
private fun buildSubtitle(game: Game): String {
    return when (game.status) {
        GameStatus.IN_PROGRESS -> {
            val date = formatEpochMillis(game.startedAt)
            val expansionParts = game.expansions.map { expansion ->
                when (expansion) {
                    GameExpansion.SEAFARERS -> stringResource(Res.string.config_seafarers)
                    GameExpansion.CITIES_AND_KNIGHTS -> stringResource(Res.string.config_cities_knights)
                }
            }
            if (expansionParts.isEmpty()) date
            else "$date · ${expansionParts.joinToString(", ")}"
        }
        GameStatus.COMPLETED -> {
            val endDate = formatEpochMillis(game.finishedAt ?: game.startedAt)
            val winner = game.players.find { it.playerId == game.winnerId }?.playerName
            if (winner != null) {
                val duration = game.finishedAt?.let { formatDuration(game.startedAt, it) }
                buildString {
                    append("🏆 $winner")
                    if (duration != null) append(" · $duration")
                    append(" · $endDate")
                }
            } else {
                "${stringResource(Res.string.games_abandoned)} · $endDate"
            }
        }
    }
}

@Composable
private fun GameStatusBadge(isInProgress: Boolean, hasWinner: Boolean) {
    val catanColors = MaterialTheme.catanColors
    when {
        isInProgress -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(catanColors.infoContainer)
                    .padding(horizontal = CatanSpacing.sm, vertical = CatanSpacing.xs)
            ) {
                Text(
                    text = stringResource(Res.string.games_badge_active),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = catanColors.infoIcon,
                    letterSpacing = 0.5.sp,
                )
            }
        }
        hasWinner -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(catanColors.successContainer)
                    .padding(horizontal = CatanSpacing.sm, vertical = CatanSpacing.xs)
            ) {
                Text(
                    text = stringResource(Res.string.games_badge_done),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = catanColors.successIcon,
                    letterSpacing = 0.5.sp,
                )
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = CatanSpacing.sm, vertical = CatanSpacing.xs)
            ) {
                Text(
                    text = stringResource(Res.string.games_abandoned),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

