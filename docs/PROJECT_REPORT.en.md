> 🌐 **English** · [中文](PROJECT_REPORT.md)

# ICU Project Report

> **One-page overview: [CHANGELOG.en.md](CHANGELOG.en.md)** (feature list + change ledger).
> This file is the complete project-management report.

> **Purpose**: project management / issue analysis / take-over / sustainable development
> **Generated**: 2026-10-01
> **One-line conclusion**: the mod compiles, loads, and the features are implemented; but **after the rename+restructure it had not yet been re-compiled and verified**, which was the biggest risk at the time.

---

## Part 1 · Project Status (read this first)

### 1.1 Project Identity

| Item | Value |
|---|---|
| Project name | **ICU** — a realistic body trauma and medical system in Minecraft |
| Open-source repo | <https://github.com/STUTTEFRFIX/ICU> (public) |
| Latest commit | 0.2.2 (review fixes + doc sync, this commit) |
| Local directory | `D:\DS\icu` |
| mod id | `icu` |
| Java package | `com.icu.icu` |
| Version | **0.2.2** |
| Platform | Minecraft **1.21.1** + NeoForge **21.1.252** |
| Build | ModDevGradle 1.0.24 · Gradle 8.14.3 · Java 21 |
| Implemented | **4 gameplay modules + 1 item + JEI integration** |

> **There is only one name**: repo `ICU` = directory `icu` = mod id `icu` = package `com.icu.icu`.
> Feature & change overview: [CHANGELOG.en.md](CHANGELOG.en.md).

### 1.2 Repository File Inventory

```
ICU/
├── README.md                        Entry page: what this is, how to build, where to start reading code
├── BUILD_FAILURES.md                CI appends a record when a compile fails
├── LICENSE                          Apache 2.0
├── build.gradle / build.bat / gradle.properties / settings.gradle
├── gradlew / gradlew.bat / gradle/wrapper/*
├── .gitignore
├── .github/workflows/
│   ├── build.yml                    Push main → auto compile check
│   └── release.yml                  Push v* tag → auto build and publish
├── docs/
│   ├── CHANGELOG.md                 Feature list + change ledger
│   ├── PROJECT_REPORT.md            ← this file
│   ├── PROJECT_STRUCTURE.md         What each file does
│   ├── DEVELOPMENT.md               Conventions for adding features + 1.21.1 API pitfalls
│   └── HANDOVER.md                  Handover report
└── src/main/
    ├── java/com/icu/icu/
    │   ├── IcuMod.java              Entry point, only does registration
    │   ├── IcuAttachments.java      All player state (5 attachments)
    │   ├── IcuItems.java            Item registration
    │   ├── item/BandageItem.java    Bandage: hold 3 seconds to stop bleeding
    │   └── gameplay/
    │       ├── bleeding/            Severe Bleeding + recovery period
    │       ├── blood/               Blood Volume
    │       ├── pain/                Pain Level
    │       └── sprain/              Fall Sprain
    ├── resources/
    │   ├── pack.mcmeta
    │   ├── assets/icu/lang/{zh_cn,en_us}.json
    │   ├── assets/icu/models/item/bandage.json
    │   ├── assets/icu/textures/item/bandage.png
    │   ├── data/icu/damage_type/bleed.json
    │   └── data/icu/recipe/bandage.json
    └── templates/META-INF/neoforge.mods.toml
```

### 1.3 The only implemented feature: Severe Bleeding

| # | Rule | Implementation |
|---|---|---|
| 1 | Attacked by a blade/axe weapon | The attacker's main hand belongs to `minecraft:swords` / `minecraft:axes` |
| 2 | Trigger damage threshold | Final damage after armor > 3 points (see 2.6 threshold calibration) |
| 3 | Causes Severe Bleeding | Stacks +1, **uncapped** |
| 4 | 2 health per second | Every 20 ticks deals `2 × stacks` points of `icu:bleed` damage (bypasses invulnerability frames) |
| 5 | Cannot move, forced prone | SWIMMING pose + horizontal speed cleared + jumping disabled + knockback resistance |
| 6 | Nausea + Darkness | `CONFUSION` + `DARKNESS`, refreshed every tick |
| 7 | Until death/respawn | No time limit, no self-heal; only death/respawn clears |

**Death speed**: 1 stack ≈ 10 seconds to die, 2 stacks ≈ 5 seconds, 3 stacks ≈ 3.3 seconds (20 points at full health).
> The inevitable result of the design: no time limit + no treatment → **the first stack is fatal**. This is a confirmed design, not a bug.

