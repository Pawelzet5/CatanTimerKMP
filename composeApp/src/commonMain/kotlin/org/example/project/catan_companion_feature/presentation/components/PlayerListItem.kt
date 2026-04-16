package org.example.project.catan_companion_feature.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.player_details_games_stats
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.designsystem.catanColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlayerListItem(
    player: Player,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(CatanSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.md)
    ) {
        PlayerAvatar(player = player, size = 44.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            val winRate = if (player.gamesPlayed == 0) 0 else (player.gamesWon * 100) / player.gamesPlayed
            Text(
                text = stringResource(
                    Res.string.player_details_games_stats,
                    player.gamesPlayed,
                    player.gamesWon,
                    winRate
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PlayerAvatar(
    player: Player,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = playerAvatarColors(player)
    val initials = player.name.take(2).uppercase()
    val fontSize = when {
        size >= 56.dp -> MaterialTheme.typography.titleLarge.fontSize
        size >= 44.dp -> MaterialTheme.typography.bodyLarge.fontSize
        else -> MaterialTheme.typography.bodySmall.fontSize
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun playerAvatarColors(player: Player): Pair<Color, Color> = when (player.id % 4) {
    0L -> MaterialTheme.catanColors.infoContainer to MaterialTheme.catanColors.infoIcon
    1L -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    2L -> MaterialTheme.catanColors.gamesContainer to MaterialTheme.catanColors.gamesIcon
    else -> MaterialTheme.catanColors.successContainer to MaterialTheme.catanColors.successIcon
}
