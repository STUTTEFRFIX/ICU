> 🌐 **中文** · [English](PROJECT_STRUCTURE.en.md)

# 项目结构说明

> 看完这一页即可知道：每个文件在哪、干什么、要改哪里。
> 这是**唯一的命名对照**：仓库 `ICU` = 目录 `icu` = mod id `icu` = 包 `com.icu.icu`。

---

## 1. 一句话简介

`ICU` 是一个 **Minecraft 1.21.1 / NeoForge** 模组，主题是「还原真实的身体损伤与医疗体系」。
当前版本 **0.2.2**，实现了 **4 个玩法模块 + 1 个物品 + JEI 集成**：大出血、血容量、疼痛值、摔落崴脚、绷带。

---

## 2. 目录结构

```
icu/
├── README.md                        # ★ 入口页：这是什么、怎么构建、代码从哪看起
├── BUILD_FAILURES.md                # CI 编译失败时自动追加记录
├── build.gradle                     # ModDevGradle 配置、运行配置、jar 清单
├── gradle.properties                # ★ 唯一配置入口（mod id / 版本 / NeoForge 版本）
├── settings.gradle                  # 仓库地址 + Foojay（自动装 JDK 21）+ 工程名
├── build.bat                        # 一键构建脚本（已内置本机所需环境变量）
├── gradlew / gradlew.bat            # Gradle wrapper（免装 Gradle）
├── .github/workflows/
│   ├── build.yml                    # ★ 推 main 自动编译检查（失败会记录）
│   └── release.yml                  # ★ 推 v* 标签 → 云端编译并发布 Release
├── .gitignore                       # 排除 build/ run/ 等产物
│
├── docs/
│   ├── CHANGELOG.md                 # ★ 功能清单 + 变更总账（先看这个）
│   ├── PROJECT_STRUCTURE.md         # 本文件：每个文件干什么
│   ├── DEVELOPMENT.md               # 开发约定：加新功能照这个来
│   ├── PROJECT_REPORT.md            # 完整项目管理报表
│   └── HANDOVER.md                  # 交接报告：规格、决策、当前状态
│
├── gradle/wrapper/
│   ├── gradle-wrapper.jar           # wrapper 本体
│   └── gradle-wrapper.properties    # 锁定 Gradle 8.14.3
│
└── src/main/
    ├── java/com/icu/icu/
    │   ├── IcuMod.java              # ★ 起点：模组入口，只做注册
    │   ├── IcuAttachments.java      # ★ 全部持久化玩家数据的唯一注册点
    │   ├── IcuItems.java            # 物品注册
    │   ├── item/
    │   │   └── BandageItem.java     # 绷带：右键按住 3 秒治疗大出血
    │   ├── compat/jei/              # ★ JEI 集成（可选，不装 JEI 也能跑）
    │   │   ├── IcuJeiPlugin.java            # 插件入口：信息页 + 注册分类
    │   │   └── recipe/
    │   │       ├── IcuInfoRecipe.java       # 一页的数据（输入/输出/文案键）
    │   │       └── IcuOverviewCategory.java # 「大出血流程」分类页
    │   └── gameplay/                # ★ 所有玩法的家，一个玩法一个子包
    │       ├── IcuPose.java         # ★ 唯一姿态控制点：谁趴下、谁锁移动（见下）
    │       ├── bleeding/            # 大出血 + 恢复期
    │       │   ├── BleedingData.java          # 数据：出血层数
    │       │   ├── BleedingDamage.java        # 伤害类型 icu:bleed（致死）
    │       │   ├── BleedingFeature.java       # ★ 规则：触发、失血、效果
    │       │   ├── BleedingRecoveryData.java  # 数据：恢复期计时/跳跃/奔跑
    │       │   └── BleedingRecoveryFeature.java # 规则：扯伤口复发
    │       ├── blood/
    │       │   └── BloodVolumeData.java       # 血容量 0–100、扣除与自然恢复
    │       ├── pain/
    │       │   ├── PainData.java              # 数据：共享疼痛值
    │       │   └── PainFeature.java           # ★ 规则：各来源增减与倒地
    │       └── sprain/
    │           ├── SprainData.java            # 数据：是否崴脚
    │           └── SprainFeature.java         # ★ 规则：摔落概率、豁免、修正
    │
    ├── resources/
    │   ├── pack.mcmeta
    │   ├── assets/icu/            # 命名空间必须 = mod id
    │   │   ├── lang/{zh_cn,en_us}.json     # 死亡消息 + 提示 + 物品名
    │   │   ├── models/item/bandage.json
    │   │   └── textures/item/bandage.png
    │   └── data/
    │       ├── icu/damage_type/bleed.json  # 伤害类型定义
    │       └── icu/recipe/bandage.json     # 绷带配方 3纸+1线+1羊毛
    │
    └── templates/META-INF/
        └── neoforge.mods.toml         # 模组元数据（构建时用 gradle.properties 填充）
```

