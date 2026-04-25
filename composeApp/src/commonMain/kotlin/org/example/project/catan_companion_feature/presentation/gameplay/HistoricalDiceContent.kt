package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.gameplay_turn
import catantimer.composeapp.generated.resources.settings_save
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.DiceRoll
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Turn
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.DiceType
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.EventDiceType
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.dice.DiceRow
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.dice.EventDiceRow
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import io.github.pawelzielinski.catantimer.core.designsystem.components.CatanButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoricalDiceContent(
    turn: Turn,
    game: Game?,
    pendingDiceEdit: DiceRoll?,
    onDiceSelected: (Int, Int, EventDiceType?) -> Unit,
    onSaveChanges: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasCitiesAndKnights = game?.expansions?.contains(GameExpansion.CITIES_AND_KNIGHTS) == true
    val isSaveEnabled = pendingDiceEdit != null
        && pendingDiceEdit.red > 0
        && pendingDiceEdit.yellow > 0
        && (!hasCitiesAndKnights || pendingDiceEdit.event != null)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(CatanSpacing.md),
        verticalArrangement = Arrangement.spacedBy(CatanSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.gameplay_turn, turn.number),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = turn.playerName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DiceRow(
            selectedValue = pendingDiceEdit?.red,
            diceType = DiceType.RedDice,
            onValueSelected = { red ->
                onDiceSelected(red, pendingDiceEdit?.yellow ?: 0, pendingDiceEdit?.event)
            }
        )

        DiceRow(
            selectedValue = pendingDiceEdit?.yellow,
            diceType = DiceType.YellowDice,
            onValueSelected = { yellow ->
                onDiceSelected(pendingDiceEdit?.red ?: 0, yellow, pendingDiceEdit?.event)
            }
        )

        if (hasCitiesAndKnights) {
            EventDiceRow(
                selectedType = pendingDiceEdit?.event,
                onTypeSelected = { event ->
                    onDiceSelected(pendingDiceEdit?.red ?: 0, pendingDiceEdit?.yellow ?: 0, event)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        CatanButton(
            text = stringResource(Res.string.settings_save),
            onClick = onSaveChanges,
            enabled = isSaveEnabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
