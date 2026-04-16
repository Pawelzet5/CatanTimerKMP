package org.example.project.catan_companion_feature.presentation.playerdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_cancel
import catantimer.composeapp.generated.resources.common_confirm
import catantimer.composeapp.generated.resources.common_save
import catantimer.composeapp.generated.resources.player_details_games_played
import catantimer.composeapp.generated.resources.player_details_wins
import catantimer.composeapp.generated.resources.players_delete
import catantimer.composeapp.generated.resources.players_delete_confirm_message
import catantimer.composeapp.generated.resources.players_hide
import catantimer.composeapp.generated.resources.players_hide_confirm_message
import catantimer.composeapp.generated.resources.players_name_hint
import org.example.project.catan_companion_feature.presentation.components.ConfirmationDialog
import org.example.project.core.designsystem.CatanSpacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailsScreen(
    playerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PlayerDetailsViewModel = koinViewModel { parametersOf(playerId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val player = uiState.player

    var editedName by remember(player?.name) { mutableStateOf(player?.name.orEmpty()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showHideDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(player?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(text = "←", fontSize = 20.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(CatanSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(CatanSpacing.md)
        ) {
            NameEditSection(
                name = editedName,
                onNameChange = { editedName = it },
                onSave = { viewModel.onUpdateName(editedName) },
                isSaveEnabled = editedName.isNotBlank() && editedName != player?.name
            )

            if (player != null) {
                PlayerStatsCard(
                    gamesPlayed = player.gamesPlayed,
                    gamesWon = player.gamesWon
                )
            }

            Spacer(modifier = Modifier.height(CatanSpacing.md))

            OutlinedButton(
                onClick = { showHideDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.players_hide))
            }

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(Res.string.players_delete))
            }
        }
    }

    if (showDeleteDialog && player != null) {
        ConfirmationDialog(
            title = stringResource(Res.string.players_delete),
            message = stringResource(Res.string.players_delete_confirm_message, player.name),
            confirmLabel = stringResource(Res.string.common_confirm),
            onConfirm = {
                viewModel.onDeletePlayer()
                showDeleteDialog = false
                onNavigateBack()
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
                viewModel.onHidePlayer()
                showHideDialog = false
                onNavigateBack()
            },
            onDismiss = { showHideDialog = false }
        )
    }
}

@Composable
private fun NameEditSection(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text(stringResource(Res.string.players_name_hint)) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = onSave,
            enabled = isSaveEnabled
        ) {
            Text(stringResource(Res.string.common_save))
        }
    }
}

@Composable
private fun PlayerStatsCard(
    gamesPlayed: Int,
    gamesWon: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(CatanSpacing.md)) {
            StatRow(
                label = stringResource(Res.string.player_details_games_played),
                value = gamesPlayed.toString()
            )
            StatRow(
                label = stringResource(Res.string.player_details_wins),
                value = gamesWon.toString()
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CatanSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
