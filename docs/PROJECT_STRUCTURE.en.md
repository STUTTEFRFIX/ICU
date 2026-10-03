> 🌐 **English** · [中文](PROJECT_STRUCTURE.md)

# Project Structure

> Read this page to know: where each file is, what it does, and where to change it.
> This is the **single naming mapping**: repo `ICU` = directory `icu` = mod id `icu` = package `com.icu.icu`.

---

## 1. One-line Introduction

`ICU` is a **Minecraft 1.21.1 / NeoForge** mod themed "a realistic body trauma and medical system".
Current version **0.2.2**, implementing **4 gameplay modules + 1 item + JEI integration**: Severe Bleeding, Blood Volume, Pain Level, Fall Sprain, Bandage.

---

## 2. Directory Structure

```
icu/
├── README.md                        # ★ Entry page: what this is, how to build, where to start reading code
├── BUILD_FAILURES.md                # CI appends a record when a compile fails
├── build.gradle                     # ModDevGradle config, run config, jar manifest
├── gradle.properties                # ★ The single config entry (mod id / version / NeoForge version)
├── settings.gradle                  # Repo URL + Foojay (auto-installs JDK 21) + project name
├── build.bat                        # One-click build script (bundles local env vars)
├── gradlew / gradlew.bat            # Gradle wrapper (no Gradle install needed)
├── .github/workflows/
│   ├── build.yml                    # ★ Push main → auto compile check (records failures)
│   └── release.yml                  # ★ Push v* tag → cloud compile and publish a Release
├── .gitignore                       # Excludes build/ run/ and other artifacts
│
├── docs/
│   ├── CHANGELOG.md                 # ★ Feature list + change ledger (read this first)
│   ├── PROJECT_STRUCTURE.md         # This file: what each file does
│   ├── DEVELOPMENT.md               # Development conventions: follow this to add features
│   ├── PROJECT_REPORT.md            # The complete project-management report
│   └── HANDOVER.md                  # Handover report: spec, decisions, current state
│
├── gradle/wrapper/
│   ├── gradle-wrapper.jar           # The wrapper itself
│   └── gradle-wrapper.properties    # Pins Gradle 8.14.3
│
└── src/main/
    ├── java/com/icu/icu/
    │   ├── IcuMod.java              # ★ Entry point: the mod entry, only does registration
    │   ├── IcuAttachments.java      # ★ Single registration point for all persisted player data
    │   ├── IcuItems.java            # Item registration
    │   ├── item/
    │   │   └── BandageItem.java     # Bandage: hold right-click 3 seconds to treat Severe Bleeding
    │   ├── compat/jei/              # ★ JEI integration (optional, runs without JEI)
    │   │   ├── IcuJeiPlugin.java            # Plugin entry: info page + category registration
    │   │   └── recipe/
    │   │       ├── IcuInfoRecipe.java       # One page's data (input/output/text keys)
    │   │       └── IcuOverviewCategory.java # The "Severe Bleeding flow" category page
    │   └── gameplay/                # ★ Home of all gameplay, one feature per subpackage
    │       ├── IcuPose.java         # ★ The single pose owner: who is prone, who is movement-locked (see below)
    │       ├── bleeding/            # Severe Bleeding + recovery period
    │       │   ├── BleedingData.java          # Data: bleeding stacks
    │       │   ├── BleedingDamage.java        # Damage type icu:bleed (lethal)
    │       │   ├── BleedingFeature.java       # ★ Rules: trigger, blood loss, effects
    │       │   ├── BleedingRecoveryData.java  # Data: recovery timer/jump/sprint
    │       │   └── BleedingRecoveryFeature.java # Rules: re-opening the wound
    │       ├── blood/
    │       │   └── BloodVolumeData.java       # Blood volume 0–100, drain and natural regeneration
    │       ├── pain/
    │       │   ├── PainData.java              # Data: the shared pain value
    │       │   └── PainFeature.java           # ★ Rules: per-source increase/decrease and collapse
    │       └── sprain/
    │           ├── SprainData.java            # Data: whether the ankle is sprained
    │           └── SprainFeature.java         # ★ Rules: fall chance, exemptions, modifiers
    │
    ├── resources/
    │   ├── pack.mcmeta
    │   ├── assets/icu/            # Namespace must equal the mod id
    │   │   ├── lang/{zh_cn,en_us}.json     # Death messages + hints + item names
    │   │   ├── models/item/bandage.json
    │   │   └── textures/item/bandage.png
    │   └── data/
    │       ├── icu/damage_type/bleed.json  # Damage type definition
    │       └── icu/recipe/bandage.json     # Bandage recipe 3 Paper + 1 String + 1 Wool
    │
    └── templates/META-INF/
        └── neoforge.mods.toml         # Mod metadata (filled from gradle.properties at build)
```

