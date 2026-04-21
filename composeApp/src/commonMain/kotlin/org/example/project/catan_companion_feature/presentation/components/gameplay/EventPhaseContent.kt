package org.example.project.catan_companion_feature.presentation.components.gameplay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.event_barbarians_arrived_header
import catantimer.composeapp.generated.resources.event_barbarians_description
import catantimer.composeapp.generated.resources.event_barbarians_title
import catantimer.composeapp.generated.resources.event_move_robber
import catantimer.composeapp.generated.resources.event_move_robber_or_pirate
import catantimer.composeapp.generated.resources.event_robber_message
import catantimer.composeapp.generated.resources.event_robber_not_active
import catantimer.composeapp.generated.resources.event_thief_title
import catantimer.composeapp.generated.resources.gameplay_continue_to_timer
import catantimer.composeapp.generated.resources.ic_barbarians
import org.example.project.catan_companion_feature.domain.dataclass.BarbarianState
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.presentation.components.dice.Dice
import org.example.project.catan_companion_feature.presentation.components.dice.EventDice
import org.example.project.core.designsystem.CatanDiceRedBackground
import org.example.project.core.designsystem.CatanDiceRedDot
import org.example.project.core.designsystem.CatanDiceYellowBackground
import org.example.project.core.designsystem.CatanDiceYellowDot
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.designsystem.CatanWarning
import org.example.project.core.designsystem.catanColors
import org.example.project.core.designsystem.components.CatanButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EventPhaseContent(
    turn: Turn,
    game: Game,
    barbarianState: BarbarianState?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCitiesAndKnights = GameExpansion.CITIES_AND_KNIGHTS in game.expansions
    val isSeafarers = GameExpansion.SEAFARERS in game.expansions
    val isBarbarianArrival = turn.eventDice == EventDiceType.BARBARIANS && barbarianState?.position == 7

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = CatanSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when {
                isBarbarianArrival -> BarbarianEventContent(turn = turn, barbarianState = barbarianState)
                turn.isRobberRoll -> RobberEventContent(
                    turn = turn,
                    isSeafarers = isSeafarers,
                    isCitiesAndKnights = isCitiesAndKnights,
                    barbarianState = barbarianState,
                )
            }
        }
        CatanButton(
            text = stringResource(Res.string.gameplay_continue_to_timer),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().padding(bottom = CatanSpacing.lg),
        )
    }
}

@Composable
private fun RobberEventContent(
    turn: Turn,
    isSeafarers: Boolean,
    isCitiesAndKnights: Boolean,
    barbarianState: BarbarianState?,
) {
    val robberNotActive = isCitiesAndKnights && barbarianState?.hasFirstRaidOccurred == false
    val moveInstruction = when {
        robberNotActive -> stringResource(Res.string.event_robber_not_active)
        isSeafarers -> stringResource(Res.string.event_move_robber_or_pirate)
        else -> stringResource(Res.string.event_move_robber)
    }

    EventIllustration { Text(text = "🦹", style = MaterialTheme.typography.displayLarge) }
    Spacer(Modifier.height(CatanSpacing.md))
    Text(
        text = stringResource(Res.string.event_thief_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(CatanSpacing.sm))
    turn.redDice?.let { red ->
        turn.yellowDice?.let { yellow ->
            DiceRollSummaryRow(redValue = red, yellowValue = yellow)
        }
    }
    Spacer(Modifier.height(CatanSpacing.md))
    Text(
        text = stringResource(Res.string.event_robber_message),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(CatanSpacing.sm))
    Text(
        text = moveInstruction,
        style = MaterialTheme.typography.bodySmall,
        fontStyle = if (robberNotActive) FontStyle.Italic else FontStyle.Normal,
        textAlign = TextAlign.Center,
        color = if (robberNotActive) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    )
}

@Composable
private fun BarbarianEventContent(turn: Turn, barbarianState: BarbarianState?) {
    EventIllustration {
        Icon(
            painter = painterResource(Res.drawable.ic_barbarians),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
    Spacer(Modifier.height(CatanSpacing.md))
    Text(
        text = stringResource(Res.string.event_barbarians_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(CatanSpacing.sm))
    EventDice(type = EventDiceType.BARBARIANS, isSelected = false, onClick = {})
    Spacer(Modifier.height(CatanSpacing.md))
    BarbarianErrorBanner()
    barbarianState?.let {
        Spacer(Modifier.height(CatanSpacing.sm))
        BarbarianTracker(state = it)
    }
}

@Composable
private fun BarbarianErrorBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
            .padding(CatanSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CatanSpacing.xs),
    ) {
        Text(
            text = stringResource(Res.string.event_barbarians_arrived_header),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.event_barbarians_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EventIllustration(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(2.dp, MaterialTheme.catanColors.borderAccent, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun DiceRollSummaryRow(redValue: Int, yellowValue: Int) {
    val sum = redValue + yellowValue
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm),
    ) {
        Dice(value = redValue, isSelected = false, backgroundColor = CatanDiceRedBackground, dotColor = CatanDiceRedDot, onClick = {}, size = 36.dp)
        Text(text = "+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Dice(value = yellowValue, isSelected = false, backgroundColor = CatanDiceYellowBackground, dotColor = CatanDiceYellowDot, onClick = {}, size = 36.dp)
        Text(text = "=", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "$sum", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CatanWarning)
    }
}
