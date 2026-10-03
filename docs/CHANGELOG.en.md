> 🌐 **English** · [中文](CHANGELOG.md)

# ICU Features & Changelog

> One page answering two questions: **what features this mod implements**, and **what changed along the way**.
> Full project-management information is in [PROJECT_REPORT.en.md](PROJECT_REPORT.en.md).

**Current version: 0.2.2**

---

## Part 1 · What the Mod Implements

### 1.1 Positioning

**ICU** — a realistic "body trauma + medical system" in Minecraft.
Gameplay is added as **independent modules**, one feature per package.

| Module | Package | Description |
|---|---|---|
| Severe Bleeding | `gameplay.bleeding` | Blood loss, bandage treatment, recovery period |
| Blood Volume | `gameplay.blood` | A hidden health resource; reaching zero during bleeding kills |
| Pain Level | `gameplay.pain` | A pain meter shared by bleeding and sprains |
| Fall Sprain | `gameplay.sprain` | Ankle sprain caused by falling |
| Bandage | `item` | The only way to treat Severe Bleeding |
| JEI integration (optional) | `compat.jei` | Bandage info page + the ICU "Severe Bleeding flow" category page |

> **The heartbeat sound is not implemented yet**: the design is fixed and awaits audio assets (provided by the project owner).

### 1.2 Severe Bleeding

| # | Behavior | Exact implementation |
|---|---|---|
| 1 | **Trigger condition** | Hit by a sword/axe weapon and the final damage **after armor + enchantments** is **> 7 points** |
| 2 | **Stacking** | **+1 stack** per hit, **uncapped** |
| 3 | **Blood loss** | Blood Volume **−5% per second** during Severe Bleeding |
| 4 | **Lethal** | **Only reaching zero blood volume kills**. The old "health drain per second" is gone; blood volume is the single source of truth |
| 5 | **Forced prone** | `SWIMMING` pose + horizontal speed zeroed + jumping disabled |
| 6 | **Knockback resistance** | Horizontal speed zeroed (`setDeltaMovement` horizontal components set to 0), which also gives knockback resistance |
| 7 | **Extra effects** | Nausea `CONFUSION` + Darkness `DARKNESS`, refreshed every tick |
| 8 | **Visuals** | Blood particles, count grows with stacks |
| 9 | **Death message** | "XXX's blood volume reached zero and died of blood loss" |
| 10 | **End** | Bandage treatment, or death/respawn |

> ⚠️ **Practical impact of threshold 7**: extremely hard to reach after armor reduction.
> Unarmored needs raw damage 7; leather ~15; iron ~18.5; **diamond ~28** (max reachable in-game ~19.5).
> So **wearing iron or better almost never bleeds**. This value was explicitly chosen by the project owner.

> **Creative / Spectator / Invulnerable players do not bleed**: `onPlayerTick` clears them out before bleed processing (`BLEEDING.clear()`),
> so the lethal path (the `setHealth(0)` fallback) is unreachable for them.

### 1.3 Blood Volume

| Rule | Value |
|---|---|
| Initial / cap | 100% |
| During Severe Bleeding | **−5%** per second (100% → 0% = 20 seconds) |
| Reaching zero | **Immediate death**, whatever health remains |
| Natural regeneration | **+5% every 10 minutes** while **not bleeding**, capped at 100% |
| Bandage treatment | **Does not reset**, keeps the current value |
| Bleeding again | Continues **down** from the current value |
| Death / respawn | Reset to 100% |

### 1.4 Bandage

| Item | Value |
|---|---|
| Recipe | **3 Paper + 1 String + 1 Wool** |
| Stack | Cap 16, using consumes 1 |
| Usage | **Hold** right-click for 3 seconds |
| During | Severe Bleeding keeps working, blood volume keeps draining −5% per second |
| **Usage gate** | At the moment the wrapping finishes, blood volume must be **≥ 15%**, otherwise it fails |
| Success | Ends Severe Bleeding, keeps blood volume, enters the recovery period |
| Failure ① | Blood volume reaches zero within 3 seconds → death |
| Failure ② | Blood volume **< 15%** at finish → bandage **fails**: shows "The bandage is useless — the blood has already drained", bleeding continues, the bandage is still consumed |

> **Why the gate is checked "at the finish moment"**: the check sits in `finishUsingItem` (after the 3-second hold),
> not at the instant you start using it. That makes "don't waste a bandage when blood is low" **perceivable** —
> the player goes through "wrap for 3 seconds, then fail" instead of "rejected on first click".
> On failure the heal sound is **not played**, but the bandage is **still consumed**, so you cannot cheese it by retrying.

