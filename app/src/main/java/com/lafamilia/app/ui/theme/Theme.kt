package com.lafamilia.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = FamilyPrimaryLight,
    onPrimary = FamilyOnPrimaryLight,
    primaryContainer = FamilyPrimaryContainerLight,
    onPrimaryContainer = FamilyOnPrimaryContainerLight,
    secondary = FamilySecondaryLight,
    tertiary = FamilyTertiaryLight,
    background = FamilyBackgroundLight,
    surface = FamilyBackgroundLight,
    surfaceVariant = FamilySurfaceVariantLight
)

private val DarkColors = darkColorScheme(
    primary = FamilyPrimaryDark,
    onPrimary = FamilyOnPrimaryDark,
    primaryContainer = FamilyPrimaryContainerDark,
    onPrimaryContainer = FamilyOnPrimaryContainerDark,
    secondary = FamilySecondaryDark,
    tertiary = FamilyTertiaryDark,
    background = FamilyBackgroundDark,
    surface = FamilyBackgroundDark,
    surfaceVariant = FamilySurfaceVariantDark
)

/**
 * App theme. Dynamic colour (Android 12+ wallpaper colours) is off by default
 * so the brand palette is what people see.
 */
@Composable
fun LaFamiliaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
