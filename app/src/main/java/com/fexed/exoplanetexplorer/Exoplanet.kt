package com.fexed.exoplanetexplorer

class Exoplanet(
    val star: String,
    val name: String,
    val year: Int,
    val period: Double,
    val radius: Double,
    val radius_errplus: Double,
    val radius_errminus: Double,
    val mass: Double,
    val mass_errplus: Double,
    val mass_errminus: Double,
    val distance: Double,
    val dist_errplus: Double,
    val dist_errminus: Double,
    val orbitdistance: Double,
    val orbitdist_errplus: Double,
    val orbitdist_errminus: Double,
    val discoveryFacility: String,
    val discoveryTelescope: String,
    val lastupdate: String) {
    val category: Int = when {
        mass <= 0.0 -> -1
        mass < 0.1 -> 0
        mass < 0.5 -> 1
        mass < 2.0 -> 2
        mass < 10.0 -> 3
        mass < 50.0 -> 4
        else -> 5
    }
    val radius_min: Double = radius - radius_errminus
    val radius_max: Double = radius + radius_errplus
    val mass_min: Double = mass - mass_errminus
    val mass_max: Double = mass + mass_errplus
    val dist_min: Double = distance - dist_errminus
    val dist_max: Double = distance + dist_errplus
    val orbitdist_min: Double = orbitdistance - orbitdist_errminus
    val orbitdist_max: Double = orbitdistance + orbitdist_errplus


    companion object {
        val Earth: Exoplanet = Exoplanet(
            star = "Sol",
            name = "Earth",
            year = 0,
            period = 365.0,
            discoveryFacility = "Humans",
            discoveryTelescope = "Human Eye",
            distance = 1.0, dist_errminus = 0.0, dist_errplus = 0.0,
            orbitdistance = 1.0, orbitdist_errminus = 0.0, orbitdist_errplus = 0.0,
            mass = 1.0, mass_errminus = 0.0, mass_errplus = 0.0,
            radius = 1.0, radius_errminus = 0.0, radius_errplus = 0.0,
            lastupdate = "-"
        )
    }
}
