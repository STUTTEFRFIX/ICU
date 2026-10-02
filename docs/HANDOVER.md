# ICU 模组 — 交接报告

> **给接手人的一页纸。** 读完即可上手。
> 工程路径：`D:\DS\icu\`　｜　主题：在 Minecraft 中还原真实的身体损伤与医疗体系
> 完整功能与变更总表见 [CHANGELOG.md](CHANGELOG.md)。

---

## 1. 这个模组是什么

| 项 | 值 |
|---|---|
| 名称 / 仓库 | **ICU** |
| 目录 | `icu` |
| mod id | `icu` |
| Java 包 | `com.icu.icu` |
| 版本 | **0.2.1** |
| 平台 | Minecraft **1.21.1** + NeoForge **21.1.252** |
| 构建 | ModDevGradle **1.0.24**，Gradle 8.14.3，Java 21 |
| 已实现 | **4 个玩法模块 + 1 个物品** |

> **命名只有一套**：仓库 `ICU` = 目录 `icu` = mod id `icu` = 包 `com.icu.icu`。
> 历史上曾用过 `nofo`，已在结构重排中**彻底移除**（源码、资源、文档、jar 全部不再出现）。

**设计定位**：不做「魔法/技能」，做「写实创伤」。规则偏残酷——重创基本等于宣判死亡。

---

## 2. 已实现功能

### 2.1 大出血（`gameplay.bleeding`）

| # | 规则 | 精确实现 |
|---|---|---|
| 1 | 被刀斧类武器攻击 | 攻击者主手属于 `minecraft:swords` 或 `minecraft:axes` |
| 2 | 触发阈值 | **护甲/附魔/抗性结算后**最终伤害 > **7 点** |
| 3 | 层数 | 每次命中 **+1**，**无上限** |
| 4 | 失血 | 血容量每秒 **−5%** |
| 5 | 致死 | **只有血容量归零致死**；不再有额外的健康伤害 |
| 6 | 强制趴下 | `SWIMMING` 姿态 + 水平速度清零 + 禁跳 + **锁击退** |
| 7 | 附加效果 | `CONFUSION` + `DARKNESS`，每 tick 刷新，随出血清除 |
| 8 | 表现 | 血粒子；死亡消息「XXX 血容量归零，失血过多而亡」|

> ⚠️ **阈值 7 的影响**：护甲减免后极难达到 —— 无甲需原始伤害 7、皮革约 15、
> 铁甲约 18.5、**钻石甲约 28**（游戏内最高约 19.5）。
> 即**穿铁甲以上几乎不会出血**。这是项目所有者明确选定的数值。

### 2.2 血容量（`gameplay.blood`）

初始/上限 100%；大出血期间每秒 **−5%**（20 秒归零即死）；
**非出血期间每 10 分钟 +5%**（上限 100%）；绷带治疗**不重置**，从当前值继续。

### 2.3 绷带（`item`）

配方 **3 纸 + 1 线 + 1 羊毛**，堆叠 16。右键**按住 3 秒**；
期间出血照常扣血容量；成功则解除出血 + 进入恢复期；3 秒内归零则死亡。

### 2.4 疼痛值（`gameplay.pain`，出血与崴脚共用）

| 来源 | 每秒 |
|---|---|
| 大出血 | +10% |
| 崴脚·行走 / 奔跑 / 跳跃 | +5% / +10% / +15%（一次性）|
| 趴下状态 | −5% |

满 **100%** → 强制趴下 + 弹 3 次「我好疼 我好疼」（仅自己可见）；
降至 **80%** 恢复行动。

### 2.5 恢复期（`gameplay.bleeding`）

治疗后 **5 分钟**。期间**跑 >15 秒**或**第 4 跳** → 复发（血容量不重置）。
任何跑/跳都**重置计时**（未达阈值不复发）。

### 2.6 摔落崴脚（`gameplay.sprain`）

用原版 `fallDistance`：≤5 不触发；(5,10) 35%；[10,15) 50%；[15,20) 90%；≥20 100%。
干草块 **−20 个百分点**；保护/摔落保护**每级 −8**，**5 级全免**。
入水（含 1 格）、缓降、鞘翅、紫颂果、抗火**自动豁免**。

### 2.7 心跳音效 —— **未实现**

设计已定稿（5 档随血容量、归零播「滴——」），但音频素材由项目所有者把关，
**素材未到之前不实现、不使用任何替代音效**。代码中已留 `TODO` 标记位置。

---

## 3. 关键设计决策与原因

| 决策 | 原因 |
|---|---|
| 用 `LivingDamageEvent.Post` | 唯一能拿到**护甲结算后**真实伤害的阶段 |
| 附件全部**不用** `copyOnDeath()` | 死亡必须清空全部伤势，重生从白纸开始 |
| 致死只由血容量承担 | 避免"扣健康"与"血容量"两套机制抢戏，让血容量成为唯一真相 |
| 绷带不恢复血容量 | 按规格：治疗只止血，血要慢慢自己造 |
| 玩法按模块分包 | 以后加骨折/感染各有去处，互不干扰 |
| 用 CI 编译而不是本机 | 本机内存不足以完成 NeoForm 反编译（详见第 5 节）|

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

**产物**：`build/libs/icu-Mod-0.2.1.jar` → 丢进实例的 `mods/` 文件夹即可使用。

> ⚠️ 产物名带 `-Mod` 后缀，是为了和 `-sources.jar`（源码包）明确区分。
> **源码包不能装进 `mods/`**：它只有 `.java` 文本、没有编译好的 `.class`，
> NeoForge 仍会把它当成一个模组列出来（因为里面有 `neoforge.mods.toml`），
> 但代码一行都不会执行。发布流程已不再附带源码包，源码改为 `.zip`。

### 5.1 本机编译的已知障碍

| # | 问题 | 处理 |
|---|---|---|
| 1 | 系统只装 JDK 25，Gradle 8.14.3 无法解析其 class 文件 | 用 `D:\DS\tools\jdk21`，`build.bat` 已内置 |
| 2 | 文件沙箱拒写 `C:\Users\...\.gradle` | `GRADLE_USER_HOME=D:\DS\.gradle-home`（已内置）|
| 3 | NeoForm 的 `jst` 工具被拒访问系统 `TEMP` | `TEMP=TMP=D:\DS\.tmp-build`（已内置）|
| 4 | **内存不足**：物理 15.8 GB、页面文件上限 8 GB，反编译 Minecraft 报 `os::commit_memory failed` | **改用 GitHub Actions 云端构建** |

> 因此本项目**必须**用 CI 编译，或在一台内存充足的机器上编译。

### 5.2 另外两处必要的构建配置

| 配置 | 位置 | 原因 |
|---|---|---|
| `useEclipseCompiler = true` | `build.gradle` → `neoForge { neoFormRuntime { } }` | javac 重编译 Minecraft 会失败；ECJ 路径正常 |
| 不设 `org.gradle.jvmargs` | `gradle.properties` | 该设置会强制派生「单次 daemon」，受限环境下无法创建命名管道 |

### 5.3 CI 工作流

| 文件 | 触发 | 作用 |
|---|---|---|
| `.github/workflows/build.yml` | 推 main / PR / 手动 | 编译检查；**失败会写 `BUILD_FAILURES.md`** |
| `.github/workflows/release.yml` | 推 `v*` 标签 | 编译并发布 Release（模组 jar + 源码 zip）|

---

## 6. 已验证 / 未验证（**如实说明**）

| 项 | 状态 |
|---|---|
| NeoForge / Java / Gradle 版本对应关系 | ✅ 已对照官方 Maven 元数据核实 |
| 事件与附件 API 签名 | ✅ 已从 NeoForge 21.1.252 源码核实 |
| 标签名 `minecraft:swords` / `minecraft:axes` | ✅ 已从 1.21.1 官方 jar 条目核实 |
| `pack_format` = 34（1.21/1.21.1） | ✅ 已对照 Minecraft Wiki 核实 |
| 0.0.x 版本的编译与游戏内加载 | ✅ 曾实测通过（服务端与客户端均正常载入）|
| **0.1.0 的编译** | ✅ CI 编译通过 |
| **0.2.1 的编译（含 JEI 集成）** | ✅ **CI 编译通过** |
| **游戏内实测** | ❌ **未做** —— 需要人进游戏实测各机制 |

---

## 7. 已知限制与后续建议

**限制**
1. 触发阈值 7 偏高，穿铁甲以上几乎不会出血（设计选定）。
2. 心跳音效未实现（等音频素材）。
3. 「趴下」用 `SWIMMING` 姿态模拟，非真正的趴下姿态。
4. 疼痛值未做 HUD 显示，属隐藏数值。
5. 只统计**攻击者主手**武器；副手、投掷物、间接伤害不触发。

**建议的下一步（需另行确认）**
- 心跳音效（等素材）
- HUD 显示血容量 / 疼痛值
- 更多损伤类型（骨折、内脏损伤、感染）
- 崴脚的自动恢复机制（目前只能靠静止降疼痛）

---

## 8. 改动索引

| 想改 | 去哪个文件 | 找什么 |
|---|---|---|
| 触发阈值（7） | `gameplay/bleeding/BleedingFeature.java` | `TRIGGER_DAMAGE` |
| 每秒失血（5%） | `gameplay/blood/BloodVolumeData.java` | `LOSS_PER_SECOND` |
| 自然恢复速度 | `gameplay/blood/BloodVolumeData.java` | `REGEN_AMOUNT` / `REGEN_INTERVAL_TICKS` |
| 绷带时长（3 秒） | `item/BandageItem.java` | `USE_TICKS` |
| 疼痛各项数值 | `gameplay/pain/PainFeature.java` | 顶部 5 个常量 |
| 崴脚概率 / 减免 | `gameplay/sprain/SprainFeature.java` | `CHANCE_TIER_*` / `HAY_CHANCE_REDUCTION` / `CHANCE_PER_ENCHANT_LEVEL` |
| 恢复期时长与阈值 | `gameplay/bleeding/BleedingRecoveryData.java` | `DURATION_TICKS` / `SPRINT_LIMIT_TICKS` / `JUMP_LIMIT` |
| 绷带配方 | `resources/data/icu/recipe/bandage.json` | ingredients |
| 死亡与提示文案 | `resources/assets/icu/lang/zh_cn.json` | `death.attack.bleed` / `message.icu.*` |
| 版本与名称 | `gradle.properties` | `mod_*` / `neo_version` |
