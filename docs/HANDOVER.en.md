> 🌐 **English** · [中文](HANDOVER.md)

# ICU Mod — Handover Report

> **One page for the next person.** Read it and you can start.
> Project path: `D:\DS\icu\`　｜　Topic: a realistic body trauma and medical system in Minecraft
> The full feature & change ledger is in [CHANGELOG.en.md](CHANGELOG.en.md).

---

## 1. What this mod is

| Item | Value |
|---|---|
| Name / repo | **ICU** |
| Directory | `icu` |
| mod id | `icu` |
| Java package | `com.icu.icu` |
| Version | **0.2.2** |
| Platform | Minecraft **1.21.1** + NeoForge **21.1.252** |
| Build | ModDevGradle **1.0.24**, Gradle 8.14.3, Java 21 |
| Implemented | **4 gameplay modules + 1 item** |

> **There is only one name**: repo `ICU` = directory `icu` = mod id `icu` = package `com.icu.icu`.
> Historically it was `nofo`, which was **fully removed** in the restructuring (source, resources, docs, jar all no longer contain it).

**Design positioning**: not "magic/skills" but "realistic trauma". The rules are harsh — a heavy wound is basically a death sentence.

---

## 2. Implemented Features

### 2.1 Severe Bleeding (`gameplay.bleeding`)

| # | Rule | Exact implementation |
|---|---|---|
| 1 | Attacked by a blade/axe weapon | The attacker's main hand belongs to `minecraft:swords` or `minecraft:axes` |
| 2 | Trigger threshold | Final damage after **armor/enchantment/resistance** > **7 points** |
| 3 | Stacks | **+1** per hit, **uncapped** |
| 4 | Blood loss | Blood Volume **−5%** per second |
| 5 | Lethal | **Only reaching zero blood volume kills**; no extra health damage |
| 6 | Forced prone | `SWIMMING` pose + horizontal speed zeroed + jumping disabled + **knockback resistance** |
| 7 | Extra effects | `CONFUSION` + `DARKNESS`, refreshed every tick, cleared with the bleeding |
| 8 | Visuals | Blood particles; death message "XXX's blood volume reached zero and died of blood loss" |

> ⚠️ **Impact of threshold 7**: extremely hard to reach after armor reduction — unarmored needs raw damage 7, leather ~15,
> iron ~18.5, **diamond ~28** (max reachable in-game ~19.5).
> So **wearing iron or better almost never bleeds**. This value was explicitly chosen by the project owner.

> **Creative / Spectator / Invulnerable players do not bleed**: the bleeding state is cleared, so the lethal path is unreachable for them.

### 2.2 Blood Volume (`gameplay.blood`)

Initial/cap 100%; **−5% per second** during Severe Bleeding (zero in 20 seconds = death);
**+5% every 10 minutes while not bleeding** (capped at 100%); bandage treatment **does not reset**, continuing from the current value.

### 2.3 Bandage (`item`)

Recipe **3 Paper + 1 String + 1 Wool**, stack size 16. **Hold** right-click for **3 seconds**;
bleeding keeps draining blood volume during it; on success it ends the bleeding + enters the recovery period; reaching zero within 3 seconds means death.

**Usage gate 15%**: at the moment the wrapping finishes, blood volume must be **≥ 15%**, otherwise the bandage **fails** —
shows "The bandage is useless — the blood has already drained", **no heal sound**, bleeding continues, the bandage is **still consumed** (cannot retry repeatedly).

> **Why it is judged "at the finish moment"**: the check runs in `finishUsingItem` (after the 3-second hold),
> not at the instant you start. That makes "don't waste a bandage when blood is low" **perceivable** —
> the player goes through "wrap for 3 seconds, then fail" instead of "rejected on first click".
> Implementation location: `BandageItem.MIN_BLOOD_TO_TREAT`.

### 2.4 Pain Level (`gameplay.pain`, shared by bleeding and sprain)

| Source | Per second |
|---|---|
| Severe Bleeding | +10% |
| Sprain · walking / sprinting / jumping | +5% / +10% / +15% (one-time) |
| Prone state | −5% |

At **100%** → forced prone + 3 "I hurt, I hurt" messages (self-only);
dropping to **80%** restores movement.

### 2.5 Recovery Period (`gameplay.bleeding`)

**5 minutes** after treatment. Sprinting **> 15 seconds** or **the 4th jump** → re-opens (blood volume not reset).
Any sprint/jump **resets the timer** (below threshold does not re-open).

### 2.6 Fall Sprain (`gameplay.sprain`)

Uses vanilla `fallDistance`: ≤5 nothing; (5,10) 35%; [10,15) 50%; [15,20) 90%; ≥20 100%.
Hay Bale **−20 percentage points**; Protection / Feather Falling **−8 per level**, **fully negated at level 4**.
Landing in water (even 1 block), Slow Falling, Elytra, Chorus Fruit, Fire Resistance are **automatically exempt**.

### 2.7 Heartbeat Sound — **not implemented**

The design is fixed (5 tiers following blood volume, a flat-line "beep—" at zero), but the audio assets are owned by the project owner;
**before the assets arrive, do not implement it and do not use any substitute sound**. A `TODO` marker is left in the code.

---

## 3. Key design decisions and why

| Decision | Reason |
|---|---|
| Use `LivingDamageEvent.Post` | The only stage that gives the real **post-armor** damage |
| All attachments **do not** use `copyOnDeath()` | Death must clear every injury; respawn starts from a clean slate |
| Only blood volume is lethal | Avoid "health drain" and "blood volume" fighting; blood volume is the single source of truth |
| Bandage does not restore blood volume | Per spec: treatment only stops bleeding, blood regrows slowly on its own |
| Gameplay is packaged per module | Future fracture/infection each has a home, no interference |
| Build with CI instead of locally | This machine lacks memory for the NeoForm decompile (see section 5) |

---

## 4. Directory & class-name mapping (current, post-rename)

| Old (nofo era) | New (icu) |
|---|---|
| `NofoMod.java` | `IcuMod.java` |
| `ModAttachments.java` | `IcuAttachments.java` |
| `bleed/BleedHandler.java` | `gameplay/bleeding/BleedingFeature.java` |
| `bleed/BleedDamage.java` | `gameplay/bleeding/BleedingDamage.java` |
| `BleedingData` (nested class) | `gameplay/bleeding/BleedingData.java` (own file) |
| `assets/nofo/` | `assets/icu/` |
| `data/nofo/` | `data/icu/` |
| `nofo:bleeding` / `nofo:bleed` | `icu:bleeding` / `icu:bleed` |

---

## 5. How to Build

```bat
cd /d D:\DS\icu
build.bat
```

**Artifact**: `build/libs/icu-Mod-0.2.2.jar` → drop it into the instance's `mods/` folder to use it.

> ⚠️ The artifact name carries `-Mod` to clearly distinguish it from `-sources.jar` (the source jar).
> **The source jar cannot go into `mods/`**: it only has `.java` text and no compiled `.class`,
> yet NeoForge still lists it as a mod (because it contains `neoforge.mods.toml`),
> but not a single line of code runs. The release flow no longer ships the source jar; source became `.zip`.

### 5.1 Known local-build obstacles

| # | Problem | Handling |
|---|---|---|
| 1 | Only JDK 25 installed; Gradle 8.14.3 cannot parse its class files | Use `D:\DS\tools\jdk21`, already bundled in `build.bat` |
| 2 | File sandbox refuses writing `C:\Users\...\.gradle` | `GRADLE_USER_HOME=D:\DS\.gradle-home` (already bundled) |
| 3 | NeoForm's `jst` tool is denied access to the system `TEMP` | `TEMP=TMP=D:\DS\.tmp-build` (already bundled) |
| 4 | **Out of memory**: 15.8 GB physical, 8 GB page-file cap, decompiling Minecraft reports `os::commit_memory failed` | **Use GitHub Actions cloud build** |

> Therefore this project **must** build with CI, or on a machine with enough memory.

### 5.2 Two more required build settings

| Setting | Location | Reason |
|---|---|---|
| `useEclipseCompiler = true` | `build.gradle` → `neoForge { neoFormRuntime { } }` | javac recompiling Minecraft fails; the ECJ path works |
| Do not set `org.gradle.jvmargs` | `gradle.properties` | That setting forces a single-use daemon, which cannot create a named pipe in restricted environments |

### 5.3 CI workflows

| File | Trigger | Role |
|---|---|---|
| `.github/workflows/build.yml` | Push main / PR / manual | Compile check; **on failure writes `BUILD_FAILURES.md`** |
| `.github/workflows/release.yml` | Push `v*` tag | Compile and publish a Release (mod jar + source zip) |

---

## 6. Verified / Unverified (**as-is**)

| Item | Status |
|---|---|
| NeoForge / Java / Gradle version correspondence | ✅ Verified against official Maven metadata |
| Event and attachment API signatures | ✅ Verified from NeoForge 21.1.252 source |
| Tag names `minecraft:swords` / `minecraft:axes` | ✅ Verified from the 1.21.1 official jar entries |
| `pack_format` = 34 (1.21/1.21.1) | ✅ Verified against the Minecraft Wiki |
| 0.0.x compile and in-game load | ✅ Once verified by actually running (server and client both loaded) |
| **0.1.0 compile** | ✅ CI compile passed |
| **0.2.1 compile (with JEI integration)** | ✅ **CI compile passed** |
| **0.2.2 (review fixes + docs) compile** | ⏳ Auto-checked by CI after pushing to main (this machine cannot compile due to memory limits) |
| **In-game testing** | ❌ **Not done** — needs a person to test each mechanic in-game |

---

## 7. Known Limitations and Next Steps

**Limitations**
1. Trigger threshold 7 is high; iron or better almost never bleeds (chosen by design).
2. Heartbeat sound not implemented (awaiting audio assets).
3. "Prone" is simulated with the `SWIMMING` pose, not a true prone pose.
4. Pain Level has no HUD display; it is a hidden value.
5. Only the **attacker's main hand** weapon counts; offhand, projectiles and indirect damage do not trigger.

**Suggested next steps (need separate confirmation)**
- Heartbeat sound (awaiting assets)
- HUD showing Blood Volume / Pain Level
- More damage types (fractures, organ damage, infection)
- Automatic recovery for sprains (currently only static rest lowers pain)

---

## 8. Change Index

| Want to change | Which file | What to look for |
|---|---|---|
| Trigger threshold (7) | `gameplay/bleeding/BleedingFeature.java` | `TRIGGER_DAMAGE` |
| Blood loss per second (5%) | `gameplay/blood/BloodVolumeData.java` | `LOSS_PER_SECOND` |
| Natural regeneration rate | `gameplay/blood/BloodVolumeData.java` | `REGEN_AMOUNT` / `REGEN_INTERVAL_TICKS` |
| Bandage duration (3 seconds) | `item/BandageItem.java` | `USE_TICKS` |
| Pain values | `gameplay/pain/PainFeature.java` | The 5 constants at the top |
| Sprain chance / reductions | `gameplay/sprain/SprainFeature.java` | `CHANCE_TIER_*` / `HAY_CHANCE_REDUCTION` / `CHANCE_PER_ENCHANT_LEVEL` |
| Recovery duration & thresholds | `gameplay/bleeding/BleedingRecoveryData.java` | `DURATION_TICKS` / `SPRINT_LIMIT_TICKS` / `JUMP_LIMIT` |
| Bandage recipe | `resources/data/icu/recipe/bandage.json` | ingredients |
| Death and message text | `resources/assets/icu/lang/zh_cn.json` | `death.attack.bleed` / `message.icu.*` |
| Version and name | `gradle.properties` | `mod_*` / `neo_version` |
