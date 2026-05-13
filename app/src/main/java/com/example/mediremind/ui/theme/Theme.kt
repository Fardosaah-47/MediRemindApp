package com.example.mediremind.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    secondary = AppPrimarySoft,
    tertiary = AppAlert,
    background = AppBackground,
    surface = AppCard,
    surfaceVariant = AppPrimarySoft,
    error = AppAlert,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = AppText,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = AppText,
    onSurface = AppText,
    onSurfaceVariant = AppSecondaryText,
    onError = androidx.compose.ui.graphics.Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = CalmMint,
    secondary = ClinicTeal,
    tertiary = AlertCoral,
    background = Ink,
    surface = DeepTeal,
    onPrimary = Ink,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White
)

@Composable
fun MediRemindTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
