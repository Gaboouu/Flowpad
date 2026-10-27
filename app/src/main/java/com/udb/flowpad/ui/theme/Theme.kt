package com.udb.flowpad.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import com.udb.flowpad.R
import androidx.core.view.WindowCompat

@Composable
private fun lightColors() = lightColorScheme(
    primary = colorResource(R.color.theme_1),
    onPrimary = Color.White,
    primaryContainer = colorResource(R.color.theme_2),
    onPrimaryContainer = colorResource(R.color.theme_3),
    background = colorResource(R.color.theme_4),
    onBackground = colorResource(R.color.theme_5),
    surface = Color.White,
    onSurface = colorResource(R.color.theme_5),
    surfaceVariant = colorResource(R.color.theme_6),
    onSurfaceVariant = colorResource(R.color.theme_7),
    outline = colorResource(R.color.theme_8),
    outlineVariant = colorResource(R.color.theme_9),
    error = colorResource(R.color.theme_10)
)

@Composable
private fun darkColors() = darkColorScheme(
    primary = colorResource(R.color.theme_11),
    onPrimary = colorResource(R.color.theme_12),
    primaryContainer = colorResource(R.color.theme_13),
    onPrimaryContainer = colorResource(R.color.theme_14),
    background = colorResource(R.color.theme_15),
    onBackground = colorResource(R.color.theme_16),
    surface = colorResource(R.color.theme_17),
    onSurface = colorResource(R.color.theme_16),
    surfaceVariant = colorResource(R.color.theme_18),
    onSurfaceVariant = colorResource(R.color.theme_19),
    outline = colorResource(R.color.theme_20),
    outlineVariant = colorResource(R.color.theme_21),
    error = colorResource(R.color.theme_22)
)

@Composable
fun FlowpadTheme(dark: Boolean, content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }
    MaterialTheme(
        colorScheme = if (dark) darkColors() else lightColors(),
        content = content
    )
}

/** Portadas disponibles para personalizar un proyecto. */
data class Cover(val name: String, val start: Color, val end: Color) {
    val brush: Brush get() = Brush.linearGradient(listOf(start, end))
}

val Covers = listOf(
    Cover("Azul", Color(0xFF2F6FED), Color(0xFF6C9BFF)),
    Cover("Atardecer", Color(0xFFE5583F), Color(0xFFF2A65A)),
    Cover("Bosque", Color(0xFF2E7D5B), Color(0xFF63B38A)),
    Cover("Violeta", Color(0xFF6A4CE0), Color(0xFFA77BFF)),
    Cover("Grafito", Color(0xFF2B2F36), Color(0xFF5B6270)),
    Cover("Rosa", Color(0xFFD6457A), Color(0xFFF28AAE))
)

fun coverAt(index: Int): Cover = Covers[index.mod(Covers.size)]
