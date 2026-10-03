> 🌐 **English** · [中文](DEVELOPMENT.md)

# Development Conventions

> Read this **before** adding a feature or changing code. Follow it and the project will not drift.

---

## 1. Naming Rules (only one name)

| Thing | Rule | Example |
|---|---|---|
| mod id | All lowercase, matching `mod_id` in `gradle.properties` | `icu` |
| Java package | `com.icu.icu`; gameplay always goes in `com.icu.icu.gameplay.<module>` | `com.icu.icu.gameplay.bleeding` |
| Module package name | Use a **gameplay noun**, not `handler`/`utils`/`misc` | `bleeding`, `fracture`, `infection` |
| Class names | See the table below | `BleedingFeature` |
| Resource directory | Namespace must equal the mod id | `assets/icu/`, `data/icu/` |
| Language key | `<category>.<namespace>.<name>` | `effect.icu.bleeding` |

> **Forbidden** to introduce a second mod name (the historical `nofo` has been fully removed).
> Repo name `ICU`, directory `icu`, mod id `icu`, package `com.icu.icu` — these are the only four correspondences.

---

## 2. One gameplay module = one package

```
gameplay/<module>/
├── <module>Data.java      Data: what this feature stores on the player (can be several)
├── <module>Damage.java    Damage: only create when a custom damage type is needed
└── <module>Feature.java   Rules: trigger conditions, per-tick behavior, end conditions (★ core)
```

**Current reference** (0.2.2):

| Module | Files |
|---|---|
| `bleeding` | `BleedingData` / `BleedingDamage` / `BleedingFeature` / `BleedingRecoveryData` / `BleedingRecoveryFeature` |
| `blood` | `BloodVolumeData` |
| `pain` | `PainData` / `PainFeature` |
| `sprain` | `SprainData` / `SprainFeature` |

> The data class and the data-rules class can be split (e.g. `BleedingRecoveryData` + `BleedingRecoveryFeature`),
> when a feature's rules grow too large for one class.

**Responsibility boundaries (do not mix)**

| File | Only should | Should not |
|---|---|---|
| `Data` | Store state, serialize, expose getters/mutators | Subscribe to events, write game logic |
| `Damage` | Build a `DamageSource` | Decide trigger conditions |
| `Feature` | Rule decisions and applying effects, subscribe to events | Directly `new` registration objects |

**Where registration goes**: all `AttachmentType`s register in `IcuAttachments`, items register in `IcuItems`,
both are invoked once by `IcuMod`. Gameplay modules **do not register themselves**, to avoid registration-order issues.

---

## 3. Where to subscribe to events

Gameplay modules use `@EventBusSubscriber(modid = IcuMod.MODID)` on the `Feature` class,
and event methods must be `public static`. Do **not** scatter event subscriptions across multiple classes.

---

## 4. Rule constants are public at the top of the class

```java
// Good: anyone tuning values sees them at a glance
public static final float TRIGGER_DAMAGE = 7.0F;
public static final float LOSS_PER_SECOND = 5.0F;

// Bad: numbers scattered through the logic
if (event.getNewDamage() <= 7.0F) { ... }
```

---

## 5. Full steps to add a module

Using "fracture" as the example:

1. Create the package `com.icu.icu.gameplay.fracture`
2. Write `FractureData.java` (e.g. fracture site + severity) with a `Codec`
3. Add a field in `IcuAttachments`:
   ```java
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<FractureData>> FRACTURE =
           ATTACHMENT_TYPES.register("fracture",
                   () -> AttachmentType.builder(() -> new FractureData())
                           .serialize(FractureData.CODEC)
                           .build());
   ```
   > ⚠️ Note `AttachmentType.builder(() -> new X())` must be written as a lambda;
   > writing `X::new` causes a compile ambiguity from the `Supplier` / `Function` overloads.
4. Write `FractureFeature.java`, subscribing to events with `@EventBusSubscriber`
5. Need a custom damage type → add `data/icu/damage_type/<name>.json` + `FractureDamage.java`
6. Need text → add keys to `assets/icu/lang/zh_cn.json` and `en_us.json`
7. Add a row to "Current Features" in `README.md`
8. Verify with `build.bat` compile before committing

---

## 6. How to change the JEI integration (`compat/jei/`)

JEI is an **optional dependency**; remember three rules before changing anything here.

### 6.1 Three rules

| Rule | Why |
|---|---|
| **JEI must use `compileOnly`** | Bundling it into the jar conflicts with the player's JEI; `build.gradle` already sets it up |
| **`mods.toml` must say `optional`** | Declaring `required` makes players without JEI **unable to start** |
| **Classes only under `compat/jei/`** | Without JEI these classes are never loaded; mixing into the main package explodes at class-load time |

### 6.2 Existing files

| File | Role |
|---|---|
| `IcuJeiPlugin.java` | `@JeiPlugin` entry; `registerRecipes` adds the info page and the category page |
| `recipe/IcuOverviewCategory.java` | `RecipeType` + an `AbstractRecipeCategory` subclass that draws the page |
| `recipe/IcuInfoRecipe.java` | Page data (record: input stack / output stack / text keys) |

