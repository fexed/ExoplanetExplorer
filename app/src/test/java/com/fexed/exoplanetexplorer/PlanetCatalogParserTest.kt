package com.fexed.exoplanetexplorer

import org.junit.Assert.assertEquals
import org.junit.Test

class PlanetCatalogParserTest {
    @Test
    fun parseCatalogBuildsFilteredFacilityCountsAndSummary() {
        val catalog = PlanetCatalogParser.parse(
            """
            pl_name,hostname,disc_year,pl_orbper,pl_orbpererr1,pl_orbpererr2,pl_rade,pl_radeerr1,pl_radeerr2,pl_bmasse,pl_bmasseerr1,pl_bmasseerr2,sy_dist,sy_disterr1,sy_disterr2,pl_orbsmax,pl_orbsmaxerr1,pl_orbsmaxerr2,disc_facility,disc_telescope,pl_controv_flag
            Planet A,Host A,2020,10,,,1,0.1,-0.1,2,0.2,-0.2,2,0.1,-0.1,0.5,0.01,-0.01,Facility Z,Telescope A,0
            Planet B,Host B,2022,20,,,3,0.2,-0.2,4,0.3,-0.3,10,0.2,-0.2,1,0.02,-0.02,Multiple Observatories,Multiple Telescopes,0
            Planet C,Host C,2023,30,,,5,0.2,-0.2,6,0.3,-0.3,20,0.2,-0.2,2,0.02,-0.02,Ignored Facility,Ignored Telescope,1
            """.trimIndent()
        )

        assertEquals(listOf("Planet A", "Planet B"), catalog.planets.map { it.name })
        assertEquals(listOf("Facility Z"), catalog.facilities)
        assertEquals(mapOf("Facility Z" to 1), catalog.planetsPerFacility)
        assertEquals(listOf("Telescope A"), catalog.telescopes)
        assertEquals(2, catalog.summary.planetsPerYear.values.sum())
        assertEquals("Planet A", catalog.summary.smallest.name)
        assertEquals("Planet B", catalog.summary.farthest.name)
    }
}
