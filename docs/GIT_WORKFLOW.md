# CatanCompanion — Git Workflow Rules

Reguły obowiązujące we wszystkich sesjach implementacji.

---

## Branch naming

Format: `session-{N}/{short-description}`

Przykłady:
- `session-1/domain-enums`
- `session-3a/entities-and-daos`
- `session-9b/gameplay-viewmodel`

---

## Branch parent

Każdy branch musi wywodzić się z bezpośredniego poprzednika sesji — **nie z `master`** — na wypadek gdy poprzednie PR-y nie zostały jeszcze zmergowane.

Kolejność tworzenia branchy w ramach sesji (jeśli sesja ma wiele PR-ów):

```
master
 └── session-1/domain-enums          ← pierwszy branch sesji 1
      └── session-1/domain-models     ← kolejny branch sesji 1
           └── session-2/...          ← pierwszy branch sesji 2
```

Zawsze checkoutuj z ostatniego brancha poprzedniej sesji, niezależnie od tego czy poprzednia sesja jest już zmergowana do `master` czy nie.

---

## Pull Request base branch

Każdy PR musi być otwarty z base branch ustawionym na **bezpośredni branch poprzednika** (nie `master`).

```
PR #1: base = master
PR #2: base = session-1/domain-models   ← ostatni branch sesji 1
PR #3: base = session-2/...             ← ostatni branch sesji 2
```

GitHub automatycznie przekieruje base branch PR-a na `master` w momencie gdy branch docelowy zostanie zmergowany i usunięty. Diff PR-a zostanie wówczas zaktualizowany i będzie pokazywał tylko zmiany wprowadzone w danym PR.

**Nigdy nie ustawiaj base branch na `master` ręcznie** (z wyjątkiem PR #1). GitHub zrobi to automatycznie.

---

## Commit messages

Format: `type(scope): description`

Typy:
- `feat` — nowy kod produkcyjny
- `refactor` — zmiana istniejącego kodu bez zmiany zachowania
- `test` — dodanie lub zmiana testów
- `fix` — poprawka błędu
- `chore` — zmiany niezwiązane z kodem produkcyjnym (np. migracje, zasoby)

Przykłady:
- `feat(domain): add Player and Game domain models`
- `feat(data): implement PlayerRepositoryImpl with Flow support`
- `refactor(session): move GameSession to domain/session package`
- `test(usecase): add CreateGameUseCase validation tests`
- `chore(i18n): add Polish string resources`

Zasady:
- Opis w języku angielskim
- Lowercase po dwukropku
- Bez kropki na końcu
- Maksymalnie 72 znaki w pierwszej linii

---

## Pull Request

### Rozmiar
- Optymalnie 5 plików w zmianach
- Maksymalnie 10 plików
- Jeśli sesja wymaga więcej — dziel na osobne PR-y zgodnie z planem sesji

### Tytuł
Format: `[Session N] Short description`

Przykłady:
- `[Session 1a] Domain enums`
- `[Session 3b] Room entities and DAOs`
- `[Session 9a] Dashboard and GameConfig ViewModels`

### Opis
Każdy PR powinien zawierać:

```
## What
Krótki opis co zostało dodane/zmienione.

## Files
Lista plików z jednozdaniowym opisem każdego.

## Notes
Opcjonalnie: decyzje, kompromisy, rzeczy do sprawdzenia przez reviewera.
```

### Przed otwarciem PR
- [ ] Projekt się kompiluje
- [ ] Testy przechodzą (jeśli sesja zawiera testy)
- [ ] Brak `!!`, `var` gdzie możliwy `val`, `println`/`Log.d` w kodzie produkcyjnym
- [ ] Kod zgodny z regułami `CLAUDE.md`