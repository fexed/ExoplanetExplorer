# Changelog

## 3.0 — 2026-10-10

Changes since the previous version tag, `2.2`:

### Added

- Browse exoplanets by host-star system, expand systems to see their planets, and sort systems by name, planet count, distance, stellar radius, or stellar mass.
- Show host-star radius, mass, and sky coordinates in expanded system details.
- Color star icons using approximate stellar temperature or spectral type, with icon size scaled to stellar radius.
- Add a landscape tablet list-and-detail layout while preserving the phone layout.
- Add discovery-facility rankings and largest-system information to catalog statistics.
- Add a nightly GitHub Actions workflow to build and publish an APK when the source changes.
- Document app features, architecture, catalog data, persistence, and build steps in the README.

### Improved

- Refactor catalog loading, CSV parsing, filtering, sorting, and UI state into dedicated components.
- Expand planet search and filtering, and keep active search and sort criteria visible in the list.
- Add planet and system sorting options and clearer comparisons in planet details.
- Update Android SDK and build configuration for this release.

### Fixed

- Correct an Italian planet-category label.
- Preserve compatibility with cached catalog responses that do not contain newer optional stellar columns.

### Build note

The nightly workflow currently publishes an unsigned release APK. A release signing configuration is not included.
