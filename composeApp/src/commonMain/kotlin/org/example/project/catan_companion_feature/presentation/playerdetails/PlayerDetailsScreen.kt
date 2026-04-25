package org.example.project.catan_companion_feature.presentation.playerdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.common_cancel
import catantimer.composeapp.generated.resources.common_confirm
import catantimer.composeapp.generated.resources.common_edit
import catantimer.composeapp.generated.resources.common_save
import catantimer.composeapp.generated.resources.player_details_games_played
import catantimer.composeapp.generated.resources.player_details_since
import catantimer.composeapp.generated.resources.player_details_statistics
import catantimer.composeapp.generated.resources.player_details_title
import catantimer.composeapp.generated.resources.player_details_win_rate
import catantimer.composeapp.generated.resources.player_details_wins
import catantimer.composeapp.generated.resources.players_delete
import catantimer.composeapp.generated.resources.players_delete_confirm_message
import catantimer.composeapp.generated.resources.players_hide
import catantimer.composeapp.generated.resources.players_hide_confirm_message
import catantimer.composeapp.generated.resources.players_name_hint
import catantimer.composeapp.generated.resources.players_overflow_cd
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.presentation.components.ConfirmationDialog
import org.example.project.catan_companion_feature.presentation.components.PlayerAvatar
import org.example.project.catan_companion_feature.presentation.components.PlayerAvatarSize
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.util.formatEpochMillisToMonthYear
import org.example.project.core.designsystem.catanColors
import org.example.project.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerDetailsScreenRoot(
    playerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PlayerDetailsViewModel = koinViewModel { parametersOf(playerId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PlayerDetailsEvent.NavigateBack -> onNavigateBack()
        }
    }

    PlayerDetailsScreen(state = uiState, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailsScreen(
    state: PlayerDetailsState,
    onAction: (PlayerDetailsAction) -> Unit
) {
    val player = state.player

    var showEditDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showHideDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.player_details_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(PlayerDetailsAction.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showEditDialog = true }) {
                        Text(
                            text = stringResource(Res.string.common_edit),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(Res.string.players_overflow_cd)
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.players_hide)) },
                                onClick = {
                                    showOverflowMenu = false
                                    showHideDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(Res.string.players_delete),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(CatanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.md)
        ) {
            if (player != null) {
                HeroSection(player = player)
                StatisticsCard(player = player)
            }
        }
    }

    if (showEditDialog && player != null) {
        EditNameDialog(
            currentName = player.name,
            onSave = { newName ->
                onAction(PlayerDetailsAction.UpdateName(newName))
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteDialog && player != null) {
        ConfirmationDialog(
            title = stringResource(Res.string.players_delete),
            message = stringResource(Res.string.players_delete_confirm_message, player.name),
            confirmLabel = stringResource(Res.string.common_confirm),
            onConfirm = {
                onAction(PlayerDetailsAction.DeletePlayerClick)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showHideDialog && player != null) {
        ConfirmationDialog(
            title = stringResource(Res.string.players_hide),
            message = stringResource(Res.string.players_hide_confirm_message, player.name),
            confirmLabel = stringResource(Res.string.common_confirm),
            onConfirm = {
                onAction(PlayerDetailsAction.HidePlayerClick)
                showHideDialog = false
            },
            onDismiss = { showHideDialog = false }
        )
    }
}

@Composable
private fun HeroSection(player: Player) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CatanSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CatanSpacing.md)
    ) {
        PlayerAvatar(name = player.name, colorIndex = player.id.toInt(), size = PlayerAvatarSize.Large)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            player.createdAt?.let { timestamp ->
                Text(
                    text = stringResource(
                        Res.string.player_details_since,
                        formatEpochMillisToMonthYear(timestamp)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = CatanSpacing.xs)
                )
            }
        }

        MiniStatsRow(player = player)
    }
}

@Composable
private fun MiniStatsRow(player: Player) {
    val winRate = if (player.gamesPlayed == 0) 0 else (player.gamesWon * 100) / player.gamesPlayed

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatPill(value = player.gamesPlayed.toString(), label = stringResource(Res.string.player_details_games_played))
        VerticalDivider()
        StatPill(value = player.gamesWon.toString(), label = stringResource(Res.string.player_details_wins))
        VerticalDivider()
        StatPill(
            value = "$winRate%",
            label = stringResource(Res.string.player_details_win_rate),
            valueColor = MaterialTheme.catanColors.successIcon
        )
    }
}

@Composable
private fun StatPill(
    value: String,
    label: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = CatanSpacing.lg)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .padding(vertical = CatanSpacing.xs)
            .background(MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(text = " ")  // gives the box height
    }
}

@Composable
private fun StatisticsCard(player: Player) {
    val winRate = if (player.gamesPlayed == 0) 0 else (player.gamesWon * 100) / player.gamesPlayed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(CatanSpacing.md)
    ) {
        Text(
            text = stringResource(Res.string.player_details_statistics),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = CatanSpacing.sm)
        )
        StatRow(
            label = stringResource(Res.string.player_details_games_played),
            value = player.gamesPlayed.toString()
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        StatRow(
            label = stringResource(Res.string.player_details_wins),
            value = player.gamesWon.toString()
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        StatRow(
            label = stringResource(Res.string.player_details_win_rate),
            value = "$winRate%",
            valueColor = MaterialTheme.catanColors.successIcon
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CatanSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.common_edit)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(Res.string.players_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name.trim()) },
                enabled = name.isNotBlank() && name != currentName
            ) {
                Text(stringResource(Res.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
}