### 1.5 Pain Level (shared by bleeding and sprain)

| Source | Change per second |
|---|---|
| Severe Bleeding | **+10%** (applies even when standing still) |
| Sprain · walking | **+5%** |
| Sprain · sprinting | **+10%** |
| Sprain · one jump | **+15%** (one-time) |
| Prone (collapsed) state | **−5%** |

| Threshold behavior | Description |
|---|---|
| Reaching **100%** | Forced prone, unable to act; chat shows **3 consecutive** "I hurt, I hurt" (**self-only**) |
| Dropping to **80%** | Movement restored immediately, pain continues counting from 80% |
| While Severe Bleeding persists | +10% and −5% both apply → net +5% → **stuck at 100%** until bleeding ends |

### 1.6 Bleeding Recovery Period

| Rule | Value |
|---|---|
| Duration | **5 minutes** |
| Re-open condition ① | Sprinting **continuously over 15 seconds** |
| Re-open condition ② | **The 4th jump** |
| Re-open result | Bleeding restarts (blood volume **not reset**) + pain +10% per second |
| Timer reset | **Any** jump or sprint during the period resets the 5-minute timer (below threshold does not re-open) |
| End | Staying sprint-free and jump-free for 5 minutes → recovery complete |

### 1.7 Fall Sprain

| Fall distance (vanilla `fallDistance`) | Trigger chance |
|---|---|
| ≤ 5 blocks | Nothing |
| > 5 and < 10 | **35%** |
| ≥ 10 and < 15 | **50%** |
| ≥ 15 and < 20 | **90%** |
| ≥ 20 | **100%** |

| Modifier | Effect |
|---|---|
| Landing on **Hay Bale** | **−20 percentage points** (35%→15%, etc.), minimum 0% (**reduces the chance, not an exemption**) |
| Protection / Feather Falling enchantment | **−8 percentage points per level**; when both are present, take the **higher level** |
| Enchantment at **level 4** | **Fully negated** |

**Automatic exemptions**: landing in water (even a single block), Slow Falling potion, Elytra gliding, Chorus Fruit teleport, Fire Resistance, plus cases the vanilla game already exempts.

**Message**: when triggered, shows 1 "Oh no, I sprained my ankle" (**self-only**).

### 1.8 Explicitly NOT implemented

Treatment other than the bleeding-stopping item, medical blocks, infection, fractures, organ damage, GUI, commands, HUD hints, achievements.
**Heartbeat sound**: design fixed, awaiting assets.

---

## Part 2 · What Changed

### 2.1 Released Versions

| Tag | Content |
|---|---|
| `v0.0.1` | First release (Severe Bleeding only), validated the CI packaging pipeline |
| `v0.0.2` | Fixed the trigger threshold (10 → 3) |
| `v0.0.3` | Artifact renamed to `icu-Mod-<version>.jar` + source zip |
| `v0.1.0` | Blood Volume + Bandage + Pain Level + Fall Sprain + recovery period; threshold 3 → 7 |
| `v0.2.0` | JEI integration (bandage info page + ICU flow category page) |
| **`v0.2.1`** | **This one: 5 defect fixes found by live testing (see 2.9)** |
| **`v0.2.2`** | **This one: review fixes + doc sync (see 2.10)** |

### 2.9 v0.2.1 live-test fix record

Five defects were found by actually playing the game, all from **real runtime logs** (`icu:bandage` recipe parse failure is written clearly in the log).

| # | Symptom | Root cause | Fix |
|---|---|---|---|
| 1 | **Bandage could not be crafted** (log `Parsing error loading recipe icu:bandage`) | `ingredients` was written as a bare string, but 1.21.1 requires `{"item": "..."}` or an array | Rewrote as object format following a vanilla recipe |
| 2 | **Third-person standing/prone flicker** | **Two modules fought over the pose**: bleeding set `SWIMMING` every tick, pain set `SWIMMING`/`STANDING` every second, toggling back and forth | Created **`IcuPose`** as the **single pose owner**, computed once per tick; other modules are forbidden from calling `setPose` |
| 3 | **"I hurt" popped up but no forced prone** | The collapse lock ran **only once per second**, and the player holding a movement key was pushed back every tick | Changed the lock to run **every tick** (movement input also comes every tick); the message still pops once at the crossing moment |
| 4 | **Elytra did not exempt the sprain** | `isFallFlying()` was not checked — a dive landing still accumulates `fallDistance` | Explicitly added the `isFallFlying()` exemption |
| 5 | **Bandage still succeeded below 15% blood volume** | The spec item was not implemented | Added `MIN_BLOOD_TO_TREAT = 15`; below that the bandage **fails** (shows message, bleeding continues, bandage still consumed) |

