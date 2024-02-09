package com.fexed.exoplanetexplorer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fexed.exoplanetexplorer.ui.theme.ExoplanetExplorerTheme
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.gms.ads.*
import com.jaikeerthick.composable_graphs.color.LinearGraphColors
import com.jaikeerthick.composable_graphs.composables.LineGraph
import com.jaikeerthick.composable_graphs.data.GraphData
import com.jaikeerthick.composable_graphs.style.LineGraphStyle
import com.jaikeerthick.composable_graphs.style.LinearGraphVisibility
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.text.DateFormat.getDateInstance
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : ComponentActivity() {
    private val dataEndpointURL = "https://exoplanetarchive.ipac.caltech.edu/TAP/sync?query=" +
            "select+" +
            "pl_name," +
            "hostname," +
            "disc_year," +
            "pl_orbper,pl_orbpererr1,pl_orbpererr2," +
            "pl_rade,pl_radeerr1,pl_radeerr2," +
            "pl_bmasse,pl_bmasseerr1,pl_bmasseerr2," +
            "sy_dist,sy_disterr1,sy_disterr2," +
            "pl_orbsmax,pl_orbsmaxerr1,pl_orbsmaxerr2," +
            "disc_facility,disc_telescope," +
            "pl_controv_flag" +
            "+from+pscomppars&format=csv"
    //ref: https://exoplanetarchive.ipac.caltech.edu/docs/API_PS_columns.html

    private val cacheFile = "cachedExoplanetsDatabase"
    val kLastUpdate = "last_update1"

    lateinit var showFilterDialog: MutableState<Boolean>
    lateinit var showPlotDialog: MutableState<Boolean>
    lateinit var scaffoldState: ScaffoldState
    var exoplanetsList: ArrayList<Exoplanet> = ArrayList()
    var originalExoplanetList: ArrayList<Exoplanet> = ArrayList()
    private var cachedData = false
    var cachedListSize = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(listOf(getString(R.string.testid))).build()
        MobileAds.setRequestConfiguration(configuration)

        val stringBuilder = StringBuilder()

        try {
            val inputStream = openFileInput(cacheFile)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var text: String?

            while (run {
                    text = bufferedReader.readLine()
                    text
                } != null) {
                stringBuilder.appendLine(text)
            }

            parseData(this, stringBuilder.toString(), false)
            cachedData = true
        } catch (_: FileNotFoundException) {}

        if (!cachedData) {
            setContent {
                scaffoldState = rememberScaffoldState()
                ExoplanetExplorerTheme {
                    StandardScaffold(scaffoldState = scaffoldState, {}, {}) {
                        Loading(true, getString(R.string.info_downloading))
                    }
                }
            }
        }

        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)

        Log.d("ParseData", dataEndpointURL)
        val request = StringRequest(Request.Method.GET, dataEndpointURL, { response ->
            Log.d("ParseData", "response:${response.length}")
            try {
                if (!cachedData) {
                    setContent {
                        ExoplanetExplorerTheme {
                            StandardScaffold(scaffoldState = scaffoldState, {}, {}) {
                                Loading(true, getString(R.string.info_downloadcomplete))
                            }
                        }
                    }
                }

                val outputStream: FileOutputStream

                Log.d("ParseData", "write to file")
                try {
                    outputStream = openFileOutput(cacheFile, Context.MODE_PRIVATE)
                    outputStream.write(response.toByteArray())
                } catch (ignored: Exception) {}

                parseData(this, response, true)

            } catch (ex: Exception) {
                Log.d("ParseData", "exception:${ex.printStackTrace()}")
                ex.printStackTrace()
                if (!cachedData) {
                    setContent {
                        ExoplanetExplorerTheme {
                            StandardScaffold(scaffoldState = scaffoldState, {}, {}) {
                                Loading(false, getString(R.string.error_duringparsing, ex.toString()))
                            }
                        }
                    }
                }
            }
        }, { err ->
            err.printStackTrace()
            if (!cachedData) {
                setContent {
                    ExoplanetExplorerTheme {
                        StandardScaffold(scaffoldState = scaffoldState, {}, {}) {
                            Loading(false, getString(R.string.error_duringdownload, err.toString()))
                        }
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

fun parseData(activity: MainActivity, response: String, fromInternet: Boolean) {
    activity.exoplanetsList = ArrayList()
    var tempYearMap = mutableMapOf<Int, Int>()

    csvReader().readAllWithHeader(response).forEach { row ->
        if (row["pl_controv_flag"]!!.toInt() != 1) {
            val exoplanet = Exoplanet(
                row["hostname"]!!,
                row["pl_name"]!!,
                row["disc_year"]!!.toInt(),
                if (row["pl_orbper"]!! == "") -1.0 else row["pl_orbper"]!!.toDouble(),
                if (row["pl_rade"]!! == "") -1.0 else row["pl_rade"]!!.toDouble(),
                if (row["pl_radeerr1"]!! == "") 0.0 else row["pl_radeerr1"]!!.toDouble(),
                if (row["pl_radeerr2"]!! == "") 0.0 else row["pl_radeerr2"]!!.toDouble(),
                if (row["pl_bmasse"]!! == "") -1.0 else row["pl_bmasse"]!!.toDouble(),
                if (row["pl_bmasseerr1"]!! == "") 0.0 else row["pl_bmasseerr1"]!!.toDouble(),
                if (row["pl_bmasseerr2"]!! == "") 0.0 else row["pl_bmasseerr2"]!!.toDouble(),
                if (row["sy_dist"]!! == "") -1.0 else row["sy_dist"]!!.toDouble()*3.26156,
                if (row["sy_disterr1"]!! == "") 0.0 else row["sy_disterr1"]!!.toDouble()*3.26156,
                if (row["sy_disterr2"]!! == "") 0.0 else row["sy_disterr2"]!!.toDouble()*3.26156,
                if (row["pl_orbsmax"]!! == "") -1.0 else row["pl_orbsmax"]!!.toDouble(),
                if (row["pl_orbsmaxerr1"]!! == "") 0.0 else row["pl_orbsmaxerr1"]!!.toDouble(),
                if (row["pl_orbsmaxerr2"]!! == "") 0.0 else row["pl_orbsmaxerr2"]!!.toDouble(),
                row["disc_facility"]!!,
                row["disc_telescope"]!!,
                getDateInstance().format(Date())
            )
            activity.exoplanetsList.add(exoplanet)

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

            if (exoplanet.category >= 0) Exoplanet.planetsPerCategory[exoplanet.category]++
            if (tempYearMap.containsKey(exoplanet.year))
                tempYearMap[exoplanet.year] = (tempYearMap[exoplanet.year] as Int) + 1
            else
                tempYearMap[exoplanet.year] = 1
            if (exoplanet.mass > 0.0 && exoplanet.radius > 0.0) {
                Exoplanet.planetMasses.add(exoplanet.mass)
                Exoplanet.planetRadiuses.add(exoplanet.radius)
            }
            if (exoplanet.distance > 0.0) Exoplanet.planetDistances.add(exoplanet.distance)
        }
    }
    activity.originalExoplanetList = ArrayList(activity.exoplanetsList)
    Log.d("ParseData", "fromInternet:$fromInternet")
    if (!fromInternet) activity.cachedListSize = activity.originalExoplanetList.size
    else {
        if ((activity.getPreferences(MODE_PRIVATE).getString(activity.kLastUpdate, null) == null) || (activity.originalExoplanetList.size != activity.cachedListSize)) {
            Log.d("ParseData", "update last_update")
            activity.getPreferences(MODE_PRIVATE).edit().putString(activity.kLastUpdate, (System.currentTimeMillis()/1000).toString()).apply()
            Toast.makeText(activity.baseContext, activity.getString(R.string.new_data_available), Toast.LENGTH_LONG).show()
        }
    }
    Log.d("ParseData", "last_update is " + activity.getPreferences(MODE_PRIVATE).getString(activity.kLastUpdate, null))

    tempYearMap = (tempYearMap.toSortedMap().toMap() as MutableMap<Int, Int>)
    val minYear = tempYearMap.keys.toList()[0]
    val maxYear = tempYearMap.keys.toList().last()
    for (currYear in minYear..maxYear) {
        if (!tempYearMap.containsKey(currYear)) tempYearMap[currYear] = 0
    }
    Exoplanet.planetsPerYear = (tempYearMap.toSortedMap().toMap() as MutableMap<Int, Int>)
    Exoplanet.total = activity.exoplanetsList.size

    activity.setContent {
        activity.scaffoldState = rememberScaffoldState()
        ExoplanetExplorerTheme {
            activity.showFilterDialog = remember { mutableStateOf(false) }
            activity.showPlotDialog = remember { mutableStateOf(false) }

            if (activity.showFilterDialog.value) {
                activity.exoplanetsList = ArrayList(activity.originalExoplanetList)
                FilterDialog(activity) {
                    activity.showFilterDialog.value = false
                }
            }

            if (activity.showPlotDialog.value) {
                PlotDialog {
                    activity.showPlotDialog.value = false
                }
            }

            StandardScaffold(scaffoldState = activity.scaffoldState, {
                FloatingActionButton(onClick = {
                    activity.showFilterDialog.value = true
                }) { Image(painter = painterResource(id = R.drawable.filter), contentDescription = null) }
            }, {
                IconButton(onClick = {
                    activity.showPlotDialog.value = true
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.plots),
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                    )
                }

                IconButton(onClick = {
                    activity.startActivity(Intent(activity.applicationContext, InfoActivity::class.java))
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.info),
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }) {
                Column {
                    ShowExoplanets( exoplanetsList = activity.exoplanetsList)
                }
            }
        }
    }
}

@Composable
fun PlotDialog(onClose: () -> Unit) {
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
                    Text(text = stringResource(R.string.label_confirmedexoplanets, Exoplanet.total), style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.label_smallest), style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExoplanetElement(exoplanet = Exoplanet.smallest_exoplanet)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.label_largest), style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExoplanetElement(exoplanet = Exoplanet.largest_exoplanet)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.label_lightest), style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExoplanetElement(exoplanet = Exoplanet.lightest_exoplanet)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.label_heaviest), style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExoplanetElement(exoplanet = Exoplanet.heaviest_exoplanet)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.label_nearest), style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExoplanetElement(exoplanet = Exoplanet.nearest_exoplanet)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.label_farthest), style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExoplanetElement(exoplanet = Exoplanet.farthest_exoplanet)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = categoriesText, style = MaterialTheme.typography.caption)
                    LineGraph(yAxisData = Exoplanet.planetsPerCategory,
                        xAxisData = listOf(
                            stringResource(R.string.label_category_rocky_mercurian),
                            stringResource(R.string.label_category_rocky_subterran),
                            stringResource(R.string.label_category_rocky_terran),
                            stringResource(R.string.label_category_rocky_superterran),
                            stringResource(R.string.label_category_gasgiant_neptunian),
                            stringResource(R.string.label_category_gasgiant_jovian)
                        ).map { GraphData.String(it.substring(0, 3) + ".") }, style = LineGraphStyle (
                            visibility = LinearGraphVisibility(
                                isYAxisLabelVisible = true
                            ), colors = LinearGraphColors(
                                lineColor = Color.Transparent,
                                pointColor = MaterialTheme.colors.secondary,
                                clickHighlightColor = MaterialTheme.colors.primary
                            )
                        ), onPointClicked = { point ->
                            categoriesLabel = when ((point.first as String).substring(0, 3)) {
                                context.getString(R.string.label_category_rocky_mercurian).substring(0, 3) -> context.getString(R.string.label_category_rocky_mercurian)
                                context.getString(R.string.label_category_rocky_subterran).substring(0, 3) -> context.getString(R.string.label_category_rocky_subterran)
                                context.getString(R.string.label_category_rocky_terran).substring(0, 3) -> context.getString(R.string.label_category_rocky_terran)
                                context.getString(R.string.label_category_rocky_superterran).substring(0, 3) -> context.getString(R.string.label_category_rocky_superterran)
                                context.getString(R.string.label_category_gasgiant_neptunian).substring(0, 3) -> context.getString(R.string.label_category_gasgiant_neptunian)
                                context.getString(R.string.label_category_gasgiant_jovian).substring(0, 3) -> context.getString(R.string.label_category_gasgiant_jovian)
                                else -> context.getString(R.string.label_category_unknown)
                            }
                            categoriesValue = point.second as Int
                            categoriesPointClicked = true
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = yearsText, style = MaterialTheme.typography.caption)
                    LineGraph(yAxisData = Exoplanet.planetsPerYear.values.toList(),
                        xAxisData = Exoplanet.planetsPerYear.keys.toList().map { GraphData.String(it.toString()) },
                        style = LineGraphStyle (
                            visibility = LinearGraphVisibility(
                                isYAxisLabelVisible = true
                            ), colors = LinearGraphColors(
                                lineColor = MaterialTheme.colors.secondary,
                                pointColor = MaterialTheme.colors.secondary,
                                clickHighlightColor = MaterialTheme.colors.primary
                            )
                        ), onPointClicked = { point ->
                            yearsLabel = (point.first as String).toInt()
                            yearsValue = point.second as Int
                            yearsPointClicked = true
                        }
                    )
                }
            }
        }
    }
}