---

## 3. Responsibility of Each File

### Build configuration

| File | Responsibility | When to change |
|---|---|---|
| `gradle.properties` | Mod identity (`mod_id`/`mod_name`/`mod_version`) and platform versions (`minecraft_version`/`neo_version`) | Rename, bump version, upgrade NeoForge |
| `build.gradle` | Applies ModDevGradle, declares run configs, generates mod metadata, Java 21 toolchain, artifact naming | Add dependencies, add run configs |
| `settings.gradle` | Repo URL + Foojay plugin | Switch repository |
| `build.bat` | Calls `gradlew build` with environment variables | Change JDK/cache paths |
| `.github/workflows/build.yml` | Push main → auto compile, writes `BUILD_FAILURES.md` on failure | Change the compile strategy |
| `.github/workflows/release.yml` | Push `v*` tag → auto build and publish a Release | Change the release strategy |

### Source code

| File | Key content |
|---|---|
| `IcuMod.java` | `MODID = "icu"`; only does registration, and adds the bandage to the creative tab |
| `IcuAttachments.java` | 5 attachments: `BLEEDING` / `BLOOD_VOLUME` / `BLEEDING_RECOVERY` / `PAIN` / `SPRAIN`; **none use `copyOnDeath()`**, death clears them |
| `IcuItems.java` | Registers the bandage (stack size 16) |
| `BandageItem.java` | `USE_TICKS = 60` (3 seconds); on finish clears bleeding, opens the recovery period, **does not touch blood volume** |
| `BleedingFeature.java` | `TRIGGER_DAMAGE = 7.0F`; trigger decision, per-second blood loss, forced prone, status effects, particles, death at zero; Creative/Spectator/Invulnerable players cleared out of bleeding |
| `BleedingRecoveryFeature.java` | Listens to jumps and sprints; `> 15 seconds sprint` or `the 4th jump` → re-opens |
| `BloodVolumeData.java` | `LOSS_PER_SECOND = 5`; `REGEN_AMOUNT = 5` / `REGEN_INTERVAL_TICKS = 12000` (10 minutes) |
| `PainFeature.java` | Bleeding +10, sprain walk +5 / sprint +10 / jump +15, prone −5; collapse at 100%, recover at 80% |
| `SprainFeature.java` | 5 chance tiers, Hay Bale −20, enchantment −8 per level and fully negated at level 4, water/Slow Falling/Fire Resistance/Elytra exemptions |

### ★ `IcuPose.java` — the single pose owner (important)

**Background (a real bug fixed in v0.2.1)**: originally the bleeding module and the pain module **each called `setPose`**,
bleeding set `SWIMMING` every tick and pain set `SWIMMING`/`STANDING` every second — they overwrote each other,
which in third person looked like **flickering between standing and prone**.

**The rules**:

| Rule | Explanation |
|---|---|
| **Only `IcuPose` may call `setPose`** | Other modules are **forbidden** from touching the pose, or they will fight again |
| Computed once per tick | Because movement input is also applied every tick; once per second lets a player holding a key push back out |
| Trigger condition | Bleeding **or** pain at 100% → forced prone |
| Release | Neither condition holds → restore `STANDING` (only undoes our prone, leaves a genuinely swimming player alone) |
| What the lock does | Prone pose + horizontal speed zeroed (which also gives knockback resistance) + upward speed cancelled (jumping disabled) |

> Other modules that want to know "is this player currently forced prone" should call `IcuPose.isForcedProne(player)`.

### Internal structure of each Feature

| Method | Role |
|---|---|
| `BleedingFeature.onLivingDamagePost` | **Trigger decision**: player + not Creative/Spectator + final damage > 7 + holding sword/axe → stacks +1 |
| `BleedingFeature.onPlayerTick` | Every tick: prone lock and status effects; every 20 ticks: particles + blood volume −5 + death at zero |
| `BleedingFeature.onPlayerRespawn` | **Death cleanup**: bleeding/recovery/blood volume/pain/sprain all reset |
| `BleedingRecoveryFeature.onLivingJump` | Records jumps during recovery, re-opens when over the limit |
| `BleedingRecoveryFeature.onPlayerTick` | Recovery timer; sprint accumulation; clearing continuous sprint when not sprinting |
| `PainFeature.onPlayerTick` | Every second sums all sources, updates pain, handles collapse and recovery |
| `SprainFeature.onLivingFall` | Uses vanilla `fallDistance` to decide, applying exemptions and modifiers one by one |