### 6.3 Add an info page (simplest)

In `IcuJeiPlugin.registerRecipes` add:

```java
registration.addItemStackInfo(
        new ItemStack(SomeItem.get()),
        Component.translatable("jei.icu.xxx.info.1"),
        Component.translatable("jei.icu.xxx.info.2"));
```

Then add those two keys to `assets/icu/lang/zh_cn.json` and `en_us.json`. **No new class needed.**

### 6.4 Add a new category page

1. Follow `IcuOverviewCategory` to write a class, `RecipeType.create(icu, "<name>", YourRecipe.class)`
2. In `IcuJeiPlugin.registerCategories` call `addRecipeCategories(new YourCategory(...))`
3. In `registerRecipes` call `addRecipes(YourCategory.RECIPE_TYPE, List.of(...))`
4. Add the language keys

### 6.5 ⚠️ What to do before changing JEI code

**JEI's API changes frequently; do not write from memory.** Last time writing `Player.getEnchantmentLevel` from memory caused a compile failure.

The correct way: **download the JEI jar first and use `javap` to check the real signature**:

```bash
# Download the matching version from Modrinth (see jei_version in gradle.properties)
javap -classpath jei-1.21.1-neoforge-19.57.0.450.jar mezz.jei.api.registration.IRecipeRegistration
```

Signatures already verified this round (safe to copy):

| API | Signature |
|---|---|
| `RecipeType.create` | `static <T> RecipeType<T> create(String namespace, String path, Class<? extends T> cls)` |
| `AbstractRecipeCategory` constructor | `(RecipeType<T>, Component title, IDrawable icon, int width, int height)` |
| `IRecipeLayoutBuilder.addSlot` | `addSlot(RecipeIngredientRole, int x, int y)` → `IRecipeSlotBuilder` |
| Fill a slot | `IIngredientAcceptor.addItemStack(ItemStack)` |
| Add text | `IRecipeExtrasBuilder.addText(FormattedText, int x, int y)` → `ITextWidget` |
| Text line spacing | `ITextWidget.setLineSpacing(int)` |
| Use an item as the icon | `IGuiHelper.createDrawableItemStack(ItemStack)` |
| Info page | `IRecipeRegistration.addItemStackInfo(ItemStack, Component...)` |

> ❌ **APIs that do not exist (do not use)**: `ITextWidget.setMaxWidth` (width is decided by the `addText` position),
> `Player.getEnchantmentLevel`, `mezz.jei.api.recipe.IRecipe`, `VanillaRecipeCategoryUid` (removed in 19.x).

### 6.6 Vanilla crafting recipes do NOT need JEI code

JEI automatically reads every vanilla recipe in the recipe manager. The bandage recipe (3 Paper + 1 String + 1 Wool) and its
"uses / ingredients" views are **already automatic**; adding JEI code is only an **enhancement**, not what makes them visible.

---

## 7. Known version pitfalls (1.21.1 specific)

| Pitfall | Fact | Correct form |
|---|---|---|
| Nausea effect name | In 1.21.1 it is `CONFUSION`, **not** `NAUSEA` | `MobEffects.CONFUSION` |
| Attachment registry | 1.21.1 has no `AttachmentRegistry` | Use `DeferredRegister` + `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` |
| `AttachmentType.builder` | The `Supplier` and `Function` overloads are ambiguous | Pass a lambda, not a method reference |
| Sword/axe item tags | Real names are `minecraft:swords` / `minecraft:axes` | `ItemTags.SWORDS` / `ItemTags.AXES` |
| Damage timing | To get the **post-armor** damage | Use `getNewDamage()` on `LivingDamageEvent.Post` |
| **Enchantment level lookup** | **`Player.getEnchantmentLevel(Holder)` does not exist** | `EnchantmentHelper.getEnchantmentLevel(Holder, LivingEntity)` |
| Slow Falling potion | Vanilla still fires `LivingFallEvent`, only the damage is 0 | Check `hasEffect(MobEffects.SLOW_FALLING)` yourself |
| Item registration | 1.21.1 uses `DeferredRegister.Items` | `DeferredRegister.createItems(MODID)` |
| Adding to the creative tab | The event is on the mod bus | `BuildCreativeModeTabContentsEvent` + `modEventBus.addListener` |

---

## 8. Pre-commit Checklist

- [ ] After pushing to main, **CI compile passes** (cannot compile locally; see HANDOVER section 5)
- [ ] `BUILD_FAILURES.md` has no new entry for this commit
- [ ] Full-text search confirms no old name remains (e.g. `nofo`)
- [ ] Resource namespace matches the mod id
- [ ] New/changed value constants sit at the top of the class with comments
- [ ] New attachments are in `IcuAttachments` and do **not** use `copyOnDeath()`
- [ ] Changed JEI code → verified the real signature with `javap` (see section 6.5)
- [ ] Changed JEI code → still only `compileOnly` + `optional`
- [ ] Docs synced (`CHANGELOG.md` / `PROJECT_STRUCTURE.md` updated with new files)
