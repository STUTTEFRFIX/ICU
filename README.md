# ICU

**在 Minecraft 中还原真实的身体损伤与医疗体系。**

| | |
|---|---|
| 平台 | Minecraft **1.21.1** + NeoForge **21.1.252** |
| 构建 | ModDevGradle 1.0.24 · Gradle 8.14.3 · Java 21 |
| mod id | `icu` |
| 当前版本 | 0.0.1（首个发布版） |
| 许可证 | **Apache-2.0**（允许修改与再分发） |
| 已实现模块 | **1 个**：大出血（`gameplay.bleeding`） |

---

## 这是什么

ICU 把「受伤」和「救治」做成一套写实的机制。玩法以**独立模块**形式添加，一个功能一个包，互不干扰。

本版本只包含第一个模块：**大出血（Haemorrhage）**——被刀斧类武器重创后会持续失血，直到死亡。

---

## 30 秒看懂代码

```
src/main/java/com/icu/icu/
├── IcuMod.java                  ← 起点：模组入口，只做注册
├── IcuAttachments.java          ← 数据存储：玩家身上的伤口状态挂在这里
└── gameplay/                    ← ★ 所有玩法的家，一个功能一个子包
    └── bleeding/                ← 大出血模块
        ├── BleedingData.java       数据：出血层数
        ├── BleedingDamage.java     伤害：自定义伤害类型 icu:bleed
        └── BleedingFeature.java    规则：触发、扣血、强制趴下、效果（★核心）
```

**想改玩法 → 只动 `gameplay/bleeding/`。**
**想加新玩法 → 在 `gameplay/` 下新建一个包（如 `fracture/`）。** 详见 [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)。

---

## 文档地图

| 文档 | 什么时候看 |
|---|---|
| **本文件** | 第一次接触项目 |
| [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) | 想知道每个文件具体干什么 |
| [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) | 要加新功能、改代码之前 |
| [docs/PROJECT_REPORT.md](docs/PROJECT_REPORT.md) | 项目管理、问题分析、接手总览 |
| [docs/HANDOVER.md](docs/HANDOVER.md) | 接手项目，想快速了解全貌与当前状态 |

---

## 怎么构建

**方式一：直接下载（普通玩家用这个）**

到 [Releases](https://github.com/STUTTEFRFIX/ICU/releases) 下载 `icu-0.0.1.jar`，丢进实例的 `mods/` 文件夹即可。

**方式二：自己编译**

```bat
build.bat
```

产物：`build/libs/icu-0.0.1.jar`。

> 本机有沙箱限制，`build.bat` 已内置所需环境变量（JDK 21 路径等）。
> 在没有限制的普通机器上，直接 `gradlew.bat build` 即可。
> 细节与踩坑记录见 [docs/PROJECT_REPORT.md](docs/PROJECT_REPORT.md) 与 [docs/HANDOVER.md](docs/HANDOVER.md)。

**方式三：云端自动构建（发新版用这个）**

推送一个 `v*` 标签，GitHub Actions 会自动编译并把 jar 发布到 Release：

```bash
git tag v0.0.2
git push origin v0.0.2
```

> 标签即版本号：`v0.0.2` → mod 版本 `0.0.2`（workflow 自动写入 `gradle.properties`）。

---

## 当前功能一览

**大出血**：被剑/斧类武器命中、且**护甲结算后**的最终伤害 > 10 点（5 心）时触发。

| 行为 | 数值 |
|---|---|
| 出血层数 | 每命中一次 +1，**无上限** |
| 失血速度 | 每层 **2 点/秒**（1 心/秒） |
| 持续时间 | **无时限**，只有死亡重生能结束 |
| 强制趴下 | 游泳姿态 + 锁移动 + 禁跳 + 锁击退 |
| 附加效果 | 反胃 + 黑暗（持续刷新） |
| 表现 | 血粒子、中文死亡消息 |

> 因为「无时限 + 无治疗手段」，**第一层就已是致命伤**，叠加只决定死亡快慢。
> 这是设计选择，不是缺陷。参数都能在 `BleedingFeature` 顶部改。

---

## 许可证

本项目采用 **Apache License 2.0**。三处声明完全一致：

| 位置 | 内容 |
|---|---|
| [`LICENSE`](LICENSE) | Apache License 2.0 全文 |
| [`gradle.properties`](gradle.properties) | `mod_license=Apache-2.0`（会写入模组元数据，游戏内模组列表可见） |
| 本文件 | 你正在看的这一节 |

**你可以**：自由使用、修改、分发本项目，包括商业用途与二次开发。
**条件**：保留版权与许可证声明，并标明你做出的修改（见 LICENSE 第 4 条）。
**不提供担保**：见 LICENSE 第 7 条。
