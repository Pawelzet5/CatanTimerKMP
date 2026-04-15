package org.example.project.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CatanTypography = Typography(
    // Timer MM:SS display — 48sp bold
    displayLarge = TextStyle(
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 56.sp,
    ),
    // Large numeric display — 32sp bold
    displayMedium = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
    ),
    // Medium display — 26sp semibold
    displaySmall = TextStyle(
        fontSize = 26.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp,
    ),
    // Screen titles — 22sp semibold
    headlineLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp,
    ),
    // Section headers — 18sp semibold
    headlineMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp,
    ),
    // Sub-section headers — 16sp medium
    headlineSmall = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp,
    ),
    // Card/item titles — 18sp medium
    titleLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp,
    ),
    // Item subtitles — 15sp medium
    titleMedium = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
    ),
    // Small titles — 13sp medium
    titleSmall = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
    ),
    // Primary body text — 15sp regular
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
    ),
    // Secondary body text — 13sp regular
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
    ),
    // Tertiary body / captions — 11sp regular
    bodySmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
    ),
    // Button labels — 15sp medium
    labelLarge = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
    ),
    // Chip / tag labels — 13sp medium
    labelMedium = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
    ),
    // Smallest labels — 11sp medium
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
    ),
)
