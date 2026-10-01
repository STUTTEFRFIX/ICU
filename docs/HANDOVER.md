# ICU 模组 — 交接报告

> **给接手人的一页纸。** 读完即可上手，无需再问原作者。
> 工程路径：`D:\DS\nofo1.21.1\`　｜　主题：在 Minecraft 中还原真实的身体损伤与医疗体系

---

## 1. 这个模组是什么

| 项 | 值 |
|---|---|
| 名称 | **ICU** |
| modid | `nofo` |
| 版本 | 1.0.0 |
| 平台 | Minecraft **1.21.1** + NeoForge **21.1.252** |
| 构建 | ModDevGradle **1.0.24**，Gradle 8.14.3，Java 21 |
| 当前功能 | **只有 1 个：大出血（Haemorrhage）** |

**设计定位**：不做“魔法/技能”，做“写实创伤”。所以规则偏残酷——中招基本等于宣判死亡。

---

## 2. 已实现功能：大出血（完整规格）

| # | 规则 | 精确实现 |
|---|---|---|
| 1 | 被刀斧类武器攻击 | 攻击者主手物品属于 `minecraft:swords` 或 `minecraft:axes` |
| 2 | 伤害 > 5 心才触发 | **护甲/附魔/抗性结算后**的最终伤害 > **10 点**（= 5 心） |
| 3 | 造成大出血 | 出血层数 **+1**，**无上限** |
| 4 | 持续出血每秒扣 2 | 每 20 tick 造成 `2 × 层数` 点 `nofo:bleed` 伤害（无视无敌帧） |
| 5 | 不能移动、强制趴下 | `SWIMMING` 姿态（趴地爬行）+ 水平速度清零 + 禁跳 + **锁定击退** |
| 6 | 附反胃 + 黑暗 | `NAUSEA` + `DARKNESS`，每 tick 刷新，随出血一同清除 |
| 7 | 直到死亡重生 | 只有死亡/重生清除；**没有时限、没有自愈** |

**附带表现**：血粒子（原版 `DAMAGE_INDICATOR`）；中文死亡消息「XXX 因大出血而死亡」。

### 死亡速度（设计上的必然结果）

| 命中次数 | 层数 | 每秒扣血 | 满血（20 点）存活 |
|---|---|---|---|
| 1 | 1 | 2 | ≈10 秒 |
| 2 | 2 | 4 | ≈5 秒 |
| 3 | 3 | 6 | ≈3.3 秒 |

> 因为「无时限 + 无治疗手段」，**第 1 层就已经必死**。叠加层数只决定“死多快”。
> 这是需求确认时明确选定的行为，不是缺陷。

---

## 3. 关键设计决策与原因

| 决策 | 原因 |
|---|---|
| 用 `LivingDamageEvent.Post` | 只有这个阶段能拿到**护甲结算后**的真实伤害，符合「真实损伤」主题 |
| 附件 `BLEEDING` **不用** `copyOnDeath()` | 死亡即清零，重生自然干净，无需额外清理逻辑 |
| 伤害类型 `nofo:bleed` 单独注册 | 需要独立死亡消息；且与原版伤害来源区分开 |
| 用 `hurt()` 每 20 tick 扣血 | 走原版伤害管线，能正确触发死亡、统计、音效 |
| 不用自定义 `MobEffect` | 原版 `NAUSEA`/`DARKNESS` 已够用，减少注册面与出错点 |
| 附件不同步到客户端 | 所有逻辑在服务端跑；效果与粒子由原版自动同步，少一处出错点 |
| 标签 JSON 覆盖原版同名文件 | 原版 `swords`/`axes` 已含全部该类型物品，覆盖后内容等价，语义清晰 |

---

## 4. 如何构建

```bash
# 在 D:\DS\nofo1.21.1\ 目录下
gradlew.bat build
```

**前置条件**：
- 出网（需访问 `maven.neoforged.net`、`maven central`、`services.gradle.org`、`library.minecraft.net`）
- Java 21 —— 无需手动安装，`settings.gradle` 里的 Foojay 插件会自动下载

**注意（本机特有）**：Gradle 主目录必须指向工作区内，否则沙箱会拒绝写入：

```bash
set GRADLE_USER_HOME=D:\DS\.gradle-home
gradlew.bat build
```

**产物**：`build/libs/nofo-1.0.0.jar` → 丢进实例的 `mods/` 文件夹即可使用。

---

## 5. 已验证 / 未验证

| 项 | 状态 |
|---|---|
| 目录结构与源码完整性 | ✅ 已完成 |
| NeoForge/Java/Gradle 版本对应关系 | ✅ 已对照官方 Maven 元数据核实 |
| 事件与附件 API 签名 | ✅ 已从 NeoForge 21.1.252 源码核实 |
| 标签名 `minecraft:swords` / `minecraft:axes` | ✅ 已从 1.21.1 官方 jar 条目核实 |
| `pack_format` = 34（1.21/1.21.1） | ✅ 已对照 Minecraft Wiki 核实 |
| **编译通过** | ⏳ 见第 6 节结论 |
| **游戏内实测** | ❌ 未做（需要启动客户端） |

---

## 6. 构建结果

**✅ 构建成功。** 产物已生成并校验。

| 项 | 结果 |
|---|---|
| 命令 | 见下方「构建命令」 |
| 结果 | `BUILD SUCCESSFUL in 17s` |
| 模组 jar | `build/libs/nofo-1.0.0.jar`（**10,440 字节**） |
| 源码 jar | `build/libs/nofo-1.0.0-sources.jar`（8,023 字节） |
| 元数据校验 | `META-INF/neoforge.mods.toml` 变量已正确展开（modId=nofo / version=1.0.0 / NeoForge `[21.1.0,)` / MC `[1.21.1,1.21.2)`） |
| 清单校验 | `MANIFEST.MF` 含 Specification/Implementation 四项，与 `gradle.properties` 一致 |
| 内容校验 | 4 个功能类 + 2 个语言文件 + 1 个伤害类型 + 2 个物品标签 + `pack.mcmeta` 全部在包内 |

### 构建命令（本机已验证）

```bat
cd /d D:\DS\nofo1.21.1
build.bat
```

`build.bat` 已随工程提供，内部设置好三项本机必需变量。若环境无限制，直接 `gradlew.bat build` 即可。

### 本机构建环境的三个特殊要求（已在 build.bat 处理）

| # | 问题 | 处理 |
|---|---|---|
| 1 | 系统只装了 JDK 25，而 Gradle 8.14.3 的 Groovy 无法解析 Java 25 class 文件（`Unsupported class file major version 69`） | 下载 Temurin JDK 21 到 `D:\DS\tools\jdk21`，构建时指定 `JAVA_HOME` |
| 2 | 文件沙箱拒写 `C:\Users\...\.gradle` | `GRADLE_USER_HOME=D:\DS\.gradle-home` |
| 3 | NeoForm 的 `jst` 工具被拒绝访问系统 `TEMP` | `TEMP=TMP=D:\DS\.tmp-build` |

### 另外两处必要的构建配置

| 配置 | 位置 | 原因 |
|---|---|---|
| `useEclipseCompiler = true` | `build.gradle` → `neoForge { neoFormRuntime { ... } }` | NeoForm 用 javac 重编译 Minecraft 时失败；ECJ 路径正常。ModDevGradle 官方同时支持两条路径 |
| 移除 `org.gradle.jvmargs` | `gradle.properties` | 该设置会强制 Gradle 派生「单次 daemon」，在受限环境下无法创建命名管道 |

### 尚未验证的部分（如实说明）

⚠️ **只验证了「能编译、能打包、元数据正确」，没有做游戏内实测。**
即：没有启动 Minecraft 客户端实际砍一刀验证效果。若要确认玩法表现，需执行 `gradlew.bat runClient`（需要图形界面与更多时间）。


---

## 7. 已知限制与后续建议

**限制**
1. 本版**没有任何止血/治疗手段** —— 出血即等于必然死亡（符合当前范围）。
2. 「趴下」用 `SWIMMING` 姿态模拟，是原版最接近“趴地”的表现，非真正的趴下姿态。
3. 只统计**攻击者主手**武器；副手、投掷物、间接伤害不触发。
4. 创造/旁观模式玩家不触发。
5. 未限制游戏规则/难度；和平模式下仍然会因出血而死。

**建议的下一步（需另行确认，当前未做）**
- 止血物品（绷带/止血带）→ 让「出血」从死刑变成可救治状态
- 出血 HUD 提示（层数、剩余时间）
- 更多损伤类型（骨折、内脏损伤、感染）→ 逐步铺开 ICU 主题

---

## 8. 改动索引

| 想改 | 去哪个文件 | 找什么 |
|---|---|---|
| 触发阈值 | `BleedHandler.java` | `BLEED_TRIGGER_DAMAGE` |
| 每秒扣血 | `BleedHandler.java` | `DAMAGE_PER_LAYER` |
| 武器白名单 | `data/minecraft/tags/item/*.json` | 物品 ID 列表 |
| 死亡文案 | `assets/nofo/lang/zh_cn.json` | `death.attack.bleed` |
| 伤害类型属性 | `data/nofo/damage_type/bleed.json` | `message_id` / `exhaustion` / `scaling` |
| 版本与名称 | `gradle.properties` | `mod_*` / `neo_version` |
