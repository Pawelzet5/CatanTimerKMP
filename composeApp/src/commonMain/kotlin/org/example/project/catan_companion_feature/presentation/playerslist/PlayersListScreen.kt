package org.example.project.catan_companion_feature.presentation.playerslist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.coming_soon
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayersListScreen(
    isSelectionMode: Boolean = false,
    onNavigateBack: () -> Unit,
    onPlayerClick: (Long) -> Unit,
    onPlayerSelected: (Player) -> Unit,
    viewModel: PlayersListViewModel = koinViewModel()
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(Res.string.coming_soon))
    }
}
