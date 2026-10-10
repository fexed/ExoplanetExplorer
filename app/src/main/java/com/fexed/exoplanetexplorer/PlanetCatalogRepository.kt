package com.fexed.exoplanetexplorer

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.FileNotFoundException

class PlanetCatalogRepository(context: Context) {
    private val appContext = context.applicationContext
    private val requestQueue: RequestQueue = Volley.newRequestQueue(appContext)
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private var cachedCount: Int? = null

    fun load(
        onCatalogLoaded: (PlanetCatalog) -> Unit,
        onNewData: () -> Unit,
        onError: (CatalogLoadFailure) -> Unit
    ) {
        loadCachedCatalog(onCatalogLoaded)

        val request = StringRequest(
            Request.Method.GET,
            DATA_ENDPOINT_URL,
            { response ->
                try {
                    val catalog = parseCatalog(response)
                    cacheResponse(response)
                    if (shouldNotifyAboutUpdate(catalog.planets.size)) onNewData()
                    onCatalogLoaded(catalog)
                } catch (error: Exception) {
                    Log.e(TAG, "Unable to parse the exoplanet archive response", error)
                    onError(CatalogLoadFailure(error.toString(), isParsingFailure = true))
                }
            },
            { error ->
                Log.e(TAG, "Unable to download the exoplanet catalog", error)
                onError(CatalogLoadFailure(error.toString(), isParsingFailure = false))
            }
        ).apply {
            retryPolicy = DefaultRetryPolicy(
                10000,
                20,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        }
        requestQueue.add(request)
    }

    fun cancelPendingRequests() {
        requestQueue.cancelAll { true }
    }

    private fun loadCachedCatalog(onCatalogLoaded: (PlanetCatalog) -> Unit) {
        try {
            val cachedResponse = appContext.openFileInput(CACHE_FILE)
                .bufferedReader()
                .use { it.readText() }
            val catalog = parseCatalog(cachedResponse)
            cachedCount = catalog.planets.size
            onCatalogLoaded(catalog)
        } catch (_: FileNotFoundException) {
            // A missing cache is expected on first launch.
        } catch (error: Exception) {
            Log.e(TAG, "Unable to read the cached exoplanet catalog", error)
        }
    }

    private fun cacheResponse(response: String) {
        try {
            appContext.openFileOutput(CACHE_FILE, Context.MODE_PRIVATE)
                .bufferedWriter()
                .use { it.write(response) }
        } catch (error: Exception) {
            Log.e(TAG, "Unable to cache the exoplanet catalog", error)
        }
    }

    private fun shouldNotifyAboutUpdate(newCount: Int): Boolean {
        val hasNotifiedBefore = preferences.contains(LAST_UPDATE_KEY)
        val hasCatalogChanged = cachedCount != null && cachedCount != newCount
        if (!hasNotifiedBefore || hasCatalogChanged) {
            preferences.edit()
                .putString(LAST_UPDATE_KEY, (System.currentTimeMillis() / 1000).toString())
                .apply()
            cachedCount = newCount
            return true
        }
        cachedCount = newCount
        return false
    }

    private fun parseCatalog(response: String): PlanetCatalog = PlanetCatalogParser.parse(response)

    private companion object {
        const val TAG = "PlanetCatalog"
        const val CACHE_FILE = "cachedExoplanetsDatabase"
        const val LAST_UPDATE_KEY = "last_update1"
        const val PREFERENCES_NAME = "MainActivity_preferences"

        val DATA_ENDPOINT_URL = "https://exoplanetarchive.ipac.caltech.edu/TAP/sync?query=" +
            "select+" +
            "pl_name," +
            "hostname," +
            "disc_year," +
            "pl_orbper,pl_orbpererr1,pl_orbpererr2," +
            "pl_rade,pl_radeerr1,pl_radeerr2," +
            "pl_bmasse,pl_bmasseerr1,pl_bmasseerr2," +
            "sy_dist,sy_disterr1,sy_disterr2," +
            "pl_orbsmax,pl_orbsmaxerr1,pl_orbsmaxerr2," +
            "st_rad,st_mass,ra,dec," +
            "disc_facility,disc_telescope," +
            "pl_controv_flag" +
            "+from+pscomppars&format=csv"
    }
}