---

## 3. 每个文件的职责

### 构建配置

| 文件 | 职责 | 什么时候改 |
|---|---|---|
| `gradle.properties` | 模组标识（`mod_id`/`mod_name`/`mod_version`）与平台版本（`minecraft_version`/`neo_version`） | 改名、升版本、升级 NeoForge |
| `build.gradle` | 应用 ModDevGradle、声明运行配置、生成 mod 元数据、Java 21 工具链、产物命名 | 加依赖、加运行配置 |
| `settings.gradle` | 仓库地址 + Foojay 插件 | 换仓库 |
| `build.bat` | 带环境变量调用 `gradlew build` | 换 JDK/缓存路径 |
| `.github/workflows/build.yml` | 推 main 自动编译，失败写 `BUILD_FAILURES.md` | 改编译策略 |
| `.github/workflows/release.yml` | 推 `v*` 标签自动构建并发布 Release | 改发布策略 |

### 源码

| 文件 | 关键内容 |
|---|---|
| `IcuMod.java` | `MODID = "icu"`；只做注册，并给创造模式物品栏加绷带 |
| `IcuAttachments.java` | 5 个附件：`BLEEDING` / `BLOOD_VOLUME` / `BLEEDING_RECOVERY` / `PAIN` / `SPRAIN`；**全部不使用 `copyOnDeath()`**，死亡即清空 |
| `IcuItems.java` | 注册绷带（堆叠 16）|
| `BandageItem.java` | `USE_TICKS = 60`（3 秒）；完成时清除出血、开启恢复期、**不动血容量** |
| `BleedingFeature.java` | `TRIGGER_DAMAGE = 7.0F`；触发判定、每秒失血、强制趴下、状态效果、粒子、归零致死；创造/旁观/无敌玩家清出出血 |
| `BleedingRecoveryFeature.java` | 监听跳跃与奔跑；`>15 秒跑` 或 `第 4 跳` → 复发 |
| `BloodVolumeData.java` | `LOSS_PER_SECOND = 5`；`REGEN_AMOUNT = 5` / `REGEN_INTERVAL_TICKS = 12000`（10 分钟）|
| `PainFeature.java` | 出血 +10、崴脚走 +5/跑 +10/跳 +15、趴下 −5；100% 倒地、80% 恢复 |
| `SprainFeature.java` | 5 档概率、干草块 −20、附魔每级 −8 且 4 级全免、入水/缓降/抗火/鞘翅豁免 |

### ★ `IcuPose.java` —— 唯一姿态控制点（重要）

**背景（v0.2.1 修的一个真 bug）**：原本出血模块和疼痛模块**各自调用 `setPose`**，
出血每 tick 设 `SWIMMING`，疼痛每秒设 `SWIMMING`/`STANDING` —— 两者互相覆盖，
第三人称看起来就是**站立/趴地反复闪烁**。

**规矩**：

| 规则 | 说明 |
|---|---|
| **只有 `IcuPose` 能调 `setPose`** | 其它模块一律**禁止**碰姿态，否则又会互相打架 |
| 每 tick 计算一次 | 因为移动输入也是每 tick 生效；每秒算一次会被玩家按键推出去 |
| 触发条件 | 出血中 **或** 疼痛达到 100% → 强制趴下 |
| 解除 | 两个条件都不满足 → 恢复 `STANDING`（只撤销我们造成的趴下，真游泳不动）|
| 锁的内容 | 趴地姿态 + 水平速度清零（兼锁击退）+ 取消向上速度（禁跳）|

> 其它模块想知道"玩家现在是不是被强制趴下"，应调用 `IcuPose.isForcedProne(player)`。

### 各 Feature 内部结构

| 方法 | 作用 |
|---|---|
| `BleedingFeature.onLivingDamagePost` | **触发判定**：玩家 + 非创造/旁观 + 最终伤害 > 7 + 手持剑/斧 → 层数 +1 |
| `BleedingFeature.onPlayerTick` | 每 tick：扒下锁定与状态效果；每 20 tick：粒子 + 血容量 −5 + 归零致死 |
| `BleedingFeature.onPlayerRespawn` | **死亡清理**：出血/恢复期/血容量/疼痛/崴脚 全部复位 |
| `BleedingRecoveryFeature.onLivingJump` | 恢复期内记跳跃，超限则复发 |
| `BleedingRecoveryFeature.onPlayerTick` | 恢复期计时；奔跑累计判定；非奔跑则清连续奔跑 |
| `PainFeature.onPlayerTick` | 每秒汇总所有来源，更新疼痛，处理倒地与恢复 |
| `SprainFeature.onLivingFall` | 用原版 `fallDistance` 判定，逐项应用豁免与修正 |

