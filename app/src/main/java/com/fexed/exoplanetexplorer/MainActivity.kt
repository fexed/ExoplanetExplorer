package com.fexed.exoplanetexplorer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fexed.exoplanetexplorer.ui.theme.ExoplanetExplorerTheme
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.lang.Exception

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ExoplanetExplorerTheme {
                val scaffoldState = rememberScaffoldState()
                StandardScaffold(scaffoldState = scaffoldState) {
                    Loading("Data is being downloaded from caltech.edu")
                }
            }
        }

        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)

        val request = StringRequest(Request.Method.GET, URL, { response ->
            try {
                setContent {
                    ExoplanetExplorerTheme {
                        val scaffoldState = rememberScaffoldState()
                        StandardScaffold(scaffoldState = scaffoldState) {
                            Loading("Download complete, parsing data...")
                        }
                    }
                }

                csvReader().readAllWithHeader(response).forEach { row ->
                    exoplanetsList.add(Exoplanet(
                        row["hostname"]!!,
                        row["pl_name"]!!,
                        row["disc_year"]!!.toInt(),
                        if (row["pl_orbper"]!! == "") 0.0 else row["pl_orbper"]!!.toDouble(),
                        if (row["pl_rade"]!! == "") 0.0 else row["pl_rade"]!!.toDouble(),
                        if (row["pl_masse"]!! == "") 0.0 else row["pl_masse"]!!.toDouble()
                    ))
                }

                setContent {
                    ExoplanetExplorerTheme {
                        val scaffoldState = rememberScaffoldState()
                        StandardScaffold(scaffoldState = scaffoldState) {
                            Column {
                                ShowExoplanets(exoplanetsList = exoplanetsList)
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }, {
            it.printStackTrace()
        })

        requestQueue.add(request)
    }
}

@Composable
fun StandardScaffold(scaffoldState: ScaffoldState, content: @Composable (PaddingValues) -> Unit) {
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

    Surface(shape = MaterialTheme.shapes.small, elevation = 1.dp, modifier = Modifier
        .padding(all = 4.dp)
        .fillMaxWidth(),
            onClick = {
                showDialog = true
            }
        ) {
        Row(modifier = Modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.saturn),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
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
    Dialog(onDismissRequest = onClose ) {
        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier.padding(all = 4.dp)
        ) {
            Row(modifier = Modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.saturn),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = exoplanet.name, style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Orbital period: ${exoplanet.period} days", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Size: ${exoplanet.radius} Earth's radiuses", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Mass: ${exoplanet.mass} Earth's masses", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Discovered in ${exoplanet.year}", style = MaterialTheme.typography.caption)
                }
            }
            Text(text = exoplanet.star, color = MaterialTheme.colors.secondary, modifier = Modifier.fillMaxWidth().padding(8.dp), textAlign = TextAlign.End)
        }
    }
}

@Composable
fun Loading(message: String) {
    Column {
        ExoplanetLoading(false)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = message, style = MaterialTheme.typography.h6, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
fun ExoplanetLoading(info: Boolean) {
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
                if (info) {
                    Text(text = "Downloading data from caltech.edu")
                } else LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
        StandardScaffold(scaffoldState = rememberScaffoldState()) {
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