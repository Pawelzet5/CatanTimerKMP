package org.example.project.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Custom colour slots that have no Material3 equivalent.
 * Light/dark resolution lives entirely in CatanTimerTheme —
 * composables read these without any isSystemInDarkTheme() call.
 */
data class CatanExtendedColors(
    val successIcon: Color,
    val successContainer: Color,
    val gamesIcon: Color,
    val gamesContainer: Color,
    val infoIcon: Color,
    val infoContainer: Color,
)

internal val LocalCatanColors = staticCompositionLocalOf { LightExtendedColors }

internal val LightExtendedColors = CatanExtendedColors(
    successIcon      = CatanSuccess,
    successContainer = CatanSuccessSubtle,
    gamesIcon        = CatanCategoryGames,
    gamesContainer   = CatanCategoryGamesSubtle,
    infoIcon         = CatanInfo,
    infoContainer    = CatanInfoSubtle,
)

internal val DarkExtendedColors = CatanExtendedColors(
    successIcon      = CatanSuccessDark,
    successContainer = CatanSuccessSubtleDark,
    gamesIcon        = CatanCategoryGamesDark,
    gamesContainer   = CatanCategoryGamesSubtleDark,
    infoIcon         = CatanInfoDark,
    infoContainer    = CatanInfoSubtleDark,
)

val MaterialTheme.catanColors: CatanExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCatanColors.current
