package com.listadecompras.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Green40 = Color(0xFF1E8E4E)
private val Green80 = Color(0xFF7FDA9E)
private val Green90 = Color(0xFFB8F2C9)
private val Green10 = Color(0xFF00210E)
private val Green20 = Color(0xFF00391C)
private val Green30 = Color(0xFF00522B)

private val Teal40 = Color(0xFF3F6654)
private val Teal80 = Color(0xFFA6D0BA)
private val Teal90 = Color(0xFFC2ECD6)

private val Amber40 = Color(0xFF8A5100)
private val Amber80 = Color(0xFFFFB871)
private val Amber90 = Color(0xFFFFDCBE)

private val LightColors = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,
    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Color(0xFF002114),
    tertiary = Amber40,
    onTertiary = Color.White,
    tertiaryContainer = Amber90,
    onTertiaryContainer = Color(0xFF2C1600),
    background = Color(0xFFF6FBF6),
    onBackground = Color(0xFF171D19),
    surface = Color(0xFFF6FBF6),
    onSurface = Color(0xFF171D19),
    surfaceVariant = Color(0xFFDCE5DC),
    onSurfaceVariant = Color(0xFF404943),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F5F0),
    surfaceContainer = Color(0xFFEAEFEA),
    surfaceContainerHigh = Color(0xFFE4EAE4),
    surfaceContainerHighest = Color(0xFFDFE4DF),
    outline = Color(0xFF707972),
    outlineVariant = Color(0xFFC0C9C0),
)

private val DarkColors = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,
    secondary = Teal80,
    onSecondary = Color(0xFF0F3727),
    secondaryContainer = Color(0xFF274E3D),
    onSecondaryContainer = Teal90,
    tertiary = Amber80,
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF693C00),
    onTertiaryContainer = Amber90,
    background = Color(0xFF101411),
    onBackground = Color(0xFFDFE4DF),
    surface = Color(0xFF101411),
    onSurface = Color(0xFFDFE4DF),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFC0C9C0),
    surfaceContainerLowest = Color(0xFF0B0F0C),
    surfaceContainerLow = Color(0xFF171D19),
    surfaceContainer = Color(0xFF1B211D),
    surfaceContainerHigh = Color(0xFF252B27),
    surfaceContainerHighest = Color(0xFF303632),
    outline = Color(0xFF8A938B),
    outlineVariant = Color(0xFF404943),
)

private val AppTypography = Typography().let { base ->
    base.copy(
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

/** Estilo do cabeçalho de seção ("🥬 Hortifrúti"). */
val SectionHeaderStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)

@Composable
fun ListaDeComprasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
