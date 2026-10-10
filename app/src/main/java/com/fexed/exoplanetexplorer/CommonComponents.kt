package com.fexed.exoplanetexplorer

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
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
                Image(
                    painter = painterResource(R.drawable.star_system),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = system.name, style = MaterialTheme.typography.subtitle1)
                    system.planets.firstOrNull()?.let { firstPlanet ->
                        Text(
                            text = stringResource(R.string.label_distancefromearth) + ": " +
                                    if (firstPlanet.distance > 0.0) {
                                        String.format("%.2f", firstPlanet.distance)
                                    } else {
                                        stringResource(R.string.label_category_unknown)
                                    },
                            style = MaterialTheme.typography.caption
                        )
                    }
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
            AnimatedVisibility(visible = expanded) {
                Column {
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