package com.fexed.exoplanetexplorer

import org.junit.Assert.assertEquals
import org.junit.Test

class PlanetSortingTest {
    @Test
    fun radiusSortExcludesUnknownValuesWithoutMutatingInput() {
        val smallest = planet("Small", radius = 0.5)
        val unknown = planet("Unknown", radius = -1.0)
        val largest = planet("Large", radius = 2.0)
        val original = listOf(largest, unknown, smallest)

        val sorted = sortPlanets(original, inverted = false, selectedOrder = 3)

        assertEquals(listOf(smallest, largest), sorted)
        assertEquals(listOf(largest, unknown, smallest), original)
    }

    @Test
    fun invertedNameSortOrdersNamesDescending() {
        val planets = listOf(planet("Alpha"), planet("Gamma"), planet("Beta"))

        assertEquals(
            listOf("Gamma", "Beta", "Alpha"),
            sortPlanets(planets, inverted = true, selectedOrder = 1).map { it.name }
        )
    }

    private fun planet(name: String, radius: Double = 1.0) = Exoplanet(
        star = "Host",
        name = name,
        year = 2020,
        period = 10.0,
        radius = radius,
        radius_errplus = 0.0,
        radius_errminus = 0.0,
        mass = 1.0,
        mass_errplus = 0.0,
        mass_errminus = 0.0,
        distance = 1.0,
        dist_errplus = 0.0,
        dist_errminus = 0.0,
        orbitdistance = 1.0,
        orbitdist_errplus = 0.0,
        orbitdist_errminus = 0.0,
        discoveryFacility = "Facility",
        discoveryTelescope = "Telescope",
        lastupdate = "-"
    )
}
