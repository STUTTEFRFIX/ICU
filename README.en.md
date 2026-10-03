> 🌐 **English** · [中文](README.md)

# ICU

**Realistic body trauma and medical system in Minecraft.**

| | |
|---|---|
| Platform | Minecraft **1.21.1** + NeoForge **21.1.252** |
| Build | ModDevGradle 1.0.24 · Gradle 8.14.3 · Java 21 |
| mod id | `icu` |
| Current version | **0.2.2** |
| License | **Apache-2.0** (modify and redistribute freely) |
| Implemented | **4 gameplay modules + 1 item + JEI integration** |

---

## Installation

**Required**

| Component | Version |
|---|---|
| Minecraft | **1.21.1** |
| NeoForge | **21.1.252** or any later 21.1.x |
| This mod | `icu-Mod-0.2.2.jar` (download from [Releases](https://github.com/STUTTEFRFIX/ICU/releases)) |

Put `icu-Mod-0.2.2.jar` into the instance's `mods/` folder and you are done.

> ⚠️ **Only download the `.jar` with `-Mod`.**
> The Release also contains `icu-<version>-src.zip`, which is the **source archive** (a `.zip`, so it cannot go into `mods/`).
> Do not download any `-sources.jar`: it is a jar and contains `neoforge.mods.toml`,
> so the mod shows up in the list after you put it into `mods/` but **the code never runs** — the same as not installing it.

**Optional: JEI** (only needed to see the bandage info page and the ICU category page)

| Component | Version |
|---|---|
| Just Enough Items (JEI) | **`jei-1.21.1-neoforge-19.57.0.450`** or any later 19.x |

Download the 1.21.1 + **NeoForge** file from [modrinth.com/mod/jei](https://modrinth.com/mod/jei) and put it into `mods/`.

> **Not installing JEI is perfectly fine**: ICU runs normally, you just lose two JEI pages.
> ICU's `mods.toml` declares JEI as `optional`, so the order and presence of the two do not affect loading.

---

## What This Is

ICU turns "being hurt" and "being treated" into a realistic set of mechanics. Gameplay is added as **independent modules**, one feature per package, so they do not interfere.

| Module | Content |
|---|---|
| **Severe Bleeding** | After a heavy blade/axe wound you keep losing blood; a bandage can treat it |
| **Blood Volume** | A hidden health bar; −5% per second while bleeding, death at zero; slow natural regeneration when not bleeding |
| **Pain Level** | A pain meter shared by bleeding and sprains; at full it forces you to collapse |
| **Fall Sprain** | Falling from height has a chance to sprain an ankle; armor enchantments can reduce it |
| **Bandage** | 3 Paper + 1 String + 1 Wool; hold for 3 seconds to stop the bleeding |
| **JEI integration** (optional) | When JEI is present: bandage info page + the ICU "Severe Bleeding flow" category page |

> **The heartbeat sound is not implemented yet**: the design is fixed and awaits audio assets.
> **JEI is optional**: the game plays fine without it, and ICU will not error.

---

## The Code in 30 Seconds

```
src/main/java/com/icu/icu/
├── IcuMod.java                  ← Entry point: the mod entry, only does registration
├── IcuAttachments.java          ← All player state (blood volume / pain / sprain...)
├── IcuItems.java                ← Item registration
├── item/BandageItem.java        ← The bandage
└── gameplay/                    ← ★ Home of all gameplay, one feature per subpackage
    ├── bleeding/                ← Severe Bleeding + recovery period
    ├── blood/                   ← Blood Volume
    ├── pain/                    ← Pain Level
    └── sprain/                  ← Fall Sprain
```

**To change gameplay → only touch the corresponding `gameplay/<module>/`.**
**To add gameplay → create a new package under `gameplay/` (e.g. `fracture/`).** See [docs/DEVELOPMENT.en.md](docs/DEVELOPMENT.en.md).

---

## Documentation Map

| Document | When to read it |
|---|---|
| **This file** | First contact with the project |
| [docs/CHANGELOG.en.md](docs/CHANGELOG.en.md) | "What features are implemented, what changed" |
| [docs/PROJECT_STRUCTURE.en.md](docs/PROJECT_STRUCTURE.en.md) | What each file actually does |
| [docs/DEVELOPMENT.en.md](docs/DEVELOPMENT.en.md) | Before adding a feature or changing code |
| [docs/PROJECT_REPORT.en.md](docs/PROJECT_REPORT.en.md) | Project management, issue analysis, take-over overview |
| [docs/HANDOVER.en.md](docs/HANDOVER.en.md) | Taking over the project, a quick full picture and current state |

---

## How to Build

**Method 1: download (for normal players)**

See "[Installation](#installation)" above — download `icu-Mod-0.2.2.jar` from Releases and drop it into `mods/`.

**Method 2: compile it yourself**

```bat
build.bat
```

Artifact: `build/libs/icu-Mod-0.2.2.jar`.

> This machine has sandbox limits; `build.bat` already bundles the required environment variables (JDK 21 path, etc.).
> On an unrestricted machine, simply run `gradlew.bat build`.
> See [docs/PROJECT_REPORT.en.md](docs/PROJECT_REPORT.en.md) and [docs/HANDOVER.en.md](docs/HANDOVER.en.md) for details and pitfalls.

**Method 3: cloud build (recommended when local compilation fails)**

Pushing to `main` runs a compile check; pushing a `v*` tag compiles and publishes a Release:

```bash
git tag v0.1.0
git push origin v0.1.0
```

> The tag is the version: `v0.1.0` → mod version `0.1.0` (the workflow writes it into `gradle.properties`).
> Build failures are recorded in [`BUILD_FAILURES.md`](BUILD_FAILURES.md).

---

## Current Features

### Severe Bleeding

Triggered when hit by a sword/axe weapon and the final damage **after armor + enchantments** is **> 7 points**.

| Behavior | Value |
|---|---|
| Bleeding stacks | +1 per hit, **uncapped** |
| Blood loss | Blood Volume **−5% per second** (100% → 0% = 20 seconds) |
| Lethal | **Only reaching zero blood volume kills** |
| Forced prone | Swimming pose + movement locked + jumping disabled + knockback resistance |
| Extra effects | Nausea + Darkness (refreshed continuously) |
| Visuals | Blood particles, Chinese-language death message |

> **Creative / Spectator / Invulnerable players do not bleed**: the bleeding state is cleared, so the lethal path is unreachable for them.

### Blood Volume

| Rule | Value |
|---|---|
| Initial / cap | 100% |
| During Severe Bleeding | −5% per second |
| Reaching zero | Immediate death |
| Natural regeneration | **+5% every 10 minutes** while not bleeding |
| Bandage treatment | **Does not reset**, keeps the current value |

### Bandage

**3 Paper + 1 String + 1 Wool**, stack size 16. Hold right-click for **3 seconds**; bleeding keeps draining 5% blood volume per second during that time.

> ⚠️ **The bandage only works when blood volume ≥ 15%.** Below that you have already bled out too far and the bandage cannot save you:
> it shows "The bandage is useless — the blood has already drained", and bleeding continues to zero.
> The check happens **the moment the 3-second hold ends**, so when you do not have enough blood you wrap for the full 3 seconds and then fail
> (the bandage is still consumed; you cannot retry repeatedly).

### Pain Level (shared)

| Source | Per second |
|---|---|
| Severe Bleeding | +10% |
| Sprain · walking | +5% |
| Sprain · sprinting | +10% |
| Sprain · jumping | +15% (one-time) |
| Prone state | −5% |

At 100% → forced prone + 3 "I hurt, I hurt" messages (self-only); back to 80% restores movement.

### Bleeding Recovery Period

**5 minutes** after treatment. Sprinting **> 15 seconds** or **the 4th jump** → the wound re-opens (blood volume is not reset). Any sprint/jump resets the timer.

### Fall Sprain

| Fall distance | Chance |
|---|---|
| ≤ 5 blocks | Nothing |
| > 5 and < 10 | 35% |
| ≥ 10 and < 15 | 50% |
| ≥ 15 and < 20 | 90% |
| ≥ 20 | 100% |

Hay Bale **−20 percentage points**; Protection / Feather Falling enchantments **−8 per level**, **fully negated at level 4**.
Landing in water, Slow Falling, Elytra, Chorus Fruit and Fire Resistance are **automatically exempt**.

---

## License

This project uses the **Apache License 2.0**. The three declarations are identical:

| Location | Content |
|---|---|
| [`LICENSE`](LICENSE) | Full Apache License 2.0 text |
| [`gradle.properties`](gradle.properties) | `mod_license=Apache-2.0` (written into mod metadata, visible in the in-game mod list) |
| This file | The section you are reading |

**You may**: freely use, modify and redistribute this project, including commercial use and derivatives.
**Condition**: keep the copyright and license notices, and mark the changes you made (see LICENSE section 4).
**No warranty**: see LICENSE section 7.
