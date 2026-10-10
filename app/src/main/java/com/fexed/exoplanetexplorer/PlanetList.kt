package com.fexed.exoplanetexplorer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.res.Configuration

@Composable
fun StandardScaffold(scaffoldState: ScaffoldState, fabAction: (@Composable () -> Unit), actions: @Composable (RowScope.() -> Unit),  content: (@Composable (PaddingValues) -> Unit)) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = { Text(stringResource(R.string.app_name)) },
            backgroundColor = MaterialTheme.colors.background,
            actions = actions
        ) },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = fabAction,
        content = content
    )
}

@Composable
fun ShowExoplanets(
    exoplanetsList: List<Exoplanet>,
    selectedOrder: Int,
    orderLabel: String?,
    searchQuery: String,
    selectedFacilityLabel: String?,
    selectedTelescopeLabel: String?,
    summary: CatalogSummary,
    showStarSystems: Boolean,
    selectedSystemOrder: Int,
    invertedSystemOrder: Boolean,
    systemOrderLabel: String?
) {
    val configuration = LocalConfiguration.current
    val isLandscapeTablet = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
        configuration.screenWidthDp >= 600
    var selectedPlanetName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedPlanet = remember(exoplanetsList, selectedPlanetName) {
        exoplanetsList.firstOrNull { it.name == selectedPlanetName } ?: exoplanetsList.firstOrNull()
    }
    val systems = remember(exoplanetsList, selectedSystemOrder, invertedSystemOrder) {
        groupPlanetsBySystem(exoplanetsList, selectedSystemOrder, invertedSystemOrder)
    }

    if (isLandscapeTablet) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight()
            ) {
                ListReminders(
                    orderLabel = orderLabel,
                    searchQuery = searchQuery,
                    selectedFacilityLabel = selectedFacilityLabel,
                    selectedTelescopeLabel = selectedTelescopeLabel,
                    showStarSystems = showStarSystems,
                    systemOrderLabel = systemOrderLabel
                )
                PlanetList(
                    exoplanetsList = exoplanetsList,
                    systems = systems,
                    selectedOrder = selectedOrder,
                    summary = summary,
                    showStarSystems = showStarSystems,
                    selectedPlanet = selectedPlanet,
                    onPlanetSelected = { selectedPlanetName = it.name },
                    selectedSystemOrder = selectedSystemOrder,
                    invertedSystemOrder = invertedSystemOrder,
                    modifier = Modifier.weight(1f)
                )
            }
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )
            Surface(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
                    .padding(start = 12.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = 1.dp
            ) {
                if (selectedPlanet != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        ExoplanetDialog(selectedPlanet, summary)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.label_select_planet_details),
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } else {
        Column {
            ListReminders(
                orderLabel = orderLabel,
                searchQuery = searchQuery,
                selectedFacilityLabel = selectedFacilityLabel,
                selectedTelescopeLabel = selectedTelescopeLabel,
                showStarSystems = showStarSystems,
                systemOrderLabel = systemOrderLabel
            )
            PlanetList(
                exoplanetsList = exoplanetsList,
                systems = systems,
                selectedOrder = selectedOrder,
                summary = summary,
                showStarSystems = showStarSystems,
                selectedSystemOrder = selectedSystemOrder,
                invertedSystemOrder = invertedSystemOrder,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ListReminders(
    orderLabel: String?,
    searchQuery: String,
    selectedFacilityLabel: String?,
    selectedTelescopeLabel: String?,
    showStarSystems: Boolean,
    systemOrderLabel: String?
) {
    val reminderParts = listOfNotNull(
        orderLabel?.takeUnless { showStarSystems }
            ?.let { stringResource(R.string.label_ordered_by, it) },
        systemOrderLabel?.takeIf { showStarSystems }
            ?.let { stringResource(R.string.label_ordered_by, it) },
        searchQuery.takeIf { it.isNotBlank() }?.let { stringResource(R.string.label_search_query, it) },
        selectedFacilityLabel,
        selectedTelescopeLabel
    )
    if (reminderParts.isNotEmpty()) {
        Text(
            text = reminderParts.joinToString(" · "),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.secondary
        )
    }
}

@Composable
private fun PlanetList(
    exoplanetsList: List<Exoplanet>,
    systems: List<StarSystem>,
    selectedOrder: Int,
    summary: CatalogSummary,
    showStarSystems: Boolean,
    selectedSystemOrder: Int,
    invertedSystemOrder: Boolean,
    selectedPlanet: Exoplanet? = null,
    onPlanetSelected: ((Exoplanet) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (showStarSystems) {
            items(items = systems, key = { it.name }) { system ->
                StarSystemElement(
                    system = system,
                    selectedOrder = selectedOrder,
                    summary = summary,
                    selectedSystemOrder = selectedSystemOrder,
                    selectedPlanet = selectedPlanet,
                    onPlanetSelected = onPlanetSelected
                )
            }
        } else {
            items(items = exoplanetsList, key = { it.name }) { exoplanet ->
                ExoplanetElement(
                    exoplanet = exoplanet,
                    selectedOrder = selectedOrder,
                    summary = summary,
                    isSelected = selectedPlanet?.name == exoplanet.name,
                    onPlanetSelected = onPlanetSelected
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExoplanetElement(
    exoplanet: Exoplanet,
    isExpanded: Boolean = false,
    selectedOrder: Int = 0,
    summary: CatalogSummary = CatalogSummary.EMPTY,
    isSelected: Boolean = false,
    onPlanetSelected: ((Exoplanet) -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(isExpanded) }

    val icon = when (exoplanet.category) {
        0 -> R.drawable.mercurian
        1, 2, 3 -> R.drawable.rocky
        4 -> R.drawable.gasgiant
        5 -> R.drawable.jovian
        else -> R.drawable.unknown
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
        color = if (isSelected && onPlanetSelected != null) {
            MaterialTheme.colors.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colors.surface
        },
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = {
            if (onPlanetSelected != null) onPlanetSelected(exoplanet)
            else showDialog = !showDialog
        }
    ) {
        Column {
            Row(modifier = Modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = exoplanet.name, style = MaterialTheme.typography.h6)
                    AnimatedVisibility(
                        !showDialog || onPlanetSelected != null,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Text(text = stringResource(R.string.label_discoveredin, exoplanet.year), style = MaterialTheme.typography.caption)
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                ExoplanetOrderValue(exoplanet, selectedOrder)
                                if (selectedOrder == 0 || selectedOrder == 1 || selectedOrder == 2 || selectedOrder == 5) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(text = exoplanet.star, color = MaterialTheme.colors.secondary, modifier = Modifier.padding(horizontal = 8.dp), textAlign = TextAlign.End, maxLines = 1)
                            }
                        }

                    }
                }
            }
            AnimatedVisibility(showDialog && onPlanetSelected == null) {
                ExoplanetDialog(exoplanet = exoplanet, summary = summary)
            }
        }
    }
}

@Composable
private fun RowScope.ExoplanetOrderValue(exoplanet: Exoplanet, selectedOrder: Int) {
    val value = when (selectedOrder) {
        3 -> String.format("%.2f", exoplanet.radius) + planetRadiusComparison(exoplanet.radius).orEmpty()
        4 -> String.format("%.2f", exoplanet.mass) + planetMassComparison(exoplanet.mass).orEmpty()
        6 -> String.format("%.2f", exoplanet.distance)
        7 -> {
            val days = String.format("%.2f", exoplanet.period)
            val periodInYears = exoplanet.period / 365.25
            if (periodInYears >= 100) {
                val years = java.text.NumberFormat.getIntegerInstance().format(periodInYears)
                "$days ${stringResource(R.string.label_period_years_approx, years)}"
            } else {
                days
            }
        }
        8 -> String.format("%.2f", exoplanet.orbitdistance)
        else -> null
    }

    if (value != null) {
        Image(
            painter = painterResource(R.drawable.filter),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.caption,
            maxLines = 1
        )
    }
}

@Composable
fun planetRadiusComparison(radius: Double): String? {
    val jupiterRadius = 11.209
    if (radius < jupiterRadius * 0.75) return null

    val multiple = java.text.NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 1
    }.format(radius / jupiterRadius)
    return stringResource(
        R.string.label_planet_radius_multiple,
        multiple,
        stringResource(R.string.planet_jupiter)
    )
}

@Composable
fun planetMassComparison(mass: Double): String? {
    val jupiterMass = 317.8
    if (mass < jupiterMass * 0.75) return null

    val multiple = java.text.NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 1
    }.format(mass / jupiterMass)
    return stringResource(
        R.string.label_planet_mass_multiple,
        multiple,
        stringResource(R.string.planet_jupiter)
    )
}

fun rangePosition(value: Double, minimum: Double, maximum: Double): Float {
    if (maximum <= minimum) return 0f
    return ((value - minimum) / (maximum - minimum)).toFloat().coerceIn(0f, 1f)
}
