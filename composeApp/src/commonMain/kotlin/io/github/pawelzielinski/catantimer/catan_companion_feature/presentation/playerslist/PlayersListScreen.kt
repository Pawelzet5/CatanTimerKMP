package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.playerslist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.common_cancel
import catantimer.composeapp.generated.resources.common_confirm
import catantimer.composeapp.generated.resources.common_delete
import catantimer.composeapp.generated.resources.ic_plus
import catantimer.composeapp.generated.resources.players_add
import catantimer.composeapp.generated.resources.players_add_dialog_title
import catantimer.composeapp.generated.resources.players_add_title
import catantimer.composeapp.generated.resources.players_delete
import catantimer.composeapp.generated.resources.players_delete_confirm_message
import catantimer.composeapp.generated.resources.players_done_count
import catantimer.composeapp.generated.resources.players_games_count
import catantimer.composeapp.generated.resources.players_hide
import catantimer.composeapp.generated.resources.players_name_hint
import catantimer.composeapp.generated.resources.players_selection_hint
import catantimer.composeapp.generated.resources.players_title
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Player
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.ConfirmationDialog
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.PlayerAvatar
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.PlayerAvatarSize
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.PlayerListItem
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import io.github.pawelzielinski.catantimer.core.designsystem.components.CatanCheckbox
import io.github.pawelzielinski.catantimer.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayersListScreenRoot(
    isSelectionMode: Boolean = false,
    onNavigateBack: () -> Unit,
    onPlayerClick: (Long) -> Unit,
    onPlayersSelected: (List<Player>) -> Unit,
    viewModel: PlayersListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PlayersListEvent.NavigateBack -> onNavigateBack()
            is PlayersListEvent.NavigateToPlayerDetails -> onPlayerClick(event.playerId)
            is PlayersListEvent.PlayersSelected -> onPlayersSelected(event.players)
        }
    }

    PlayersListScreen(
        state = uiState,
        isSelectionMode = isSelectionMode,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersListScreen(
    state: PlayersListState,
    isSelectionMode: Boolean = false,
    onAction: (PlayersListAction) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var playerWithMenu by remember { mutableStateOf<Player?>(null) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }
    val selectedIds = remember { mutableStateOf(emptySet<Long>()) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopBar(
                    selectedCount = selectedIds.value.size,
                    onCancel = { onAction(PlayersListAction.BackClick) },
                    onDone = {
                        onAction(PlayersListAction.DoneWithSelection(
                            state.players.filter { it.id in selectedIds.value }
                        ))
                    }
                )
            } else {
                NormalTopBar(
                    onNavigateBack = { onAction(PlayersListAction.BackClick) },
                    onAdd = { showAddDialog = true }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isSelectionMode) {
                SelectionHintBanner()
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(CatanSpacing.md),
                verticalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
            ) {
                items(state.players, key = { it.id }) { player ->
                    if (isSelectionMode) {
                        SelectionPlayerItem(
                            player = player,
                            isSelected = player.id in selectedIds.value,
                            onToggle = {
                                selectedIds.value = if (player.id in selectedIds.value) {
                                    selectedIds.value - player.id
                                } else {
                                    selectedIds.value + player.id
                                }
                            }
                        )
                    } else {
                        NormalPlayerItem(
                            player = player,
                            isMenuExpanded = playerWithMenu == player,
                            onItemClick = { onAction(PlayersListAction.PlayerClick(player.id)) },
                            onLongClick = { playerWithMenu = player },
                            onDismissMenu = { playerWithMenu = null },
                            onHide = {
                                playerWithMenu = null
                                onAction(PlayersListAction.HidePlayer(player))
                            },
                            onDelete = {
                                playerWithMenu = null
                                playerToDelete = player
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPlayerDialog(
            onConfirm = { name ->
                onAction(PlayersListAction.CreatePlayer(name))
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    playerToDelete?.let { player ->
        ConfirmationDialog(
            title = stringResource(Res.string.players_delete),
            message = stringResource(Res.string.players_delete_confirm_message, player.name),
            confirmLabel = stringResource(Res.string.common_delete),
            onConfirm = {
                onAction(PlayersListAction.DeletePlayer(player))
                playerToDelete = null
            },
            onDismiss = { playerToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopBar(
    onNavigateBack: () -> Unit,
    onAdd: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(Res.string.players_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back)
                )
            }
        },
        actions = {
            IconButton(
                onClick = onAdd,
                shape = CircleShape,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .padding(end = CatanSpacing.xs)
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_plus),
                    contentDescription = stringResource(Res.string.players_add),
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(Res.string.players_add_title)) },
        navigationIcon = {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(Res.string.common_cancel),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            TextButton(
                onClick = onDone,
                enabled = selectedCount > 0,
                modifier = Modifier
                    .padding(end = CatanSpacing.xs)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selectedCount > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Text(
                    text = stringResource(Res.string.players_done_count, selectedCount),
                    color = if (selectedCount > 0) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
private fun SelectionHintBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = CatanSpacing.md, vertical = CatanSpacing.sm)
    ) {
        Text(
            text = stringResource(Res.string.players_selection_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun SelectionPlayerItem(
    player: Player,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val borderModifier = if (isSelected)
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    else Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .then(borderModifier)
            .combinedClickable(onClick = onToggle)
            .padding(CatanSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.md)
    ) {
        CatanCheckbox(checked = isSelected, onCheckedChange = onToggle)
        PlayerAvatar(name = player.name, colorIndex = player.id.toInt(), size = PlayerAvatarSize.Small)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(Res.string.players_games_count, player.gamesPlayed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NormalPlayerItem(
    player: Player,
    isMenuExpanded: Boolean,
    onItemClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit
) {
    androidx.compose.foundation.layout.Box {
        PlayerListItem(
            player = player,
            onClick = onItemClick,
            modifier = Modifier.combinedClickable(
                onClick = onItemClick,
                onLongClick = onLongClick
            )
        )
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = onDismissMenu
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.players_hide)) },
                onClick = onHide
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(Res.string.players_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun AddPlayerDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.players_add_dialog_title)) },
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
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(Res.string.common_confirm))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
}
