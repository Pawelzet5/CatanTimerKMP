# CatanCompanion — Design Specification

**Version:** 1.0  
**Last Updated:** 2026-04-12  
**Status:** Draft

---

## 1. Overview

### 1.1 Product Description

CatanCompanion is a feature module within the CatanTimer application. It serves as a digital companion for Catan board game sessions, providing turn timing, dice roll tracking, and game statistics.

### 1.2 Target Platforms

- Android (phones and tablets)
- iOS (phones and tablets)
- Desktop (JVM) — included for architectural consistency

### 1.3 Supported Game Modes

| Mode | Description |
|------|-------------|
| Base Game | 3–4 players, standard rules |
| 5–6 Player Expansion | Enables "in-between turns" for the third player after the active player |
| Seafarers (SEAFARERS) | Adds pirate option when rolling 7 |
| Cities & Knights (CITIES_AND_KNIGHTS) | Adds Event Dice, barbarian track mechanics |

Expansions can be combined (e.g., Seafarers + Cities & Knights + 5–6 players).

---

## 2. User Flows

### 2.1 Application Entry Point

```
┌─────────────────────────────────────────┐
│              DASHBOARD                  │
├─────────────────────────────────────────┤
│  ┌─────────────────────────────────┐    │
│  │  ▶ Resume Last Game             │    │  ← Visible only if in-progress game exists
│  │    "Game with Alice, Bob..."    │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │  NEW    │ │  GAMES  │ │ PLAYERS │   │
│  │  GAME   │ │  LIST   │ │  LIST   │   │
│  └─────────┘ └─────────┘ └─────────┘   │
└─────────────────────────────────────────┘
```

**Actions:**
- **Resume Last Game** — navigates directly to active gameplay (visible only when an in-progress game exists)
- **New Game** — navigates to Game Configuration
- **Games List** — navigates to list of all games (in-progress and completed)
- **Players List** — navigates to global player management

### 2.2 Game Configuration Flow

```
┌──────────────────────────────────────────────────────────────┐
│                   GAME CONFIGURATION                         │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  TURN DURATION                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ◀  02:00  ▶                                         │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  NUMBER OF PLAYERS                                           │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐                           │
│  │  3  │ │  4  │ │  5  │ │  6  │                           │
│  └─────┘ └─────┘ └─────┘ └─────┘                           │
│                                                              │
│  PLAYERS                               [+ Add Player]        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ≡  1. Alice                                    ✕    │   │
│  │  ≡  2. Bob                                      ✕    │   │
│  │  ≡  3. Charlie                                  ✕    │   │
│  └──────────────────────────────────────────────────────┘   │
│  (drag handles ≡ for reordering)                             │
│                                                              │
│  GAME OPTIONS                                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ☐  In-Between Turns (5-6 players)                   │   │
│  │  ☐  Seafarers                                        │   │
│  │  ☐  Cities & Knights                                 │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│              ┌─────────────────────┐                        │
│              │     START GAME      │                        │
│              └─────────────────────┘                        │
└──────────────────────────────────────────────────────────────┘
```

**Validation Rules:**
- Minimum 3 players, maximum 6 players
- Player count must match selected number
- No duplicate players allowed
- "In-Between Turns" option only available when 5 or 6 players selected

**Player Selection:**
- Tapping [+ Add Player] navigates to Players List with selection mode enabled
- Selected players appear in ordered list
- Drag & drop to reorder players
- X button removes player from game (not from global list)
- If player count is reduced, list is truncated (keeps first N players)

### 2.3 Active Gameplay Flow

The active gameplay consists of sequential phases within each turn:

```
┌─────────────────────────────────────────────────────────────┐
│  TURN FLOW                                                  │
│                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │   DICE      │───▶│   EVENT     │───▶│   MAIN      │     │
│  │  SELECTION  │    │  (optional) │    │   TIMER     │     │
│  └─────────────┘    └─────────────┘    └─────────────┘     │
│                                               │             │
│                                               ▼             │
│                     ┌─────────────────────────────────┐     │
│                     │  IN-BETWEEN TIMER (if enabled)  │     │
│                     └─────────────────────────────────┘     │
│                                               │             │
│                                               ▼             │
│                                        NEXT TURN            │
└─────────────────────────────────────────────────────────────┘
```

#### 2.3.1 Dice Selection Phase

