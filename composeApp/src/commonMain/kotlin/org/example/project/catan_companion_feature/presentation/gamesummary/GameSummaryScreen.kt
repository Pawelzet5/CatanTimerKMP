package org.example.project.catan_companion_feature.presentation.gamesummary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.coming_soon
import org.example.project.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GameSummaryScreenRoot(
    gameId: Long,
    onNavigateBack: () -> Unit,
    viewModel: GameSummaryViewModel = koinViewModel { parametersOf(gameId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            GameSummaryEvent.NavigateBack -> onNavigateBack()
        }
    }

    GameSummaryScreen(state = uiState, onAction = viewModel::onAction)
}

@Composable
fun GameSummaryScreen(
    state: GameSummaryState,
    onAction: (GameSummaryAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(Res.string.coming_soon))
    }
}
