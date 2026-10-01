# ICU 项目报表

> **用途**：项目管理 / 问题分析 / 交接接手 / 可持续开发
> **生成时间**：2026-10-01
> **一句话结论**：模组已能编译、能加载、功能已实现；但**改名重排后尚未重新编译验证**，这是当前最大风险点。

---

## 第一部分 · 项目现状（接手先看这节）

### 1.1 项目身份

| 项 | 值 |
|---|---|
| 项目名 | **ICU** — 在 Minecraft 中还原真实的身体损伤与医疗体系 |
| 开源仓库 | <https://github.com/STUTTEFRFIX/ICU>（公开） |
| 最新提交 | `ca3a8fe` — Rename project to icu and restructure for maintainability |
| 本地目录 | `D:\DS\icu` |
| mod id | `icu` |
| Java 包 | `com.icu.icu` |
| 版本 | 0.0.1（首个发布版） |
| 平台 | Minecraft **1.21.1** + NeoForge **21.1.252** |
| 构建 | ModDevGradle 1.0.24 · Gradle 8.14.3 · Java 21 |
| 已实现模块 | **1 个** — `gameplay.bleeding`（大出血） |

> **命名只有一套**：仓库 `ICU` = 目录 `icu` = mod id `icu` = 包 `com.icu.icu`。

### 1.2 仓库文件清单（26 个）

```
ICU/
├── README.md                        入口页：这是什么、怎么构建、从哪看代码
├── LICENSE                          Apache 2.0
├── build.gradle / build.bat / gradle.properties / settings.gradle
├── gradlew / gradlew.bat / gradle/wrapper/*
├── .gitignore
├── docs/
│   ├── PROJECT_REPORT.md            ← 本文件
│   ├── PROJECT_STRUCTURE.md         每个文件干什么
│   ├── DEVELOPMENT.md               加新功能的规矩 + 1.21.1 API 坑
│   └── HANDOVER.md                  交接报告
└── src/main/
    ├── java/com/icu/icu/
    │   ├── IcuMod.java              入口，只做注册
    │   ├── IcuAttachments.java      玩家状态存储
    │   └── gameplay/bleeding/
    │       ├── BleedingData.java    数据：出血层数
    │       ├── BleedingDamage.java  伤害：icu:bleed
    │       └── BleedingFeature.java 规则：触发/扣血/趴下/效果 ★核心
    ├── resources/
    │   ├── pack.mcmeta
    │   ├── assets/icu/lang/{zh_cn,en_us}.json
    │   ├── data/icu/damage_type/bleed.json
    │   └── data/minecraft/tags/item/{swords,axes}.json
    └── templates/META-INF/neoforge.mods.toml
```

### 1.3 已实现的唯一功能：大出血

| # | 规则 | 实现 |
|---|---|---|
| 1 | 被刀斧类武器攻击 | 攻击者主手属于 `minecraft:swords` / `minecraft:axes` |
| 2 | 触发伤害阈值 | 护甲结算后最终伤害 > 3 点（见 2.6 阈值标定） |
| 3 | 造成大出血 | 层数 +1，**无上限** |
| 4 | 每秒扣 2 血 | 每 20 tick 造成 `2 × 层数` 点 `icu:bleed` 伤害（绕过无敌帧） |
| 5 | 不能移动、强制趴下 | SWIMMING 姿态 + 清水平速度 + 禁跳 + 锁击退 |
| 6 | 附反胃 + 黑暗 | `CONFUSION` + `DARKNESS`，每 tick 刷新 |
| 7 | 直到死亡重生 | 无时限、无自愈，仅死亡重生清除 |

**死亡速度**：1 层 ≈10 秒死、2 层 ≈5 秒、3 层 ≈3.3 秒（满血 20 点）。
> 设计上的必然结果：无时限 + 无治疗 → **第一层就必死**。这是确认过的设计，不是缺陷。

### 1.4 可调参数（都在 `BleedingFeature.java` 顶部）

| 常量 | 当前值 | 含义 |
|---|---|---|
| `TRIGGER_DAMAGE` | 3.0F | 触发阈值（见 2.6） |
| `DAMAGE_PER_LAYER` | 2.0F | 每层每秒扣血 |
| `EFFECT_REFRESH_TICKS` | 20 | 状态效果刷新窗口 |
| `TICK_INTERVAL` | 20 | 扣血/粒子间隔 |

