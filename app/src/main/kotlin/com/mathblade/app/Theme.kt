package com.mathblade.app

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFFD9A441)
private val Background = Color(0xFF12100C)
private val Surface = Color(0xFF1E1A14)
private val Ink = Color(0xFFF2EAD8)
private val Muted = Color(0xFFA99B7E)

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
    error = Color(0xFFC0392B)
)

@Composable
fun MathBladeTheme(content: @Composable () -> Unit) {
    androidx.compose.material3.MaterialTheme(
        colorScheme = MathBladeColors,
        typography = Typography(),
        content = content
    )
}
