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
    val discoverer: String) {
    val earthlike: Int = ( if (mass <= 0.0) -1 else (if (mass < 10.0) 0 else 1) )
    val radius_min: Double = radius - radius_errminus
    val radius_max: Double = radius + radius_errplus
    val mass_min: Double = mass - mass_errminus
    val mass_max: Double = mass + mass_errplus
    val dist_min: Double = distance - mass_errminus
    val dist_max: Double = distance + mass_errplus

}