---

## 4. Data Flow (one full "heavy hit → death")

```
Player is hit by a sword/axe entity
        │
        ▼
LivingDamageEvent.Post   ← armor/enchantment/resistance all settled
        │  final damage > 7 ?
        ▼
Attachment BLEEDING stacks +1
        │
        ▼
PlayerTickEvent.Post (every tick)
        ├── setPose(SWIMMING) + speed/knockback all locked
        ├── every 20 ticks: blood particles + blood volume −5%
        ├── every 20 ticks: PainData +10%
        └── every tick: CONFUSION + DARKNESS refreshed
        │
        ├── blood volume = 0  → immediate death (icu:bleed)
        ├── pain = 100  → forced prone + 3 "I hurt" messages
        │
        ▼
Use bandage (hold 3 seconds)
        ├── success → bleeding ends, blood volume kept, enters recovery period 5 minutes
        └── blood volume reaches zero within 3 seconds → death
        │
        ▼
During recovery: sprint > 15 seconds or the 4th jump → re-opens (blood volume not reset)
```

---

## 5. "What happens if I change this"

| What you want to change | Which file | Where exactly |
|---|---|---|
| Trigger threshold (now 7) | `BleedingFeature.java` | `TRIGGER_DAMAGE` |
| Blood loss per second (now 5%) | `BloodVolumeData.java` | `LOSS_PER_SECOND` |
| Natural regeneration rate (now 10 minutes 5%) | `BloodVolumeData.java` | `REGEN_AMOUNT` / `REGEN_INTERVAL_TICKS` |
| Bandage use duration (now 3 seconds) | `BandageItem.java` | `USE_TICKS` |
| Bandage gate (now 15%) | `BandageItem.java` | `MIN_BLOOD_TO_TREAT` |
| Pain values | `PainFeature.java` | The 5 constants at the top |
| Sprain chance tiers | `SprainFeature.java` | `CHANCE_TIER_1..4` |
| Hay Bale reduction (now 20) | `SprainFeature.java` | `HAY_CHANCE_REDUCTION` |
| Enchantment reduction per level (now 8) | `SprainFeature.java` | `CHANCE_PER_ENCHANT_LEVEL` |
| Full immunity level (now 4) | `SprainFeature.java` | `FULL_IMMUNITY_LEVEL` |
| Recovery duration / re-open thresholds | `BleedingRecoveryData.java` | `DURATION_TICKS` / `SPRINT_LIMIT_TICKS` / `JUMP_LIMIT` |
| Weapon whitelist | `BleedingFeature.isBladedWeaponAttack` | Uses vanilla `ItemTags.SWORDS` / `ItemTags.AXES` (no custom override files anymore) |
| Death and message text | `resources/assets/icu/lang/zh_cn.json` | `death.attack.bleed` / `message.icu.*` |
| mod id / version / display name | `gradle.properties` | The corresponding fields |

---

## 6. Dependencies and Versions (verified)

| Component | Version | Note |
|---|---|---|
| Minecraft | 1.21.1 | `minecraft_version` |
| NeoForge | 21.1.252 | The latest 21.1.x for 1.21.1 |
| ModDevGradle | 1.0.24 | The plugin line matching 1.21.1 |
| Gradle | 8.14.3 | Pinned by the wrapper |
| Java | 21 | Build target; auto-downloaded by Foojay |

---

## 7. Build Artifacts

| Artifact | Path |
|---|---|
| Mod jar | `build/libs/icu-Mod-0.2.2.jar` |
| Source jar | `build/libs/icu-Mod-0.2.2-sources.jar` (do **not** put into `mods/`) |

Install: put `icu-Mod-0.2.2.jar` into the instance's `mods/` folder.

**Files published to a Release** (produced by CI):

| File | Use |
|---|---|
| `icu-Mod-<version>.jar` | The mod itself, goes into `mods/` |
| `icu-<version>-src.zip` | Source archive (`.zip`, **cannot go into `mods/`**, for reading / further development) |
