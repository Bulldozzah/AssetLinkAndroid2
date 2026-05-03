package com.example.assetlinkandroid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    background              = AppBackground,
    onBackground            = AppForeground,
    surface                 = AppBackground,
    onSurface               = AppForeground,
    surfaceVariant          = AppMuted,
    onSurfaceVariant        = AppMutedForeground,
    surfaceContainerLowest  = SurfaceLowest,
    surfaceContainerLow     = SurfaceLow,
    surfaceContainer        = SurfaceBase,
    surfaceContainerHigh    = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    primary                 = AppPrimary,
    onPrimary               = AppPrimaryForeground,
    primaryContainer        = AppSecondary,
    onPrimaryContainer      = AppPrimary,
    secondary               = AppSecondary,
    onSecondary             = AppSecondaryFg,
    secondaryContainer      = AppAccent,
    onSecondaryContainer    = AppAccentForeground,
    tertiary                = AppMuted,
    onTertiary              = AppPrimary,
    tertiaryContainer       = AppMuted,
    onTertiaryContainer     = AppPrimary,
    error                   = AppDestructive,
    onError                 = Color.White,
    errorContainer          = Color(0xFFFEE2E2),
    onErrorContainer        = AppDestructive,
    outline                 = AppBorder,
    outlineVariant          = AppBorder,
    scrim                   = Color.Black,
    inverseSurface          = AppPrimary,
    inverseOnSurface        = AppPrimaryForeground,
    inversePrimary          = AppSecondary,
)

@Composable
fun AssetlinkAndroidTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content,
    )
}