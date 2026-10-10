# Exoplanets Explorer

Exoplanets Explorer is an Android app for browsing confirmed exoplanets and the star systems that host them. It retrieves catalog data from the NASA Exoplanet Archive, and presents searchable, sortable planet and system lists, individual planet details, and catalog statistics.

## Features

- Browse confirmed exoplanets or group them by host star.
- Search by planet, host star, discovery facility, telescope, or localized planet category.
- Filter by discovery facility and telescope.
- Sort planets by their available properties and star systems by name, planet count, distance, stellar radius, or stellar mass.
- Expand a star system to see its planets and available host-star radius, mass, and coordinates.
- View planet details, catalog statistics, and discovery-facility rankings.
- Star-system icons use approximate temperature/spectral-type colors and scale with the reported stellar radius. The color mapping is credited in the in-app Infos screen.

## Data source

The app requests confirmed-planet records from the NASA Exoplanet Archive's `pscomppars` table using its [TAP service](https://exoplanetarchive.ipac.caltech.edu/docs/TAP/usingTAP.html). The response is CSV. The app excludes records marked as controversial and parses planet, discovery, distance, orbit, and host-star properties.

The archive returns a host-star record alongside each planet, so stellar properties can be repeated for planets in the same system. The app groups planets by `hostname` and uses a member planet's available host-star values when showing system-level information. Missing measurements are represented as unknown rather than inferred.

Distances are converted from parsecs to light years when parsed. Planet radius and mass are represented in Earth units, stellar radius and mass in solar units, orbital distance in astronomical units, and stellar effective temperature in kelvin.

## Architecture

The app is implemented in Kotlin with Jetpack Compose and Material components.

```text
NASA Exoplanet Archive (TAP/CSV)
                 |
                 v
PlanetCatalogRepository ---- private CSV cache
                 |
                 v
        PlanetCatalogParser
                 |
                 v
         ExoplanetViewModel
       (state, filters, sorting)
                 |
                 v
 Compose screens and dialogs
```

- **`PlanetCatalogRepository`** loads the locally cached response first, requests the latest catalog with Volley, parses and caches successful responses, and reports load failures.
- **`PlanetCatalogParser`** maps CSV rows to app models, handles missing optional values, converts distance units, filters controversial records, and builds catalog summaries and discovery counts.
- **`Exoplanet.kt` and `PlanetCatalog.kt`** define the planet, catalog, load-failure, and summary models.
- **`ExoplanetViewModel`** exposes UI state with Kotlin `StateFlow` and applies search, facility/telescope filters, and planet ordering.
- **`PlanetSorting.kt` and `StellarClassification.kt`** contain star-system grouping and ordering, plus the temperature/spectral-type star color categories and radius-based icon sizing.
- **Compose UI** is organized across `HomeScreen.kt`, `PlanetList.kt`, `PlanetDetails.kt`, `FilterDialog.kt`, `StatsDialog.kt`, and `CommonComponents.kt`.
- **Resources** under `app/src/main/res` contain localized strings, icons, and themes.

## Persistence and data schema

The app does **not** use a relational database, SQLite, or Room. The local catalog cache is the raw CSV response stored in the app's private files directory as `cachedExoplanetsDatabase`. It is parsed on load, so the CSV header is the cache's effective schema.

The queried archive columns include:

| Group | Archive columns |
| --- | --- |
| Planet identity and discovery | `pl_name`, `hostname`, `disc_year`, `disc_facility`, `disc_telescope`, `pl_controv_flag` |
| Orbit | `pl_orbper`, `pl_orbpererr1`, `pl_orbpererr2`, `pl_orbsmax`, `pl_orbsmaxerr1`, `pl_orbsmaxerr2` |
| Planet size and mass | `pl_rade`, `pl_radeerr1`, `pl_radeerr2`, `pl_bmasse`, `pl_bmasseerr1`, `pl_bmasseerr2` |
| Host-star and distance | `sy_dist`, `sy_disterr1`, `sy_disterr2`, `st_rad`, `st_mass`, `st_teff`, `st_spectype`, `ra`, `dec` |

The parsed in-memory catalog consists of:

- **`Exoplanet`**: one planet record, including the host-star values included in its archive row.
- **`PlanetCatalog`**: the planet list, distinct facility and telescope options, counts by facility/telescope, and a `CatalogSummary`.
- **`CatalogSummary`**: planet extrema, counts by planet category, and counts by discovery year.

Shared preferences named `MainActivity_preferences` store the `last_update1` timestamp used for the app's new-data notification. They do not store the catalog itself.

Older cached CSVs that lack newer optional host-star columns remain supported by the parser.

## Build and test

Requirements: Android Studio/Android SDK platform 36 and a JDK supported by the project's Gradle wrapper and Android Gradle Plugin.

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

On macOS or Linux:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/`.

## Project layout

```text
app/src/main/java/com/fexed/exoplanetexplorer/  Kotlin app code
app/src/main/res/                               Android resources and translations
app/src/test/java/                              JVM unit tests
gradle/                                         Gradle wrapper configuration
```
