package com.fexed.exoplanetexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fexed.exoplanetexplorer.ui.theme.ExoplanetExplorerTheme
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader


class MainActivity : ComponentActivity() {
    private val URL = "https://exoplanetarchive.ipac.caltech.edu/TAP/sync?query=" +
            "select+" +
            "pl_name," +
            "hostname," +
            "disc_year," +
            "pl_orbper,pl_orbpererr1,pl_orbpererr2," +
            "pl_rade,pl_radeerr1,pl_radeerr2," +
            "pl_masse,pl_masseerr1,pl_masseerr2," +
            "sy_dist,sy_disterr1,sy_disterr2," +
            "disc_facility,disc_telescope" +
            "+from+ps&format=csv"
    //ref: https://exoplanetarchive.ipac.caltech.edu/docs/API_PS_columns.html

    private var exoplanetsList: ArrayList<Exoplanet> = ArrayList()
    private var originalExoplanetList: ArrayList<Exoplanet> = ArrayList()
    private lateinit var scaffoldState: ScaffoldState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            scaffoldState = rememberScaffoldState()
            ExoplanetExplorerTheme {
                StandardScaffold(scaffoldState = scaffoldState, {}, {}) {
                    Loading(true, "Downloading data from caltech.edu")
                }
            }
        }

        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)

        val request = StringRequest(Request.Method.GET, URL, { response ->
            try {
                setContent {
                    ExoplanetExplorerTheme {
                        StandardScaffold(scaffoldState = scaffoldState, {}, {}) {
                            Loading(true, "Download complete, parsing data...")
                        }
                    }
                }

                csvReader().readAllWithHeader(response).forEach { row ->
                    val exoplanet = Exoplanet(
                        row["hostname"]!!,
                        row["pl_name"]!!,
                        row["disc_year"]!!.toInt(),
                        if (row["pl_orbper"]!! == "") -1.0 else row["pl_orbper"]!!.toDouble(),
                        if (row["pl_rade"]!! == "") -1.0 else row["pl_rade"]!!.toDouble(),
                        if (row["pl_radeerr1"]!! == "") 0.0 else row["pl_radeerr1"]!!.toDouble(),
                        if (row["pl_radeerr2"]!! == "") 0.0 else row["pl_radeerr2"]!!.toDouble(),
                        if (row["pl_masse"]!! == "") -1.0 else row["pl_masse"]!!.toDouble(),
                        if (row["pl_masseerr1"]!! == "") 0.0 else row["pl_masseerr1"]!!.toDouble(),
                        if (row["pl_masseerr2"]!! == "") 0.0 else row["pl_masseerr2"]!!.toDouble(),
                        if (row["sy_dist"]!! == "") -1.0 else row["sy_dist"]!!.toDouble()*3.26156,
                        if (row["sy_disterr1"]!! == "") 0.0 else row["sy_disterr1"]!!.toDouble()*3.26156,
                        if (row["sy_disterr2"]!! == "") 0.0 else row["sy_disterr2"]!!.toDouble()*3.26156,
                        row["disc_facility"]!! + " with " + row["disc_telescope"]!!
                    )
                    exoplanetsList.add(exoplanet)

                    if (exoplanet.mass > 0.0) {
                        if (exoplanet.mass < Exoplanet.lightest_exoplanet.mass) Exoplanet.lightest_exoplanet = exoplanet
                        if (exoplanet.mass > Exoplanet.heaviest_exoplanet.mass) Exoplanet.heaviest_exoplanet = exoplanet
                    }

                    if (exoplanet.radius > 0.0) {
                        if (exoplanet.radius < Exoplanet.smallest_exoplanet.radius) Exoplanet.smallest_exoplanet = exoplanet
                        if (exoplanet.radius > Exoplanet.largest_exoplanet.radius) Exoplanet.largest_exoplanet = exoplanet
                    }

                    if (exoplanet.distance > 0.0) {
                        if (exoplanet.distance < Exoplanet.nearest_exoplanet.distance) Exoplanet.nearest_exoplanet = exoplanet
                        if (exoplanet.distance > Exoplanet.farthest_exoplanet.distance) Exoplanet.farthest_exoplanet = exoplanet
                    }
                }

                originalExoplanetList = ArrayList(exoplanetsList)

                setContent {
                    ExoplanetExplorerTheme {
                        var showFilterDialog by remember { mutableStateOf(false) }
                        var showPlotDialog by remember { mutableStateOf(false) }

                        if (showFilterDialog) {
                            exoplanetsList = ArrayList(originalExoplanetList)
                            FilterDialog(exoplanetsList) {
                                showFilterDialog = false
                            }
                        }

                        if (showPlotDialog) {
                            PlotDialog() {
                                showPlotDialog = false
                            }
                        }

                        StandardScaffold(scaffoldState = scaffoldState, {
                            FloatingActionButton(onClick = {
                                showFilterDialog = true
                            }) { Image(painter = painterResource(id = R.drawable.filter), contentDescription = null) }
                        }, {
                                IconButton(onClick = {
                                    showPlotDialog = true
                                }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.plots),
                                        contentDescription = null,
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                        }) {
                            Column {
                                ShowExoplanets(exoplanetsList = exoplanetsList)
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                setContent {
                    ExoplanetExplorerTheme {
                        StandardScaffold(scaffoldState = scaffoldState, {}, {}) {
                            Loading(false, "An error occurred during parsing, ${ex.message}")
                        }
                    }
                }

            }
        }, { err ->
            err.printStackTrace()
            setContent {
                ExoplanetExplorerTheme {
                    StandardScaffold(scaffoldState = scaffoldState, {}, {}) {
                        Loading(false, "An error occurred during download, ${err.message}")
                    }
                }
            }
        })
        request.retryPolicy = DefaultRetryPolicy(
            10000,
            20,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(request)
    }
}

@Composable
fun PlotDialog(onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier
            .padding(all = 16.dp)
            .wrapContentSize()) {
            Column(modifier = Modifier
                .padding(24.dp)
                .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
                .wrapContentHeight()) {
                Text(text = "Stats", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Smallest Exoplanet", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                ExoplanetElement(exoplanet = Exoplanet.smallest_exoplanet)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Largest Exoplanet", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                ExoplanetElement(exoplanet = Exoplanet.largest_exoplanet)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Lightest Exoplanet", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                ExoplanetElement(exoplanet = Exoplanet.lightest_exoplanet)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Heaviest Exoplanet", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                ExoplanetElement(exoplanet = Exoplanet.heaviest_exoplanet)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Nearest Exoplanet", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                ExoplanetElement(exoplanet = Exoplanet.nearest_exoplanet)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Farthest Exoplanet", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                ExoplanetElement(exoplanet = Exoplanet.farthest_exoplanet)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FilterDialog(exoplanetsList: ArrayList<Exoplanet>, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose ) {
        var expanded by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf(0) }
        var inverted by remember { mutableStateOf(false) }
        val items = listOf("None", "Name", "Year", "Size", "Mass", "System", "Distance")

        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier
            .padding(all = 16.dp)
            .wrapContentSize()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Filter", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Order by")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = items[selected],
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { expanded = true })
                    )
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    items.forEachIndexed { index, value ->
                        DropdownMenuItem(onClick = {
                            expanded = false
                            selected = index

                            sortList(exoplanetsList, inverted, selected)
                        }) {
                            Text(text = value)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Invert order")
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = inverted, onCheckedChange = { state ->
                        inverted = state
                        sortList(exoplanetsList, inverted, selected)
                    })
                }
            }
        }
    }
}

