package com.fexed.exoplanetexplorer

import androidx.compose.ui.graphics.Color
import kotlin.math.ln

enum class StellarColorCategory {
    BLUE,
    BLUE_WHITE,
    WHITE_YELLOW,
    YELLOW_ORANGE,
    ORANGE_RED,
    UNKNOWN
}

fun classifyStellarColor(
    temperatureKelvin: Double?,
    spectralType: String?
): StellarColorCategory {
    if (temperatureKelvin != null && temperatureKelvin.isFinite() && temperatureKelvin > 0.0) {
        return when {
            temperatureKelvin >= 10_000.0 -> StellarColorCategory.BLUE
            temperatureKelvin >= 7_500.0 -> StellarColorCategory.BLUE_WHITE
            temperatureKelvin >= 6_000.0 -> StellarColorCategory.WHITE_YELLOW
            temperatureKelvin >= 4_900.0 -> StellarColorCategory.YELLOW_ORANGE
            else -> StellarColorCategory.ORANGE_RED
        }
    }

    val type = spectralType
        ?.trim()
        ?.removePrefix("sd")
        ?.removePrefix("SD")
        ?.firstOrNull()
        ?.uppercaseChar()

    return when (type) {
        'O', 'B' -> StellarColorCategory.BLUE
        'A' -> StellarColorCategory.BLUE_WHITE
        'F' -> StellarColorCategory.WHITE_YELLOW
        'G' -> StellarColorCategory.YELLOW_ORANGE
        'K', 'M' -> StellarColorCategory.ORANGE_RED
        else -> StellarColorCategory.UNKNOWN
    }
}

private val StarColorUnknown = Color(0xFFBA1E68)

fun starColorFor(temperatureKelvin: Double?, spectralType: String?): Color =
    when (classifyStellarColor(temperatureKelvin, spectralType)) {
        StellarColorCategory.BLUE -> Color(0xFF4B78FF)
        StellarColorCategory.BLUE_WHITE -> Color(0xFF80B5FF)
        StellarColorCategory.WHITE_YELLOW -> Color(0xFFFFD166)
        StellarColorCategory.YELLOW_ORANGE -> Color(0xFFF4A340)
        StellarColorCategory.ORANGE_RED -> Color(0xFFE45B48)
        StellarColorCategory.UNKNOWN -> StarColorUnknown
    }

fun stellarRadiusIconScale(stellarRadius: Double?): Float {
    if (stellarRadius == null || !stellarRadius.isFinite() || stellarRadius <= 0.0) return 1.0f
    return (1.0 + 0.12 * ln(stellarRadius))
        .coerceIn(0.72, 1.35)
        .toFloat()
}