---

## 第二部分 · 任务执行总账

### 2.1 任务清单（按时间顺序，含状态）

| # | 任务 | 结果 | 状态 |
|---|---|---|---|
| T1 | 制定协作规则（7 条） | 用户定义，全程按此执行 | ✅ 已完成 |
| T2 | 环境可行性探测 | 出网正常、仅有 JDK 25、NeoForge 21.1.252 可用 | ✅ 已完成 |
| T3 | 需求澄清（多轮问答） | 锁定功能规格、命名、工具链 | ✅ 已完成 |
| T4 | 搭建工程骨架 | 19 个文件 | ✅ 已完成 |
| T5 | 编译打包成 jar | 曾成功产出 jar（modId=nofo 时代） | ✅ 已完成（后已被改名取代） |
| T6 | 数据包与模组加载验证 | 服务端 + 客户端均启动成功、零报错 | ✅ 已完成（nofo 时代） |
| T7 | 上传源码到 GitHub | `54e9168` | ✅ 已完成 |
| T8 | 撤回任务（.gitattributes + 本地 git 仓库） | 按用户要求删除 | ✅ 已撤回 |
| T9 | 清除下载的游戏文件 | 释放约 1.1 GB | ✅ 已完成 |
| T10 | **改名 + 结构重排** | `ca3a8fe`，仓库 26 文件零 `nofo` | ✅ 已完成 |
| T11 | **改名后重新编译验证** | 项目所有者实测：运行无任何问题 | ✅ **已验证（用户实测）** |
| T12 | 游戏内实测降级验证 | 改名前的版本验证过 | ⚠️ 部分 |
| T13 | 发布 jar 到 Releases | — | ⏸ 未做，待用户决定 |
| T14 | 本地工程 git 仓库 | 已按用户要求撤回 | ⏸ 已撤回 |

### 2.2 关键决策记录

| 决策 | 原因 | 影响 |
|---|---|---|
| 用 `LivingDamageEvent.Post` | 唯一能拿到护甲结算后真实伤害的阶段 | 符合「写实创伤」主题 |
| 附件不用 `copyOnDeath()` | 死亡即清零，重生自然干净 | 无需额外清理逻辑 |
| 自定义伤害类型 `icu:bleed` | 需要独立死亡消息 | 可配中文死亡文案 |
| 改名统一到 `icu` 而非 `nofo` | 仓库与主题都叫 ICU，改仓库名会变动 URL | 仓库无需改名 |
| 玩法按模块分包 `gameplay/<模块>` | 以后加骨折/感染有固定去处 | 结构可持续 |
| `gameplay/bleeding/` 三文件分离 | 数据/伤害/规则职责不混 | 改动只动一个包 |

### 2.3 问题与解决记录（真实踩坑）

| # | 问题 | 根因 | 解决 |
|---|---|---|---|
| P1 | `Could not create parent directory for lock file` | 沙箱拒写 `C:\Users\...\.gradle` | `GRADLE_USER_HOME=D:\DS\.gradle-home` |
| P2 | `CreatePipe error=5 拒绝访问` | 沙箱禁止创建命名管道，Gradle 无法派生 daemon | 需完整权限 |
| P3 | `Unsupported class file major version 69` | 系统只有 JDK 25，Gradle 8.14.3 无法解析 | 下载 Temurin JDK 21 |
| P4 | `maven.neoforged.net` 502 | 临时故障 | 重试 |
| P5 | `jst` 工具 `AccessDeniedException` | NeoForm 被拒访问系统 TEMP | `TEMP=TMP=D:\DS\.tmp-build` |
| P6 | `MobEffects.NAUSEA` 找不到符号 | **1.21.1 里叫 `CONFUSION`** | 改名 |
| P7 | `AttachmentType.builder` 引用歧义 | `Supplier`/`Function` 重载冲突 | 传 lambda，不传方法引用 |
| P8 | NeoForm 重编译 Minecraft 失败 | javac 路径在本环境异常 | 切 ECJ：`useEclipseCompiler = true` |
| P9 | 客户端启动崩溃 `EXCEPTION_ACCESS_VIOLATION` | Dell A-Volute/Nahimic 音效注入 `NahimicOSD.dll` 注入 OpenGL 进程 | 停 NahimicService（重启自动恢复） |
| P10 | 反编译报 `页面文件太小` | 物理 15.8GB、页面文件上限 8GB，Vineflower 内存不足 | 未解决（需关程序或加大页面文件） |
| P11 | **许可证三处自相矛盾** | `gradle.properties` 写 `All Rights Reserved`，而仓库 `LICENSE` 是 Apache-2.0，README 又完全没提许可证 | ✅ 已统一为 Apache-2.0（见 3.3） |
| P12 | **本机无法编译**（两次尝试均失败） | NeoForm 反编译 Minecraft 需内存超过本机提交上限（可用提交 4.55 GB），报 `Native memory allocation failed / 页面文件太小` | ✅ 改用 GitHub Actions 云端构建（见 2.5） |
| P13 | **特性"没有生效"**（玩家反馈） | 触发阈值 = 护甲结算后 >10，而护甲减免后最高只能到 ~2.8，**永远不可能触发**。审核确认代码无 bug，是数值标定错误（我方在需求确认时未做可行性推演） | ✅ 阈值改为 3，并补做护甲梯度标定（见 2.6） |
| P14 | **玩家装了模组却"完全没反应"**（第二次反馈） | 发布时把 `icu-x.y.z-sources.jar`（源码包）和模组包并列挂在 Release，玩家下载了**源码包**。源码包只有 `.java` 文本、没有编译好的 `.class`，但含 `neoforge.mods.toml`，所以 NeoForge 仍把它列为模组 —— 结果是"列表里看得见、代码一行不跑" | ✅ 见 2.7 |

