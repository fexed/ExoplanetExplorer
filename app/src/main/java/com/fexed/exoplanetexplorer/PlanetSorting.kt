package com.fexed.exoplanetexplorer

fun sortPlanets(
    planets: List<Exoplanet>,
    inverted: Boolean,
    selectedOrder: Int
): List<Exoplanet> {
    val filtered = when (selectedOrder) {
        3 -> planets.filter { it.radius > 0.0 }
        4 -> planets.filter { it.mass > 0.0 }
        6 -> planets.filter { it.distance > 0.0 }
        7 -> planets.filter { it.period > 0.0 }
        8 -> planets.filter { it.orbitdistance > 0.0 }
        else -> planets
    }

    return when (selectedOrder) {
        1 -> filtered.sortedWith(if (inverted) compareByDescending { it.name } else compareBy { it.name })
        2 -> filtered.sortedWith(if (inverted) compareByDescending { it.year } else compareBy { it.year })
        3 -> filtered.sortedWith(if (inverted) compareByDescending { it.radius } else compareBy { it.radius })
        4 -> filtered.sortedWith(if (inverted) compareByDescending { it.mass } else compareBy { it.mass })
        5 -> filtered.sortedWith(if (inverted) compareByDescending { it.star } else compareBy { it.star })
        6 -> filtered.sortedWith(if (inverted) compareByDescending { it.distance } else compareBy { it.distance })
        7 -> filtered.sortedWith(if (inverted) compareByDescending { it.period } else compareBy { it.period })
        8 -> filtered.sortedWith(if (inverted) compareByDescending { it.orbitdistance } else compareBy { it.orbitdistance })
        else -> filtered
    }
}
