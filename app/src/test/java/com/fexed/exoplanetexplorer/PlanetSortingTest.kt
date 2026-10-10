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

    @Test
    fun groupPlanetsBySystemSortsSystemsAndPreservesPlanetOrder() {
        val zetaFirst = planet("Zeta 1", star = "Zeta")
        val alpha = planet("Alpha", star = "Alpha")
        val zetaSecond = planet("Zeta 2", star = "Zeta")

        val systems = groupPlanetsBySystem(listOf(zetaFirst, alpha, zetaSecond))

        assertEquals(listOf("Alpha", "Zeta"), systems.map { it.name })
        assertEquals(listOf("Zeta 1", "Zeta 2"), systems.last().planets.map { it.name })
    }

    @Test
    fun groupPlanetsBySystemOrdersPlanetsByOrbitalDistanceWithUnknownLast() {
        val planets = listOf(
            planet("Unknown", orbit = -1.0),
            planet("Outer", orbit = 5.0),
            planet("Inner", orbit = 0.1)
        )

        assertEquals(
            listOf("Inner", "Outer", "Unknown"),
            groupPlanetsBySystem(planets).single().planets.map { it.name }
        )
    }

    @Test
    fun groupPlanetsBySystemCanSortByPlanetCountWithAlphabeticalTies() {
        val planets = listOf(
            planet("Beta b", star = "Beta"),
            planet("Alpha b", star = "Alpha"),
            planet("Beta c", star = "Beta"),
            planet("Gamma b", star = "Gamma"),
            planet("Beta d", star = "Beta")
        )

        assertEquals(
            listOf("Beta", "Alpha", "Gamma"),
            groupPlanetsBySystem(planets, SYSTEM_ORDER_BY_PLANET_COUNT).map { it.name }
        )
    }

    @Test
    fun groupPlanetsBySystemCanSortByDistanceInBothDirectionsAndKeepsUnknownLast() {
        val planets = listOf(
            planet("Far planet", star = "Far", distance = 50.0),
            planet("Unknown planet", star = "Unknown", distance = -1.0),
            planet("Near planet", star = "Near", distance = 5.0)
        )

        assertEquals(
            listOf("Near", "Far", "Unknown"),
            groupPlanetsBySystem(
                planets,
                SYSTEM_ORDER_BY_DISTANCE
            ).map { it.name }
        )
        assertEquals(
            listOf("Far", "Near", "Unknown"),
            groupPlanetsBySystem(
                planets,
                SYSTEM_ORDER_BY_DISTANCE,
                invertedOrder = true
            ).map { it.name }
        )
    }

    @Test
    fun groupPlanetsBySystemSortsByStellarRadiusAndMassWithUnknownLast() {
        val planets = listOf(
            planet("Large radius", star = "Large radius", stellarRadius = 2.0, stellarMass = 1.0),
            planet("Unknown", star = "Unknown", stellarRadius = -1.0, stellarMass = -1.0),
            planet("Small radius", star = "Small radius", stellarRadius = 0.5, stellarMass = 0.5),
            planet("Large mass", star = "Large mass", stellarRadius = 1.0, stellarMass = 2.0)
        )

        assertEquals(
            listOf("Small radius", "Large mass", "Large radius", "Unknown"),
            groupPlanetsBySystem(planets, SYSTEM_ORDER_BY_STELLAR_RADIUS).map { it.name }
        )
        assertEquals(
            listOf("Large radius", "Large mass", "Small radius", "Unknown"),
            groupPlanetsBySystem(
                planets,
                SYSTEM_ORDER_BY_STELLAR_RADIUS,
                invertedOrder = true
            ).map { it.name }
        )
        assertEquals(
            listOf("Small radius", "Large radius", "Large mass", "Unknown"),
            groupPlanetsBySystem(planets, SYSTEM_ORDER_BY_STELLAR_MASS).map { it.name }
        )
        assertEquals(
            listOf("Large mass", "Large radius", "Small radius", "Unknown"),
            groupPlanetsBySystem(
                planets,
                SYSTEM_ORDER_BY_STELLAR_MASS,
                invertedOrder = true
            ).map { it.name }
        )
    }

    private fun planet(
        name: String,
        radius: Double = 1.0,
        star: String = "Host",
        orbit: Double = 1.0,
        distance: Double = 1.0,
        stellarRadius: Double = 1.0,
        stellarMass: Double = 1.0
    ) = Exoplanet(
        star = star,
        name = name,
        year = 2020,
        period = 10.0,
        radius = radius,
        radius_errplus = 0.0,
        radius_errminus = 0.0,
        mass = 1.0,
        mass_errplus = 0.0,
        mass_errminus = 0.0,
        distance = distance,
        dist_errplus = 0.0,
        dist_errminus = 0.0,
        orbitdistance = orbit,
        orbitdist_errplus = 0.0,
        orbitdist_errminus = 0.0,
        discoveryFacility = "Facility",
        discoveryTelescope = "Telescope",
        lastupdate = "-",
        stellarRadius = stellarRadius,
        stellarMass = stellarMass
    )
}
