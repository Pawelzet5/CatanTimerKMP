package org.example.project.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val CheckboxSize = 20.dp
private val CheckboxCorner = RoundedCornerShape(6.dp)
private val CheckboxInnerSize = 10.dp
private val CheckboxInnerCorner = RoundedCornerShape(2.dp)
private val CheckboxBorderWidth = 1.5.dp

/**
 * Custom checkbox matching the CatanCompanion design system.
 *
 * Unchecked: neutral surface background with a gray border.
 * Checked: accent-subtle background with an accent-colored border and a filled inner square
 * (instead of a checkmark, matching the mockup design).
 */
@Composable
fun CatanCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (checked)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val borderColor = if (checked)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .semantics { role = Role.Checkbox }
            .size(CheckboxSize)
            .background(bgColor, CheckboxCorner)
            .border(CheckboxBorderWidth, borderColor, CheckboxCorner)
            .clip(CheckboxCorner)
            .clickable(onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn() + scaleIn(initialScale = 0.6f),
            exit = fadeOut() + scaleOut(targetScale = 0.6f)
        ) {
            Box(
                modifier = Modifier
                    .size(CheckboxInnerSize)
                    .background(MaterialTheme.colorScheme.primary, CheckboxInnerCorner)
            )
        }
    }
}
