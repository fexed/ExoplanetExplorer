package com.fexed.exoplanetexplorer

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fexed.exoplanetexplorer.ui.theme.ExoplanetExplorerTheme

@Composable
fun DataExplDialog(onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(24.dp)) {
            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Column(modifier = Modifier
                    .padding(all = 16.dp)
                    .wrapContentSize()) {
                    Text(text = stringResource(R.string.title_datainfo), style = MaterialTheme.typography.h5, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.label_datainfo_category), style = MaterialTheme.typography.h6, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Text(text = stringResource(R.string.text_datainfo_category), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.label_datainfo_distancefromearth), style = MaterialTheme.typography.h6, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Text(text = stringResource(R.string.text_datainfo_distancefromearth), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.label_datainfo_orbitaldata), style = MaterialTheme.typography.h6, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Text(text = stringResource(R.string.text_datainfo_orbitaldata), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.label_datainfo_physicaldata), style = MaterialTheme.typography.h6, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Text(text = stringResource(R.string.text_datainfo_physicaldata), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.label_datainfo_starcolor), style = MaterialTheme.typography.h6, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Text(text = stringResource(R.string.text_datainfo_starcolor), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

        }
    }
}

@Composable
fun Loading(isLoading:Boolean, message: String) {
    Column {
        ExoplanetLoading(isLoading)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = message, style = MaterialTheme.typography.h5, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
fun ExoplanetLoading(isLoading: Boolean) {
    Surface(shape = MaterialTheme.shapes.small, elevation = 1.dp, modifier = Modifier
        .padding(all = 4.dp)
        .fillMaxWidth()) {
        Row(modifier = Modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.saturn),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun StarSystemElement(
    system: StarSystem,
    selectedOrder: Int = 0,
    summary: CatalogSummary = CatalogSummary.EMPTY,
    selectedSystemOrder: Int = SYSTEM_ORDER_BY_NAME,
    selectedPlanet: Exoplanet? = null,
    onPlanetSelected: ((Exoplanet) -> Unit)? = null
) {
    var expanded by remember(system.name) { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarSystemIcon(
                    color = system.planets.firstOrNull()?.let {
                        starColorFor(it.stellarTemperature, it.spectralType)
                    } ?: starColorFor(null, null),
                    radiusScale = stellarRadiusIconScale(
                        system.planets.firstOrNull()?.stellarRadius
                    ),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = system.name, style = MaterialTheme.typography.subtitle1)
                    Text(
                        text = pluralStringResource(
                            R.plurals.system_planet_count,
                            system.planets.size,
                            system.planets.size
                        ),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.secondary
                    )
                }
                Image(
                    painter = painterResource(R.drawable.dropdownarrow),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
            if (
                selectedSystemOrder == SYSTEM_ORDER_BY_DISTANCE ||
                selectedSystemOrder == SYSTEM_ORDER_BY_STELLAR_RADIUS ||
                selectedSystemOrder == SYSTEM_ORDER_BY_STELLAR_MASS
            ) {
                StarSystemSortValue(system, selectedSystemOrder)
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    system.planets.firstOrNull()?.let { firstPlanet ->
                        StarSystemInfo(firstPlanet)
                    }
                    system.planets.forEach { planet ->
                        ExoplanetElement(
                            exoplanet = planet,
                            selectedOrder = selectedOrder,
                            summary = summary,
                            isSelected = selectedPlanet?.name == planet.name,
                            onPlanetSelected = onPlanetSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StarSystemIcon(
    color: Color,
    radiusScale: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = size.minDimension * 0.255f * radiusScale

        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = radius * 1.14f,
            center = Offset(centerX, centerY),
            style = Stroke(width = radius * 0.12f)
        )
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(centerX, centerY)
        )
    }
}

@Composable
private fun StarSystemSortValue(
    system: StarSystem,
    selectedSystemOrder: Int
) {
    val numberFormat = remember {
        java.text.NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 2
        }
    }
    val planet = system.planets.firstOrNull()
    val unknown = stringResource(R.string.label_category_unknown)
    val value = when (selectedSystemOrder) {
        SYSTEM_ORDER_BY_NAME -> system.name
        SYSTEM_ORDER_BY_PLANET_COUNT -> pluralStringResource(
            R.plurals.system_planet_count,
            system.planets.size,
            system.planets.size
        )
        SYSTEM_ORDER_BY_DISTANCE -> planet?.distance?.takeIf { it > 0.0 }
            ?.let { "${numberFormat.format(it)} LY" } ?: unknown
        SYSTEM_ORDER_BY_STELLAR_RADIUS -> planet?.stellarRadius?.takeIf { it > 0.0 }
            ?.let { "${numberFormat.format(it)} R☉" } ?: unknown
        SYSTEM_ORDER_BY_STELLAR_MASS -> planet?.stellarMass?.takeIf { it > 0.0 }
            ?.let { "${numberFormat.format(it)} M☉" } ?: unknown
        else -> unknown
    }

    Row(
        modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.filter),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.secondary
        )
    }
}

@Composable
private fun StarSystemInfo(planet: Exoplanet) {
    val unknown = stringResource(R.string.label_category_unknown)
    val numberFormat = remember {
        java.text.NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 2
        }
    }
    val stellarRadius = planet.stellarRadius.takeIf { it > 0.0 }
        ?.let { numberFormat.format(it) }
        ?: unknown
    val stellarMass = planet.stellarMass.takeIf { it > 0.0 }
        ?.let { numberFormat.format(it) }
        ?: unknown
    val rightAscension = planet.rightAscension
        ?.let { "${numberFormat.format(it)}°" }
        ?: unknown
    val declination = planet.declination
        ?.let { "${numberFormat.format(it)}°" }
        ?: unknown

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.label_stellar_radius, stellarRadius),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.secondary
        )
        Text(
            text = stringResource(R.string.label_stellar_mass, stellarMass),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.secondary
        )
        Text(
            text = stringResource(R.string.label_stellar_coordinates, rightAscension, declination),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.secondary
        )
    }
}

@Preview(apiLevel = 33, locale = "en")
@Composable
fun PreviewStarSystemElement() {
    ExoplanetExplorerTheme {
        Surface(color = MaterialTheme.colors.background) {
            StarSystemElement(
                system = StarSystem("Solar System", listOf(Exoplanet.Earth))
            )
        }
    }
}

@Preview(apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "en")
@Composable
fun PreviewStarSystemElementDark() {
    ExoplanetExplorerTheme {
        Surface(color = MaterialTheme.colors.background) {
            StarSystemElement(
                system = StarSystem("Solar System", listOf(Exoplanet.Earth))
            )
        }
    }
}

@Preview(apiLevel = 33, locale = "it")
@Composable
fun PreviewExoplanetElement() {
    ExoplanetExplorerTheme {
        Surface(color = MaterialTheme.colors.background) {
            ExoplanetElement(exoplanet = Exoplanet.Earth)
        }
    }
}

@Preview(apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "en")
@Composable
fun PreviewExoplanetElementDark() {
    ExoplanetExplorerTheme {
        Surface(color = MaterialTheme.colors.background) {
            ExoplanetElement(exoplanet = Exoplanet.Earth)
        }
    }
}

@Preview(apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "ja")
@Composable
fun PreviewExoplanetElementExpanded() {
    ExoplanetExplorerTheme {
        Surface(color = MaterialTheme.colors.background) {
            ExoplanetElement(exoplanet = Exoplanet.Earth, isExpanded = true)
        }
    }
}