fun sortList(exoplanetsList: ArrayList<Exoplanet>, inverted: Boolean, selected: Int) {
    when (selected) {
        1 -> {
            if (inverted) exoplanetsList.sortWith(compareByDescending { it.name })
            else exoplanetsList.sortWith(compareBy { it.name })
        }
        2 -> {
            if (inverted) exoplanetsList.sortWith(compareByDescending { it.year })
            else exoplanetsList.sortWith(compareBy { it.year })
        }
        3 -> {
            val toRemove: ArrayList<Exoplanet> = ArrayList()
            for (exoplanet in exoplanetsList) {
                if (exoplanet.radius <= 0.0) toRemove.add(exoplanet)
            }
            exoplanetsList.removeAll(toRemove.toSet())

            if (inverted) exoplanetsList.sortWith(compareByDescending { it.radius })
            else exoplanetsList.sortWith(compareBy { it.radius })
        }
        4 -> {
            val toRemove: ArrayList<Exoplanet> = ArrayList()
            for (exoplanet in exoplanetsList) {
                if (exoplanet.mass <= 0.0) toRemove.add(exoplanet)
            }
            exoplanetsList.removeAll(toRemove.toSet())

            if (inverted) exoplanetsList.sortWith(compareByDescending { it.mass })
            else exoplanetsList.sortWith(compareBy { it.mass })
        }
        5 -> {
            if (inverted) exoplanetsList.sortWith(compareByDescending { it.star })
            else exoplanetsList.sortWith(compareBy { it.star })
        }
        6 -> {
            val toRemove: ArrayList<Exoplanet> = ArrayList()
            for (exoplanet in exoplanetsList) {
                if (exoplanet.distance <= 0.0) toRemove.add(exoplanet)
            }
            exoplanetsList.removeAll(toRemove.toSet())

            if (inverted) exoplanetsList.sortWith(compareByDescending { it.distance })
            else exoplanetsList.sortWith(compareBy { it.distance })
        }
        else -> {}
    }
}

