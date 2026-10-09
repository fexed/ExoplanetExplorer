package com.fexed.exoplanetexplorer

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        configuration.screenWidthDp > 840
    } else {
        configuration.screenWidthDp > 600
    }
}

@Composable
fun PlotDialog(
    facilityCounts: Map<String, Int>,
    summary: CatalogSummary,
    largestSystem: StarSystem?,
    onClose: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    var categoriesPointClicked by remember { mutableStateOf(false) }
    var categoriesLabel by remember { mutableStateOf("") }
    var categoriesValue by remember { mutableIntStateOf(0) }
    var categoriesText by remember { mutableStateOf(context.getString(R.string.title_categories)) }
    var yearsPointClicked by remember { mutableStateOf(false) }
    var yearsLabel by remember { mutableIntStateOf(0) }
    var yearsValue by remember { mutableIntStateOf(0) }
    var yearsText by remember { mutableStateOf(context.getString(R.string.title_years)) }

    if (categoriesPointClicked) {
        categoriesText = stringResource(R.string.plotprompt_numberincategory, categoriesValue, categoriesLabel.lowercase())
    }

    if (yearsPointClicked) {
        yearsText = stringResource(R.string.plotprompt_numberperyear, yearsValue, yearsLabel)
    }

    Dialog(onDismissRequest = onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(24.dp)) {
            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Column(modifier = Modifier
                    .padding(16.dp)
                    .wrapContentHeight()) {
                    Text(text = stringResource(R.string.title_stats), style = MaterialTheme.typography.h5)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.label_confirmedexoplanets, summary.planetsPerYear.values.sum()),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.title_top_discovery_facilities), style = MaterialTheme.typography.h6)
                    facilityCounts.entries
                        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                        .take(3)
                        .forEachIndexed { index, entry ->
                            Text(
                                text = stringResource(
                                    R.string.label_top_discovery_facility,
                                    index + 1,
                                    entry.key,
                                    java.text.NumberFormat.getIntegerInstance().format(entry.value)
                                ),
                                style = MaterialTheme.typography.body2
                            )
                        }
                    Spacer(modifier = Modifier.height(16.dp))
                    largestSystem?.let { system ->
                        Text(
                            text = stringResource(R.string.title_largest_star_system),
                            style = MaterialTheme.typography.caption
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StarSystemElement(
                            system = system,
                            summary = summary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    CatalogExtrema(summary, isWide = isTablet() && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = categoriesText, style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(8.dp))

                    val columnEntryModel = entryModelOf(
                        0 to summary.planetsPerCategory[0],
                        1 to summary.planetsPerCategory[1],
                        2 to summary.planetsPerCategory[2],
                        3 to summary.planetsPerCategory[3],
                        4 to summary.planetsPerCategory[4],
                        5 to summary.planetsPerCategory[5]
                    )
                    val horizontalAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                        when (value) {
                            0.0f -> context.getString(R.string.label_category_rocky_mercurian)
                            1.0f -> context.getString(R.string.label_category_rocky_subterran)
                            2.0f -> context.getString(R.string.label_category_rocky_terran)
                            3.0f -> context.getString(R.string.label_category_rocky_superterran)
                            4.0f -> context.getString(R.string.label_category_gasgiant_neptunian)
                            5.0f -> context.getString(R.string.label_category_gasgiant_jovian)
                            else -> ""
                        }
                    }


                    Chart(chart = columnChart(), model = columnEntryModel,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis().apply {
                            valueFormatter = horizontalAxisValueFormatter
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = yearsText, style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(8.dp))

                    val listOfEntries : MutableList<FloatEntry> = arrayListOf()
                    for (pair in summary.planetsPerYear.toList()) {
                        listOfEntries.add(FloatEntry(pair.first.toFloat(), pair.second.toFloat()))
                    }
                    val lineEntryModel = ChartEntryModelProducer(listOfEntries)
                    val datesAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                        val str = value.toInt().toString()
                        "\'" + str.substring(str.length - 2)
                    }

                    Chart(chart = lineChart(), chartModelProducer = lineEntryModel,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis().apply {
                            valueFormatter = datesAxisValueFormatter
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogExtrema(summary: CatalogSummary, isWide: Boolean) {
    val groups = listOf(
        listOf(
            stringResource(R.string.label_smallest) to summary.smallest,
            stringResource(R.string.label_largest) to summary.largest
        ),
        listOf(
            stringResource(R.string.label_lightest) to summary.lightest,
            stringResource(R.string.label_heaviest) to summary.heaviest
        ),
        listOf(
            stringResource(R.string.label_nearest) to summary.nearest,
            stringResource(R.string.label_farthest) to summary.farthest
        )
    )

    if (isWide) {
        Row(modifier = Modifier.fillMaxWidth()) {
            groups.forEach { group ->
                Column(modifier = Modifier.weight(1f)) {
                    group.forEach { (title, planet) ->
                        Text(text = title, style = MaterialTheme.typography.caption)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExoplanetElement(exoplanet = planet, summary = summary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    } else {
        groups.flatten().forEach { (title, planet) ->
            Text(text = title, style = MaterialTheme.typography.caption)
            Spacer(modifier = Modifier.height(4.dp))
            ExoplanetElement(exoplanet = planet, summary = summary)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
