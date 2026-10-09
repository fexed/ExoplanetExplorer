package com.fexed.exoplanetexplorer

data class PlanetCatalog(
    val planets: List<Exoplanet>,
    val facilities: List<String>,
    val telescopes: List<String>,
    val planetsPerFacility: Map<String, Int>,
    val planetsPerTelescope: Map<String, Int>,
    val summary: CatalogSummary
)

data class CatalogLoadFailure(
    val message: String,
    val isParsingFailure: Boolean
)

data class CatalogSummary(
    val smallest: Exoplanet,
    val largest: Exoplanet,
    val lightest: Exoplanet,
    val heaviest: Exoplanet,
    val nearest: Exoplanet,
    val farthest: Exoplanet,
    val planetsPerCategory: List<Int>,
    val planetsPerYear: Map<Int, Int>
) {
    companion object {
        val EMPTY = CatalogSummary(
            smallest = Exoplanet.Earth,
            largest = Exoplanet.Earth,
            lightest = Exoplanet.Earth,
            heaviest = Exoplanet.Earth,
            nearest = Exoplanet.Earth,
            farthest = Exoplanet.Earth,
            planetsPerCategory = List(6) { 0 },
            planetsPerYear = emptyMap()
        )
    }
}
