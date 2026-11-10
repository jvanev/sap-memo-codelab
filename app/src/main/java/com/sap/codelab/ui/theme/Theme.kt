package com.sap.codelab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

private val CompactSizeDimens = Dimens(
    paddingMedium = 16.dp,
    spacingMedium = 16.dp,
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SAPMemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColorScheme, typography = Typography) {
        val sizeClass = currentWindowAdaptiveInfo().windowSizeClass.widthSizeClass

        CompositionLocalProvider(
            LocalWindowWidthSizeClass provides sizeClass,
            LocalDimens provides CompactSizeDimens,
        ) {
            content()
        }
    }
}

/**
 * The `CompositionLocal` that contains the [WindowWidthSizeClass].
 */
val LocalWindowWidthSizeClass = staticCompositionLocalOf { WindowWidthSizeClass.Compact }
