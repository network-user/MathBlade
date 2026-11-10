package com.mathblade.app

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

internal object GameColors {
    val ink = Color(0xFFF2EAD8)
    val muted = Color(0xFFE8D7B0)
}

private val Gold = Color(0xFFD9A441)
private val Background = Color(0xFF12100C)
private val Surface = Color(0xFF1E1A14)
private val Ink = GameColors.ink
private val Muted = GameColors.muted

private val MathBladeColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color.Black,
    secondary = Gold,
    background = Background,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Surface,
    onSurfaceVariant = Muted,
    error = Color(0xFFE07060),
    onError = Ink,
    outline = Muted
)

/**
 * Material3 text styles ship with a concrete dark color, and that color
 * wins over [LocalContentColor]. On a dark surface the glyphs stay black.
 * Body and title styles are therefore cream. Label styles stay unspecified
 * so buttons can paint black on gold and gold on the dark panels.
 */
private fun Typography.readableOnDark(): Typography {
    fun TextStyle.ink() = copy(
        color = Ink,
        fontWeight = if (fontWeight == null || fontWeight == FontWeight.Normal) {
            FontWeight.Medium
        } else {
            fontWeight
        }
    )
    fun TextStyle.unset() = copy(color = Color.Unspecified)
    return copy(
        displayLarge = displayLarge.ink(),
        displayMedium = displayMedium.ink(),
        displaySmall = displaySmall.ink(),
        headlineLarge = headlineLarge.ink(),
        headlineMedium = headlineMedium.ink(),
        headlineSmall = headlineSmall.ink(),
        titleLarge = titleLarge.ink(),
        titleMedium = titleMedium.ink(),
        titleSmall = titleSmall.ink(),
        bodyLarge = bodyLarge.ink(),
        bodyMedium = bodyMedium.ink(),
        bodySmall = bodySmall.ink(),
        labelLarge = labelLarge.unset(),
        labelMedium = labelMedium.unset(),
        labelSmall = labelSmall.unset()
    )
}

@Composable
fun MathBladeTheme(content: @Composable () -> Unit) {
    androidx.compose.material3.MaterialTheme(
        colorScheme = MathBladeColors,
        typography = Typography().readableOnDark()
    ) {
        CompositionLocalProvider(LocalContentColor provides Ink, content = content)
    }
}
