package com.trip.dayplan.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Modern minimal iOS-style color palette
// Light theme: warm neutrals, sage green accent
private val LightBackground = Color(0xFFF9F7F3)
private val LightSurface = Color(0xFFFFFFFF)
private val LightPrimary = Color(0xFF4A6741)     // Sage green
private val LightPrimaryVariant = Color(0xFF5F8355)
private val LightTextPrimary = Color(0xFF1C1C1E)
private val LightTextSecondary = Color(0xFF8E8E93)
private val LightDivider = Color(0xFFE5E5EA)
private val LightCardBorder = Color(0xFFF0EDE8)
private val LightTimeTrack = Color(0xFFF2F0EB)
private val LightNowIndicator = Color(0xFFFF3B30) // iOS red
private val LightOverlay = Color(0x40000000)

private val DarkBackground = Color(0xFF000000)
private val DarkSurface = Color(0xFF1C1C1E)
private val DarkPrimary = Color(0xFF5F8355)
private val DarkPrimaryVariant = Color(0xFF7A9E70)
private val DarkTextPrimary = Color(0xFFF2F2F7)
private val DarkTextSecondary = Color(0xFF8E8E93)
private val DarkDivider = Color(0xFF38383A)
private val DarkCardBorder = Color(0xFF2C2C2E)
private val DarkTimeTrack = Color(0xFF1C1C1E)
private val DarkNowIndicator = Color(0xFFFF453A)
private val DarkOverlay = Color(0x80000000)

data class AppColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val primaryVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val cardBorder: Color,
    val timeTrack: Color,
    val nowIndicator: Color,
    val overlay: Color,
)

val LightAppColors = AppColors(
    background = LightBackground, surface = LightSurface, primary = LightPrimary,
    primaryVariant = LightPrimaryVariant, textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary, divider = LightDivider, cardBorder = LightCardBorder,
    timeTrack = LightTimeTrack, nowIndicator = LightNowIndicator, overlay = LightOverlay,
)

val DarkAppColors = AppColors(
    background = DarkBackground, surface = DarkSurface, primary = DarkPrimary,
    primaryVariant = DarkPrimaryVariant, textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary, divider = DarkDivider, cardBorder = DarkCardBorder,
    timeTrack = DarkTimeTrack, nowIndicator = DarkNowIndicator, overlay = DarkOverlay,
)

// Mutable holder for non-@Composable access (Canvas, etc.)
object DayPlanTheme {
    private var _colors: AppColors = LightAppColors
    private var _isDark: Boolean = false
    val colors: AppColors get() = _colors
    val isDark: Boolean get() = _isDark
    internal fun apply(colors: AppColors, dark: Boolean = false) { _colors = colors; _isDark = dark }

    // Non-@Composable getters
    val background: Color get() = _colors.background
    val surface: Color get() = _colors.surface
    val primary: Color get() = _colors.primary
    val primaryVariant: Color get() = _colors.primaryVariant
    val textPrimary: Color get() = _colors.textPrimary
    val textSecondary: Color get() = _colors.textSecondary
    val divider: Color get() = _colors.divider
    val cardBorder: Color get() = _colors.cardBorder
    val timeTrack: Color get() = _colors.timeTrack
    val nowIndicator: Color get() = _colors.nowIndicator
    val overlay: Color get() = _colors.overlay
}

@Composable
fun DayPlanAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            surface = colors.surface,
            background = colors.background,
            onPrimary = Color.White,
            onSurface = colors.textPrimary,
            onBackground = colors.textPrimary,
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            surface = colors.surface,
            background = colors.background,
            onPrimary = Color.White,
            onSurface = colors.textPrimary,
            onBackground = colors.textPrimary,
        )
    }

    SideEffect { DayPlanTheme.apply(colors, darkTheme) }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
