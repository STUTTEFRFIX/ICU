# ICU — 项目结构说明（交接用）

> 本文档面向接手人。看完这一页即可知道每个文件在哪、干什么、改哪里。
> 项目根目录：`D:\DS\nofo1.21.1\`

---

## 1. 一句话简介

`ICU` 是一个 **Minecraft 1.21.1 / NeoForge** 模组，主题是「还原真实的身体损伤与医疗体系」。
当前版本（1.0.0）**只实现了第一个功能：大出血（Haemorrhage）**。除此之外没有任何其它内容。

---

## 2. 目录结构

```
nofo1.21.1/
├── settings.gradle                  # 仓库声明 + Foojay（自动下载 JDK 21）+ 工程名
├── build.gradle                     # ModDevGradle 配置、NeoForge 版本、运行配置、jar 清单
├── gradle.properties                # ★ 唯一需要改的配置入口（modid/版本/NeoForge 版本）
├── build.bat                        # ★ 一键构建脚本（已内置本机所需的环境变量）
├── gradlew / gradlew.bat            # Gradle wrapper 启动脚本（免装 Gradle）
├── .gitignore                       # 排除 build/ run/ 等产物
│
├── docs/                            # 交接文档
│   ├── PROJECT_STRUCTURE.md         # 本文件：项目结构说明
│   └── HANDOVER.md                  # 交接报告：功能规格、设计决策、构建结果
│
├── gradle/wrapper/
│   ├── gradle-wrapper.jar           # wrapper 本体（43,764 字节）
│   └── gradle-wrapper.properties    # 锁定 Gradle 8.14.3 发行版
│
├── src/main/templates/META-INF/
│   └── neoforge.mods.toml           # 模组元数据模板（构建时用 gradle.properties 的值填充）
│
├── src/main/resources/              # 资源与数据包（进 jar 的内容）
│   ├── pack.mcmeta                  # 资源包声明（pack_format 34 = 1.21/1.21.1）
│   ├── assets/nofo/lang/
│   │   ├── zh_cn.json               # 中文死亡消息
│   │   └── en_us.json               # 英文死亡消息
│   └── data/
│       ├── nofo/damage_type/bleed.json      # 「大出血」伤害类型定义
│       └── minecraft/tags/item/
│           ├── swords.json                  # 剑类武器标签
│           └── axes.json                    # 斧类武器标签
│
└── src/main/java/com/nofo/nofo/     # 源码（包结构 = 组名 + modid）
    ├── NofoMod.java                 # 模组入口（@Mod），注册总入口
    ├── ModAttachments.java          # 出血层数的数据存储（Attachment 注册）
    └── bleed/                       # 大出血功能全部逻辑，集中在这一个包
        ├── BleedHandler.java        # ★ 核心：触发判定、扣血、强制趴下、状态效果、粒子
        └── BleedDamage.java         # 自定义伤害类型 → DamageSource + 死亡消息键
