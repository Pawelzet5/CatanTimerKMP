package org.example.project.catan_companion_feature.presentation.playerslist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_back
import catantimer.composeapp.generated.resources.common_cancel
import catantimer.composeapp.generated.resources.common_confirm
import catantimer.composeapp.generated.resources.common_delete
import catantimer.composeapp.generated.resources.players_add
import catantimer.composeapp.generated.resources.players_add_dialog_title
import catantimer.composeapp.generated.resources.players_delete
import catantimer.composeapp.generated.resources.players_delete_confirm_message
import catantimer.composeapp.generated.resources.players_hide
import catantimer.composeapp.generated.resources.players_name_hint
import catantimer.composeapp.generated.resources.players_title
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.presentation.components.ConfirmationDialog
import org.example.project.catan_companion_feature.presentation.components.PlayerListItem
import org.example.project.core.designsystem.CatanSpacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersListScreen(
    isSelectionMode: Boolean = false,
    onNavigateBack: () -> Unit,
    onPlayerClick: (Long) -> Unit,
    onPlayerSelected: (Player) -> Unit,
    viewModel: PlayersListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var playerWithMenu by remember { mutableStateOf<Player?>(null) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.players_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(text = "←", fontSize = 20.sp)
                    }
                },
                actions = {
                    if (!isSelectionMode) {
                        TextButton(onClick = { showAddDialog = true }) {
                            Text(stringResource(Res.string.players_add))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = CatanSpacing.xs)
        ) {
            items(uiState.players, key = { it.id }) { player ->
                PlayerListItemWithActions(
                    player = player,
                    isSelectionMode = isSelectionMode,
                    isMenuExpanded = playerWithMenu == player,
                    onItemClick = {
                        if (isSelectionMode) {
                            onPlayerSelected(player)
                            onNavigateBack()
                        } else {
                            onPlayerClick(player.id)
                        }
                    },
                    onLongClick = { playerWithMenu = player },
                    onDismissMenu = { playerWithMenu = null },
                    onHide = {
                        playerWithMenu = null
                        viewModel.onHidePlayer(player)
                    },
                    onDelete = {
                        playerWithMenu = null
                        playerToDelete = player
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = CatanSpacing.md),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }

    if (showAddDialog) {
        AddPlayerDialog(
            onConfirm = { name ->
                viewModel.onCreatePlayer(name)
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
                viewModel.onDeletePlayer(player)
                playerToDelete = null
            },
            onDismiss = { playerToDelete = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayerListItemWithActions(
    player: Player,
    isSelectionMode: Boolean,
    isMenuExpanded: Boolean,
    onItemClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        if (isSelectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = onItemClick)
                    .padding(horizontal = CatanSpacing.md, vertical = CatanSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "›",
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            PlayerListItem(
                player = player,
                onClick = onItemClick,
                modifier = Modifier.combinedClickable(
                    onClick = onItemClick,
                    onLongClick = onLongClick
                )
            )
        }

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
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
}
