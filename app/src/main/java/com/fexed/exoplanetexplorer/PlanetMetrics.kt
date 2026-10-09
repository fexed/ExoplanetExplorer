package com.fexed.exoplanetexplorer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log10

@Composable
fun PlanetMetrics(planet: Exoplanet) {
    Column {
        PlanetMetric(
            icon = R.drawable.distance,
            title = stringResource(R.string.label_distancefromearth),
            isKnown = planet.distance > 0.0
        ) {
            NumberWithErrors(planet.distance, planet.dist_errplus, planet.dist_errminus)
        }
        PlanetMetric(
            icon = R.drawable.orbital_period,
            title = stringResource(R.string.label_orbitalperiod),
            isKnown = planet.period > 0.0
        ) {
            val years = planet.period / DAYS_PER_YEAR
            val days = String.format("%.2f", planet.period)
            val value = if (years >= 100) {
                stringResource(
                    R.string.label_period_years_approx,
                    java.text.NumberFormat.getIntegerInstance().format(years)
                ).let { "$days $it" }
            } else {
                days
            }
            Text(value, style = MaterialTheme.typography.body1)
        }
        PlanetMetric(
            icon = R.drawable.orbital_distance,
            title = stringResource(R.string.label_orbitaldistance),
            isKnown = planet.orbitdistance > 0.0
        ) {
            OrbitalDistanceValue(planet)
        }
        PlanetMetric(
            icon = R.drawable.radius,
            title = stringResource(R.string.label_size),
            isKnown = planet.radius > 0.0
        ) {
            NumberWithErrors(
                planet.radius,
                planet.radius_errplus,
                planet.radius_errminus,
                planetRadiusComparison(planet.radius)
            )
        }
        PlanetMetric(
            icon = R.drawable.mass,
            title = stringResource(R.string.label_mass),
            isKnown = planet.mass > 0.0
        ) {
            NumberWithErrors(
                planet.mass,
                planet.mass_errplus,
                planet.mass_errminus,
                planetMassComparison(planet.mass)
            )
        }
    }
}

@Composable
private fun PlanetMetric(
    icon: Int,
    title: String,
    isKnown: Boolean,
    value: @Composable () -> Unit
) {
    ExoplanetDataRow(icon, title) {
        if (isKnown) value()
        else Text(
            text = stringResource(R.string.label_category_unknown),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun NumberWithErrors(
    value: Double,
    errorPlus: Double,
    errorMinus: Double,
    comparison: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = String.format("%.2f", value) + comparison?.let { " $it" }.orEmpty(),
            style = MaterialTheme.typography.body1
        )
        ErrorValues(errorPlus, errorMinus)
    }
}

@Composable
private fun ErrorValues(errorPlus: Double, errorMinus: Double) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        if (errorPlus > 0.0) {
            Text("+ ${String.format("%.2f", errorPlus)}", style = MaterialTheme.typography.caption)
        }
        if (errorMinus < 0.0) {
            Text("- ${String.format("%.2f", -errorMinus)}", style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
private fun OrbitalDistanceValue(planet: Exoplanet) {
    val references = listOf(
        0.387 to R.string.planet_mercury,
        0.723 to R.string.planet_venus,
        1.0 to R.string.planet_earth,
        1.524 to R.string.planet_mars,
        5.203 to R.string.planet_jupiter,
        9.537 to R.string.planet_saturn,
        19.191 to R.string.planet_uranus,
        30.069 to R.string.planet_neptune,
        39.482 to R.string.planet_pluto
    )
    val (referenceDistance, planetNameResource) = references.minByOrNull {
        abs(log10(planet.orbitdistance / it.first))
    }!!
    val multiple = java.text.NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 1
    }.format(planet.orbitdistance / referenceDistance)
    val comparison = stringResource(
        R.string.label_orbit_distance_multiple,
        multiple,
        stringResource(planetNameResource)
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${String.format("%.2f", planet.orbitdistance)} $comparison",
            style = MaterialTheme.typography.body1
        )
        PreciseErrorValues(planet.orbitdist_errplus, planet.orbitdist_errminus)
    }
}

@Composable
private fun PreciseErrorValues(errorPlus: Double, errorMinus: Double) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        if (errorPlus > 0.0) {
            Text(
                text = "+ ${formatPreciseError(errorPlus)}",
                style = MaterialTheme.typography.caption
            )
        }
        if (errorMinus < 0.0) {
            Text(
                text = "- ${formatPreciseError(-errorMinus)}",
                style = MaterialTheme.typography.caption
            )
        }
    }
}

private fun formatPreciseError(error: Double): String {
    val decimalPlaces = ceil(-log10(abs(error))).toInt().coerceAtLeast(0)
    return String.format("%.${decimalPlaces}f", error)
}

@Composable
private fun ExoplanetDataRow(icon: Int, title: String, value: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.caption)
            value()
        }
    }
}

private const val DAYS_PER_YEAR = 365.25
