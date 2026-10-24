# Awesome Android Project Wizard

[![Platform](https://img.shields.io/badge/Platform-Android%20Studio%20%7C%20IntelliJ%20IDEA-3DDC84.svg?logo=android)](https://developer.android.com/studio)
[![Gradle](https://img.shields.io/badge/Gradle-9.8.1-02303A.svg?logo=gradle)](https://gradle.org)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.21-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-9.4.1-34A853.svg?logo=android)](https://developer.android.com/build)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.09.00-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![SDK](https://img.shields.io/badge/Compile%20SDK-37%20(Android%2017)-E8710A.svg)](https://developer.android.com/about/versions)

An enterprise-grade project and modular component scaffolding platform for **Android Studio** and **IntelliJ IDEA**. Enables mobile developers and engineering teams to generate production-ready, highly decoupled Android projects, feature modules, architectural slices, and multiplatform companions with zero boilerplate.

---

## ✨ Key Capabilities

### 1. 🚀 One-Click Production Archetypes (Blueprints)
- **Consumer App / Startup**: Jetpack Compose M3, Koin, Ktor Client, Clean MVI, GitHub Actions CI.
- **Enterprise / Fintech**: Jetpack Compose M3, Hilt, Retrofit 2, Room DB, Proguard rules, Strict Detekt, GitHub Actions CI.
- **Minimal / Spike Prototype**: Single `:app` module, ultra-fast builds for rapid proofs of concept.
- **Custom Configuration**: Full manual control over every library, core module, and architecture parameter.
- **Interactive Two-Way State Synchronization**: Selecting an archetype dynamically updates and pre-fills all subsequent wizard steps while preserving customized adjustments.

### 2. ⚡ Modular Architecture Scaffolding: Feature & Core Modules
- **New Feature Module** (<kbd>Ctrl+Alt+Shift+F</kbd>):
  - Generates decoupled `:feature:<name>` with Compose UI, ViewModel, and automatic settings/app dependency wiring.
- **New Core Module** (<kbd>Ctrl+Alt+Shift+C</kbd>):
  - Instantly scaffolds `:core:<name>` with selectable module type:
    - **JVM Library**: Pure Kotlin DSL (`awesome.jvm.library`) with serialization and coroutines (zero AGP overhead).
    - **Android Library**: Standard library (`awesome.android.library`) with Android namespace.
    - **Compose Library**: Android library pre-configured with Jetpack Compose & Material 3.
- **New MVI Feature Slice** (<kbd>Ctrl+Alt+Shift+M</kbd>):
  - Generates dedicated `<Feature>Contract.kt` (`UiState`, `UiIntent`, `UiEffect`), `<Feature>ViewModel.kt` (Hilt or Koin), `<Feature>Screen.kt`, `<Feature>Navigation.kt` (Navigation Compose 2.8+ or Navigation 3), and Turbine unit tests.
- **New Repository Slice** (<kbd>Ctrl+Alt+Shift+R</kbd>):
  - Generates Domain Entity, Network DTO + Mapper, Repository interface, Default implementation, and InMemory Test Fake.
- **Companion Modules (Wear OS, Android Auto, Benchmark)**:
  - Automated scaffolding for `:wear` (<kbd>Ctrl+Alt+Shift+W</kbd>), `:automotive` (<kbd>Ctrl+Alt+Shift+U</kbd>), and `:benchmark` (<kbd>Ctrl+Alt+Shift+B</kbd>).

### 3. 🛡️ Robust Generator Engine (`GeneratorUtils`)
- **Smart Package Introspection**: Automatically resolves base application namespace and applicationId from `app/build.gradle.kts` (with manifest fallback) to prefill dialogs cleanly.
- **Safe Gradle Settings Synchronization**: Idempotently inserts `include(...)` statements without creating duplicates or formatting regressions.
- **Safe Dependency Block Wiring**: Injects `implementation(project(":feature:..."))` into `app/build.gradle.kts` without touching or breaking other dependencies.
- **Single Source of Truth (`AwesomeVersions`)**: All generated version catalog entries, build scripts, and dependencies strictly synchronize with central toolchain constants.

### 4. 📦 Modern Build System & Convention Plugins
- **Gradle Convention Plugins (`build-logic`)**: Eliminates build-script duplication across all modules:
  - `awesome.android.application`: App plugin configuration, SDK bounds, compiler options, and packaging resource exclusions.
  - `awesome.android.library`: Shared Android library conventions.
  - `awesome.android.library.compose`: Jetpack Compose & Compose Compiler configuration.
  - `awesome.jvm.library`: **Pure Kotlin/JVM convention plugin** for `:core:model` (zero Android SDK / AGP overhead, ultra-fast test runs).
- **Gradle Version Catalog (`gradle/libs.versions.toml`)**: Centralized, type-safe dependency management.

### 5. 💾 Offline-First Persistence & Test Architecture
- **`:core:database` (Room + KSP)**: Offline-first Room persistence layer:
  - Pre-configured `ItemEntity.kt` with `@PrimaryKey` and table mappings.
  - Reactive `ItemDao.kt` with `Flow<List<ItemEntity>>` observation and `@Upsert` queries.
  - `AwesomeDatabase.kt` Room instance ready for DI wiring.
- **`:core:testing` (Shared Test Fixtures & Semantic Tags)**:
  - `MainDispatcherRule.kt`: Reusable JUnit rule for overriding coroutine dispatchers in unit tests.
  - `FakeItemRepository.kt`: InMemory reactive fake repository for isolated UI/ViewModel testing.
  - `AwesomeTestTags.kt`: Standard Compose test tags for UI testing automation.

### 6. 🎨 Enterprise Clean Architecture & Design System
- **`:core:designsystem`**: Complete Design System token ecosystem adhering to Google Material 3 and *Now in Android* standards:
  - **Spacing & Dimensions (`Spacing.kt`)**: 4dp/8dp grid tokens (`extraSmall = 4.dp`, `small = 8.dp`, `medium = 16.dp`, `large = 24.dp`, `huge = 48.dp`, etc.) + component sizes (`iconMedium = 24.dp`, `minTouchTarget = 48.dp`, `cardElevation = 2.dp`).
  - **Corner Radius & Shapes (`Radius.kt`)**: Unified rounding tokens (`none = 0.dp`, `small = 8.dp`, `medium = 12.dp`, `large = 16.dp`, `full = 9999.dp`) mapped to `AwesomeShapes`.
  - **Typography Standard (`Type.kt`)**: Standard `.sp` font sizes & line-height tokens (`HeadlineMediumSize`, `TitleLargeSize`, `BodyMediumSize`, etc.) configured into `AwesomeTypography`.
  - **Semantic Color Scheme & Extended Colors (`Color.kt`)**: Material 3 Light/Dark schemes + semantic tokens (`ExtendedColors`) for `success`, `warning`, and `info` status indicators.
  - **Centralized Icon Catalog (`AwesomeIcons.kt`)**: Unified `AwesomeIconSource` handling both `ImageVector` and drawable resource IDs (`AwesomeIcons.Home`, `Search`, `Notifications`, `Settings`, `Profile`, `Logo`) paired with reusable `AwesomeIcon` composable.
  - **Theme DataStore Persistence (`ThemeConfig.kt`, `ThemeDataStore.kt`)**: Pre-configured Jetpack Preferences DataStore reactive flow (`userThemePreferences: Flow<UserThemePreferences>`) supporting `DarkThemeConfig` (`FOLLOW_SYSTEM`, `LIGHT`, `DARK`) and `dynamicColor`.
  - **Type-Safe Theme Accessor (`AwesomeTheme`)**: Static access to all design tokens anywhere in Compose UI:
    ```kotlin
    AwesomeTheme.spacing.medium
    AwesomeTheme.radius.large
    AwesomeTheme.typography.headlineMedium
    AwesomeTheme.colorScheme.primary
    AwesomeTheme.extendedColors.success
    ```
- **`:core:ui`**: Reusable Compose widgets (`AwesomeCard`, `AwesomeLoadingWheel`, etc.) consuming `AwesomeTheme` tokens.
- **`:core:model`**: Pure Kotlin JVM module for domain models, value objects, and serialization.
- **`:core:network`**: Isolated API services, Ktor/Retrofit data sources, and `NetworkResult` contracts.
- **`:core:data`**: Clean repository contracts (`ItemRepository`) with internal implementation encapsulation.
- **Interactive Multi-Feature Navigation**: `MainActivity.kt` provides a bottom `NavigationBar` with icon mapping across all selected features.

### 7. 🛠 DevOps, Git Hooks & Code Quality
- **Git Pre-Commit Hook**: Automated `.githooks/pre-commit` hook running `./gradlew lintDebug` with root `installGitHooks` task to ensure bad commits never reach source control.
- **GitHub Actions**: `.github/workflows/android-ci.yml` (automated lint, unit test matrix, debug APK assembly).
- **Dependabot**: `.github/dependabot.yml` for automated weekly Gradle Version Catalog updates.
- **Detekt**: `config/detekt/detekt.yml` tuned for Jetpack Compose PascalCase functions.
- **`.editorconfig`**: Built-in coding standard compliance across Kotlin, XML, and Gradle files.
- **Pre-configured Run Configurations**: Instant `app` run configuration in `.idea/runConfigurations/app.xml`.

### 8. 🩺 Awesome Architecture Tool Window & Project Doctor
- **Awesome Architecture Tool Window**: Interactive side panel accessible on the right edge of Android Studio / IntelliJ:
  - Visual module graph and dependency tree.
  - Real-time Clean Architecture boundary audit.
  - Version Catalog dependency inspection and upgrade advisory.
- **Awesome Project Doctor** (<kbd>Ctrl+Alt+Shift+D</kbd>):
  - Deep-dive diagnostic tool scanning project configuration, SDK installations, Gradle Wrapper integrity, and circular module dependencies.

---

## 🏗 Architecture & Modules

The generated projects follow Google's official Android Architecture Guide and *Now in Android* modularization principles:

```text
                               ┌─────────┐
                               │  :app   │ (Application entry & Navigation)
                               └────┬────┘
                                    │
           ┌────────────────────────┼────────────────────────┐
           ▼                        ▼                        ▼
    ┌──────────────┐         ┌──────────────┐         ┌──────────────┐
    │:feature:<one>│         │:feature:<two>│         │:feature:...  │
    └──────┬───────┘         └──────┬───────┘         └──────┬───────┘
           │                        │                        │
           └────────────────────────┼────────────────────────┘
                                    │
           ┌────────────────────────┼────────────────────────┐
           ▼                        ▼                        ▼
     ┌───────────┐            ┌───────────┐            ┌───────────┐
     │ :core:ui  │            │:core:data │            │:core:model│ (Pure JVM)
     └─────┬─────┘            └─────┬─────┘            └───────────┘
           ▼                        ▼
 ┌──────────────────┐     ┌───────────────┐
 │:core:designsystem│     │:core:database │ (Room + KSP)
 └──────────────────┘     └───────┬───────┘
                                  ▼
                            ┌─────────────┐
                            │:core:network│
                            └─────────────┘
```

### Module Matrix

| Module | Type | Purpose | Needs `res/`? |
| :--- | :--- | :--- | :---: |
| **`:app`** | Android Application | Composition root, `AwesomeApplication`, `MainActivity` with bottom tab navigation. | ✅ Yes |
| **`:core:designsystem`** | Android Library | M3 `AwesomeTheme`, Dynamic colors, typography styles, vector drawables & icons. | ✅ Yes |
| **`:core:ui`** | Android Library | Reusable Composable widgets (`AwesomeCard`, `AwesomeLoadingWheel`). | ❌ Compose only |
| **`:core:model`** | **Pure Kotlin JVM** | Domain entities, value objects, data classes. Zero AGP/Android SDK overhead. | ❌ None |
| **`:core:network`** | Android Library | API services, data sources, serialization adapters, DTO models. | ❌ None |
| **`:core:database`** | Android Library | Room entities, DAOs, and database definition. Offline-first local store. | ❌ None |
| **`:core:data`** | Android Library | Repositories, offline caching, domain mappers, data sync. | ❌ None |
| **`:core:testing`** | Android Library | JUnit test rules (`MainDispatcherRule`), test fakes, Compose test tags. | ❌ None |
| **`:feature:*`** | Android Library | Vertical feature slices (e.g. `home`, `auth`, `profile`, `settings`). | ❌ Optional |
| **`:wear`** | Android Application | Standalone Wear OS companion app with Wear Compose M3 and TileService. | ✅ Yes |
| **`:automotive`** | Android Application | Android Auto & AAOS application using AndroidX Car App Library. | ✅ Yes |
| **`:benchmark`** | Android Test | Macrobenchmark & Baseline Profile generator module targeting `:app`. | ❌ None |

---

## 💻 Usage Guide

### 1. Installing the Plugin in Android Studio / IntelliJ IDEA
1. Build the distribution zip:
   ```bash
   ./gradlew buildPlugin
   ```
2. In Android Studio or IntelliJ IDEA, open:
   - **Settings** (Windows/Linux) or **Preferences** (macOS) ➔ **Plugins**.
3. Click the gear icon (⚙️) ➔ **Install Plugin from Disk...**
4. Select the built `.zip` from `build/distributions/` and restart the IDE.

---

### 2. Creating a New Project (The 5-Step Wizard)
1. In Android Studio, go to:
   - **File ➔ New ➔ New Awesome Android Project...**
   - Or press shortcut: <kbd>Ctrl+Alt+Shift+A</kbd> (Mac: <kbd>Cmd+Option+Shift+A</kbd>).
2. **Step 1: Project Basics**:
   - Choose an Archetype (*Consumer App*, *Enterprise / Fintech*, *Minimal Prototype*, or *Custom*).
   - Enter Application Name, Package Name, and Destination Directory.
   - Select Minimum SDK (API 26+) and Target/Compile SDK (API 37).
3. **Step 2: Architecture & Modules**:
   - Select Architecture Pattern (*Modular Clean Architecture (MVI)* or *MVVM*).
   - Toggle Core Modules (`:core:designsystem`, `:core:ui`, `:core:model`, `:core:data`, `:core:network`, `:core:database`, `:core:testing`).
   - Add custom dynamic feature module names (e.g., `home`, `feed`, `profile`, `checkout`).
4. **Step 3: Device & Theme Setup**:
   - Select target form factor (*Mobile*, *Tablet & Foldables*, *Android TV*, or *Adaptive Universal*).
   - Select theme color palette (*Dynamic Material You*, *Ocean Blue*, *Emerald Green*, etc.).
5. **Step 4: Tech Stack & Tooling**:
   - Dependency Injection: **Hilt / Dagger** or **Koin**.
   - Networking: **Retrofit 2** or **Ktor Client**.
   - Storage: **Room Database** or **None**.
   - DevOps & CI: Check GitHub Actions, Dependabot, and Detekt.
6. **Step 5: Summary & Generate**:
   - Review the generated module tree and click **Finish**.
   - The plugin scaffolds the complete project, configures Gradle wrapper binaries, and auto-detects `local.properties`.

---

### 3. Scaffolding a Feature Module (<kbd>Ctrl+Alt+Shift+F</kbd>)
1. Right-click anywhere in the Project View ➔ **Awesome Android ➔ Awesome Feature Module...**
2. Enter the feature name (e.g., `checkout` or `search`).
3. Click **OK**.
4. The generator will:
   - Create `:feature:<name>` with `build.gradle.kts`, `<Name>Contract.kt`, `<Name>ViewModel.kt`, `<Name>Screen.kt`, and `<Name>Navigation.kt`.
   - Idempotently register `include(":feature:<name>")` in `settings.gradle.kts`.
   - Wire `implementation(project(":feature:<name>"))` into `app/build.gradle.kts`.

---

### 4. Scaffolding a Core Module (<kbd>Ctrl+Alt+Shift+C</kbd>)
1. Right-click anywhere in the Project View ➔ **Awesome Android ➔ Awesome Core Module...**
2. Enter the core module name (e.g., `analytics`, `security`, `datastore`).
3. Select Module Type:
   - **Pure Kotlin JVM Library**: For domain logic and calculation engines (zero Android SDK overhead).
   - **Android Library**: Standard Android library.
   - **Compose Library**: Android library pre-configured with Jetpack Compose & Material 3.
4. Click **OK**.

---

### 5. Scaffolding an MVI Feature Slice (<kbd>Ctrl+Alt+Shift+M</kbd>)
1. Right-click any package or module in the Project View ➔ **Awesome Android ➔ Awesome MVI Feature Slice...**
2. Configure Slice Name, Target Package, and Navigation Framework (Navigation Compose or Navigation 3).
3. The generator will produce:
   - `<Feature>Contract.kt`: Immutable `UiState`, `UiIntent`, and single-shot `UiEffect`.
   - `<Feature>ViewModel.kt`: StateFlow + Channel MVI implementation wired with Hilt or Koin.
   - `<Feature>Screen.kt`: Material 3 Jetpack Compose screen consuming `AwesomeTheme`.
   - `<Feature>Navigation.kt`: Type-safe route navigation integration.
   - `<Feature>ViewModelTest.kt`: Unit tests verifying StateFlow emissions and effects using Turbine.

---

### 6. Scaffolding a Repository Slice (<kbd>Ctrl+Alt+Shift+R</kbd>)
1. Right-click any package or module ➔ **Awesome Android ➔ Awesome Repository Slice...**
2. Enter Entity Name (e.g., `Product` or `User`).
3. Generates complete Clean Architecture data flow:
   - `model/<Entity>.kt`: Immutable domain entity.
   - `network/Network<Entity>.kt`: Serialization DTO with domain mapper.
   - `repository/<Entity>Repository.kt`: Clean interface contract exposing `Flow`.
   - `repository/Default<Entity>Repository.kt`: Production implementation with offline cache support.
   - `repository/Fake<Entity>Repository.kt`: Test double fake for isolated unit/UI testing.

---

### 7. Scaffolding Multiplatform Companions
- **Wear OS Companion Module** (<kbd>Ctrl+Alt+Shift+W</kbd>):
  - Scaffolds standalone `:wear` module with Wear Compose M3, `AmbientLifecycleObserver`, and `AwesomeTileService`.
- **Android Auto & Automotive Module** (<kbd>Ctrl+Alt+Shift+U</kbd>):
  - Scaffolds `:automotive` module configured with AndroidX Car App Library, `CarAppService`, and navigation session templates.
- **Macrobenchmark & Baseline Profiles Module** (<kbd>Ctrl+Alt+Shift+B</kbd>):
  - Scaffolds `:benchmark` module with `BaselineProfileGenerator` and `StartupBenchmark` measuring cold startup and frame timing against `:app`.

---

### 8. Awesome Architecture Tool Window & Project Doctor (<kbd>Ctrl+Alt+Shift+D</kbd>)
- Access the **Awesome Architecture** tool window on the right side of the IDE to visualize module dependency flows and spot boundary violations in real time.
- Launch **Awesome Project Doctor** via **Tools ➔ Awesome Project Doctor...** or <kbd>Ctrl+Alt+Shift+D</kbd> to audit:
  - Clean Architecture boundary violations (e.g. features directly referencing `:core:network`).
  - Circular module dependencies.
  - Version Catalog dependency alignment against latest Google/Android standards.
  - SDK environment setup and Git pre-commit hook installation.

---

## ⌨️ Keyboard Shortcuts Cheatsheet

| Action | Shortcut (macOS) | Shortcut (Windows/Linux) |
| :--- | :--- | :--- |
| **New Awesome Project** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>A</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>A</kbd> |
| **New Feature Module** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>F</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>F</kbd> |
| **New Core Module** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd> |
| **New MVI Feature Slice** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>M</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>M</kbd> |
| **New Repository Slice** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>R</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>R</kbd> |
| **New Benchmark Module** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>B</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>B</kbd> |
| **New Wear OS Module** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>W</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>W</kbd> |
| **New Automotive Module** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>U</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>U</kbd> |
| **Awesome Project Doctor** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>D</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>D</kbd> |

---

## 🛠 Developing & Building the Plugin

### Prerequisites
- JDK 17 (Amazon Corretto, Eclipse Temurin, or OpenJDK)
- IntelliJ Platform Gradle Plugin 2.x

### Common Gradle Commands
```bash
# 1. Run a sandboxed IntelliJ/Android Studio instance with the plugin active
./gradlew runIde

# 2. Run unit and generator verification test suites
./gradlew test

# 3. Run full verification and lint checks
./gradlew check

# 4. Build plugin distribution zip
./gradlew buildPlugin
```

---

## 💡 Troubleshooting & FAQ

#### 1. "Could not install Gradle distribution... SocketTimeoutException"
The plugin includes pre-configured `networkTimeout=60000` and `validateDistributionUrl=false` in `gradle-wrapper.properties`. If developing in an offline or restricted environment, ensure Gradle 9.8.1 binary is extracted in `~/.gradle/wrapper/dists/gradle-9.8.1-bin/`.

#### 2. "SDK location not found"
The wizard automatically detects `ANDROID_HOME`, `ANDROID_SDK_ROOT`, or standard paths (`~/Library/Android/sdk` on macOS, `~/AppData/Local/Android/Sdk` on Windows) and generates a `local.properties` file with `sdk.dir` pointing to your local SDK.

#### 3. Why is `:core:model` a pure JVM module instead of an Android Library?
Making `:core:model` a pure Kotlin JVM library (`awesome.jvm.library`) eliminates Android SDK, manifest merging, and AAPT2 overhead, reducing compilation time by up to 5x and making your business models completely portable.