---

## 4. 数据流（一次完整的「被重击 → 死亡」）

```
玩家被持剑/斧实体攻击
        │
        ▼
LivingDamageEvent.Post   ← 护甲/附魔/抗性已全部结算
        │  最终伤害 > 7 ？
        ▼
Attachment BLEEDING 层数 +1
        │
        ▼
PlayerTickEvent.Post（每 tick）
        ├── setPose(SWIMMING) + 速度/击退全锁
        ├── 每 20 tick：血粒子 + 血容量 −5%
        ├── 每 20 tick：PainData +10%
        └── 每 tick：CONFUSION + DARKNESS 刷新
        │
        ├── 血容量 = 0  → 立即死亡（icu:bleed）
        ├── 疼痛 = 100  → 强制趴下 + 弹 3 次「我好疼」
        │
        ▼
使用绷带（按住 3 秒）
        ├── 成功 → 出血解除、血容量保留、进入恢复期 5 分钟
        └── 3 秒内血容量归零 → 死亡
        │
        ▼
恢复期内 跑 >15 秒 或 第 4 跳 → 复发（血容量不重置）
```

---

## 5. 「改这里会怎样」

| 想改的东西 | 改哪个文件 | 具体位置 |
|---|---|---|
| 触发阈值（现在 7） | `BleedingFeature.java` | `TRIGGER_DAMAGE` |
| 每秒失血（现在 5%） | `BloodVolumeData.java` | `LOSS_PER_SECOND` |
| 自然恢复速度（现在 10 分钟 5%） | `BloodVolumeData.java` | `REGEN_AMOUNT` / `REGEN_INTERVAL_TICKS` |
| 绷带使用时长（现在 3 秒） | `BandageItem.java` | `USE_TICKS` |
| 绷带生效门槛（现在 15%） | `BandageItem.java` | `MIN_BLOOD_TO_TREAT` |
| 疼痛各项数值 | `PainFeature.java` | 顶部 5 个常量 |
| 崴脚概率档位 | `SprainFeature.java` | `CHANCE_TIER_1..4` |
| 干草块减免（现在 20） | `SprainFeature.java` | `HAY_CHANCE_REDUCTION` |
| 附魔每级减免（现在 8） | `SprainFeature.java` | `CHANCE_PER_ENCHANT_LEVEL` |
| 完全免除等级（现在 4） | `SprainFeature.java` | `FULL_IMMUNITY_LEVEL` |
| 恢复期时长 / 复发阈值 | `BleedingRecoveryData.java` | `DURATION_TICKS` / `SPRINT_LIMIT_TICKS` / `JUMP_LIMIT` |
| 武器白名单 | `BleedingFeature.isBladedWeaponAttack` | 用原版 `ItemTags.SWORDS` / `ItemTags.AXES`（不再有自定义覆盖文件）|
| 死亡与提示文案 | `resources/assets/icu/lang/zh_cn.json` | `death.attack.bleed` / `message.icu.*` |
| mod id / 版本 / 显示名 | `gradle.properties` | 对应字段 |

---

## 6. 依赖与版本（已核实）

| 组件 | 版本 | 说明 |
|---|---|---|
| Minecraft | 1.21.1 | `minecraft_version` |
| NeoForge | 21.1.252 | 1.21.1 对应的最新 21.1.x |
| ModDevGradle | 1.0.24 | 1.21.1 同期插件线 |
| Gradle | 8.14.3 | 由 wrapper 锁定 |
| Java | 21 | 构建目标；由 Foojay 自动下载 |

---

## 7. 构建产物

| 产物 | 路径 |
|---|---|
| 模组 jar | `build/libs/icu-Mod-0.2.2.jar` |
| 源码 jar | `build/libs/icu-Mod-0.2.2-sources.jar`（**不要**装进 `mods/`） |

安装方式：把 `icu-Mod-0.2.2.jar` 放进对应实例的 `mods/` 文件夹。

**发布到 Release 的文件**（由 CI 产出）：

| 文件 | 用途 |
|---|---|
| `icu-Mod-<版本>.jar` | 模组本体，装进 `mods/` |
| `icu-<版本>-src.zip` | 源码压缩包（`.zip`，**装不进 `mods/`**，供阅读/二次开发）|