### 1.4 Tunable parameters (all at the top of `BleedingFeature.java`)

| Constant | Current value | Meaning |
|---|---|---|
| `TRIGGER_DAMAGE` | 3.0F | Trigger threshold (see 2.6) |
| `DAMAGE_PER_LAYER` | 2.0F | Health drained per stack per second |
| `EFFECT_REFRESH_TICKS` | 20 | Status-effect refresh window |
| `TICK_INTERVAL` | 20 | Drain/particle interval |

---

## Part 2 · Task Execution Ledger

### 2.1 Task list (chronological, with status)

| # | Task | Result | Status |
|---|---|---|---|
| T1 | Define collaboration rules (7 rules) | User-defined, followed throughout | ✅ Completed |
| T2 | Environment feasibility probe | Network OK, only JDK 25, NeoForge 21.1.252 usable | ✅ Completed |
| T3 | Requirements clarification (multi-round Q&A) | Locked feature spec, naming, toolchain | ✅ Completed |
| T4 | Scaffold the project | 19 files | ✅ Completed |
| T5 | Compile and package into jar | Once produced a jar (modId=nofo era) | ✅ Completed (later superseded by rename) |
| T6 | Datapack and mod load verification | Server + client both started, zero errors | ✅ Completed (nofo era) |
| T7 | Upload source to GitHub | `54e9168` | ✅ Completed |
| T8 | Revert task (.gitattributes + local git repo) | Deleted per user request | ✅ Reverted |
| T9 | Remove downloaded game files | Freed ~1.1 GB | ✅ Completed |
| T10 | **Rename + restructure** | `ca3a8fe`, repo 26 files, zero `nofo` | ✅ Completed |
| T11 | **Re-compile verification after rename** | Owner tested: runs with no issues | ✅ **Verified (user tested)** |
| T12 | In-game downgrade verification | Verified the pre-rename version | ⚠️ Partial |
| T13 | Publish jar to Releases | — | ⏸ Not done, awaiting user decision |
| T14 | Local project git repo | Reverted per user request | ⏸ Reverted |

### 2.2 Key decisions

| Decision | Reason | Impact |
|---|---|---|
| Use `LivingDamageEvent.Post` | The only stage giving the real post-armor damage | Fits the "realistic trauma" theme |
| Attachments do not use `copyOnDeath()` | Death clears everything, respawn is naturally clean | No extra cleanup logic |
| Custom damage type `icu:bleed` | Needs an independent death message | Chinese death text can be configured |
| Rename unified to `icu` instead of `nofo` | Repo and theme are both called ICU; renaming the repo changes the URL | No repo rename needed |
| Gameplay packaged per module `gameplay/<module>` | Future fracture/infection each have a home | Structure is sustainable |
| `gameplay/bleeding/` split into three files | Data/damage/rules responsibilities stay separate | A change touches only one package |

### 2.3 Issues and resolutions (real pitfalls)

| # | Issue | Root cause | Resolution |
|---|---|---|---|
| P1 | `Could not create parent directory for lock file` | Sandbox refuses writing `C:\Users\...\.gradle` | `GRADLE_USER_HOME=D:\DS\.gradle-home` |
| P2 | `CreatePipe error=5 access denied` | Sandbox forbids named pipes, Gradle cannot spawn a daemon | Needs full permissions |
| P3 | `Unsupported class file major version 69` | Only JDK 25 installed, Gradle 8.14.3 cannot parse it | Download Temurin JDK 21 |
| P4 | `maven.neoforged.net` 502 | Transient failure | Retry |
| P5 | `jst` tool `AccessDeniedException` | NeoForm denied access to the system TEMP | `TEMP=TMP=D:\DS\.tmp-build` |
| P6 | `MobEffects.NAUSEA` symbol not found | **In 1.21.1 it is `CONFUSION`** | Rename |
| P7 | `AttachmentType.builder` reference ambiguity | `Supplier`/`Function` overload conflict | Pass a lambda, not a method reference |
| P8 | NeoForm recompiling Minecraft fails | javac path is broken in this environment | Switch to ECJ: `useEclipseCompiler = true` |
| P9 | Client crash `EXCEPTION_ACCESS_VIOLATION` | Dell A-Volute/Nahimic audio injection `NahimicOSD.dll` injected into the OpenGL process | Stop NahimicService (auto-recovers on reboot) |
| P10 | Decompile reports `page file too small` | 15.8 GB physical, 8 GB page-file cap, Vineflower out of memory | Unresolved (close programs or raise the page file) |
| P11 | **License contradicted itself in three places** | `gradle.properties` said `All Rights Reserved`, the repo `LICENSE` is Apache-2.0, README never mentioned a license | ✅ Unified to Apache-2.0 (see 3.3) |
| P12 | **Local build impossible** (two attempts failed) | NeoForm decompiling Minecraft needs more memory than this machine can commit (4.55 GB available), reports `Native memory allocation failed / page file too small` | ✅ Switched to GitHub Actions cloud build (see 2.5) |
| P13 | **Feature "had no effect"** (player feedback) | Trigger threshold = post-armor final damage > 10, but after armor reduction the max reachable is ~2.8, so it **could never trigger**. Review confirmed no code bug; it was a value calibration error (we did not run feasibility math during requirements) | ✅ Changed threshold to 3 and added armor-gradient calibration (see 2.6) |
| P14 | **Player installed the mod but "nothing happened"** (second feedback) | The release put `icu-x.y.z-sources.jar` (source jar) next to the mod jar, and the player downloaded the **source jar**. The source jar only has `.java` text, no compiled `.class`, but contains `neoforge.mods.toml`, so NeoForge still lists it as a mod — "visible in the list, but not a line of code runs" | ✅ See 2.7 |

