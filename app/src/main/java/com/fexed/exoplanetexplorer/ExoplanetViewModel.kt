package com.fexed.exoplanetexplorer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExoplanetUiState(
    val catalog: PlanetCatalog? = null,
    val visiblePlanets: List<Exoplanet> = emptyList(),
    val selectedOrder: Int = 0,
    val invertedOrder: Boolean = false,
    val searchQuery: String = "",
    val selectedFacility: String? = null,
    val selectedTelescope: String? = null,
    val showFilterDialog: Boolean = false,
    val showStatsDialog: Boolean = false,
    val isLoading: Boolean = true,
    val loadingError: CatalogLoadFailure? = null
)

class ExoplanetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlanetCatalogRepository(application)
    private val mutableUiState = MutableStateFlow(ExoplanetUiState())
    val uiState = mutableUiState.asStateFlow()

    private val mutableNewDataEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val newDataEvents = mutableNewDataEvents.asSharedFlow()

    init {
        refreshCatalog()
    }

    fun refreshCatalog() {
        mutableUiState.update { it.copy(isLoading = it.catalog == null, loadingError = null) }
        repository.load(
            onCatalogLoaded = { catalog ->
                mutableUiState.update { current ->
                    current.copy(
                        catalog = catalog,
                        isLoading = false,
                        loadingError = null,
                        visiblePlanets = filterAndSort(catalog, current)
                    )
                }
            },
            onNewData = { mutableNewDataEvents.tryEmit(Unit) },
            onError = { error ->
                mutableUiState.update { current ->
                    current.copy(isLoading = false, loadingError = error.takeIf { current.catalog == null })
                }
            }
        )
    }

    fun showFilterDialog(show: Boolean) {
        mutableUiState.update { it.copy(showFilterDialog = show) }
    }

    fun showStatsDialog(show: Boolean) {
        mutableUiState.update { it.copy(showStatsDialog = show) }
    }

    fun setFacility(facility: String?) {
        mutableUiState.update { it.copy(selectedFacility = facility) }
    }

    fun setTelescope(telescope: String?) {
        mutableUiState.update { it.copy(selectedTelescope = telescope) }
    }

    fun setOrder(order: Int) {
        mutableUiState.update { current ->
            current.copy(
                selectedOrder = order,
                visiblePlanets = current.catalog?.let { filterAndSort(it, current.copy(selectedOrder = order)) }
                    ?: current.visiblePlanets
            )
        }
    }

    fun setInvertedOrder(inverted: Boolean) {
        mutableUiState.update { current ->
            val updated = current.copy(invertedOrder = inverted)
            updated.copy(
                visiblePlanets = updated.catalog?.let { filterAndSort(it, updated) } ?: updated.visiblePlanets
            )
        }
    }

    fun applyFilters(
        query: String,
        facility: String?,
        telescope: String?,
        localizedCategoryNames: List<String>
    ) {
        this.localizedCategoryNames = localizedCategoryNames
        mutableUiState.update { current ->
            val updated = current.copy(
                searchQuery = query.trim(),
                selectedFacility = facility,
                selectedTelescope = telescope
            )
            updated.copy(
                visiblePlanets = updated.catalog?.let { filterAndSort(it, updated) } ?: updated.visiblePlanets,
                showFilterDialog = false
            )
        }
    }

    override fun onCleared() {
        repository.cancelPendingRequests()
        super.onCleared()
    }

    private var localizedCategoryNames = emptyList<String>()

    private fun filterAndSort(
        catalog: PlanetCatalog,
        state: ExoplanetUiState
    ): List<Exoplanet> {
        val query = state.searchQuery.trim().lowercase()
        val filtered = catalog.planets.filter { planet ->
            val matchesQuery = query.isEmpty() ||
                planet.name.lowercase().contains(query) ||
                planet.discoveryFacility.lowercase().contains(query) ||
                planet.discoveryTelescope.lowercase().contains(query) ||
                planet.star.lowercase().contains(query) ||
                localizedCategoryNames.getOrNull(planet.category)?.lowercase()?.contains(query) == true
            val matchesFacility = state.selectedFacility == null ||
                planet.discoveryFacility.trim() == state.selectedFacility
            val matchesTelescope = state.selectedTelescope == null ||
                planet.discoveryTelescope.trim() == state.selectedTelescope
            matchesQuery && matchesFacility && matchesTelescope
        }
        return sortPlanets(filtered, state.invertedOrder, state.selectedOrder)
    }
}
