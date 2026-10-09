package com.fexed.exoplanetexplorer

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.text.DateFormat
import java.util.Date

object PlanetCatalogParser {
    private const val LIGHT_YEARS_PER_PARSEC = 3.26156
    private const val MULTIPLE_OBSERVATORIES = "Multiple Observatories"
    private const val MULTIPLE_TELESCOPES = "Multiple Telescopes"

    fun parse(response: String): PlanetCatalog {
        val planets = csvReader().readAllWithHeader(response)
            .asSequence()
            .filter { row -> row.required("pl_controv_flag").toInt() != 1 }
            .map { row ->
                Exoplanet(
                    star = row.required("hostname"),
                    name = row.required("pl_name"),
                    year = row.required("disc_year").toInt(),
                    period = row.doubleOr("pl_orbper", -1.0),
                    radius = row.doubleOr("pl_rade", -1.0),
                    radius_errplus = row.doubleOr("pl_radeerr1", 0.0),
                    radius_errminus = row.doubleOr("pl_radeerr2", 0.0),
                    mass = row.doubleOr("pl_bmasse", -1.0),
                    mass_errplus = row.doubleOr("pl_bmasseerr1", 0.0),
                    mass_errminus = row.doubleOr("pl_bmasseerr2", 0.0),
                    distance = row.doubleOr("sy_dist", -1.0) * LIGHT_YEARS_PER_PARSEC,
                    dist_errplus = row.doubleOr("sy_disterr1", 0.0) * LIGHT_YEARS_PER_PARSEC,
                    dist_errminus = row.doubleOr("sy_disterr2", 0.0) * LIGHT_YEARS_PER_PARSEC,
                    orbitdistance = row.doubleOr("pl_orbsmax", -1.0),
                    orbitdist_errplus = row.doubleOr("pl_orbsmaxerr1", 0.0),
                    orbitdist_errminus = row.doubleOr("pl_orbsmaxerr2", 0.0),
                    discoveryFacility = row.required("disc_facility"),
                    discoveryTelescope = row.required("disc_telescope"),
                    lastupdate = DateFormat.getDateInstance().format(Date())
                )
            }
            .toList()

        require(planets.isNotEmpty()) { "The exoplanet archive returned no confirmed planets." }
        val yearCounts = planets.groupingBy { it.year }.eachCount()
        val allYears = (yearCounts.keys.minOrNull()!!..yearCounts.keys.maxOrNull()!!)
            .associateWith { yearCounts[it] ?: 0 }
        val collator = java.text.Collator.getInstance()
        val facilities = planets.map { it.discoveryFacility.trim() }
            .filter { it.isNotEmpty() && !it.equals(MULTIPLE_OBSERVATORIES, ignoreCase = true) }
            .distinct()
            .sortedWith(collator)
        val telescopes = planets.map { it.discoveryTelescope.trim() }
            .filter { it.isNotEmpty() && !it.equals(MULTIPLE_TELESCOPES, ignoreCase = true) }
            .distinct()
            .sortedWith(collator)

        return PlanetCatalog(
            planets = planets,
            facilities = facilities,
            telescopes = telescopes,
            planetsPerFacility = facilities.associateWith { facility ->
                planets.count { it.discoveryFacility.trim() == facility }
            },
            planetsPerTelescope = telescopes.associateWith { telescope ->
                planets.count { it.discoveryTelescope.trim() == telescope }
            },
            summary = CatalogSummary(
                smallest = planets.filter { it.radius > 0.0 }.minByOrNull { it.radius } ?: Exoplanet.Earth,
                largest = planets.filter { it.radius > 0.0 }.maxByOrNull { it.radius } ?: Exoplanet.Earth,
                lightest = planets.filter { it.mass > 0.0 }.minByOrNull { it.mass } ?: Exoplanet.Earth,
                heaviest = planets.filter { it.mass > 0.0 }.maxByOrNull { it.mass } ?: Exoplanet.Earth,
                nearest = planets.filter { it.distance > 0.0 }.minByOrNull { it.distance } ?: Exoplanet.Earth,
                farthest = planets.filter { it.distance > 0.0 }.maxByOrNull { it.distance } ?: Exoplanet.Earth,
                planetsPerCategory = (0..5).map { category ->
                    planets.count { it.category == category }
                },
                planetsPerYear = allYears
            )
        )
    }

    private fun Map<String, String?>.required(key: String): String =
        requireNotNull(this[key]) { "Missing '$key' column in exoplanet archive response." }

    private fun Map<String, String?>.doubleOr(key: String, fallback: Double): Double =
        this[key].takeUnless { it.isNullOrEmpty() }?.toDouble() ?: fallback
}