### 2.5 First release 0.0.1 (outcome of P12)

**Background**: two local builds both failed at NeoForm `decompile` from lack of memory; an environment limit, not a code problem.

**Plan**: add `.github/workflows/release.yml` and move the build to GitHub's servers.

| Item | Content |
|---|---|
| Trigger condition | Push a `v*` tag |
| Version source | Derived from the tag (`v0.0.1` → mod version `0.0.1`) |
| Build environment | ubuntu-latest + JDK 21 (temurin) |
| Output | Mod jar attached to the Release (`icu-Mod-<version>.jar`). Source jar **no longer published**, see P14 |
| Trigger result | ✅ **Build succeeded** (~2 minutes), run #1 `conclusion=success` |

**Release URL**: <https://github.com/STUTTEFRFIX/ICU/releases/tag/v0.0.1>

**Artifact verification (checked item by item after downloading the online jar)**:

| Check | Result |
|---|---|
| `modId` = `icu` | ✅ |
| `version` = `0.0.1` | ✅ |
| `license` = `Apache-2.0` | ✅ |
| No `nofo` remaining | ✅ |
| Classes under `com/icu/icu/` | ✅ |
| All three `gameplay/bleeding/` classes present | ✅ |
| Contains `data/icu/damage_type/bleed.json` | ✅ |
| Contains `assets/icu/lang/zh_cn.json` | ✅ |

**Version audit**: `1.0.0` → `0.0.1` across 12 places (5 files) all updated;
`minecraft_version=1.21.1` and `neo_version=21.1.252` are platform versions, **unchanged**.

### 2.6 Trigger threshold calibration (outcome of P13)

**Problem**: the player reported "the mod content has no effect". Review confirmed no code bug;
the real cause was that **the trigger threshold could never be reached**.

**Derivation**: original threshold = final damage after armor+enchantments > 10. Using the 1.21.1 official armor formula
(`f = min(20, max(armor/5, armor - dmg/(2+toughness/4)))`, reduction `f/25`;
Protection gives EPF=1 per level, capped at 20), the minimum raw damage needed to trigger is:

| Target armor | Raw damage needed for old threshold (>10) | Raw damage needed for new threshold (>3) |
|---|---|---|
| Unarmored | 10.0 | **3.0** |
| Full Leather | 29.5 | **9.2** |
| Full Chainmail | 30.8 | **11.3** |
| Full Iron | 31.6 | **12.8** |
| Full Diamond | 43.6 | 20.6 |
| Full Netherite | 47.7 | 22.2 |

**The highest raw damage a player can deal in a single hit in-game is ~19.5** (Netherite axe + Sharpness V + crit).

> ❌ Under the old threshold: **any armor made it impossible to trigger**, and even unarmored barely triggered with full enchantments + crit.
> ✅ Under the new threshold 3: unarmored almost always triggers, Leather needs a heavy hit, Iron needs an iron sword or better,
> **Diamond and Netherite become true life-saving armor** — fitting the "realistic trauma" design intent.

**Change**: `BleedingFeature.TRIGGER_DAMAGE` changed from `10.0F` to `3.0F`,
and the calibration reasoning was written into that constant's Javadoc to prevent it being changed back by mistake.
Synced 9 doc/code mentions, and published `v0.0.2`.

