package com.sap.codelab.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

/**
 * Contains a collection of dimens-related values. Used for padding, spacing, sizing, etc.
 */
data class Dimens(
    val paddingMedium: Dp = Dp.Unspecified,
    val spacingExtraSmall: Dp = Dp.Unspecified,
    val spacingSmall: Dp = Dp.Unspecified,
    val spacingMedium: Dp = Dp.Unspecified,
)

/**
 * The `CompositionLocal` that contains the dimens settings tailored to the device's screen.
 */
val LocalDimens = staticCompositionLocalOf { Dimens() }