@Composable
fun StandardScaffold(scaffoldState: ScaffoldState, fabAction: (@Composable () -> Unit), actions: @Composable (RowScope.() -> Unit),  content: (@Composable (PaddingValues) -> Unit)) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { TopAppBar(
            title = { Text("Exoplanets Explorer") },
            backgroundColor = MaterialTheme.colors.background,
            actions = actions,
            navigationIcon = { Image(
                painter = painterResource(id = R.drawable.planets),
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )})
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = fabAction,
        content = content
    )
}

@Composable
fun ShowExoplanets(exoplanetsList: ArrayList<Exoplanet>) {
    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        items(items = exoplanetsList, itemContent = { exoplanet ->
            ExoplanetElement(exoplanet)
        })
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExoplanetElement(exoplanet: Exoplanet) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ExoplanetDialog(exoplanet = exoplanet) {
            showDialog = false
        }
    }

    Surface(shape = MaterialTheme.shapes.large, elevation = 1.dp, modifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth()
        .padding(4.dp),
            onClick = {
                showDialog = true
            }
        ) {
        Row(modifier = Modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {

            when (exoplanet.earthlike) {
                0 -> {
                    Image(
                        painter = painterResource(R.drawable.earthlike),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
                1 -> {
                    Image(
                        painter = painterResource(R.drawable.jovianlike),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(R.drawable.unknown),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row {
                    Text(text = exoplanet.name, style = MaterialTheme.typography.h6)
                    Text(text = exoplanet.star, color = MaterialTheme.colors.secondary, modifier = Modifier
                        .weight(1f)
                        .padding(8.dp), textAlign = TextAlign.End,
                    maxLines = 1)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Discovered in ${exoplanet.year}", style = MaterialTheme.typography.caption)
            }
        }
    }
}

@Composable
fun ExoplanetDialog(exoplanet: Exoplanet, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier
            .padding(all = 16.dp)
            .wrapContentSize()
        ) {
            Row(modifier = Modifier
                .padding(all = 24.dp)
                .wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = exoplanet.name, style = MaterialTheme.typography.h6)
                    when (exoplanet.earthlike) {
                        0 -> Text(text = "Rocky", style = MaterialTheme.typography.caption)
                        1 -> Text(text = "Gas Giant", style = MaterialTheme.typography.caption)
                        else -> Text(text = "Unknown", style = MaterialTheme.typography.caption)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Text(text = "Distance (light years):", style = MaterialTheme.typography.subtitle1)
                        if (exoplanet.distance > 0) Text(text = String.format("%.3f", exoplanet.distance), style = MaterialTheme.typography.subtitle1, modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(), textAlign = TextAlign.End)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = "Orbital period (days):", style = MaterialTheme.typography.subtitle1)
                        if (exoplanet.period > 0) Text(text = String.format("%.3f", exoplanet.period), style = MaterialTheme.typography.subtitle1, modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(), textAlign = TextAlign.End)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = "Size (Earth = 1):", style = MaterialTheme.typography.subtitle1)
                        if (exoplanet.radius > 0) Text(text = String.format("%.3f", exoplanet.radius), style = MaterialTheme.typography.subtitle1, modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(), textAlign = TextAlign.End)
                    }
                    Row {
                        Text(text = "Mass (Earth = 1):", style = MaterialTheme.typography.subtitle1)
                        if (exoplanet.mass > 0) Text(text = String.format("%.3f", exoplanet.mass), style = MaterialTheme.typography.subtitle1, modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(), textAlign = TextAlign.End)
                    }
                    Row {
                        Text(text = "System:", style = MaterialTheme.typography.subtitle1)
                        Text(text = exoplanet.star, style = MaterialTheme.typography.subtitle1, modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(), textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    var percentage: Float

                    if (exoplanet.distance > 0) {
                        percentage = (((exoplanet.distance - Exoplanet.nearest_exoplanet.distance) * 100) / (Exoplanet.farthest_exoplanet.distance - Exoplanet.nearest_exoplanet.distance)).toFloat() / 100

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Nearest", style = MaterialTheme.typography.caption)
                            Spacer(modifier = Modifier.width(4.dp))
                            Slider(value = percentage, onValueChange = {}, enabled = false, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Farthest", style = MaterialTheme.typography.caption)
                        }
                    }

                    if (exoplanet.radius > 0) {
                        percentage = (((exoplanet.radius - Exoplanet.smallest_exoplanet.radius) * 100) / (Exoplanet.largest_exoplanet.radius - Exoplanet.smallest_exoplanet.radius)).toFloat() / 100

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Smallest", style = MaterialTheme.typography.caption)
                            Spacer(modifier = Modifier.width(4.dp))
                            Slider(value = percentage, onValueChange = {}, enabled = false, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Largest", style = MaterialTheme.typography.caption)
                        }
                    }

                    if (exoplanet.mass > 0) {
                        percentage = (((exoplanet.mass - Exoplanet.lightest_exoplanet.mass) * 100) / (Exoplanet.heaviest_exoplanet.mass - Exoplanet.lightest_exoplanet.mass)).toFloat() / 100

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Lightest", style = MaterialTheme.typography.caption)
                            Spacer(modifier = Modifier.width(4.dp))
                            Slider(value = percentage, onValueChange = {}, enabled = false, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Heaviest", style = MaterialTheme.typography.caption)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Discovered by ${exoplanet.discoverer} in ${exoplanet.year}", style = MaterialTheme.typography.caption)
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
        Text(text = message, style = MaterialTheme.typography.h6, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun DefaultPreview() {
    val exoplanetsList: ArrayList<Exoplanet> = ArrayList()

    ExoplanetExplorerTheme {
        StandardScaffold(scaffoldState = rememberScaffoldState(), {}, {}) {
            ShowExoplanets(exoplanetsList = exoplanetsList)
        }
    }
}

/*
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun PreviewWithNightMode() {
    ExoplanetExplorerTheme {
        StandardScaffold(scaffoldState = rememberScaffoldState()) {
            Column {

            }
        }
    }
}
*/