**Also**: the bleeding module **no longer deals any health damage** — reaching zero blood volume is the only lethal mechanism, so the player always gets the full 20 seconds to wrap a bandage.

### 2.10 v0.2.2 review fix record

A **read-only** runtime review of the 0.2.1 code found 7 "compiles but misbehaves at runtime" defects;
5 were fixed following the captain's corrected instructions (the other 2: 1 false positive, 1 left for the captain to confirm the value).

| # | Location | Handling | Behavior change |
|---|---|---|---|
| #2 | `BleedingFeature.onPlayerTick` | Added a guard before bleed processing: Creative/Spectator/Invulnerable players get `BLEEDING.clear()` and return | **Yes**: such players can no longer die from bleeding |
| #3 | `SprainFeature.applyProtection` comment | Changed to "EnchantmentHelper returns the highest level across equipped pieces" | No (comment only) |
| #4 | `SprainFeature` class Javadoc | Clarified that Slow Falling and Elytra still fire `LivingFallEvent`, hence the explicit exemption | No (comment only) |
| #6 | `data/minecraft/tags/item/{swords,axes}.json` | Deleted the redundant override; use vanilla `ItemTags.SWORDS/AXES` | No (the override matched the intent anyway) |
| #7 | `IcuPose.applyProneLock` comment | Clarified that knockback resistance comes from zeroing horizontal speed | No (comment only) |

**Not fixed**:

- #1 (`IcuPose` `STANDING`-restore branch): verified via `javap` to be a false positive — `isSwimming()` reads `FLAG_SWIMMING` and `setPose` writes `DATA_POSE`, the two are independent, so the branch is reachable and correct; left unchanged.

**Handled afterwards (folded into v0.2.2)**:

- #5 (`SprainFeature.FULL_IMMUNITY_LEVEL`): originally recorded as "value unchanged, left for the captain". Now confirmed by the owner: vanilla Protection / Feather Falling top out at **level 4**, so "fully negated at level 5" was unreachable; `FULL_IMMUNITY_LEVEL` changed **5 → 4** (`SprainFeature.java:64`, Javadoc `{@value}` auto-syncs), the rest of the logic (−8 per level, take the higher) unchanged.

**Constraints kept**: pose control still only in `IcuPose`; reaching zero blood volume is still the only lethal mechanism; no new health damage.

### 2.2 Commit History (newest → oldest)

| Commit | Description |
|---|---|
| `aaa0f1c` | Validate recipe JSON in CI so a broken recipe cannot ship again |
| `f5d189c` | Document the bandage blood-volume threshold |
| `88ac6a3` | Release 0.2.1 with the five fixes |
| `17fa3c0` | Fix five defects found by running the mod |
| `5d4fdf5` | Document how to install JEI and how to change the JEI code |
| `655b34e` | Bump to 0.2.0 and document the JEI integration |
| `470d1ef` | Add JEI integration (optional at runtime) |
| `60b3418` | Document the 0.1.0 modules and bump the version |
| `93b5e70` | Fix the enchantment level lookup and complete the fall exemptions |
| `0fd2f92` | build-failure: record failing build for b0ff36c (CI auto-recorded) |
| `b0ff36c` | Add blood volume, bandage, pain, sprain and recovery modules |
| `43b91c3` | Attach a source zip to releases |
| `b2940b7` | Fix the release asset name check |
| `412f605` | Ship the mod jar under an unmistakable name |
| `d571b08` | Fix the trigger threshold: the effect could never fire |
| `db46acd` | Document the 0.0.1 release and the GitHub Actions build |
| `c3f3a5c` | Release 0.0.1 and build the jar on GitHub Actions |
| `20855ed` | Record that the renamed code has been verified by running it |
| `8bded6d` | Unify the licence to Apache-2.0 and add the project report |
| `ca3a8fe` | Rename project to icu and restructure for maintainability |
| `54e9168` | Add ICU mod source code (NeoForge 1.21.1) |
| `45f61be` | Add files via upload |
| `c570ed8` | Initialize README with project details |
| `0638ce9` | Initial commit |

### 2.3 Change Ledger

#### A. Naming unification (ending "talking past each other")