### 2.5 首个发布版 0.0.1（P12 的处理结果）

**背景**：本机两次编译都在 NeoForm `decompile` 步骤因内存不足失败，属环境限制而非代码问题。

**方案**：新增 `.github/workflows/release.yml`，把构建搬到 GitHub 的服务器上。

| 项 | 内容 |
|---|---|
| 触发条件 | 推送 `v*` 标签 |
| 版本来源 | 标签自动推导（`v0.0.1` → mod 版本 `0.0.1`） |
| 构建环境 | ubuntu-latest + JDK 21 (temurin) |
| 产出 | 附加到 Release 的模组 jar（`icu-<版本>-Mod.jar`）。源码包**不再发布**，见 P14 |
| 触发结果 | ✅ **构建成功**（约 2 分钟），run #1 `conclusion=success` |

**发布地址**：<https://github.com/STUTTEFRFIX/ICU/releases/tag/v0.0.1>

**产物校验（下载线上 jar 后逐项核对）**：

| 检查项 | 结果 |
|---|---|
| `modId` = `icu` | ✅ |
| `version` = `0.0.1` | ✅ |
| `license` = `Apache-2.0` | ✅ |
| 无 `nofo` 残留 | ✅ |
| 类在 `com/icu/icu/` 下 | ✅ |
| `gameplay/bleeding/` 三个类齐全 | ✅ |
| 含 `data/icu/damage_type/bleed.json` | ✅ |
| 含 `assets/icu/lang/zh_cn.json` | ✅ |

**版本号审计**：`1.0.0` → `0.0.1` 共 12 处（5 个文件）全部更新；
`minecraft_version=1.21.1`、`neo_version=21.1.252` 属平台版本，**未改动**。

### 2.6 触发阈值标定（P13 的处理结果）

**问题**：玩家反馈「模组内容没有正常生效」。审核后确认代码无 bug，
真正原因是**触发阈值永远达不到**。

**推导**：原阈值 = 护甲+附魔结算后最终伤害 > 10。按 1.21.1 官方护甲公式
（`f = min(20, max(armor/5, armor - dmg/(2+toughness/4)))`，减免 `f/25`；
保护附魔每级 EPF=1，上限 20）推演，要触发所需的最低原始伤害是：

| 目标护甲 | 原阈值 (>10) 所需原始伤害 | 新阈值 (>3) 所需原始伤害 |
|---|---|---|
| 无甲 | 10.0 | **3.0** |
| 皮革全套 | 29.5 | **9.2** |
| 锁链全套 | 30.8 | **11.3** |
| 铁全套 | 31.6 | **12.8** |
| 钻石全套 | 43.6 | 20.6 |
| 下界合金全套 | 47.7 | 22.2 |

**游戏内玩家单次可达到的最高原始伤害约 19.5**（下界合金斧 + 锋利V + 暴击）。

