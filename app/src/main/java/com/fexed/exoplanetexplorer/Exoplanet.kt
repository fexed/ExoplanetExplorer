package com.fexed.exoplanetexplorer

class Exoplanet(val star: String, val name: String, val year: Int, val period: Double, val radius: Double, val mass: Double) {
    var earthlike: Int = ( if (mass <= 0.0) -1 else (if (mass < 10.0) 0 else 1) )
}
