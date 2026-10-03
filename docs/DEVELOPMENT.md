> 🌐 **中文** · [English](DEVELOPMENT.en.md)

# 开发约定（DEVELOPMENT）

> 加功能、改代码**之前**先看这份。照着做，项目就不会长歪。

---

## 1. 命名规则（只允许一套名字）

| 东西 | 规则 | 例子 |
|---|---|---|
| mod id | 全小写，与 `gradle.properties` 的 `mod_id` 一致 | `icu` |
| Java 包 | `com.icu.icu`，玩法一律放 `com.icu.icu.gameplay.<模块>` | `com.icu.icu.gameplay.bleeding` |
| 模块包名 | 用**玩法名词**，不用 `handler`/`utils`/`misc` | `bleeding`、`fracture`、`infection` |
| 类名 | 见下表 | `BleedingFeature` |
| 资源目录 | 命名空间必须等于 mod id | `assets/icu/`、`data/icu/` |
| 语言键 | `<类别>.<命名空间>.<名字>` | `effect.icu.bleeding` |

> **禁止**再出现第二个 mod 名（历史上的 `nofo` 已被彻底移除）。
> 仓库名 `ICU`、目录 `icu`、mod id `icu`、包 `com.icu.icu` —— 这是唯一的四个对应关系。

---

## 2. 一个玩法模块 = 一个包

```
gameplay/<模块>/
├── <模块>Data.java      数据：这个玩法要在玩家身上存什么（可多个）
├── <模块>Damage.java    伤害：需要自定义伤害类型时才建
└── <模块>Feature.java   规则：触发条件、每 tick 行为、结束条件（★核心）
```

**现状参考**（0.2.2）：

| 模块 | 文件 |
|---|---|
| `bleeding` | `BleedingData` / `BleedingDamage` / `BleedingFeature` / `BleedingRecoveryData` / `BleedingRecoveryFeature` |
| `blood` | `BloodVolumeData` |
| `pain` | `PainData` / `PainFeature` |
| `sprain` | `SprainData` / `SprainFeature` |

> 数据类和数据规则类可以拆开（如 `BleedingRecoveryData` + `BleedingRecoveryFeature`），
> 当一个玩法的规则多到一类装不下时就拆。

**职责边界（别混）**

| 文件 | 只该做的事 | 不该做的事 |
|---|---|---|
| `Data` | 存状态、序列化、暴露 getter/改变方法 | 订阅事件、写游戏逻辑 |
| `Damage` | 造 `DamageSource` | 判断触发条件 |
| `Feature` | 规则判定与效果施加、订阅事件 | 直接 new 注册对象 |

**注册放哪**：所有 `AttachmentType` 统一注册在 `IcuAttachments`，物品统一注册在 `IcuItems`，
两者都由 `IcuMod` 调用一次。玩法模块**不自己注册**，避免注册顺序问题。

---

## 3. 事件订阅的位置

玩法模块用 `@EventBusSubscriber(modid = IcuMod.MODID)` 标注在 `Feature` 类上，
事件方法必须是 `public static`。**不要**把事件订阅散落在多个类里。

---

## 4. 规则常量一律公开放在类顶部

```java
// 好：想调数值的人一眼就看到
public static final float TRIGGER_DAMAGE = 7.0F;
public static final float LOSS_PER_SECOND = 5.0F;

// 差：数字散落在逻辑中间
if (event.getNewDamage() <= 7.0F) { ... }
```

---

## 5. 新增一个模块的完整步骤

以「骨折（fracture）」为例：

1. 建包 `com.icu.icu.gameplay.fracture`
2. 写 `FractureData.java`（例如：骨折部位 + 严重度），带 `Codec`
3. 在 `IcuAttachments` 加一个字段：
   ```java
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<FractureData>> FRACTURE =
           ATTACHMENT_TYPES.register("fracture",
                   () -> AttachmentType.builder(() -> new FractureData())
                           .serialize(FractureData.CODEC)
                           .build());
   ```
   > ⚠️ 注意 `AttachmentType.builder(() -> new X())` 必须写成 lambda，
   > 写成 `X::new` 会因 `Supplier` / `Function` 重载而编译歧义。
4. 写 `FractureFeature.java`，用 `@EventBusSubscriber` 订阅事件
5. 需要自定义伤害类型 → 加 `data/icu/damage_type/<名字>.json` + `FractureDamage.java`
6. 需要文案 → 加 `assets/icu/lang/zh_cn.json` 与 `en_us.json` 的键
7. 在 `README.md` 的「当前功能一览」补一行
8. `build.bat` 编译验证通过后再提交

---

## 6. JEI 集成怎么改（`compat/jei/`）

JEI 是**可选依赖**，改这里之前先记住三条规矩。

### 6.1 三条规矩

| 规矩 | 为什么 |
|---|---|
| **JEI 只能用 `compileOnly`** | 打进 jar 会与玩家的 JEI 冲突；`build.gradle` 里已这么配 |
| **`mods.toml` 里必须是 `optional`** | 声明成 `required` 会导致没装 JEI 的玩家**无法启动** |
| **类只放在 `compat/jei/` 下** | 不装 JEI 时这些类永远不被加载；混进主包会在类加载阶段炸 |

### 6.2 现有文件

| 文件 | 作用 |
|---|---|
| `IcuJeiPlugin.java` | `@JeiPlugin` 入口；`registerRecipes` 加信息页与分类页 |
| `recipe/IcuOverviewCategory.java` | `RecipeType` + `AbstractRecipeCategory` 子类，画那一页 |
| `recipe/IcuInfoRecipe.java` | 页面数据（record：输入栈 / 输出栈 / 文案键）|

