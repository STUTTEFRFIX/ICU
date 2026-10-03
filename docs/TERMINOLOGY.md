# ICU 术语对照表 / Terminology

**本文件是 ICU 文档中英双语的唯一译名依据。**
**This file is the single source of truth for Chinese↔English terminology in ICU documentation.**

> ⚠️ **改中文文档时，必须同步改英文文档；改译名时，必须先改本文件。**
> ⚠️ **When you edit the Chinese docs, update the English docs in the same commit. When you change a term, change this file first.**
>
> 自动化检查：`tools/check_docs_parity.py`（由 `.github/workflows/build.yml` 调用）会比对
> 中英文档的标题数、代码块数、表格行数与版本号，不一致即构建失败。
> Automated check: `tools/check_docs_parity.py` (run by `.github/workflows/build.yml`) compares
> heading counts, code-block counts, table-row counts and version strings; any mismatch fails the build.

---

## 1. 玩法模块名 / Gameplay Module Names

| 中文 | English | 代码包 / Package（不译 / do not translate） |
|---|---|---|
| 大出血 | Severe Bleeding | `com.icu.icu.gameplay.bleeding` |
| 血容量 | Blood Volume | `com.icu.icu.gameplay.blood` |
| 疼痛值 | Pain Level | `com.icu.icu.gameplay.pain` |
| 摔落崴脚 | Fall Sprain | `com.icu.icu.gameplay.sprain` |
| 绷带 | Bandage | `com.icu.icu.item.BandageItem` |
| 大出血恢复期 | Bleeding Recovery Period | `com.icu.icu.gameplay.bleeding` |
| 心跳音效（未实现） | Heartbeat Sound (not implemented) | — |

---

## 2. 机制与数值术语 / Mechanics & Values

| 中文 | English |
|---|---|
| 出血层数 | bleeding stacks |
| 失血 | blood loss |
| 归零 | reaching zero |
| 致死路径 | lethal path |
| 强制趴下 | forced prone |
| 强制倒下 | forced collapse |
| 游泳姿态 | swimming pose |
| 锁移动 | movement locked |
| 禁跳 | jumping disabled |
| 锁击退 | knockback resistance |
| 自然恢复 | natural regeneration |
| 隐藏血量 | hidden health |
| 伤口复发 | the wound re-opens |
| 触发阈值 | trigger threshold |
| 最终伤害（护甲结算后） | final damage (after armor calculation) |
| 剑/斧类武器 | sword/axe weapons |
| 堆叠 | stack size |
| 右键按住 | hold right-click |
| 血粒子 | blood particles |
| 中文死亡消息 | Chinese-language death message |
| 反胃 | Nausea |
| 黑暗 | Darkness |
| 创造 / 旁观 / 无敌玩家 | Creative / Spectator / Invulnerable players |
| 豁免 | exempt |
| 完全免除 | fully negated |
| 附魔等级 | enchantment level |
| 免疫等级 | immunity level |

---

## 3. 原版游戏名词 / Vanilla Game Terms

> ⚠️ **必须使用官方英文名，不得自译。**
> ⚠️ **Use the official English names; never invent your own.**

| 中文 | English (official) |
|---|---|
| 保护（附魔） | Protection |
| 摔落保护（附魔） | Feather Falling |
| 干草块 | Hay Bale |
| 缓降 | Slow Falling |
| 鞘翅 | Elytra |
| 紫颂果 | Chorus Fruit |
| 抗火 | Fire Resistance |
| 纸 | Paper |
| 线 | String |
| 羊毛 | Wool |

---

## 4. 文档与工程术语 / Documentation & Engineering

| 中文 | English |
|---|---|
| 交接报告 | Handover Report |
| 项目结构说明 | Project Structure |
| 项目报表 | Project Report |
| 开发约定 | Development Conventions |
| 功能与变更总表 | Features & Changelog |
| 30 秒看懂代码 | The Code in 30 Seconds |
| 文档地图 | Documentation Map |
| 怎么构建 | How to Build |
| 当前功能一览 | Current Features |
| 安装 | Installation |
| 许可证 | License |
| 这是什么 | What This Is |
| 已核实 | Verified |
| 未验证 | Unverified |
| 踩坑记录 | Pitfalls |
| 风险登记 | Risk Register |
| 任务总账 | Task Ledger |
| 变更分类总账 | Change Ledger |
| 自检清单 | Pre-commit Checklist |
| 构建产物 | Build Artifacts |
| 源码压缩包 | source archive |

### 状态标记 / Status Markers

| 中文 | English |
|---|---|
| 待确认 | Pending Confirmation |
| 待执行 | Ready to Execute |
| 执行中 | In Progress |
| 已完成 | Completed |
| 阻塞 | Blocked |

---

## 5. ⛔ 一律不翻译 / Never Translate

以下内容在中英两版中**逐字相同**，包括大小写与标点。
The following stays **byte-for-byte identical** in both language versions.

| 类别 / Category | 例子 / Examples |
|---|---|
| 类名、包名 | `IcuMod` `IcuPose` `IcuAttachments` `IcuItems` `BleedingFeature` `SprainData` `PainFeature` `com.icu.icu.gameplay.pain` |
| 文件名、产物名 | `gradle.properties` `build.gradle` `icu-Mod-0.2.2.jar` `icu-0.2.2-src.zip` `BUILD_FAILURES.md` `neoforge.mods.toml` |
| 标签、版本号 | `v0.2.2` `0.2.2` `21.1.252` `1.21.1` `jei-1.21.1-neoforge-19.57.0.450` |
| 命令 | `build.bat` `gradlew.bat build` `git tag v0.1.0` |
| 代码块 | 缩进、键名、JSON 结构一律不动 / indentation, keys and JSON structure unchanged |
| **所有数值** | `7.0` `5%/s` `15%` `35/50/90/100%` `−20` `−8` `4 级` `100%` `20 秒` `5 分钟` — 必须逐字一致 / must match exactly |

---

## 6. 更新规程 / Update Procedure

1. 改中文正文 → 同一提交内改对应英文正文。 / Edit the Chinese text → edit the matching English text in the same commit.
2. 需要新译名 → **先改本文件**，再改正文。 / Need a new term → change **this file first**, then the text.
3. 提交流程 / Commit flow：

```bash
python tools/check_docs_parity.py
```

4. 检查不通过 → 构建失败，先修文档再提交。 / Check fails → build fails; fix the docs before committing.