| Item | Before | After |
|---|---|---|
| mod id / package / main class | `nofo` / `com.nofo.nofo` / `NofoMod` | **`icu` / `com.icu.icu` / `IcuMod`** |
| Attachment class / gameplay package | `ModAttachments` / `bleed/` | **`IcuAttachments` / `gameplay/bleeding/`** |
| Logic class / damage class / data class | `BleedHandler` / `BleedDamage` / nested | **`BleedingFeature` / `BleedingDamage` / `BleedingData`** |
| Resource directory / registration id | `assets/nofo/` / `nofo:bleed` | **`assets/icu/` / `icu:bleed`** |
| Local directory | `nofo1.21.1` | **`icu`** |

#### B. Restructuring (for sustainable maintenance)

```
src/main/java/com/icu/icu/
├── IcuMod.java              Entry point, only does registration
├── IcuAttachments.java      The single registration point for all persisted data
├── IcuItems.java            Item registration
├── item/BandageItem.java    The bandage
└── gameplay/                ★ One feature per subpackage
    ├── bleeding/            Bleeding + recovery period
    ├── blood/               Blood Volume
    ├── pain/                Pain Level
    └── sprain/              Fall Sprain
```

#### C. Functional corrections

| Item | Before | After | Reason |
|---|---|---|---|
| Trigger threshold | 10 | 3 (v0.0.2) | 10 is **never reachable** after armor reduction |
| Trigger threshold | 3 | **7** (v0.1.0) | The project owner asked for a high value |
| Lethal mechanism | Health drain per second + blood volume | **Only reaching zero blood volume** | Avoid two mechanisms fighting; blood volume is the single source of truth |

#### D. Build & release

| Item | Before | After |
|---|---|---|
| Build location | Local (fails from lack of memory) | **GitHub Actions cloud** |
| Trigger | Manual | **Push `v*` tag auto-publishes**; push main auto-compile-checks |
| Artifact name | `icu-0.0.1.jar` | **`icu-Mod-<version>.jar`** |
| Source distribution | `-sources.jar` (**can be mis-installed as the mod**) | **`icu-<version>-src.zip`** |
| Failure visibility | Had to open the Actions page | **CI auto-writes `BUILD_FAILURES.md`** |

#### E. Compliance & documentation

| Item | Content |
|---|---|
| License | Unified to **Apache-2.0** in three places |
| Documentation | `README.md`, `docs/DEVELOPMENT.md`, `docs/PROJECT_REPORT.md`, `docs/CHANGELOG.md` |
| Corrections | Fixed 2 broken links; synced the version number across the project |

### 2.4 Issue Handling Log

| # | Issue | Root cause | Handling |
|---|---|---|---|
| P11 | License contradicted itself | Three declarations disagreed | Unified to Apache-2.0 |
| P12 | Local build impossible | Not enough memory to decompile MC | Switched to GitHub Actions |
| P13 | Feature "had no effect" | Threshold 10 never reachable after armor reduction | Changed to 3, later to 7 per requirements |
| P14 | Feature "no reaction at all" | Player installed **`-sources.jar`** (no class but recognized as a mod) | Renamed artifact + source became zip |
| P15 | CI build failed (2 errors) | `Player.getEnchantmentLevel(Holder)` does not exist | Switched to `EnchantmentHelper.getEnchantmentLevel(Holder, LivingEntity)` |
| P16 | Two exemptions missing | Fire Resistance, Slow Falling (vanilla event still fires, only cancels damage) | Added explicit checks |

### 2.5 Debugging Methods (archived)

**"Installed but no reaction"** — check three log spots:

| Observation point | Normal | Abnormal |
|---|---|---|
| `Found mod file "..."` | `icu-Mod-<version>.jar` | `icu-<version>-sources.jar` |
| `[ICU] loaded` | Appears 1 time | **0 times** |
| `@EventBusSubscriber ... for icu` | Registration records follow | **Nothing follows** |

**Compile errors**: look at `BUILD_FAILURES.md`, or the Build step of the corresponding run on the Actions page.

---

## Part 3 · Current Status at a Glance

| Metric | Value |
|---|---|
| Latest version | **0.2.2** |
| Implemented modules | **4 gameplay modules + 1 item** |
| Naming unification | **100%** (zero `nofo` remaining) |
| License | Apache-2.0 (consistent in three places) |
| Compile | ✅ **CI compile passed** (auto-checked on push to main) |
| Local compile | ⚠️ This machine lacks memory; must use CI or another machine |
| Not implemented | Heartbeat sound (awaiting audio assets) |

**First thing when taking over**: `git clone` → read `README.md` → read this file → read `docs/DEVELOPMENT.md` before changing code.
