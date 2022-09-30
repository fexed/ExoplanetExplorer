package com.fexed.exoplanetexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            "pl_masse,pl_masseerr1,pl_masseerr2" +
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
                StandardScaffold(scaffoldState = scaffoldState, {}) {
                    Loading(true, "Downloading data from caltech.edu")
                }
            }
        }

        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)

        val request = StringRequest(Request.Method.GET, URL, { response ->
            try {
                setContent {
                    ExoplanetExplorerTheme {
                        StandardScaffold(scaffoldState = scaffoldState, {}) {
                            Loading(true, "Download complete, parsing data...")
                        }
                    }
                }

                csvReader().readAllWithHeader(response).forEach { row ->
                    exoplanetsList.add(Exoplanet(
                        row["hostname"]!!,
                        row["pl_name"]!!,
                        row["disc_year"]!!.toInt(),
                        if (row["pl_orbper"]!! == "") -1.0 else row["pl_orbper"]!!.toDouble(),
                        if (row["pl_rade"]!! == "") -1.0 else row["pl_rade"]!!.toDouble(),
                        if (row["pl_masse"]!! == "") -1.0 else row["pl_masse"]!!.toDouble()
                    ))
                    originalExoplanetList.add(Exoplanet(
                        row["hostname"]!!,
                        row["pl_name"]!!,
                        row["disc_year"]!!.toInt(),
                        if (row["pl_orbper"]!! == "") -1.0 else row["pl_orbper"]!!.toDouble(),
                        if (row["pl_rade"]!! == "") -1.0 else row["pl_rade"]!!.toDouble(),
                        if (row["pl_masse"]!! == "") -1.0 else row["pl_masse"]!!.toDouble()
                    ))
                }

                setContent {
                    ExoplanetExplorerTheme {
                        var showFilterDialog by remember { mutableStateOf(false) }
                        if (showFilterDialog) {
                            exoplanetsList = ArrayList(originalExoplanetList)
                            FilterDialog(exoplanetsList) {
                                showFilterDialog = false
                            }
                        }

                        StandardScaffold(scaffoldState = scaffoldState, {
                            FloatingActionButton(onClick = {
                                showFilterDialog = true
                            }) { Image(painter = painterResource(id = R.drawable.filter), contentDescription = null) }
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
                        StandardScaffold(scaffoldState = scaffoldState, {}) {
                            Loading(false, "An error occurred during parsing, ${ex.message}")
                        }
                    }
                }

            }
        }, { err ->
            err.printStackTrace()
            setContent {
                ExoplanetExplorerTheme {
                    StandardScaffold(scaffoldState = scaffoldState, {}) {
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
fun FilterDialog(exoplanetsList: ArrayList<Exoplanet>, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose ) {
        var expanded by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf(0) }
        var inverted by remember { mutableStateOf(false) }
        val items = listOf("None", "Name", "Year", "Size", "Mass", "System")

        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier
            .padding(all = 16.dp)
            .wrapContentSize()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Filter", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Filter by")
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
        else -> {}
    }
}

@Composable
fun StandardScaffold(scaffoldState: ScaffoldState, fabAction: (@Composable () -> Unit),  content: (@Composable (PaddingValues) -> Unit)) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { TopAppBar(
            title = { Text("Exoplanets Explorer") },
            backgroundColor = MaterialTheme.colors.background,
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
                Text(text = exoplanet.name, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Discovered in ${exoplanet.year}", style = MaterialTheme.typography.caption)
            }
        }
        Text(text = exoplanet.star, color = MaterialTheme.colors.secondary, modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(8.dp), textAlign = TextAlign.End)
    }
}

@Composable
fun ExoplanetDialog(exoplanet: Exoplanet, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier.padding(all = 16.dp)
        ) {
            Row(modifier = Modifier.padding(all = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = exoplanet.name, style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (exoplanet.period > 0) Text(text = "Orbital period: ${exoplanet.period} days", style = MaterialTheme.typography.subtitle1)
                    else Text(text = "Unknown orbital period", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (exoplanet.radius > 0) Text(text = "Size: ${exoplanet.radius} Earth's radiuses", style = MaterialTheme.typography.subtitle1)
                    else Text(text = "Unknown exoplanet radius", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (exoplanet.mass > 0) Text(text = "Mass: ${exoplanet.mass} Earth's masses", style = MaterialTheme.typography.subtitle1)
                    else Text(text = "Unknown exoplanet mass", style = MaterialTheme.typography.subtitle1)
                    when (exoplanet.earthlike) {
                        0 -> {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Rocky Exoplanet", style = MaterialTheme.typography.subtitle1)
                        }
                        1 -> {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Gas Giant Exoplanet", style = MaterialTheme.typography.subtitle1)
                        }
                        else -> {}
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Discovered in ${exoplanet.year}", style = MaterialTheme.typography.caption)
                }
            }
            Text(text = exoplanet.star, color = MaterialTheme.colors.secondary, modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), textAlign = TextAlign.End)
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
    exoplanetsList.add(Exoplanet("Sole", "Terra", 0, 1.0, 1.0, 1.0))
    exoplanetsList.add(Exoplanet("Sole", "Terra", 0, 1.0, 1.0, 1.0))
    exoplanetsList.add(Exoplanet("Sole", "Terra", 0, 1.0, 1.0, 1.0))
    exoplanetsList.add(Exoplanet("Sole", "Terra", 0, 1.0, 1.0, 1.0))
    exoplanetsList.add(Exoplanet("Sole", "Terra", 0, 1.0, 1.0, 1.0))

    ExoplanetExplorerTheme {
        StandardScaffold(scaffoldState = rememberScaffoldState(), {}) {
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