```
┌──────────────────────────────────────────────────────────────┐
│  Turn 5                              Alice's Turn            │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  RED DICE                                                    │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐                 │
│  │ ⚀  │ │ ⚁  │ │ ⚂  │ │ ⚃  │ │ ⚄  │ │ ⚅  │                 │
│  └────┘ └────┘ └────┘ └────┘ └────┘ └────┘                 │
│                                                              │
│  YELLOW DICE                                                 │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐                 │
│  │ ⚀  │ │ ⚁  │ │ ⚂  │ │ ⚃  │ │ ⚄  │ │ ⚅  │                 │
│  └────┘ └────┘ └────┘ └────┘ └────┘ └────┘                 │
│                                                              │
│  EVENT DICE (Cities & Knights only)                         │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐                               │
│  │ ⚔️  │ │ 🔬 │ │ 💰 │ │ 🚢 │                               │
│  └────┘ └────┘ └────┘ └────┘                               │
│                                                              │
│              ┌─────────────────────┐                        │
│              │       CONTINUE      │                        │
│              └─────────────────────┘                        │
└──────────────────────────────────────────────────────────────┘
```

**Dice Visualization:**
- Red dice: red background, yellow dots
- Yellow dice: yellow background, red dots  
- Event dice: white background, black icons
- Selected state: green border
- Single selection per row (selecting new value deselects previous)

**Recommended Implementation:** Canvas-based rendering for dot positions (see Implementation Spec for details).

#### 2.3.2 Event Phase (Conditional)

This phase appears after dice selection when specific conditions are met:

**Condition A: Roll equals 7**

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│                      [Thief Image]                           │
│                                                              │
│         All players must count their cards.                  │
│         If you have more than 7, discard half.               │
│                                                              │
│         Move the thief to a different tile.                  │
│         (or pirate — Seafarers)                              │   ← Seafarers variant
│                                                              │
│              ┌─────────────────────┐                        │
│              │       CONTINUE      │                        │
│              └─────────────────────┘                        │
└──────────────────────────────────────────────────────────────┘
```

**Condition B: Roll equals 7 + Cities & Knights + Before First Raid**

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│                      [Thief Image]                           │
│                                                              │
│         All players must count their cards.                  │
│         If you have more than 7, discard half.               │
│                                                              │
│         The thief cannot be moved yet.                       │
│         (First barbarian raid has not occurred)              │
│                                                              │
│              ┌─────────────────────┐                        │
│              │       CONTINUE      │                        │
│              └─────────────────────┘                        │
└──────────────────────────────────────────────────────────────┘
```

**Condition C: Barbarians Reach the Island (Cities & Knights)**

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│                   [Barbarians Image]                         │
│                                                              │
│              The barbarians have arrived!                    │
│              Resolve the attack now.                         │
│                                                              │
│              ┌─────────────────────┐                        │
│              │       CONTINUE      │                        │
│              └─────────────────────┘                        │
└──────────────────────────────────────────────────────────────┘
```

#### 2.3.3 Timer Phase

```
┌──────────────────────────────────────────────────────────────┐
│  Turn 5                              Alice's Turn            │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│                                                              │
│                         01:47                                │
│                                                              │
│                                                              │
│         ┌────────┐  ┌────────┐  ┌────────┐                  │
│         │  ▶/⏸  │  │  +10   │  │   ↺    │                  │
│         │ Start/ │  │  sec   │  │ Reset  │                  │
│         │  Stop  │  │        │  │        │                  │
│         └────────┘  └────────┘  └────────┘                  │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  NEXT TURN                                          │    │  ← Without in-between
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  IN-BETWEEN TURN (Charlie)                          │    │  ← With in-between (5-6p)
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│                                                 🚢 3/8       │  ← Barbarian tracker
└──────────────────────────────────────────────────────────────┘
```

**Timer Controls:**
- **Start/Stop** — toggles countdown
- **+10** — adds 10 seconds to current time
- **Reset** — resets timer to configured `turnDurationMillis`

**Timer Behavior:**
- Counts down from `turnDurationMillis` to 0
- Stops at 0 (does not go negative)
- Three short vibrations when reaching 0

**Barbarian Tracker (Cities & Knights only):**
- Subtle indicator in bottom corner
- Shows current position as "X/8" with ship icon
- Track positions: Start → 1 → 2 → 3 → 4 → 5 → 6 → Island
- Increments each time BARBARIANS is rolled on Event Dice
- Resets to Start after reaching Island

#### 2.3.4 In-Between Timer Phase (5-6 Players with Special Rule)

Identical layout to main timer, but:
- Header shows in-between player name (3rd player after active player)
- Only "NEXT TURN" button (no in-between option)
- Same timer duration as main timer

#### 2.3.5 Turn Navigation

```
┌──────────────────────────────────────────────────────────────┐
│  ←  Turn 3 of 5                         (viewing history)   │  →  ⏭
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  (Previous turn data displayed - editable)                   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

