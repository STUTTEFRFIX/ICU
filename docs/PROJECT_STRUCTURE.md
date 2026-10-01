# 项目结构说明

> 看完这一页即可知道：每个文件在哪、干什么、要改哪里。
> 这是**唯一的命名对照**：仓库 `ICU` = 目录 `icu` = mod id `icu` = 包 `com.icu.icu`。

---

## 1. 一句话简介

`ICU` 是一个 **Minecraft 1.21.1 / NeoForge** 模组，主题是「还原真实的身体损伤与医疗体系」。
当前版本 0.0.1 **只实现了一个玩法模块：大出血（bleeding）**。

---

## 2. 目录结构

```
icu/
├── README.md                        # ★ 入口页：这是什么、怎么构建、代码从哪看起
├── build.gradle                     # ModDevGradle 配置、运行配置、jar 清单
├── gradle.properties                # ★ 唯一配置入口（mod id / 版本 / NeoForge 版本）
├── settings.gradle                  # 仓库地址 + Foojay（自动装 JDK 21）+ 工程名
├── build.bat                        # 一键构建脚本（已内置本机所需环境变量）
├── gradlew / gradlew.bat            # Gradle wrapper（免装 Gradle）
├── .github/workflows/release.yml    # ★ 自动构建：推送 v* 标签 → 云端编译并发布 Release
├── .gitignore                       # 排除 build/ run/ 等产物
│
├── docs/
│   ├── PROJECT_STRUCTURE.md         # 本文件：每个文件干什么
│   ├── DEVELOPMENT.md               # 开发约定：加新功能照这个来
│   └── HANDOVER.md                  # 交接报告：规格、决策、当前状态
│
├── gradle/wrapper/
│   ├── gradle-wrapper.jar           # wrapper 本体
│   └── gradle-wrapper.properties    # 锁定 Gradle 8.14.3
│
└── src/main/
    ├── java/com/icu/icu/
    │   ├── IcuMod.java              # ★ 起点：模组入口，只做注册
    │   ├── IcuAttachments.java      # 数据存储：伤口状态挂在这里
    │   └── gameplay/                # ★ 所有玩法的家，一个玩法一个子包
    │       └── bleeding/            # 大出血模块
    │           ├── BleedingData.java      # 数据：出血层数
    │           ├── BleedingDamage.java    # 伤害：自定义伤害类型 icu:bleed
    │           └── BleedingFeature.java   # ★ 规则：触发、扣血、强制趴下、效果
    │
    ├── resources/
    │   ├── pack.mcmeta
    │   ├── assets/icu/lang/       # 命名空间必须 = mod id
    │   │   ├── zh_cn.json
    │   │   └── en_us.json
    │   └── data/
    │       ├── icu/damage_type/bleed.json          # 「大出血」伤害类型定义
    │       └── minecraft/tags/item/
    │           ├── swords.json                     # 剑类武器标签
    │           └── axes.json                       # 斧类武器标签
    │
    └── templates/META-INF/
        └── neoforge.mods.toml         # 模组元数据（构建时用 gradle.properties 填充）
```

---

## 3. 每个文件的职责

### 构建配置

| 文件 | 职责 | 什么时候改 |
|---|---|---|
| `gradle.properties` | 模组标识（`mod_id`/`mod_name`/`mod_version`）与平台版本（`minecraft_version`/`neo_version`） | 改名、升级 NeoForge |
| `build.gradle` | 应用 ModDevGradle、声明 `client`/`server`/`data` 运行配置、生成 mod 元数据、Java 21 工具链 | 加依赖、加运行配置 |
| `settings.gradle` | 仓库地址 + Foojay 插件 | 换仓库 |
| `build.bat` | 带环境变量调用 `gradlew build` | 换 JDK/缓存路径 |

### 源码

| 文件 | 关键内容 |
|---|---|
| `IcuMod.java` | `MODID = "icu"`；`@Mod` 入口只做一件事：`IcuAttachments.register(modEventBus)` |
| `IcuAttachments.java` | 注册 `AttachmentType<BleedingData> BLEEDING`；用 `Codec.INT` 序列化，**故意不用 `copyOnDeath()`**，所以一死就清零 |
| `BleedingData.java` | 出血层数的载体；`addLayer()` / `clear()` / `isBleeding()` |
| `BleedingDamage.java` | 把 `ResourceKey<DamageType>`（`icu:bleed`）转成 `DamageSource`，并绕过无敌帧 |
| `BleedingFeature.java` | **全部玩法规则**，见下表 |