fun getCategoryLocalizedName(context: Context, category: Int): String {
    val str = (
            when(category) {
                0 -> context.getString(R.string.label_category_rocky_mercurian)
                1 -> context.getString(R.string.label_category_rocky_subterran)
                2 -> context.getString(R.string.label_category_rocky_terran)
                3 -> context.getString(R.string.label_category_rocky_superterran)
                4 -> context.getString(R.string.label_category_gasgiant_neptunian)
                5 -> context.getString(R.string.label_category_gasgiant_jovian)
                else -> context.getString(R.string.label_category_unknown)
            }
    )
    return str
}

@Composable
fun FilterDialog(activity: MainActivity, onClose: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableIntStateOf(0) }
    var inverted by remember { mutableStateOf(false) }
    val items = listOf(
        stringResource(R.string.label_order_none),
        (if (inverted) stringResource(R.string.label_order_name_desc) else stringResource(R.string.label_order_name_asc)),
        (if (inverted) stringResource(R.string.label_order_year_desc) else stringResource(R.string.label_order_year_asc)),
        (if (inverted) stringResource(R.string.label_order_radius_desc) else stringResource(R.string.label_order_radius_asc)),
        (if (inverted) stringResource(R.string.label_order_mass_desc) else stringResource(R.string.label_order_mass_asc)),
        (if (inverted) stringResource(R.string.label_order_star_desc) else stringResource(R.string.label_order_star_asc)),
        (if (inverted) stringResource(R.string.label_order_distanceearth_desc) else stringResource(R.string.label_order_distanceearth_asc)),
        (if (inverted) stringResource(R.string.label_order_period_desc) else stringResource(R.string.label_order_period_asc)),
        (if (inverted) stringResource(R.string.label_order_distancestar_desc) else stringResource(R.string.label_order_distancestar_asc)),
    )

    Dialog(onDismissRequest = onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier
            .padding(all = 16.dp)
            .wrapContentHeight()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = stringResource(R.string.title_filter), style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.title_search))
                TextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth() )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.label_orderby))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = items[selected],
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { expanded = true })
                            .weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(painter = painterResource(R.drawable.dropdownarrow), contentDescription = null, modifier = Modifier.clickable { expanded = true })
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    items.forEachIndexed { index, value ->
                        DropdownMenuItem(onClick = {
                            expanded = false
                            selected = index
                        }) {
                            Text(text = value)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.label_invertorder))
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = inverted, onCheckedChange = { state ->
                        inverted = state
                    })
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    val context = LocalContext.current
                    Button(onClick = {
                        val toRemove: ArrayList<Exoplanet> = ArrayList()
                        query = query.lowercase()
                        for (exoplanet in activity.exoplanetsList) {
                            val category = getCategoryLocalizedName(context, exoplanet.category)
                            if (
                                !exoplanet.name.lowercase().contains(query) &&
                                !exoplanet.discoveryFacility.lowercase().contains(query) &&
                                !exoplanet.discoveryTelescope.lowercase().contains(query) &&
                                !exoplanet.star.lowercase().contains(query) &&
                                !category.lowercase().contains(query)
                            ) toRemove.add(exoplanet)
                        }
                        activity.exoplanetsList.removeAll(toRemove.toSet())
                        sortList(activity.exoplanetsList, inverted, selected)
                        activity.showFilterDialog.value = false
                    }) {
                        Text(text = stringResource(R.string.title_filter), color = MaterialTheme.colors.onPrimary)
                    }
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
        7 -> {
            val toRemove: ArrayList<Exoplanet> = ArrayList()
            for (exoplanet in exoplanetsList) {
                if (exoplanet.period <= 0.0) toRemove.add(exoplanet)
            }
            exoplanetsList.removeAll(toRemove.toSet())

            if (inverted) exoplanetsList.sortWith(compareByDescending { it.period })
            else exoplanetsList.sortWith(compareBy { it.period })
        }
        8 -> {
            val toRemove: ArrayList<Exoplanet> = ArrayList()
            for (exoplanet in exoplanetsList) {
                if (exoplanet.orbitdistance <= 0.0) toRemove.add(exoplanet)
            }
            exoplanetsList.removeAll(toRemove.toSet())

            if (inverted) exoplanetsList.sortWith(compareByDescending { it.orbitdistance })
            else exoplanetsList.sortWith(compareBy { it.orbitdistance })
        }
        else -> {}
    }
}