**Navigation Controls:**
| Button | Visibility | Action |
|--------|------------|--------|
| ← (Back) | Hidden on turn 1, visible otherwise | Go to previous turn |
| → (Forward) | Only when not on current turn | Go to next turn |
| ⏭ (Jump to Current) | Only when 2+ turns behind current | Jump directly to current turn |

**Edit Behavior:**
- Viewing historical turn allows editing dice selection
- Changes require explicit confirmation (Save/Cancel)
- Editing dice roll that changes sum to/from 7 recalculates barbarian position
- Editing BARBARIANS event recalculates barbarian position

### 2.4 In-Game Menu (Bottom Sheet)

Triggered by menu button in top app bar:

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  📊  View Statistics                                   │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  ⚙️   Change Game Settings                             │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  🏁  End Game                                          │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

#### 2.4.1 Statistics Popup

Floating popup at bottom of screen showing dice roll distribution:

```
┌──────────────────────────────────────────────────────────────┐
│  DICE STATISTICS                                        ✕   │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  STANDARD DICE (2-12)                                        │
│  ┌─────────────────────────────────────────────────────┐    │
│  │     ▄▄                                              │    │
│  │     ██ ▄▄                                           │    │
│  │  ▄▄ ██ ██ ▄▄                                        │    │
│  │  ██ ██ ██ ██ ▄▄    ▄▄                               │    │
│  │  ██ ██ ██ ██ ██ ▄▄ ██                               │    │
│  │  2  3  4  5  6  7  8  9  10 11 12                   │    │
│  │  1  3  5  7  4  2  6  2  3  1  0                    │    │  ← counts
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  EVENT DICE (Cities & Knights)                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │        ▄▄▄▄                                         │    │
│  │  ▄▄▄▄  ████                                         │    │
│  │  ████  ████  ▄▄▄▄  ▄▄▄▄                             │    │
│  │  ⚔️     🔬    💰    🚢                               │    │
│  │   5     8     3     4                               │    │  ← counts
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

- Bar chart with animated growth from 0
- Numeric count below each bar
- Event dice section only visible when Cities & Knights enabled

#### 2.4.2 Change Game Settings

```
┌──────────────────────────────────────────────────────────────┐
│  CHANGE GAME SETTINGS                                       │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ⚠️  WARNING                                                 │
│  Changing settings mid-game may cause data inconsistencies. │
│  Consider starting a new game instead.                       │
│                                                              │
│  GAME OPTIONS                                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ☑  In-Between Turns (5-6 players)                   │   │
│  │  ☐  Seafarers                                        │   │
│  │  ☑  Cities & Knights                                 │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌────────────────┐  ┌────────────────┐                     │
│  │     CANCEL     │  │  SAVE CHANGES  │                     │
│  └────────────────┘  └────────────────┘                     │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           START NEW GAME WITH THESE SETTINGS         │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### 2.5 End Game Flow

#### 2.5.1 Winner Selection

```
┌──────────────────────────────────────────────────────────────┐
│  SELECT WINNER                                              │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ○  Alice                                            │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  ●  Bob                                              │   │  ← selected
│  ├──────────────────────────────────────────────────────┤   │
│  │  ○  Charlie                                          │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  ○  No winner (game abandoned)                       │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│              ┌─────────────────────┐                        │
│              │       CONFIRM       │                        │
│              └─────────────────────┘                        │
└──────────────────────────────────────────────────────────────┘
```

#### 2.5.2 Game Summary

```
┌──────────────────────────────────────────────────────────────┐
│  GAME SUMMARY                                               │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  🏆 WINNER: Bob                                              │
│                                                              │
│  ⏱️  DURATION                                                │
│  Started: 14:30 • Ended: 16:45 • Total: 2h 15m              │
│                                                              │
│  📊 STATISTICS                                               │
│  Total turns: 47                                             │
│  Average turn time: 1m 42s                                   │
│                                                              │
│  👥 PLAYER STATS                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Alice    avg: 1m 38s                               │    │
│  │  Bob      avg: 1m 52s                               │    │
│  │  Charlie  avg: 1m 35s                               │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  🎲 DICE DISTRIBUTION                                        │
│  (same charts as statistics popup)                          │
│                                                              │
│              ┌─────────────────────┐                        │
│              │    BACK TO HOME     │                        │
│              └─────────────────────┘                        │
└──────────────────────────────────────────────────────────────┘
```

### 2.6 Players List