### `BleedingFeature.java` 内部结构

| 方法 | 作用 |
|---|---|
| `onLivingDamagePost(LivingDamageEvent.Post)` | **触发判定**：是玩家 + 非创造/旁观 + 最终伤害 > 3 + 攻击者手持剑/斧 → 层数 +1 |
| `onPlayerTick(PlayerTickEvent.Post)` | 每 tick 维持：强制趴下、每秒扣血、每秒血粒子、刷新反胃+黑暗 |
| `applyProneLock(Player)` | `setPose(SWIMMING)` + 清零水平速度 + 取消向上速度（禁跳）+ `push(0,0,0)`（锁击退） |
| `applyStatusEffects(Player)` | 每 tick 刷新 `CONFUSION` 与 `DARKNESS`（各 20 tick） |
| `emitBloodParticles(...)` | 用原版 `DAMAGE_INDICATOR` 粒子模拟血点 |
| `onPlayerRespawn(PlayerRespawnEvent)` | **结束条件**：层数清零 + 移除两种负面效果 |

---

## 4. 数据流（一次完整的「被砍 → 死亡」）

```
玩家被持剑/斧实体攻击
        │
        ▼
LivingDamageEvent.Post   ← 护甲/附魔/抗性已全部结算
        │  最终伤害 > 3 ？
        ▼
AttachmentType BLEEDING 层数 +1（无上限）
        │
        ▼
PlayerTickEvent.Post（每 tick）
        ├── setPose(SWIMMING) + 速度/击退全锁
        ├── 每 20 tick：hurt(icu:bleed, 2 × 层数)
        ├── 每 20 tick：血粒子
        └── 每 tick：CONFUSION + DARKNESS 刷新
        │
        ▼
玩家死亡 → 重生
        │
        ▼
PlayerRespawnEvent → 层数清零 + 移除效果
```

---

## 5. 「改这里会怎样」

| 想改的东西 | 改哪个文件 | 具体位置 |
|---|---|---|
| 触发伤害阈值（现在 >3） | `BleedingFeature.java` | `TRIGGER_DAMAGE` |
| 每层每秒扣血（现在 2 点） | `BleedingFeature.java` | `DAMAGE_PER_LAYER` |
| 效果刷新间隔（现在 20 tick） | `BleedingFeature.java` | `EFFECT_REFRESH_TICKS` |
| 扣血/粒子间隔（现在 20 tick） | `BleedingFeature.java` | `TICK_INTERVAL` |
| 允许触发的武器种类 | `resources/data/minecraft/tags/item/*.json` | 增删物品 ID |
| 死亡消息文案 | `resources/assets/icu/lang/zh_cn.json` | `death.attack.bleed` |
| mod id / 版本 / 显示名 | `gradle.properties` | 对应字段 |
| NeoForge 版本 | `gradle.properties` | `neo_version`（须与 `minecraft_version` 匹配） |

---

## 6. 依赖与版本（已核实）

| 组件 | 版本 | 说明 |
|---|---|---|
| Minecraft | 1.21.1 | `minecraft_version` |
| NeoForge | 21.1.252 | 1.21.1 对应的最新 21.1.x（该线共 249 个版本） |
| ModDevGradle | 1.0.24 | 1.21.1 同期插件线 |
| Gradle | 8.14.3 | 由 wrapper 锁定 |
| Java | 21 | 构建目标；由 Foojay 自动下载 |

---

## 7. 构建产物

| 产物 | 路径 |
|---|---|
| 模组 jar | `build/libs/icu-Mod-0.0.3.jar` |
| 源码 jar | `build/libs/icu-Mod-0.0.3-sources.jar`（**不要**装进 `mods/`） |

安装方式：把 `icu-Mod-0.0.3.jar` 放进对应实例的 `mods/` 文件夹。
