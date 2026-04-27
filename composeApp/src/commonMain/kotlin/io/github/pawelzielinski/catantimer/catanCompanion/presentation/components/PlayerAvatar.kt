package io.github.pawelzielinski.catantimer.catanCompanion.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import io.github.pawelzielinski.catantimer.core.designsystem.catanColors

enum class PlayerAvatarSize(val dp: Dp) {
    Small(32.dp),
    Medium(44.dp),
    Large(80.dp)
}

@Composable
fun PlayerAvatar(
    name: String,
    colorIndex: Int,
    size: PlayerAvatarSize,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = avatarColors(colorIndex)
    val initials = resolveInitials(name)
    val textStyle = when (size) {
        PlayerAvatarSize.Small -> MaterialTheme.typography.bodySmall
        PlayerAvatarSize.Medium -> MaterialTheme.typography.bodyLarge
        PlayerAvatarSize.Large -> MaterialTheme.typography.titleLarge
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = textColor,
            fontSize = textStyle.fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun resolveInitials(name: String): String {
    val words = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    return if (words.size >= 2) {
        "${words[0].first()}${words[1].first()}".uppercase()
    } else {
        name.trim().take(2).uppercase()
    }
}

@Composable
private fun avatarColors(colorIndex: Int): Pair<Color, Color> = when (colorIndex % 4) {
    0 -> MaterialTheme.catanColors.infoContainer to MaterialTheme.catanColors.infoIcon
    1 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    2 -> MaterialTheme.catanColors.gamesContainer to MaterialTheme.catanColors.gamesIcon
    else -> MaterialTheme.catanColors.successContainer to MaterialTheme.catanColors.successIcon
}
