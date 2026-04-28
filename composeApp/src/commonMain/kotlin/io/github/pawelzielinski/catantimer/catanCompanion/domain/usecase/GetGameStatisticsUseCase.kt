package io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.DiceDistribution
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.GameStatistics
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.GameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.TurnRepository
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.Result

class GetGameStatisticsUseCase(
    private val gameRepository: GameRepository,
    private val turnRepository: TurnRepository
) {
    suspend operator fun invoke(gameId: Long): Result<GameStatistics, DataError.Local> {
        val game = gameRepository.getGameById(gameId).firstOrNull()
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        val turns = turnRepository.getTurnsForGame(gameId).first()

        val completedTurns = turns.filter { it.durationMillis > 0 }

        val avgDuration = if (completedTurns.isEmpty()) 0L
        else completedTurns.sumOf { it.durationMillis } / completedTurns.size

        val diceCounts = turns
            .mapNotNull { it.diceSum }
            .groupingBy { it }
            .eachCount()

        val eventCounts = turns
            .mapNotNull { it.eventDice }
            .groupingBy { it }
            .eachCount()

        val playerAvgDurations = game.players.associate { gamePlayer ->
            val playerTurns = completedTurns.filter { it.playerId == gamePlayer.playerId }
            val avg = if (playerTurns.isEmpty()) 0L
            else playerTurns.sumOf { it.durationMillis } / playerTurns.size
            gamePlayer.playerId to avg
        }

        return Result.Success(
            GameStatistics(
                totalTurns = turns.size,
                averageTurnDurationMillis = avgDuration,
                diceDistribution = DiceDistribution(diceCounts),
                eventDiceDistribution = eventCounts,
                playerAverageTurnDurations = playerAvgDurations
            )
        )
    }
}
