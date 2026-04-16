package org.example.project.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Palette ──────────────────────────────────────────────────────────────────

val CatanOrange = Color(0xFFD97706)
val CatanOrangeHover = Color(0xFFB45309)
val CatanOrangeSubtle = Color(0xFFFFEDD5)
val CatanOrangeDark = Color(0xFFF59E0B)
val CatanOrangeDarkHover = Color(0xFFFBBF24)

val CatanBrown = Color(0xFF9A3412)
val CatanBrownDark = Color(0xFFFDBA74)

val CatanBgPrimary = Color(0xFFFFFFFF)
val CatanBgSecondary = Color(0xFFFFF7ED)
val CatanBgTertiary = Color(0xFFF5F5F4)

val CatanBgPrimaryDark = Color(0xFF0C0A09)
val CatanBgSecondaryDark = Color(0xFF1C1917)
val CatanBgTertiaryDark = Color(0xFF292524)

val CatanSurfaceAccent = Color(0xFFFFEDD5)
val CatanSurfaceAccentDark = Color(0xFF451A03)

val CatanTextPrimary = Color(0xFF1C1917)
val CatanTextSecondary = Color(0xFF57534E)
val CatanTextTertiary = Color(0xFFA8A29E)
val CatanTextInverse = Color(0xFFFAFAF9)

val CatanTextPrimaryDark = Color(0xFFFAFAF9)
val CatanTextSecondaryDark = Color(0xFFD6D3D1)

val CatanBorderPrimary = Color(0xFFD6D3D1)
val CatanBorderSecondary = Color(0xFFE7E5E4)
val CatanBorderPrimaryDark = Color(0xFF57534E)
val CatanBorderSecondaryDark = Color(0xFF44403C)

val CatanSuccess = Color(0xFF059669)
val CatanSuccessSubtle = Color(0xFFD1FAE5)
val CatanSuccessSubtleDark = Color(0xFF064E3B)
val CatanSuccessDark = Color(0xFF34D399)

val CatanError = Color(0xFFDC2626)
val CatanErrorSubtle = Color(0xFFFEE2E2)
val CatanErrorDark = Color(0xFFF87171)

val CatanWarning = Color(0xFFD97706)
val CatanWarningSubtle = Color(0xFFFEF3C7)

val CatanInfo = Color(0xFF2563EB)
val CatanInfoSubtle = Color(0xFFDBEAFE)
val CatanInfoSubtleDark = Color(0xFF1E3A5F)
val CatanInfoDark = Color(0xFF60A5FA)

// Dashboard category colors
val CatanCategoryGames = Color(0xFF7C3AED)
val CatanCategoryGamesSubtle = Color(0xFFEDE9FE)
val CatanCategoryGamesDark = Color(0xFFA78BFA)
val CatanCategoryGamesSubtleDark = Color(0xFF2E1065)

val CatanHeaderGradientEnd = Color(0xFF7B2010)

// Dice-specific palette (used via extension functions in presentation layer)
val CatanDiceRedBackground = Color(0xFFDC2626)
val CatanDiceRedDot = Color(0xFFFDE047)
val CatanDiceYellowBackground = Color(0xFFEAB308)
val CatanDiceYellowDot = Color(0xFFDC2626)
val CatanDiceEventBackground = Color(0xFFFFFFFF)
val CatanDiceEventBackgroundDark = Color(0xFFF5F5F4)
val CatanDiceEventIcon = Color(0xFF1C1917)
val CatanDiceSelectedBorder = Color(0xFF059669)
val CatanDiceSelectedBorderDark = Color(0xFF34D399)

// ── Semantic color schemes ────────────────────────────────────────────────────

internal val LightColorScheme = lightColorScheme(
    primary = CatanOrange,
    onPrimary = CatanTextInverse,
    primaryContainer = CatanOrangeSubtle,
    onPrimaryContainer = CatanTextPrimary,
    secondary = CatanBrown,
    onSecondary = CatanTextInverse,
    secondaryContainer = CatanSurfaceAccent,
    onSecondaryContainer = CatanTextPrimary,
    background = CatanBgPrimary,
    onBackground = CatanTextPrimary,
    surface = CatanBgPrimary,
    onSurface = CatanTextPrimary,
    surfaceVariant = CatanBgTertiary,
    onSurfaceVariant = CatanTextSecondary,
    error = CatanError,
    onError = CatanTextInverse,
    errorContainer = CatanErrorSubtle,
    onErrorContainer = CatanError,
    outline = CatanBorderPrimary,
    outlineVariant = CatanBorderSecondary,
)

internal val DarkColorScheme = darkColorScheme(
    primary = CatanOrangeDark,
    onPrimary = CatanTextPrimary,
    primaryContainer = CatanSurfaceAccentDark,
    onPrimaryContainer = CatanTextPrimaryDark,
    secondary = CatanBrownDark,
    onSecondary = CatanTextPrimary,
    secondaryContainer = CatanSurfaceAccentDark,
    onSecondaryContainer = CatanTextPrimaryDark,
    background = CatanBgPrimaryDark,
    onBackground = CatanTextPrimaryDark,
    surface = CatanBgSecondaryDark,
    onSurface = CatanTextPrimaryDark,
    surfaceVariant = CatanBgTertiaryDark,
    onSurfaceVariant = CatanTextSecondaryDark,
    error = CatanErrorDark,
    onError = CatanTextPrimary,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = CatanErrorDark,
    outline = CatanBorderPrimaryDark,
    outlineVariant = CatanBorderSecondaryDark,
)
