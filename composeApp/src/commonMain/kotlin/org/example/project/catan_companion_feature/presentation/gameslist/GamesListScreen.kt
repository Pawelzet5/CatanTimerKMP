package org.example.project.catan_companion_feature.presentation.gameslist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
import catantimer.composeapp.generated.resources.ic_close
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.presentation.components.ConfirmationDialog
import org.example.project.catan_companion_feature.presentation.components.GameListItem
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

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
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = CatanSpacing.sm, bottom = CatanSpacing.xs)
    )
}

@Composable
private fun SwipeToDeleteItem(
    onSwipedToDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var itemWidth by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .onSizeChanged { itemWidth = it.width.toFloat() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = stringResource(Res.string.common_delete),
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(end = CatanSpacing.md)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(-itemWidth, 0f))
                        }
                    },
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            val dismissedByDistance = -offsetX.value >= itemWidth * 0.85f
                            val dismissedByFling = velocity < -1500f
                            if (dismissedByDistance || dismissedByFling) {
                                offsetX.animateTo(-itemWidth, tween(durationMillis = 200))
                                onSwipedToDelete()
                                offsetX.snapTo(0f)
                            } else {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                            }
                        }
                    }
                )
        ) {
            content()
        }
    }
}
