package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.dashboard_games_list
import catantimer.composeapp.generated.resources.dashboard_new_game
import catantimer.composeapp.generated.resources.dashboard_players_list
import catantimer.composeapp.generated.resources.dashboard_recent_activity
import catantimer.composeapp.generated.resources.dashboard_resume_game
import catantimer.composeapp.generated.resources.dashboard_subtitle
import catantimer.composeapp.generated.resources.dashboard_title
import catantimer.composeapp.generated.resources.ic_menu
import catantimer.composeapp.generated.resources.ic_person
import catantimer.composeapp.generated.resources.ic_play
import catantimer.composeapp.generated.resources.ic_plus
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.core.designsystem.CatanBgSecondary
import io.github.pawelzielinski.catantimer.core.designsystem.CatanBrown
import io.github.pawelzielinski.catantimer.core.designsystem.CatanHeaderGradientEnd
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import io.github.pawelzielinski.catantimer.core.designsystem.catanColors
import io.github.pawelzielinski.catantimer.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreenRoot(
    onNewGame: () -> Unit,
    onResumeGame: (Long) -> Unit,
    onGamesList: () -> Unit,
    onPlayersList: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            DashboardEvent.NavigateToNewGame -> onNewGame()
            is DashboardEvent.NavigateToResumeGame -> onResumeGame(event.gameId)
            DashboardEvent.NavigateToGamesList -> onGamesList()
            DashboardEvent.NavigateToPlayersList -> onPlayersList()
        }
    }

    DashboardScreen(state = uiState, onAction = viewModel::onAction)
}

@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DashboardHeader()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(
                start = CatanSpacing.md,
                end = CatanSpacing.md,
                top = CatanSpacing.md,
                bottom = CatanSpacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.md)
        ) {
            state.resumableGame?.let { game ->
                item {
                    ResumeGameCard(
                        game = game,
                        onResume = { onAction(DashboardAction.ResumeGameClick(game.id)) }
                    )
                }
            }
            item {
                ActionCardsRow(
                    onNewGame = { onAction(DashboardAction.NewGameClick) },
                    onGamesList = { onAction(DashboardAction.GamesListClick) },
                    onPlayersList = { onAction(DashboardAction.PlayersListClick) }
                )
            }
            item {
                RecentActivitySection(
                    totalGames = state.totalGames,
                    totalPlayers = state.totalPlayers
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(CatanBrown, CatanHeaderGradientEnd))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(
                start = CatanSpacing.lg,
                end = CatanSpacing.lg,
                top = CatanSpacing.xxl,
                bottom = CatanSpacing.lg
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.dashboard_title),
                style = MaterialTheme.typography.displaySmall,
                color = CatanBgSecondary,
            )
            Spacer(modifier = Modifier.height(CatanSpacing.xs))
            Text(
                text = stringResource(Res.string.dashboard_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = CatanBgSecondary.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun ResumeGameCard(
    game: Game,
    onResume: () -> Unit
) {
    val ext = MaterialTheme.catanColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onResume),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(CatanSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CatanSpacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = ext.successContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_play),
                    contentDescription = null,
                    tint = ext.successIcon,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.dashboard_resume_game),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                val playerNames = game.players
                    .sortedBy { it.orderIndex }
                    .joinToString(", ") { it.playerName }
                if (playerNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = playerNames,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                painter = painterResource(Res.drawable.ic_play),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ActionCardsRow(
    onNewGame: () -> Unit,
    onGamesList: () -> Unit,
    onPlayersList: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val ext = MaterialTheme.catanColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
    ) {
        ActionCard(
            label = stringResource(Res.string.dashboard_new_game),
            icon = Res.drawable.ic_plus,
            iconColor = scheme.primary,
            iconBackground = scheme.primaryContainer,
            borderColor = scheme.primary,
            onClick = onNewGame,
            modifier = Modifier.weight(1f)
        )
        ActionCard(
            label = stringResource(Res.string.dashboard_games_list),
            icon = Res.drawable.ic_menu,
            iconColor = ext.gamesIcon,
            iconBackground = ext.gamesContainer,
            borderColor = ext.gamesIcon,
            onClick = onGamesList,
            modifier = Modifier.weight(1f)
        )
        ActionCard(
            label = stringResource(Res.string.dashboard_players_list),
            icon = Res.drawable.ic_person,
            iconColor = ext.infoIcon,
            iconBackground = ext.infoContainer,
            borderColor = ext.infoIcon,
            onClick = onPlayersList,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionCard(
    label: String,
    icon: DrawableResource,
    iconColor: Color,
    iconBackground: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(width = 2.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = CatanSpacing.md, horizontal = CatanSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color = iconBackground, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RecentActivitySection(
    totalGames: Int,
    totalPlayers: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(CatanSpacing.md)) {
            Text(
                text = stringResource(Res.string.dashboard_recent_activity),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(CatanSpacing.sm))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CatanSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    value = totalGames.toString(),
                    label = stringResource(Res.string.dashboard_games_list),
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider()
                StatItem(
                    value = totalPlayers.toString(),
                    label = stringResource(Res.string.dashboard_players_list),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(48.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