> ❌ 原阈值下：**只要穿任何护甲就永远不可能触发**，无甲也仅满附魔+暴击才勉强够。
> ✅ 新阈值 3 下：无甲几乎必触发，皮革需重击，铁甲需铁剑以上，
> **钻石甲与下界合金甲成为真正的保命装** —— 符合「写实创伤」的设计意图。

**改动**：`BleedingFeature.TRIGGER_DAMAGE` 由 `10.0F` 改为 `3.0F`，
并把标定依据写进该常量的 Javadoc，防止以后被误改回去。
共同步 9 处文档/代码表述，并发布 `v0.0.2`。

### 2.7 产物命名与发布规则（P14 的处理结果）

**问题**：玩家反馈装了模组却毫无反应。分析运行日志后定位到三条证据：

| 证据 | 日志内容 |
|---|---|
| 1 | `Found mod file "icu-0.0.2-sources.jar"` —— 装的是**源码包** |
| 2 | `Attempting to inject @EventBusSubscriber classes into the eventbus for icu` 之后**没有任何后续** —— 扫不到 class，一个事件监听都没注册 |
| 3 | 模组启动时会打印的 `[ICU] loaded` —— **出现 0 次**，主类从未执行 |

**原因**：`-sources.jar` 是源码包（只有 `.java`），但里面带了 `neoforge.mods.toml`，
所以 NeoForge 仍把它识别为一个有效模组并显示在模组列表里，
**但没有任何 `.class` 可以执行**。而它和真正的模组包名字只差 `-sources`，极易下错。

**修复（三件事）**：

1. **产物改名**：`build.gradle` 的 `archivesName` 由 `mod_id` 改为
   `"${mod_id}-${mod_version}-Mod"` → 产物变为 `icu-<版本>-Mod.jar`，
   一看就知道是模组本身，不会再和源码包混淆。
2. **发布流程不再附带源码包**：`.github/workflows/release.yml` 的收集步骤
   显式删除 `dist/*-sources.jar`，并校验模组 jar 确实存在，
   否则让构建**直接失败**（避免又发出一个空壳）。
3. **清理历史 Release**：已删除 `v0.0.1` 与 `v0.0.2` 上的两个源码包资产，
   现在两个 Release 各自只剩模组 jar。

> 源码包仍会作为 **workflow artifact** 保留（供开发者下载），
> 只是不再出现在玩家的下载列表里。

### 2.4 许可证统一（P11 的处理结果）

**统一选择**：Apache License 2.0（允许修改、再分发、商用）。

| 位置 | 改前 | 改后 |
|---|---|---|
| 仓库 `LICENSE` | Apache-2.0 全文 | 不变 ✅ |
| `gradle.properties` | `All Rights Reserved` | **`Apache-2.0`** |
| `README.md` | 未提及 | **新增「许可证」章节 + 概况表一行** |
| 本地工程 `LICENSE` | **缺失**（只在仓库里有） | **已补齐**（11,357 字节） |

> `mod_license` 会经 `build.gradle` 注入 `neoforge.mods.toml`，最终显示在游戏内模组列表。
> 另：`gradlew` / `gradlew.bat` 头部本来就是 Apache-2.0，因此统一后**全项目无冲突**。
>
> 顺带修掉 README 中指向不存在文件的 2 处断链（原 `docs/BUILD_AND_TEST.md`）。

---

## 第三部分 · 风险与未完成项（接手必读）

### 3.1 风险登记

| 等级 | 风险 | 说明 | 建议动作 |
|---|---|---|---|
| ✅ 已消除 | ~~改名后未编译验证~~ | 项目所有者已实测：**运行没有任何问题**（2026-10-01） | 无需动作 |
| ✅ 已消除 | ~~许可证修复尚未同步到仓库~~ | 已推送（`8bded6d`） | 无需动作 |
| ✅ 已消除 | ~~已发布 jar 与源码不一致~~ | `v0.0.1` 的 jar 由 CI 从当前源码构建，已逐项校验一致 | 无需动作 |
| 🟠 中 | 本机构建受内存限制 | 本机可用提交 4.55 GB，低于反编译需求，`decompile` 必失败（环境限制，非代码问题） | 用 `.github/workflows/release.yml` 云端构建，或换内存充足机器 |
| 🟡 低 | 文档中保留 10 处 `nofo` | 全是故意的历史说明（对照表、禁用提醒） | 保留，防止改回旧名 |
| 🟡 低 | 发布流程依赖 GitHub Actions | 仓库公开，Actions 分钟数免费；若 Actions 被关闭则无法自动发版 | 发版前确认 Actions 为启用状态 |
| 🟡 低 | 未做游戏内实测（改名后） | 改名前后行为逻辑相同 | 需要时 `runClient` 实测 |