```
┌──────────────────────────────────────────────────────────────┐
│  PLAYERS                                        [+ Add]     │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Alice                                    12 games   │   │  → tap for details
│  ├──────────────────────────────────────────────────────┤   │
│  │  Bob                                       8 games   │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  Charlie                                  15 games   │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

**Selection Mode (when adding to game):**
- Checkboxes appear next to each player
- "Done" button confirms selection
- Already selected players shown as checked and disabled

### 2.7 Player Details

```
┌──────────────────────────────────────────────────────────────┐
│  ←  PLAYER DETAILS                              [Edit] [⋮]  │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Alice                                                       │
│                                                              │
│  📊 STATISTICS                                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Games played:  12                                  │    │
│  │  Games won:      5                                  │    │
│  │  Win rate:      42%                                 │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

**Menu (⋮) options:**
- Hide player (soft delete)
- Delete player (only if no games played)

### 2.8 Games List

```
┌──────────────────────────────────────────────────────────────┐
│  GAMES                                                      │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ── IN PROGRESS ──────────────────────────────────────────  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  🔵 Alice, Bob, Charlie              Apr 12, 14:30   │   │
│  │     Turn 23 • Cities & Knights                       │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ── COMPLETED ────────────────────────────────────────────  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ✓  Alice, Bob, Charlie, Dave        Apr 10, 19:00   │   │
│  │     Winner: Bob • 2h 15m                             │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  ✓  Alice, Charlie                   Apr 8, 20:30    │   │
│  │     Winner: Alice • 1h 45m                           │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

**Interactions:**
- Tap in-progress game → resume gameplay
- Tap completed game → view summary
- Swipe to delete (with confirmation)

---

## 3. Data Model (Conceptual)

### 3.1 Entities

```
Player
├── id: Long
├── name: String
├── isHidden: Boolean
└── (derived) gamesPlayed: Int
└── (derived) gamesWon: Int

Game
├── id: Long
├── turnDurationMillis: Long
├── expansions: Set<GameExpansion>
├── specialTurnRuleEnabled: Boolean
├── status: GameStatus (IN_PROGRESS, COMPLETED)
├── startedAt: Timestamp
├── finishedAt: Timestamp?
└── winnerId: Long?

GamePlayer (junction)
├── gameId: Long
├── playerId: Long
└── orderIndex: Int

Turn
├── id: Long
├── gameId: Long
├── number: Int
├── playerId: Long
├── secondaryPlayerId: Long?
├── redDice: Int?
├── yellowDice: Int?
├── eventDice: EventDiceType?
└── durationMillis: Long

Enums:
- GameExpansion: SEAFARERS, CITIES_AND_KNIGHTS
- GameStatus: IN_PROGRESS, COMPLETED
- EventDiceType: POLITICS, SCIENCE, TRADE, BARBARIANS
```

### 3.2 Derived Data

| Data | Derivation |
|------|------------|
| Player.gamesPlayed | COUNT of GamePlayer entries for player |
| Player.gamesWon | COUNT of Games where winnerId = player.id |
| Barbarian position | COUNT of Turns where eventDice = BARBARIANS, MOD 8 |
| First raid occurred | Barbarian position has reached 8 at least once |

---

## 4. Internationalization (i18n)

### 4.1 Strategy

- All user-facing strings externalized to resource files
- Default language: **English (EN)**
- Additional language: **Polish (PL)**
- Language selection follows device settings

### 4.2 Resource Structure

```
commonMain/
└── composeResources/
    ├── values/
    │   └── strings.xml          ← English (default)
    └── values-pl/
        └── strings.xml          ← Polish
```

### 4.3 Key String Categories

- Navigation labels
- Button texts
- Timer displays
- Game status messages
- Error messages
- Confirmation dialogs
- Player/game statistics labels

---

## 5. Accessibility Considerations

- All interactive elements have content descriptions
- Timer state announced to screen readers
- Dice selection provides haptic feedback
- Sufficient color contrast for all UI elements
- Touch targets minimum 48dp

---

## 6. Tablet Considerations

- Responsive layouts that utilize additional screen space
- Game configuration may show player list inline (instead of navigation)
- Statistics popup may be larger/more detailed
- Consider master-detail pattern for Games List → Game Summary

---

## 7. Open Questions / Future Considerations

1. **Sound effects** — Should timer completion have audio cue in addition to vibration?
2. **Theme support** — Dark mode?
3. **Data export** — Export game history to CSV/JSON?
4. **Undo functionality** — Beyond turn navigation, full undo stack?
5. **Multiplayer sync** — Future feature for shared games across devices?

---

*End of Design Specification*