### 2.7 Artifact naming & release rules (outcome of P14)

**Problem**: the player reported installing the mod but nothing happened. Analyzing the runtime log found three pieces of evidence:

| Evidence | Log content |
|---|---|
| 1 | `Found mod file "icu-0.0.2-sources.jar"` — they installed the **source jar** |
| 2 | `Attempting to inject @EventBusSubscriber classes into the eventbus for icu` was followed by **nothing** — no class could be scanned, zero event listeners registered |
| 3 | The mod's startup print `[ICU] loaded` — **appeared 0 times**, the main class never ran |

**Cause**: `-sources.jar` is the source jar (only `.java`), but it carries `neoforge.mods.toml`,
so NeoForge still recognizes it as a valid mod and shows it in the mod list,
**but there is no `.class` to execute**. Its name differs from the real mod jar only by `-sources`, so it is easy to download the wrong one.

**Fix (three things)**:

1. **Rename the artifact**: `build.gradle`'s `archivesName` changed from `mod_id` to `"${mod_id}-Mod"`
   (Gradle auto-appends `<archivesName>-<version>`) → the artifact becomes
   **`icu-Mod-<version>.jar`**, obviously the mod itself, no longer confusable with the source jar.
   > Pitfall: initially written as `"${mod_id}-${mod_version}-Mod"`, which made Gradle append the version again,
   > producing `icu-0.0.3-Mod-0.0.3.jar` (duplicated version); CI validation therefore failed. Fixed.
2. **The release flow no longer ships the source jar**: `.github/workflows/release.yml`'s collection step
   explicitly deletes `dist/*-sources.jar`, and validates that the mod jar actually exists,
   otherwise it makes the build **fail outright** (to avoid shipping another empty shell).
3. **Clean historical Releases**: deleted the two source-jar assets on `v0.0.1` and `v0.0.2`,
   so both Releases now each have only the mod jar.

> The source jar is still kept as a **workflow artifact** (for developers to download),
> it just no longer appears in the player's download list.

### 2.8 Source archive (option C)

**Requirement**: the source should be directly downloadable on the Release page.

**Problem**: restoring `-sources.jar` directly would repeat P14 (jar format + contains `neoforge.mods.toml`,
players might put it into `mods/` again).

**Plan**: use a **`.zip` source archive** `icu-<version>-src.zip`, triple protection:

| Protection | Explanation |
|---|---|
| Extension is `.zip` | Cannot be dropped into `mods/` for the loader to read |
| Name carries `-src` | Obviously source at a glance |
| The mod itself carries `-Mod` | The two cannot be confused |

**Implementation**: the workflow added a `Pack the source archive` step,
packing `src/ docs/ .github/ gradle/` and all build scripts; the Release attaches both
`dist/*.jar` and `dist/*-src.zip`.

**Backfilled to v0.0.3**: `icu-0.0.3-src.zip` (84,012 bytes, 28 files),
uploaded to <https://github.com/STUTTEFRFIX/ICU/releases/tag/v0.0.3>.

> Also: every GitHub Release already comes with "Source code (zip/tar.gz)",
> but the filename is `ICU-0.0.3.zip`, not clear enough; this plan provides a self-named archive that is clearer.

### 2.4 License unification (outcome of P11)

**Unified choice**: Apache License 2.0 (allows modification, redistribution, commercial use).

| Location | Before | After |
|---|---|---|
| Repo `LICENSE` | Apache-2.0 full text | Unchanged ✅ |
| `gradle.properties` | `All Rights Reserved` | **`Apache-2.0`** |
| `README.md` | Not mentioned | **Added a "License" section + one row in the overview table** |
| Local project `LICENSE` | **Missing** (only in the repo) | **Added** (11,357 bytes) |

> `mod_license` is injected by `build.gradle` into `neoforge.mods.toml`, finally shown in the in-game mod list.
> Also: `gradlew` / `gradlew.bat` headers are already Apache-2.0, so after unification **the whole project has no conflict**.
>
> Also fixed 2 broken links in README that pointed to a non-existent file (the old `docs/BUILD_AND_TEST.md`).

---

## Part 3 · Risks and Open Items (read before taking over)

### 3.1 Risk Register

