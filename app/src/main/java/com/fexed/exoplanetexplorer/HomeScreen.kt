package com.fexed.exoplanetexplorer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ExoplanetHomeScreen(
    state: ExoplanetUiState,
    scaffoldState: ScaffoldState,
    onOpenFilters: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenInfo: () -> Unit,
    onCloseFilters: () -> Unit,
    onCloseStats: () -> Unit,
    onOrderSelected: (Int) -> Unit,
    onDirectionChanged: (Boolean) -> Unit,
    onFiltersApplied: (String, String?, String?, List<String>) -> Unit,
    onFacilitySelected: (String?) -> Unit,
    onTelescopeSelected: (String?) -> Unit
) {
    val catalog = state.catalog
    if (catalog == null) {
        StandardScaffold(scaffoldState, {}, {}) {
            val message = state.loadingError?.let { error ->
                stringResource(
                    if (error.isParsingFailure) R.string.error_duringparsing else R.string.error_duringdownload,
                    error.message
                )
            } ?: stringResource(R.string.info_downloading)
            Loading(state.isLoading, message)
        }
        return
    }

    val orderOptions = getOrderOptions(state.invertedOrder)
    val context = LocalContext.current
    val categoryNames = remember(context) {
        (0..5).map { getCategoryLocalizedName(context, it) }
    }
    if (state.showFilterDialog) {
        FilterDialog(
            selectedOrder = state.selectedOrder,
            invertedOrder = state.invertedOrder,
            orderOptions = orderOptions,
            searchQuery = state.searchQuery,
            facilityOptions = catalog.facilities,
            telescopeOptions = catalog.telescopes,
            facilityCounts = catalog.planetsPerFacility,
            telescopeCounts = catalog.planetsPerTelescope,
            selectedFacility = state.selectedFacility,
            selectedTelescope = state.selectedTelescope,
            onOrderSelected = onOrderSelected,
            onInvertedOrderChanged = onDirectionChanged,
            onFiltersApplied = { query ->
                onFiltersApplied(
                    query,
                    state.selectedFacility,
                    state.selectedTelescope,
                    categoryNames
                )
            },
            onFacilitySelected = onFacilitySelected,
            onTelescopeSelected = onTelescopeSelected,
            onClose = onCloseFilters
        )
    }
    if (state.showStatsDialog) {
        PlotDialog(
            facilityCounts = catalog.planetsPerFacility,
            summary = catalog.summary,
            onClose = onCloseStats
        )
    }

    StandardScaffold(
        scaffoldState = scaffoldState,
        fabAction = {
            FloatingActionButton(
                modifier = Modifier.systemBarsPadding(),
                shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 25)),
                onClick = onOpenFilters
            ) {
                Image(painter = painterResource(R.drawable.filter), contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onOpenStats) {
                Image(
                    painter = painterResource(R.drawable.plots),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
            IconButton(onClick = onOpenInfo) {
                Image(
                    painter = painterResource(R.drawable.info),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    ) { _ ->
        ShowExoplanets(
            exoplanetsList = state.visiblePlanets,
            selectedOrder = state.selectedOrder,
            orderLabel = orderOptions.getOrNull(state.selectedOrder)?.takeIf { state.selectedOrder != 0 },
            searchQuery = state.searchQuery,
            selectedFacilityLabel = state.selectedFacility?.let {
                stringResource(R.string.label_filter_facility, it)
            },
            selectedTelescopeLabel = state.selectedTelescope?.let {
                stringResource(R.string.label_filter_telescope, it)
            },
            summary = catalog.summary
        )
    }
}