@Composable
fun StandardScaffold(scaffoldState: ScaffoldState, fabAction: (@Composable () -> Unit), actions: @Composable (RowScope.() -> Unit),  content: (@Composable (PaddingValues) -> Unit)) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
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
    Column {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = context.getString(R.string.admob_bannerlistaid)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
        LazyColumn {
            items(items = exoplanetsList, itemContent = { exoplanet ->
                ExoplanetElement(exoplanet)
            })
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExoplanetElement(exoplanet: Exoplanet) {
    var showDialog by remember { mutableStateOf(false) }

    val icon = when (exoplanet.category) {
        0 -> R.drawable.mercurian
        1, 2, 3 -> R.drawable.rocky
        4 -> R.drawable.gasgiant
        5 -> R.drawable.jovian
        else -> R.drawable.unknown
    }

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
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
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
                Text(text = stringResource(R.string.label_discoveredin, exoplanet.year), style = MaterialTheme.typography.caption)
            }
        }
    }
}

@Composable
fun ExoplanetDialog(exoplanet: Exoplanet, onClose: () -> Unit) {
    var showExplDialog by remember { mutableStateOf(false) }

    if (showExplDialog) {
        DataExplDialog {
            showExplDialog = false
        }
    }

    Dialog(onDismissRequest = onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(24.dp)) {
            Column(modifier = Modifier
                .padding(all = 16.dp)
                .wrapContentSize()) {
                Row {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)) {
                        Text(text = exoplanet.name, style = MaterialTheme.typography.h6)
                        when (exoplanet.category) {
                            0 -> Text(text = stringResource(R.string.label_category_rocky_mercurian), style = MaterialTheme.typography.caption)
                            1 -> Text(text = stringResource(R.string.label_category_rocky_subterran), style = MaterialTheme.typography.caption)
                            2 -> Text(text = stringResource(R.string.label_category_rocky_terran), style = MaterialTheme.typography.caption)
                            3 -> Text(text = stringResource(R.string.label_category_rocky_superterran), style = MaterialTheme.typography.caption)
                            4 -> Text(text = stringResource(R.string.label_category_gasgiant_neptunian), style = MaterialTheme.typography.caption)
                            5 -> Text(text = stringResource(R.string.label_category_gasgiant_jovian), style = MaterialTheme.typography.caption)
                            else -> Text(text = stringResource(R.string.label_category_unknown), style = MaterialTheme.typography.caption)
                        }
                        Row{
                            Text(text = stringResource(R.string.label_system), style = MaterialTheme.typography.caption)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = exoplanet.star, style = MaterialTheme.typography.caption)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { showExplDialog = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.info),
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.label_distancefromearth),style = MaterialTheme.typography.subtitle1)
                if (exoplanet.distance > 0)
                    Text(text = String.format("%.2f", exoplanet.distance), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                else
                    Text(text = stringResource(R.string.label_category_unknown), style = MaterialTheme.typography.caption, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.label_orbitalperiod), style = MaterialTheme.typography.subtitle1)
                if (exoplanet.period > 0)
                    Text(text = String.format("%.2f", exoplanet.period), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                else
                    Text(text = stringResource(R.string.label_category_unknown), style = MaterialTheme.typography.caption, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.label_orbitaldistance), style = MaterialTheme.typography.subtitle1)
                if (exoplanet.orbitdistance > 0)
                    Text(text = String.format("%.2f", exoplanet.orbitdistance), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                else
                    Text(text = stringResource(R.string.label_category_unknown), style = MaterialTheme.typography.caption, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.label_size), style = MaterialTheme.typography.subtitle1)
                if (exoplanet.radius > 0)
                    Text(text = String.format("%.2f", exoplanet.radius), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                else
                    Text(text = stringResource(R.string.label_category_unknown), style = MaterialTheme.typography.caption, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.label_mass), style = MaterialTheme.typography.subtitle1)
                if (exoplanet.mass > 0)
                    Text(text = String.format("%.2f", exoplanet.mass), style = MaterialTheme.typography.subtitle1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                else
                    Text(text = stringResource(R.string.label_category_unknown), style = MaterialTheme.typography.caption, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(8.dp))

                var percentage: Float

                if (exoplanet.distance > 0) {
                    percentage = (((exoplanet.distance - Exoplanet.nearest_exoplanet.distance) * 100) / (Exoplanet.farthest_exoplanet.distance - Exoplanet.nearest_exoplanet.distance)).toFloat() / 100

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.label_nearest), style = MaterialTheme.typography.caption)
                        Spacer(modifier = Modifier.width(4.dp))
                        Slider(value = percentage, onValueChange = {}, enabled = false, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.label_farthest), style = MaterialTheme.typography.caption)
                    }
                }

                if (exoplanet.radius > 0) {
                    percentage = (((exoplanet.radius - Exoplanet.smallest_exoplanet.radius) * 100) / (Exoplanet.largest_exoplanet.radius - Exoplanet.smallest_exoplanet.radius)).toFloat() / 100

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.label_smallest), style = MaterialTheme.typography.caption)
                        Spacer(modifier = Modifier.width(4.dp))
                        Slider(value = percentage, onValueChange = {}, enabled = false, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.label_largest), style = MaterialTheme.typography.caption)
                    }
                }

                if (exoplanet.mass > 0) {
                    percentage = (((exoplanet.mass - Exoplanet.lightest_exoplanet.mass) * 100) / (Exoplanet.heaviest_exoplanet.mass - Exoplanet.lightest_exoplanet.mass)).toFloat() / 100

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.label_lightest), style = MaterialTheme.typography.caption)
                        Spacer(modifier = Modifier.width(4.dp))
                        Slider(value = percentage, onValueChange = {}, enabled = false, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.label_heaviest), style = MaterialTheme.typography.caption)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.label_discoveredbyin, exoplanet.discoveryFacility, exoplanet.discoveryTelescope, exoplanet.year), style = MaterialTheme.typography.caption)
            }
        }
    }
}

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

@Preview(showBackground = true, apiLevel = 33)
@Composable
fun PreviewExoplanetElement() {
}