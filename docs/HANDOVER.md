# ICU 模组 — 交接报告

> **给接手人的一页纸。** 读完即可上手。
> 工程路径：`D:\DS\icu\`　｜　主题：在 Minecraft 中还原真实的身体损伤与医疗体系

---

## 1. 这个模组是什么

| 项 | 值 |
|---|---|
| 名称 / 仓库 | **ICU** |
| 目录 | `icu` |
| mod id | `icu` |
| Java 包 | `com.icu.icu` |
| 版本 | 0.0.1 |
| 平台 | Minecraft **1.21.1** + NeoForge **21.1.252** |
| 构建 | ModDevGradle **1.0.24**，Gradle 8.14.3，Java 21 |
| 当前功能 | **只有 1 个模块：大出血（`gameplay.bleeding`）** |

> **命名只有一套**：仓库 `ICU` = 目录 `icu` = mod id `icu` = 包 `com.icu.icu`。
> 历史上曾用过 `nofo`，已在结构重排中**彻底移除**（源码、资源、文档、jar 全部不再出现）。

**设计定位**：不做「魔法/技能」，做「写实创伤」。规则偏残酷——中招基本等于宣判死亡。

---

## 2. 已实现功能：大出血（完整规格）

| # | 规则 | 精确实现 |
|---|---|---|
| 1 | 被刀斧类武器攻击 | 攻击者主手物品属于 `minecraft:swords` 或 `minecraft:axes` |
| 2 | 触发伤害阈值 | **护甲/附魔/抗性结算后**的最终伤害 > **3 点**（详见下方「阈值标定」） |
| 3 | 造成大出血 | 出血层数 **+1**，**无上限** |
| 4 | 持续出血每秒扣 2 | 每 20 tick 造成 `2 × 层数` 点 `icu:bleed` 伤害（绕过无敌帧） |
| 5 | 不能移动、强制趴下 | `SWIMMING` 姿态 + 水平速度清零 + 禁跳 + **锁定击退** |
| 6 | 附反胃 + 黑暗 | `CONFUSION` + `DARKNESS`，每 tick 刷新，随出血一同清除 |
| 7 | 直到死亡重生 | 只有死亡/重生清除；**没有时限、没有自愈** |

**附带表现**：血粒子（原版 `DAMAGE_INDICATOR`）；中文死亡消息「XXX 因大出血而死亡」。

### 死亡速度（设计上的必然结果）

| 命中次数 | 层数 | 每秒扣血 | 满血（20 点）存活 |
|---|---|---|---|
| 1 | 1 | 2 | ≈10 秒 |
| 2 | 2 | 4 | ≈5 秒 |
| 3 | 3 | 6 | ≈3.3 秒 |

> 因为「无时限 + 无治疗手段」，**第 1 层就已经必死**。叠加层数只决定「死多快」。
> 这是需求确认时明确选定的行为，不是缺陷。

---

## 3. 关键设计决策与原因

| 决策 | 原因 |
|---|---|
| 用 `LivingDamageEvent.Post` | 只有这个阶段能拿到**护甲结算后**的真实伤害 |
| 附件 `BLEEDING` **不用** `copyOnDeath()` | 死亡即清零，重生自然干净 |
| 伤害类型 `icu:bleed` 单独注册 | 需要独立死亡消息；且与原版伤害来源区分 |
| 用 `hurt()` 每 20 tick 扣血 | 走原版伤害管线，死亡/统计/音效都正确 |
| 不用自定义 `MobEffect` | 原版 `CONFUSION`/`DARKNESS` 已够用，减少注册面 |
| 附件不同步到客户端 | 逻辑全在服务端；效果与粒子由原版自动同步 |
| 玩法按模块分目录 | 以后加骨折/感染只需新建一个包，互不干扰 |

---

## 4. 目录与类名对照（改名后的现状）

| 旧（nofo 时代） | 新（icu） |
|---|---|
| `NofoMod.java` | `IcuMod.java` |
| `ModAttachments.java` | `IcuAttachments.java` |
| `bleed/BleedHandler.java` | `gameplay/bleeding/BleedingFeature.java` |
| `bleed/BleedDamage.java` | `gameplay/bleeding/BleedingDamage.java` |
| `BleedingData`（内嵌类） | `gameplay/bleeding/BleedingData.java`（独立文件） |
| `assets/nofo/` | `assets/icu/` |
| `data/nofo/` | `data/icu/` |
| `nofo:bleeding` / `nofo:bleed` | `icu:bleeding` / `icu:bleed` |

---

## 5. 如何构建

```bat
cd /d D:\DS\icu
build.bat
```

**产物**：`build/libs/icu-Mod-0.0.3.jar` → 丢进实例的 `mods/` 文件夹即可使用。

> ⚠️ 产物名带 `-Mod` 后缀，是为了和 `-sources.jar`（源码包）明确区分。
> **源码包不能装进 `mods/`**：它只有 `.java` 文本、没有编译好的 `.class`，
> NeoForge 仍会把它当成一个模组列出来（因为里面有 `neoforge.mods.toml`），
> 但代码一行都不会执行。发布流程已不再附带源码包。

**前置条件**：出网；Java 21（`settings.gradle` 的 Foojay 插件会自动下载）。

### 本机构建环境的三个特殊要求（`build.bat` 已内置）

| # | 问题 | 处理 |
|---|---|---|
| 1 | 系统只装 JDK 25，Gradle 8.14.3 无法解析其 class 文件 | 用 `D:\DS\tools\jdk21`，构建时指定 `JAVA_HOME` |
| 2 | 文件沙箱拒写 `C:\Users\...\.gradle` | `GRADLE_USER_HOME=D:\DS\.gradle-home` |
| 3 | NeoForm 的 `jst` 工具被拒访问系统 `TEMP` | `TEMP=TMP=D:\DS\.tmp-build` |

### 另外两处必要的构建配置

| 配置 | 位置 | 原因 |
|---|---|---|
| `useEclipseCompiler = true` | `build.gradle` → `neoForge { neoFormRuntime { } }` | javac 重编译 Minecraft 会失败；ECJ 路径正常 |
| 不设 `org.gradle.jvmargs` | `gradle.properties` | 该设置会强制派生「单次 daemon」，受限环境下无法创建命名管道 |

---

## 6. 已验证 / 未验证（**如实说明**）

| 项 | 状态 |
|---|---|
| NeoForge / Java / Gradle 版本对应关系 | ✅ 已对照官方 Maven 元数据核实 |
| 事件与附件 API 签名 | ✅ 已从 NeoForge 21.1.252 源码核实 |
| 标签名 `minecraft:swords` / `minecraft:axes` | ✅ 已从 1.21.1 官方 jar 条目核实 |
| `pack_format` = 34（1.21/1.21.1） | ✅ 已对照 Minecraft Wiki 核实 |
| **改名前（modId = `nofo`）的编译与打包** | ✅ 曾成功构建出 jar |
| **改名前（modId = `nofo`）的游戏内加载** | ✅ 专用服务端与客户端均启动成功，模组正常载入、无报错 |
| **改名后（modId = `icu`）的编译与运行** | ✅ **已由项目所有者实测通过：运行没有任何问题** |

> ⚠️ 本机无法完成编译：NeoForm 反编译 Minecraft 需要的内存超过本机提交上限，
> 报错为 `os::commit_memory failed / 页面文件太小`。这是**环境限制，不是代码问题**。
> 因此本项目改用 **GitHub Actions 构建**（见 `.github/workflows/release.yml`）：
> 推送 `v*` 标签后，云端自动编译并把 jar 挂到 Release。
> 实测：`v0.0.1` 标签触发的构建成功，产出模组 jar（10,598 字节）。

---

## 7. 已知限制与后续建议

**限制**
1. 本版**没有任何止血/治疗手段** —— 出血即必然死亡。
2. 「趴下」用 `SWIMMING` 姿态模拟，非真正的趴下姿态。
3. 只统计**攻击者主手**武器；副手、投掷物、间接伤害不触发。
4. 创造/旁观模式玩家不触发。
5. 未限制难度；和平模式下仍会因出血而死。

**建议的下一步（需另行确认）**
- 止血物品（绷带/止血带）→ 让出血从死刑变成可救治状态
- 出血 HUD 提示（层数）
- 更多损伤类型（骨折、内脏损伤、感染）

---

## 8. 改动索引

| 想改 | 去哪个文件 | 找什么 |
|---|---|---|
| 触发阈值 | `gameplay/bleeding/BleedingFeature.java` | `TRIGGER_DAMAGE` |
| 每秒扣血 | `gameplay/bleeding/BleedingFeature.java` | `DAMAGE_PER_LAYER` |
| 武器白名单 | `resources/data/minecraft/tags/item/*.json` | 物品 ID 列表 |
| 死亡文案 | `resources/assets/icu/lang/zh_cn.json` | `death.attack.bleed` |
| 伤害类型属性 | `resources/data/icu/damage_type/bleed.json` | `message_id` / `exhaustion` / `scaling` |
| 版本与名称 | `gradle.properties` | `mod_*` / `neo_version` |
