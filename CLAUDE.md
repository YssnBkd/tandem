# tandem Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-12-31

## Active Technologies
- Kotlin 2.3.0 (Kotlin Multiplatform) + SQLDelight 2.0+, Kotlin Coroutines Flow, kotlinx.datetime (002-task-data-layer)
- SQLDelight (local), offline-first (no sync in this feature) (002-task-data-layer)
- Kotlin 2.1+ (Kotlin Multiplatform) + Compose Multiplatform, Koin, SQLDelight, DataStore, kotlinx.datetime (003-week-view)
- SQLDelight (via Feature 002), DataStore (segment preference persistence) (003-week-view)
- Kotlin 2.1+ (Kotlin Multiplatform) + Compose Multiplatform, Koin, SQLDelight, DataStore, Jetpack Navigation Compose, kotlinx.datetime (004-week-planning)
- SQLDelight (via Feature 002 repositories), DataStore (planning progress persistence), offline-firs (004-week-planning)
- SQLDelight (via Feature 002 repositories), DataStore (review progress persistence), offline-firs (005-week-review)
- Kotlin 2.1+ (Kotlin Multiplatform) + Compose Multiplatform, Koin, SQLDelight, DataStore, Supabase Android SDK (Realtime) (006-partner-system)
- SQLDelight (local cache), Supabase (remote partnership/invite data), offline-first with sync queue (006-partner-system)
- SQLDelight (local), offline-first (sync via existing partner infrastructure for shared goals) (007-goals-system)

- Kotlin 2.3.0 (Kotlin Multiplatform) (001-core-infrastructure)

## Project Structure

```text
src/
tests/
```

## Commands

# Add commands for Kotlin 2.3.0 (Kotlin Multiplatform)

## Code Style

Kotlin 2.3.0 (Kotlin Multiplatform): Follow standard conventions

## Recent Changes
- 007-goals-system: Added Kotlin 2.1+ (Kotlin Multiplatform) + Compose Multiplatform, Koin, SQLDelight, DataStore, kotlinx.datetime
- 006-partner-system: Added Kotlin 2.1+ (Kotlin Multiplatform) + Compose Multiplatform, Koin, SQLDelight, DataStore, Supabase Android SDK (Realtime)
- 005-week-review: Added Kotlin 2.1+ (Kotlin Multiplatform) + Compose Multiplatform, Koin, SQLDelight, DataStore, Jetpack Navigation Compose, kotlinx.datetime


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
