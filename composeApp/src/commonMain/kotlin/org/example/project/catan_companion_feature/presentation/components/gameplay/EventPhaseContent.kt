package org.example.project.catan_companion_feature.presentation.components.gameplay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.event_barbarians_arrived
import catantimer.composeapp.generated.resources.event_move_robber
import catantimer.composeapp.generated.resources.event_move_robber_or_pirate
import catantimer.composeapp.generated.resources.event_robber_message
import catantimer.composeapp.generated.resources.event_robber_not_active
import catantimer.composeapp.generated.resources.event_robber_title
import catantimer.composeapp.generated.resources.gameplay_continue
import org.example.project.catan_companion_feature.domain.dataclass.BarbarianState
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.designsystem.components.CatanButton
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
    val isBarbarianArrival = turn.eventDice == EventDiceType.BARBARIANS
        && barbarianState?.position == 7

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CatanSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CatanSpacing.md),
    ) {
        when {
            isBarbarianArrival -> {
                Text(
                    text = stringResource(Res.string.event_barbarians_arrived),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            turn.isRobberRoll -> {
                Text(
                    text = stringResource(Res.string.event_robber_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(Res.string.event_robber_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val robberMessage = when {
                    isCitiesAndKnights && barbarianState?.hasFirstRaidOccurred == false ->
                        stringResource(Res.string.event_robber_not_active)
                    isSeafarers ->
                        stringResource(Res.string.event_move_robber_or_pirate)
                    else ->
                        stringResource(Res.string.event_move_robber)
                }
                Text(
                    text = robberMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                // No event to display — should not be shown
            }
        }

        Spacer(modifier = Modifier.height(CatanSpacing.md))

        CatanButton(
            text = stringResource(Res.string.gameplay_continue),
            onClick = onContinue,
        )
    }
}
