package org.example.project.catan_companion_feature.presentation.gameslist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.common_cancel
import catantimer.composeapp.generated.resources.common_delete
import catantimer.composeapp.generated.resources.games_completed
import catantimer.composeapp.generated.resources.games_in_progress
import catantimer.composeapp.generated.resources.games_title
import catantimer.composeapp.generated.resources.ic_close
import org.example.project.catan_companion_feature.presentation.components.ConfirmationDialog
import org.example.project.catan_companion_feature.presentation.components.GameListItem
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GamesListScreenRoot(
    onNavigateBack: () -> Unit,
    onResumeGame: (Long) -> Unit,
    onGameSummary: (Long) -> Unit,
    viewModel: GamesListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            GamesListEvent.NavigateBack -> onNavigateBack()
            is GamesListEvent.NavigateToGameplay -> onResumeGame(event.gameId)
            is GamesListEvent.NavigateToGameSummary -> onGameSummary(event.gameId)
        }
    }

    GamesListScreen(state = uiState, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesListScreen(
    state: GamesListState,
    onAction: (GamesListAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.games_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(GamesListAction.BackClick) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.inProgressGames.isEmpty() && state.completedGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.games_completed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = CatanSpacing.md,
                    vertical = CatanSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
            ) {
                if (state.inProgressGames.isNotEmpty()) {
                    item {
                        SectionHeader(stringResource(Res.string.games_in_progress))
                    }
                    items(state.inProgressGames, key = { it.id }) { game ->
                        SwipeToDeleteItem(
                            onSwipedToDelete = { onAction(GamesListAction.RequestDeleteGame(game)) }
                        ) {
                            GameListItem(
                                game = game,
                                onClick = { onAction(GamesListAction.GameClick(game.id)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (state.completedGames.isNotEmpty()) {
                    item {
                        SectionHeader(stringResource(Res.string.games_completed))
                    }
                    items(state.completedGames, key = { it.id }) { game ->
                        SwipeToDeleteItem(
                            onSwipedToDelete = { onAction(GamesListAction.RequestDeleteGame(game)) }
                        ) {
                            GameListItem(
                                game = game,
                                onClick = { onAction(GamesListAction.GameClick(game.id)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    state.gameToDelete?.let { game ->
        ConfirmationDialog(
            title = stringResource(Res.string.common_delete),
            message = game.players.sortedBy { it.orderIndex }.joinToString(", ") { it.playerName },
            confirmLabel = stringResource(Res.string.common_delete),
            dismissLabel = stringResource(Res.string.common_cancel),
            onConfirm = { onAction(GamesListAction.ConfirmDeleteGame) },
            onDismiss = { onAction(GamesListAction.DismissDeleteGame) }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(
            top = CatanSpacing.sm,
            bottom = CatanSpacing.xs
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    onSwipedToDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onSwipedToDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = stringResource(Res.string.common_delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = CatanSpacing.md)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        content = { content() }
    )
}
