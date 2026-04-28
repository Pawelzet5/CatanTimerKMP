Catan Timer

A turn timer and game tracker for Catan and Cities & Knights, built with Kotlin Multiplatform and Compose Multiplatform — running natively on Android, iOS, and Desktop.

Features

- Turn timer — configurable per-game turn duration with haptic feedback when time runs out
- Cities & Knights support — tracks the barbarian track, event die, and city gate phases per turn
- Dice distribution — live histogram of dice rolls across the game
- Game history — browse past games, review per-turn statistics and dice distributions
- Player profiles — track win/loss records and turn-time averages per player
- Winner selection — end-of-game flow with score entry

Screenshots

Tech stack

┌──────────────┬───────────────────────────────────────┐                                                                                                                                                        
│    Layer     │              Technology               │
├──────────────┼───────────────────────────────────────┤                                                                                                                                                        
│ UI           │ Compose Multiplatform                 │  
├──────────────┼───────────────────────────────────────┤
│ Architecture │ MVI (State / Action / Event)          │
├──────────────┼───────────────────────────────────────┤                                                                                                                                                        
│ Navigation   │ Navigation Compose (type-safe routes) │
├──────────────┼───────────────────────────────────────┤                                                                                                                                                        
│ DI           │ Koin                                  │  
├──────────────┼───────────────────────────────────────┤                                                                                                                                                        
│ Local DB     │ Room (KMP)                            │
├──────────────┼───────────────────────────────────────┤                                                                                                                                                        
│ Async        │ Kotlin Coroutines + Flow              │  
├──────────────┼───────────────────────────────────────┤                                                                                                                                                        
│ Testing      │ kotlin.test, Turbine, Koin test       │
└──────────────┴───────────────────────────────────────┘

Architecture

The app follows a strict layered architecture:

presentation/   ← Composables, ViewModels, State/Action/Event                                                                                                                                                   
domain/         ← Use cases, repository interfaces, domain models                                                                                                                                               
data/           ← Room DAOs, entities, repository implementations                                                                                                                                               
core/           ← Shared types: Result, DataError, UiText, design system

GameSessionCoordinator is the central domain object — it manages in-memory game session state across turns and is the single source of truth for the active game, keeping ViewModels thin.

Building

# Android
./gradlew :composeApp:assembleDebug

# Desktop
./gradlew :composeApp:run

# iOS — open iosApp/iosApp.xcodeproj in Xcode

Running tests

./gradlew :composeApp:cleanAllTests :composeApp:allTests                                                              