```

---

## 3. 每个文件的职责

### 构建配置

| 文件 | 职责 | 什么时候改 |
|---|---|---|
| `gradle.properties` | 模组标识（`mod_id`/`mod_name`/`mod_version`）与平台版本（`minecraft_version`/`neo_version`） | 改名、升级 NeoForge 时 |
| `build.gradle` | 应用 ModDevGradle 插件、声明 `client`/`server`/`data` 运行配置、生成 mod 元数据、设置 Java 21 工具链 | 加依赖、加运行配置时 |
| `settings.gradle` | 三个仓库地址 + Foojay 插件（自动下载 JDK 21） | 换仓库时 |

### 源码

| 文件 | 关键内容 |
|---|---|
| `NofoMod.java` | `MODID = "nofo"`；`@Mod` 入口里只做一件注册：`ModAttachments.register(modEventBus)` |
| `ModAttachments.java` | 注册 `AttachmentType<BleedingData> BLEEDING`；`BleedingData` 用 `Codec.INT` 序列化，**故意不用 `copyOnDeath()`**，所以一死就清零 |
| `BleedDamage.java` | 把 `ResourceKey<DamageType>`（`nofo:bleed`）转成 `DamageSource` |
| `BleedHandler.java` | **全部玩法逻辑**，见下表 |

### `BleedHandler.java` 内部结构

| 方法 | 作用 |
|---|---|
| `onLivingDamagePost(LivingDamageEvent.Post)` | **触发判定**：目标是玩家 + 非创造/旁观 + 最终伤害 > 10 + 攻击者手持剑/斧 → 层数 +1 |
| `onPlayerTick(PlayerTickEvent.Post)` | 每 tick 维持：强制趴下、每秒扣血、每秒血粒子、维持反胃+黑暗 |
| `applyProneLock(Player)` | `setPose(SWIMMING)` + 清零水平速度 + 取消向上速度（禁跳）+ `push(0,0,0)`（锁击退） |
| `applyStatusEffects(Player)` | 每 tick 刷新 `NAUSEA` 与 `DARKNESS`（各 20 tick） |
| `emitBloodParticles(...)` | 用原版 `DAMAGE_INDICATOR` 粒子模拟血点 |
| `onPlayerRespawn(PlayerRespawnEvent)` | **结束条件**：层数清零 + 移除两种负面效果 |

---

## 4. 数据流（一次完整的“被砍 → 死亡”）

```
玩家被持剑/斧实体攻击
        │
        ▼
LivingDamageEvent.Post   ← 护甲/附魔/抗性已全部结算
        │  最终伤害 > 10 ？
        ▼
AttachmentType BLEEDING 层数 +1（无上限）
        │
        ▼
PlayerTickEvent.Post（每 tick）
        ├── setPose(SWIMMING) + 速度/击退全锁
        ├── 每 20 tick：hurt(bleed, 2 × 层数)
        ├── 每 20 tick：血粒子
        └── 每 tick：NAUSEA + DARKNESS 刷新
        │
        ▼
玩家死亡 → 重生
        │
        ▼
PlayerRespawnEvent → 层数清零 + 移除效果
```

---

## 5. 各文件的“改这里会怎样”

| 想改的东西 | 改哪个文件 | 具体位置 |
|---|---|---|
| 触发伤害阈值（现在 >10） | `BleedHandler.java` | `BLEED_TRIGGER_DAMAGE` |
| 每层每秒扣血（现在 2 点） | `BleedHandler.java` | `DAMAGE_PER_LAYER` |
| 效果刷新间隔（现在 20 tick） | `BleedHandler.java` | `EFFECT_REFRESH_TICKS` |
| 允许触发的武器种类 | `data/minecraft/tags/item/*.json` | 增删物品 ID |
| 死亡消息文案 | `assets/nofo/lang/zh_cn.json` | `death.attack.bleed` |
| modid / 版本 / 显示名 | `gradle.properties` | 对应字段 |
| NeoForge 版本 | `gradle.properties` | `neo_version`（须与 `minecraft_version` 匹配） |

---

## 6. 依赖与版本（已核实）

| 组件 | 版本 | 说明 |
|---|---|---|
| Minecraft | 1.21.1 | `minecraft_version` |
| NeoForge | 21.1.252 | 1.21.1 对应的最新 21.1.x（该线共 249 个版本） |
| ModDevGradle | 1.0.24 | 1.21.1 同期插件线（2.0.x 面向更新的 MC） |
| Gradle | 8.14.3 | 由 wrapper 锁定 |
| Java | 21 | 构建目标与运行环境；由 Foojay 自动下载 |

---

## 7. 构建产物

| 产物 | 路径 |
|---|---|
| 模组 jar | `build/libs/nofo-1.0.0.jar` |
| 源码 jar | `build/libs/nofo-1.0.0-sources.jar` |

安装方式：把 `nofo-1.0.0.jar` 放进对应实例的 `mods/` 文件夹。
