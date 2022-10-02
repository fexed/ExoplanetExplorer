package com.fexed.exoplanetexplorer

class Exoplanet(
    val star: String,
    val name: String,
    val year: Int,
    val period: Double,
    val radius: Double,
    radius_errplus: Double,
    radius_errminus: Double,
    val mass: Double,
    mass_errplus: Double,
    mass_errminus: Double,
    val distance: Double,
    dist_errplus: Double,
    dist_errminus: Double,
    val orbitdistance: Double,
    orbitdist_errplus: Double,
    orbitdist_errminus: Double,
    val discoverer: String,
    val lastupdate: String) {
    val category: Int = (
        if (mass <= 0.0) -1 else ( // Unknown
                if (mass < 0.1) 0  // Mercurian
                else (
                    if (mass < 0.5) 1 // Subterran
                    else (
                        if (mass < 2) 2 // Terran
                        else (
                            if (mass < 10) 3 // Superterran
                            else (
                                if (mass < 50) 4 // Neptunian
                                else 5 // Jovian
                            )
                        )
                    )
                )
        )
    )
    val radius_min: Double = radius - radius_errminus
    val radius_max: Double = radius + radius_errplus
    val mass_min: Double = mass - mass_errminus
    val mass_max: Double = mass + mass_errplus
    val dist_min: Double = distance - dist_errminus
    val dist_max: Double = distance + dist_errplus
    val orbitdist_min: Double = orbitdistance - orbitdist_errminus
    val orbitdist_max: Double = orbitdistance + orbitdist_errplus


    companion object {
        var smallest_exoplanet: Exoplanet = Exoplanet("", "", 0, 0.0, Double.MAX_VALUE, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,"", "")
        var largest_exoplanet: Exoplanet = Exoplanet("", "", 0, 0.0, Double.MIN_VALUE, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", "")
        var lightest_exoplanet: Exoplanet = Exoplanet("", "", 0, 0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", "")
        var heaviest_exoplanet: Exoplanet = Exoplanet("", "", 0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", "")
        var nearest_exoplanet: Exoplanet = Exoplanet("", "", 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE, 0.0, 0.0, 0.0, 0.0, 0.0, "", "")
        var farthest_exoplanet: Exoplanet = Exoplanet("", "", 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE, 0.0, 0.0, 0.0, 0.0, 0.0, "", "")
        var total: Int = -1

        val planetsPerCategory = mutableListOf(0, 0, 0, 0, 0, 0)
        val planetMasses = mutableListOf<Double>()
        val planetRadiuses = mutableListOf<Double>()
        val planetDistances = mutableListOf<Double>()
    }
}