### 3.2 未完成 / 待决事项

| # | 事项 | 等待什么 |
|---|---|---|
| U1 | ~~重新编译验证~~ | ✅ 已完成（项目所有者实测通过） |
| U2 | ~~发布模组 jar 到 GitHub Releases~~ | ✅ 已完成（`v0.0.1`、`v0.0.2`，CI 构建成功） |
| U3 | ~~本报表是否同步到仓库~~ | ✅ 已同步 |
| U4 | ~~推广到其它平台~~ | ✅ 项目所有者已完成 |
| U5 | 是否把本地工程也初始化成 git 仓库 | 用户决定 |
| U6 | 游戏内实测（真砍一刀验证） | 项目所有者表示已实测通过 |
| U7 | ~~把许可证修复同步到仓库~~ | ✅ 已同步（`8bded6d`） |
| U8 | ~~本报表同步到仓库~~ | ✅ 已同步 |

---

## 第四部分 · 可持续开发路线

### 4.1 加新功能的标准流程（详见 docs/DEVELOPMENT.md）

1. 建包 `com.icu.icu.gameplay.<模块名>`
2. 写 `<模块>Data.java`（状态 + Codec）
3. 在 `IcuAttachments` 注册一个 `AttachmentType`
4. 写 `<模块>Feature.java`，用 `@EventBusSubscriber` 订阅事件
5. 需要自定义伤害 → 加 `data/icu/damage_type/<名>.json`
6. 需要文案 → 加 `assets/icu/lang/*.json`
7. 更新 `README.md` 功能一览
8. 编译通过后提交

> ⚠️ `AttachmentType.builder(() -> new X())` 必须写 lambda；写 `X::new` 会编译歧义。

### 4.2 建议的功能路线（按主题推进）

| 阶段 | 模块 | 作用 | 依赖 |
|---|---|---|---|
| **当前** | `bleeding` | 大出血 | — |
| 下一步 | `treatment`（绷带/止血带） | 让出血从「死刑」变「可救治」 | bleeding |
| 再下一步 | `fracture`（骨折） | 限制移动的第二种损伤 | treatment |
| 之后 | `infection`（感染） | 伤口不处理会恶化 | treatment |
| 之后 | `hud` | 显示伤口状态 | bleeding |
| 最后 | `vitals`（生命体征） | 统一的生命值/意识系统 | 以上全部 |

### 4.3 长期维护要点

| 要点 | 说明 |
|---|---|
| 一个功能一个包 | 不要在 `gameplay/` 外放玩法逻辑 |
| 数值常量放类顶部 | 调参不用读逻辑 |
| 命名空间必须 = mod id | 新增资源一律放 `assets/icu/`、`data/icu/` |
| 升级 NeoForge 时 | 只改 `gradle.properties` 的 `neo_version`，并核对 `docs/DEVELOPMENT.md` 的 API 坑 |
| 每次提交前 | 跑第 7 节自检清单 |

---

## 第五部分 · 数据速查

| 指标 | 值 |
|---|---|
| 仓库文件数 | 26 |
| 源码类数 | 5（IcuMod + IcuAttachments + 3 个 bleeding 类） |
| 文档数 | 5（README + 4 个 docs） |
| 已实现玩法模块 | 1 |
| 重排提交改动量 | 22 文件，+561 / −355 |
| 释放磁盘 | 约 1.1 GB（清除游戏文件） |
| 命名统一度 | **100%**（代码/资源/配置零 `nofo` 残留） |
| 编译验证状态 | ✅ 已由项目所有者实测通过（改名后运行无问题） |

---

## 附：一句话交接

> 这是一个 NeoForge 1.21.1 模组，mod id `icu`，包 `com.icu.icu`，
> 玩法按模块放在 `gameplay/` 下，目前只有「大出血」一个模块。
> 代码已全部推到 GitHub 的 `STUTTEFRFIX/ICU`。
> **接手第一件事：跑一次 `build.bat` 确认改名后仍能编译。**
