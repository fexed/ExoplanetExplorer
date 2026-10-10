package com.fexed.exoplanetexplorer

import org.junit.Assert.assertEquals
import org.junit.Test

class StellarClassificationTest {
    @Test
    fun classifiesTemperatureIntoFiveColorBands() {
        assertEquals(StellarColorCategory.BLUE, classifyStellarColor(10_000.0, null))
        assertEquals(StellarColorCategory.BLUE_WHITE, classifyStellarColor(7_500.0, null))
        assertEquals(StellarColorCategory.WHITE_YELLOW, classifyStellarColor(6_000.0, null))
        assertEquals(StellarColorCategory.YELLOW_ORANGE, classifyStellarColor(4_900.0, null))
        assertEquals(StellarColorCategory.ORANGE_RED, classifyStellarColor(3_500.0, null))
    }

    @Test
    fun usesSpectralTypeWhenTemperatureIsMissingOrInvalid() {
        assertEquals(StellarColorCategory.BLUE, classifyStellarColor(null, "B2V"))
        assertEquals(StellarColorCategory.BLUE_WHITE, classifyStellarColor(Double.NaN, "A5"))
        assertEquals(StellarColorCategory.WHITE_YELLOW, classifyStellarColor(null, "F"))
        assertEquals(StellarColorCategory.YELLOW_ORANGE, classifyStellarColor(null, "G2V"))
        assertEquals(StellarColorCategory.ORANGE_RED, classifyStellarColor(null, "sdM3"))
        assertEquals(StellarColorCategory.UNKNOWN, classifyStellarColor(null, "unknown"))
    }

    @Test
    fun prefersMeasuredTemperatureOverSpectralType() {
        assertEquals(StellarColorCategory.BLUE, classifyStellarColor(12_000.0, "M5"))
    }
}
