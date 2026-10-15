
package com.example.evaluacion2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val LouArayaColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = PureWhite,

    secondary = OliveGreen,
    onSecondary = PureWhite,

    background = CottonBeige,
    onBackground = WarmCharcoal,

    surface = SoftCream,
    onSurface = WarmCharcoal,

    surfaceVariant = BorderBeige,
    onSurfaceVariant = SecondaryText,

    outline = BorderBeige,

    error = Color(0xFFBA1A1A),
    onError = PureWhite
)

private val LouArayaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun Evaluacion2Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LouArayaColorScheme,
        typography = AppTypography,
        shapes = LouArayaShapes,
        content = content
    )
}
