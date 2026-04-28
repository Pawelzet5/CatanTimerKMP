# Date Formatting — Technical Debt

## Problem

Date/time logic is currently scattered across the codebase with no single source of truth:

| Location | Function | Approach |
|---|---|---|
| `core/util/DateFormatter.kt` | `formatEpochMillis` | `expect/actual` — locale-aware short date |
| `presentation/gameslist/GameListItem.kt` | `formatDuration` | Manual arithmetic over `kotlin.time.Duration` |
| `presentation/playerdetails/PlayerDetailsScreen.kt` | `formatEpochMillisToMonthYear` | Manual Gregorian calendar algorithm |

## Goal

Consolidate all date-related utilities into `core/util/DateUtils.kt` in `commonMain`.
Replace every ad-hoc implementation with idiomatic `kotlinx-datetime` equivalents.
Keep `expect/actual` only for locale-aware display formatting where the platform must decide.

## Prerequisite

Add `kotlinx-datetime` to the project:

```toml
# gradle/libs.versions.toml
kotlinx-datetime = "0.6.x"
kotlinx-datetime = { group = "org.jetbrains.kotlinx", name = "kotlinx-datetime", version.ref = "kotlinx-datetime" }
```

```kotlin
// composeApp/build.gradle.kts
commonMain.dependencies {
    implementation(libs.kotlinx.datetime)
}
```