| Level | Risk | Description | Suggested action |
|---|---|---|---|
| ✅ Eliminated | ~~Not re-compiled after rename~~ | Owner tested: **runs with no issues** (2026-10-01) | No action |
| ✅ Eliminated | ~~License fix not yet synced to the repo~~ | Pushed (`8bded6d`) | No action |
| ✅ Eliminated | ~~Published jar inconsistent with source~~ | `v0.0.1`'s jar was built by CI from the current source and verified item by item | No action |
| 🟠 Medium | Local build limited by memory | This machine can commit 4.55 GB, below the decompile requirement; `decompile` always fails (environment, not code) | Use `.github/workflows/release.yml` cloud build, or a machine with enough memory |
| 🟡 Low | Docs keep 10 `nofo` mentions | All are intentional historical notes (mapping tables, removal reminders) | Keep them, to prevent reverting to the old name |
| 🟡 Low | Release flow depends on GitHub Actions | Repo is public, Actions minutes are free; if Actions is disabled it cannot auto-release | Confirm Actions is enabled before releasing |
| 🟡 Low | No in-game test after rename | Behavior is identical before/after the rename | Run `runClient` when needed |

### 3.2 Open / pending items

| # | Item | Waiting on |
|---|---|---|
| U1 | ~~Re-compile verification~~ | ✅ Completed (owner tested, passed) |
| U2 | ~~Publish mod jar to GitHub Releases~~ | ✅ Completed (`v0.0.1`, `v0.0.2`, CI build succeeded) |
| U3 | ~~Whether to sync this report to the repo~~ | ✅ Synced |
| U4 | ~~Promotion to other platforms~~ | ✅ Owner completed |
| U5 | Whether to also init the local project as a git repo | User decides |
| U6 | In-game test (actually swing and verify) | Owner said it already passed |
| U7 | ~~Sync the license fix to the repo~~ | ✅ Synced (`8bded6d`) |
| U8 | ~~Sync this report to the repo~~ | ✅ Synced |

---

## Part 4 · Sustainable Development Roadmap

### 4.1 Standard flow for new features (see docs/DEVELOPMENT.en.md)

1. Create the package `com.icu.icu.gameplay.<module name>`
2. Write `<module>Data.java` (state + Codec)
3. Register an `AttachmentType` in `IcuAttachments`
4. Write `<module>Feature.java`, subscribe to events with `@EventBusSubscriber`
5. Need a custom damage type → add `data/icu/damage_type/<name>.json`
6. Need text → add `assets/icu/lang/*.json`
7. Update the feature list in `README.md`
8. Commit after the compile passes

> ⚠️ `AttachmentType.builder(() -> new X())` must be a lambda; `X::new` causes a compile ambiguity.

### 4.2 Suggested feature roadmap (by theme)

| Stage | Module | Role | Depends on |
|---|---|---|---|
| **Current** | `bleeding` | Severe Bleeding | — |
| Next | `treatment` (bandage/tourniquet) | Turn bleeding from a "death sentence" into "treatable" | bleeding |
| After | `fracture` | A second movement-limiting injury | treatment |
| Later | `infection` | Untreated wounds worsen | treatment |
| Later | `hud` | Show wound state | bleeding |
| Last | `vitals` | A unified health/consciousness system | all of the above |

### 4.3 Long-term maintenance points

| Point | Explanation |
|---|---|
| One feature per package | Do not put gameplay logic outside `gameplay/` |
| Value constants at the top of the class | Tuning without reading logic |
| Namespace must equal mod id | New resources always go in `assets/icu/`, `data/icu/` |
| When upgrading NeoForge | Only change `neo_version` in `gradle.properties`, and check the API pitfalls in `docs/DEVELOPMENT.md` |
| Before every commit | Run the section-7 checklist |

---

## Part 5 · Quick Data Reference

| Metric | Value |
|---|---|
| Repo file count | 26 |
| Source class count | 5 (IcuMod + IcuAttachments + 3 bleeding classes) |
| Doc count | 5 (README + 4 docs) |
| Implemented gameplay modules | 1 |
| Restructure commit size | 22 files, +561 / −355 |
| Disk freed | ~1.1 GB (removed game files) |
| Naming unification | **100%** (zero `nofo` in code/resources/config) |
| Compile verification status | ✅ Verified by the owner (runs fine after rename) |

---

## Appendix: One-line handover

> This is a NeoForge 1.21.1 mod, mod id `icu`, package `com.icu.icu`,
> gameplay is organized by module under `gameplay/`, currently only the "Severe Bleeding" module.
> All code is pushed to `STUTTEFRFIX/ICU` on GitHub.
> **First thing when taking over: run `build.bat` once to confirm it still compiles after the rename.**
