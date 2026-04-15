# PROJECT_OVERRIDES.md

> Reguły w tej sekcji mają pierwszeństwo przed odpowiadającymi im regułami w CLAUDE.md.

---

## Struktura warstwy Presentation

Zastępuje strukturę katalogów zdefiniowaną w sekcji **Architecture** w CLAUDE.md.

Warstwa `presentation/` jest organizowana **feature-first** (per widok), nie per typ artefaktu.

```
presentation/
├── <screen>/                       # Jeden pakiet na widok
│   ├── <Screen>Screen.kt           # Composable — główny plik widoku
│   ├── <Screen>ViewModel.kt        # ViewModel dla tego widoku
│   ├── <Screen>UiState.kt          # Stan UI i sealed classes eventów
│   └── <ScreenSpecificWidget>.kt   # Composable używany tylko w tym widoku,
│                                   # a w kilku miejscach w ramach Screen.kt
├── components/                     # Komponenty współdzielone między wieloma widokami
│   └── DiceStatisticsChart.kt      # np. feature-level shared composables
└── ...
```

### Zasady lokowania komponentów Compose

| Zasięg użycia | Lokalizacja |
|---|---|
| Używany tylko w jednym pliku `*Screen.kt` | Prywatna funkcja `@Composable` w tym samym pliku |
| Używany w kilku plikach w ramach jednego `<screen>/` | Osobny plik `.kt` w tym samym pakiecie `<screen>/` |
| Współdzielony między wieloma widokami (feature-level) | `presentation/components/` |
| Bazowy komponent UI (Button, Text, LoadingAnimation) reużywalny poza feature | `core/designsystem/components/` |

---

## Konwencja nazewnictwa testów jednostkowych

Zastępuje brak zdefiniowanej konwencji nazewnictwa testów w CLAUDE.md.

Testy jednostkowe (ViewModels, UseCases, Repositories) nazywamy według schematu:

```
`<Action>, <Prerequisites>, <Effect>`
```

### Przykłady

```kotlin
fun `Timer start, game not started, timer begins from zero`()
fun `Turn end, one player remaining, game over state emitted`()
fun `Dice roll, loaded dice disabled, result is within valid range`()
```

### Zasady

- Trzy człony oddzielone przecinkiem i spacją
- Forma rzeczownikowa lub bezokolicznikowa — nie pytania, nie `should`
- Nazwa musi być zrozumiała bez czytania ciała testu
- Konwencja obowiązuje testy jednostkowe; konwencja dla testów UI zostanie zdefiniowana osobno gdy zostaną wprowadzone do projektu
