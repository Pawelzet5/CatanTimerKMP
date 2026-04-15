package org.example.project.catan_companion_feature.presentation.playerdetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.coming_soon
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerDetailsScreen(
    playerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PlayerDetailsViewModel = koinViewModel { parametersOf(playerId) }
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(Res.string.coming_soon))
    }
}
