package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameslist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.common_cancel
import catantimer.composeapp.generated.resources.common_delete
import catantimer.composeapp.generated.resources.games_completed
import catantimer.composeapp.generated.resources.games_empty
import catantimer.composeapp.generated.resources.games_in_progress
import catantimer.composeapp.generated.resources.games_title
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.components.ConfirmationDialog
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import io.github.pawelzielinski.catantimer.core.presentation.ObserveAsEvents
import io.github.pawelzielinski.catantimer.core.presentation.components.SwipeToDeleteItem
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.games_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .padding(start = CatanSpacing.sm)
                            .semantics(mergeDescendants = true) { role = Role.Button }
                            .clip(RoundedCornerShape(CatanSpacing.sm))
                            .clickable { onAction(GamesListAction.BackClick) }
                            .padding(horizontal = CatanSpacing.sm, vertical = CatanSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(Res.string.common_back),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = { Spacer(modifier = Modifier.width(60.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                    text = stringResource(Res.string.games_empty),
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
                            deleteActionLabel = stringResource(Res.string.common_delete),
                            onDelete = { onAction(GamesListAction.RequestDeleteGame(game)) }
                        ) { accessibilityModifier ->
                            GameListItem(
                                game = game,
                                onClick = { onAction(GamesListAction.GameClick(game.id)) },
                                modifier = Modifier.fillMaxWidth().then(accessibilityModifier)
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
                            deleteActionLabel = stringResource(Res.string.common_delete),
                            onDelete = { onAction(GamesListAction.RequestDeleteGame(game)) }
                        ) { accessibilityModifier ->
                            GameListItem(
                                game = game,
                                onClick = { onAction(GamesListAction.GameClick(game.id)) },
                                modifier = Modifier.fillMaxWidth().then(accessibilityModifier)
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
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
        modifier = Modifier
            .padding(top = CatanSpacing.sm, bottom = CatanSpacing.xs)
            .semantics { heading() }
    )
}
