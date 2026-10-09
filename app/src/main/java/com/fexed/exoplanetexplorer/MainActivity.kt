package com.fexed.exoplanetexplorer

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.fexed.exoplanetexplorer.ui.theme.ExoplanetExplorerTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = resources.getColor(R.color.black, theme),
                darkScrim = resources.getColor(R.color.white, theme),
                detectDarkMode = { resources ->
                    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                        Configuration.UI_MODE_NIGHT_YES
                }
            )
        )
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[ExoplanetViewModel::class.java]
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val scaffoldState = rememberScaffoldState()
            LaunchedEffect(viewModel) {
                viewModel.newDataEvents.collect {
                    Toast.makeText(this@MainActivity, R.string.new_data_available, Toast.LENGTH_LONG).show()
                }
            }
            ExoplanetExplorerTheme {
                ExoplanetHomeScreen(
                    state = state,
                    scaffoldState = scaffoldState,
                    onOpenFilters = { viewModel.showFilterDialog(true) },
                    onOpenStats = { viewModel.showStatsDialog(true) },
                    onOpenInfo = {
                        startActivity(Intent(this@MainActivity, InfoActivity::class.java))
                    },
                    onCloseFilters = { viewModel.showFilterDialog(false) },
                    onCloseStats = { viewModel.showStatsDialog(false) },
                    onOrderSelected = viewModel::setOrder,
                    onDirectionChanged = viewModel::setInvertedOrder,
                    onFiltersApplied = viewModel::applyFilters,
                    onFacilitySelected = viewModel::setFacility,
                    onTelescopeSelected = viewModel::setTelescope
                )
            }
        }
    }
}