### 6.3 加一个信息页（最简单）

在 `IcuJeiPlugin.registerRecipes` 里加一段：

```java
registration.addItemStackInfo(
        new ItemStack(SomeItem.get()),
        Component.translatable("jei.icu.xxx.info.1"),
        Component.translatable("jei.icu.xxx.info.2"));
```

再在 `assets/icu/lang/zh_cn.json` 与 `en_us.json` 补上那两个键即可。**不需要新类。**

### 6.4 加一个新分类页

1. 仿照 `IcuOverviewCategory` 写一个类，`RecipeType.create(icu, "<名字>", YourRecipe.class)`
2. 在 `IcuJeiPlugin.registerCategories` 里 `addRecipeCategories(new YourCategory(...))`
3. 在 `registerRecipes` 里 `addRecipes(YourCategory.RECIPE_TYPE, List.of(...))`
4. 补语言键

### 6.5 ⚠️ 改 JEI 代码前必须做的事

**JEI 的 API 变动很频繁，不要凭记忆写。** 上次就是因为凭记忆写了 `Player.getEnchantmentLevel` 而编译失败。

正确做法：**先把 JEI 的 jar 下载下来，用 `javap` 查真实签名**：

```bash
# 从 Modrinth 下载对应版本（见 gradle.properties 的 jei_version）
javap -classpath jei-1.21.1-neoforge-19.57.0.450.jar mezz.jei.api.registration.IRecipeRegistration
```

本次已核实的签名（可直接照抄）：

| API | 签名 |
|---|---|
| `RecipeType.create` | `static <T> RecipeType<T> create(String namespace, String path, Class<? extends T> cls)` |
| `AbstractRecipeCategory` 构造 | `(RecipeType<T>, Component title, IDrawable icon, int width, int height)` |
| `IRecipeLayoutBuilder.addSlot` | `addSlot(RecipeIngredientRole, int x, int y)` → `IRecipeSlotBuilder` |
| 填槽位 | `IIngredientAcceptor.addItemStack(ItemStack)` |
| 加文字 | `IRecipeExtrasBuilder.addText(FormattedText, int x, int y)` → `ITextWidget` |
| 文字换行间距 | `ITextWidget.setLineSpacing(int)` |
| 用物品当图标 | `IGuiHelper.createDrawableItemStack(ItemStack)` |
| 信息页 | `IRecipeRegistration.addItemStackInfo(ItemStack, Component...)` |

> ❌ **不存在的 API（别用）**：`ITextWidget.setMaxWidth`（宽度由 `addText` 的位置决定）、
> `Player.getEnchantmentLevel`、`mezz.jei.api.recipe.IRecipe`、`VanillaRecipeCategoryUid`（19.x 已移除）。

### 6.6 原版合成配方**不需要**写 JEI 代码

JEI 自动读取配方管理器里的所有原版配方。绷带配方（3 纸 + 1 线 + 1 羊毛）以及它的
「用途 / 原料」两个视图**本来就是自动的**，加 JEI 代码只是**增强**，不是让它能显示。

---

## 7. 已知的版本坑（1.21.1 专有）

| 坑 | 事实 | 正确写法 |
|---|---|---|
| 反胃效果名字 | 1.21.1 里叫 `CONFUSION`，**不是** `NAUSEA` | `MobEffects.CONFUSION` |
| 附件注册表 | 1.21.1 没有 `AttachmentRegistry` | 用 `DeferredRegister` + `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` |
| `AttachmentType.builder` | `Supplier` 与 `Function` 重载会歧义 | 传 lambda，不要传方法引用 |
| 剑/斧物品标签 | 真名是 `minecraft:swords` / `minecraft:axes` | `ItemTags.SWORDS` / `ItemTags.AXES` |
| 伤害结算时机 | 想拿**护甲结算后**的伤害 | 用 `LivingDamageEvent.Post` 的 `getNewDamage()` |
| **附魔等级查询** | **`Player.getEnchantmentLevel(Holder)` 不存在** | `EnchantmentHelper.getEnchantmentLevel(Holder, LivingEntity)` |
| 缓降药水 | 原版仍会触发 `LivingFallEvent`，只是伤害为 0 | 必须自己检查 `hasEffect(MobEffects.SLOW_FALLING)` |
| 物品注册 | 1.21.1 用 `DeferredRegister.Items` | `DeferredRegister.createItems(MODID)` |
| 创造栏加物品 | 事件在 mod 总线 | `BuildCreativeModeTabContentsEvent` + `modEventBus.addListener` |

---

## 8. 提交前的自检清单

- [ ] 推 main 后 **CI 编译通过**（本机编译不了，见 HANDOVER 第 5 节）
- [ ] `BUILD_FAILURES.md` 没有新增本次提交的记录
- [ ] 全文搜索确认没有旧名字残留（如 `nofo`）
- [ ] 资源命名空间与 mod id 一致
- [ ] 新增/修改的数值常量都放在类顶部并带注释
- [ ] 新附件已加进 `IcuAttachments` 且**没有** `copyOnDeath()`
- [ ] 改了 JEI 相关代码 → 已用 `javap` 核对真实签名（见第 6.5 节）
- [ ] 改了 JEI 相关代码 → 仍然只用 `compileOnly` + `optional`
- [ ] 文档同步更新（`CHANGELOG.md` / `PROJECT_STRUCTURE.md` 补上新文件）
