package com.droidbubble.app.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class IslandFontOption(
    val key: String,
    val name: String,
    val family: FontFamily
)

val ISLAND_FONTS = listOf(
    IslandFontOption("DEFAULT", "System Default", FontFamily.Default),
    IslandFontOption("SANS_SERIF", "Clean Sans", FontFamily.SansSerif),
    IslandFontOption("SERIF", "Classic Serif", FontFamily.Serif),
    IslandFontOption("MONOSPACE", "Monospace", FontFamily.Monospace),
    IslandFontOption("ROUNDED", "Modern Rounded", FontFamily(Typeface.create("sans-serif-rounded", Typeface.NORMAL))),
    IslandFontOption("CONDENSED", "Condensed", FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))),
    IslandFontOption("CURSIVE", "Casual Script", FontFamily.Cursive)
)

fun getIslandFontFamily(key: String): FontFamily {
    return ISLAND_FONTS.firstOrNull { it.key == key }?.family ?: FontFamily.Default
}

fun createTypography(fontFamily: FontFamily): Typography {
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(fontFamily = fontFamily),
        bodySmall = TextStyle(fontFamily = fontFamily),
        titleLarge = TextStyle(fontFamily = fontFamily),
        titleMedium = TextStyle(fontFamily = fontFamily),
        titleSmall = TextStyle(fontFamily = fontFamily),
        labelLarge = TextStyle(fontFamily = fontFamily),
        labelMedium = TextStyle(fontFamily = fontFamily),
        labelSmall = TextStyle(fontFamily = fontFamily),
        headlineLarge = TextStyle(fontFamily = fontFamily),
        headlineMedium = TextStyle(fontFamily = fontFamily),
        headlineSmall = TextStyle(fontFamily = fontFamily)
    )
}

val Typography = createTypography(FontFamily.Default)
