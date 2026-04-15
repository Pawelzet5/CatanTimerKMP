package org.example.project.catan_companion_feature.presentation.components.dice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.core.designsystem.CatanDiceSelectedBorder

@Composable
fun Dice(
    value: Int,
    isSelected: Boolean,
    backgroundColor: Color,
    dotColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
) {
    Canvas(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick),
    ) {
        val canvasSize = this.size.width
        val cornerRadius = canvasSize * 0.15f
        val dotRadius = canvasSize * 0.09f
        val borderWidth = canvasSize * 0.06f

        // Background
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
        )

        // Selection border
        if (isSelected) {
            drawRoundRect(
                color = CatanDiceSelectedBorder,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                style = Stroke(width = borderWidth),
            )
        }

        // Dots
        val positions = DiceLayout.getPositions(value)
        positions.forEach { fraction ->
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = androidx.compose.ui.geometry.Offset(
                    x = fraction.x * canvasSize,
                    y = fraction.y * canvasSize,
                ),
            )
        }
    }
}
