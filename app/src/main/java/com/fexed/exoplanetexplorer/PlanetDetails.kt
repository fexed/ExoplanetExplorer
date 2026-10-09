package com.fexed.exoplanetexplorer

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.fexed.exoplanetexplorer.ui.theme.blue
import com.fexed.exoplanetexplorer.ui.theme.pink
import com.fexed.exoplanetexplorer.ui.theme.purple
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log10

@Composable
fun ExoplanetDialog(exoplanet: Exoplanet, summary: CatalogSummary) {
    var showExplDialog by remember { mutableStateOf(false) }
    val wikiBrief = rememberWikiBrief(exoplanet.name)

    if (showExplDialog) {
        DataExplDialog(onClose = { showExplDialog = false })
    }

    Column(modifier = Modifier.padding(16.dp).wrapContentSize()) {
        PlanetDetailHeader(exoplanet, onShowDataInfo = { showExplDialog = true })
        Spacer(modifier = Modifier.height(16.dp))
        PlanetMetrics(exoplanet)
        Spacer(modifier = Modifier.height(16.dp))
        PlanetComparisonRanges(exoplanet, summary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(
                R.string.label_discoveredbyin,
                exoplanet.discoveryFacility,
                exoplanet.discoveryTelescope,
                exoplanet.year
            ),
            style = MaterialTheme.typography.caption
        )
        WikiSummary(wikiBrief)
    }
}

@Composable
private fun PlanetDetailHeader(planet: Exoplanet, onShowDataInfo: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = getCategoryLocalizedName(LocalContext.current, planet.category),
                style = MaterialTheme.typography.caption
            )
            Row {
                Text(text = stringResource(R.string.label_system), style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = planet.star, style = MaterialTheme.typography.caption)
            }
        }
        IconButton(onClick = onShowDataInfo) {
            Image(painter = painterResource(R.drawable.info), contentDescription = null)
        }
    }
}

@Composable
private fun PlanetComparisonRanges(planet: Exoplanet, summary: CatalogSummary) {
    Column {
        if (planet.distance > 0.0) {
            ComparisonRange(
                value = planet.distance,
                minimum = summary.nearest.distance,
                maximum = summary.farthest.distance,
                startLabel = stringResource(R.string.label_nearest),
                endLabel = stringResource(R.string.label_farthest),
                color = blue
            )
        }
        if (planet.radius > 0.0) {
            ComparisonRange(
                value = planet.radius,
                minimum = summary.smallest.radius,
                maximum = summary.largest.radius,
                startLabel = stringResource(R.string.label_smallest),
                endLabel = stringResource(R.string.label_largest),
                color = purple
            )
        }
        if (planet.mass > 0.0) {
            ComparisonRange(
                value = planet.mass,
                minimum = summary.lightest.mass,
                maximum = summary.heaviest.mass,
                startLabel = stringResource(R.string.label_lightest),
                endLabel = stringResource(R.string.label_heaviest),
                color = pink
            )
        }
    }
}

@Composable
private fun ComparisonRange(
    value: Double,
    minimum: Double,
    maximum: Double,
    startLabel: String,
    endLabel: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = startLabel, style = MaterialTheme.typography.caption)
        Spacer(modifier = Modifier.width(4.dp))
        Slider(
            value = rangePosition(value, minimum, maximum),
            onValueChange = {},
            enabled = false,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                disabledThumbColor = color,
                disabledActiveTrackColor = color
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = endLabel, style = MaterialTheme.typography.caption)
    }
}

@Composable
private fun WikiSummary(summary: String?) {
    Spacer(modifier = Modifier.height(16.dp))
    when {
        summary == null -> {
            Text(text = stringResource(R.string.wiki_loading), style = MaterialTheme.typography.caption)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = pink)
            }
        }
        summary.isEmpty() -> {
            Text(
                text = stringResource(R.string.wiki_notFound),
                style = MaterialTheme.typography.caption,
                modifier = Modifier.alpha(0.5f)
            )
        }
        else -> {
            Text(text = summary, style = MaterialTheme.typography.caption)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.wiki_source),
                style = MaterialTheme.typography.caption,
                modifier = Modifier.alpha(0.5f)
            )
        }
    }
}

@Composable
private fun rememberWikiBrief(planetName: String): String? {
    var wikiBrief by remember(planetName) { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val baseUrl = stringResource(R.string.wiki_url)
    val query = "?action=query&prop=extracts&exsentences=2&exsectionformat=plain&explaintext=true&format=json&titles=$planetName"

    LaunchedEffect(planetName, baseUrl) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val request = StringRequest(Request.Method.GET, baseUrl + query, { response ->
            try {
                val gson = Gson()
                val adapter = gson.getAdapter(object : TypeToken<Map<String, JsonElement>>() {})
                val model: Map<String, JsonElement> = adapter.fromJson(response)
                val pages = model["query"]?.asJsonObject?.get("pages")?.asJsonObject
                val page = pages?.takeUnless { it.has("-1") }
                    ?.keySet()
                    ?.firstOrNull()
                    ?.let { pages.get(it)?.asJsonObject }
                wikiBrief = page?.let {
                    gson.fromJson(it, Page::class.java).extract.orEmpty()
                }.orEmpty()
            } catch (error: Exception) {
                Log.e("WIKI", "Unable to parse the Wikipedia response for $planetName", error)
                wikiBrief = ""
            }
        }) { error ->
            Log.e("WIKI", "Unable to load the Wikipedia summary for $planetName", error)
            wikiBrief = ""
        }
        requestQueue.add(request)
    }

    return wikiBrief
}
