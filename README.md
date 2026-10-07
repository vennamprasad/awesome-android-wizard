# Awesome Android Project Wizard

[![Platform](https://img.shields.io/badge/Platform-Android%20Studio%20%7C%20IntelliJ%20IDEA-3DDC84.svg?logo=android)](https://developer.android.com/studio)
[![Gradle](https://img.shields.io/badge/Gradle-8.10.2-02303A.svg?logo=gradle)](https://gradle.org)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10.00-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

An enterprise-grade project and module scaffolding platform for **Android Studio** and **IntelliJ IDEA**. Enables mobile developers and engineering teams to generate production-ready, highly decoupled Android projects and feature modules with zero boilerplate.

---

## ✨ Key Capabilities

### 1. 🚀 One-Click Production Archetypes (Blueprints)
- **Consumer App / Startup**: Jetpack Compose M3, Koin, Ktor Client, Clean MVI, GitHub Actions CI.
- **Enterprise / Fintech**: Jetpack Compose M3, Hilt, Retrofit 2, Room DB, Proguard rules, Strict Detekt, GitHub Actions CI.
- **Minimal / Spike Prototype**: Single `:app` module, ultra-fast builds for rapid proofs of concept.
- **Custom Configuration**: Full manual control over every library, core module, and architecture parameter.

### 2. ⚡ Day-2 Scaffolding: Feature & Core Modules
- **New Feature Module**: Right-click anywhere ➔ **New ➔ Awesome Feature Module...** (<kbd>Ctrl+Alt+Shift+F</kbd>)
  - Generates decoupled `:feature:<name>` with Compose UI, ViewModel, and automatic settings/app dependency wiring.
- **New Core Module**: Right-click anywhere ➔ **New ➔ Awesome Core Module...** (<kbd>Ctrl+Alt+Shift+C</kbd>)
  - Instantly scaffolds `:core:<name>` with selectable module type:
    - **JVM Library**: Pure Kotlin DSL (`awesome.jvm.library`) with serialization and coroutines.
    - **Android Library**: Standard library (`awesome.android.library`) with Android namespace.
    - **Compose Library**: Android library pre-configured with Jetpack Compose & Material 3.
  - Automatically updates `settings.gradle.kts` and VFS file tree.

### 3. 📦 Modern Build System & Convention Plugins
- **Gradle Convention Plugins (`build-logic`)**: Eliminates build-script duplication across all modules:
  - `awesome.android.application`: App plugin configuration, SDK bounds, compiler options, and packaging resource exclusions.
  - `awesome.android.library`: Shared Android library conventions.
  - `awesome.android.library.compose`: Jetpack Compose & Compose Compiler configuration.
  - `awesome.jvm.library`: **Pure Kotlin/JVM convention plugin** for `:core:model` (zero Android SDK / AGP overhead, ultra-fast test runs).
- **Gradle Version Catalog (`gradle/libs.versions.toml`)**: Centralized, type-safe dependency management.

### 4. 💾 Offline-First Persistence & Test Architecture
- **`:core:database` (Room + KSP)**: Offline-first Room persistence layer:
  - Pre-configured `ItemEntity.kt` with `@PrimaryKey` and table mappings.
  - Reactive `ItemDao.kt` with `Flow<List<ItemEntity>>` observation and `@Upsert` queries.
  - `AwesomeDatabase.kt` Room instance ready for DI wiring.
- **`:core:testing` (Shared Test Fixtures & Semantic Tags)**:
  - `MainDispatcherRule.kt`: Reusable JUnit rule for overriding coroutine dispatchers in unit tests.
  - `FakeItemRepository.kt`: InMemory reactive fake repository for isolated UI/ViewModel testing.
  - `AwesomeTestTags.kt`: Standard Compose test tags for UI testing automation.

### 5. 🎨 Enterprise Clean Architecture & Design System
- **`:core:designsystem`**: Complete Design System token ecosystem adhering to Google Material 3 and *Now in Android* standards:
  - **Spacing & Dimensions (`Spacing.kt`)**: 4dp/8dp grid tokens (`extraSmall = 4.dp`, `small = 8.dp`, `medium = 16.dp`, `large = 24.dp`, `huge = 48.dp`, etc.) + component sizes (`iconMedium = 24.dp`, `minTouchTarget = 48.dp`, `cardElevation = 2.dp`).
  - **Corner Radius & Shapes (`Radius.kt`)**: Unified rounding tokens (`none = 0.dp`, `small = 8.dp`, `medium = 12.dp`, `large = 16.dp`, `full = 9999.dp`) mapped to `AwesomeShapes`.
  - **Typography Standard (`Type.kt`)**: Standard `.sp` font sizes & line-height tokens (`FontTokens.HeadlineMediumSize`, `TitleLargeSize`, `BodyMediumSize`, etc.) configured into `AwesomeTypography`.
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

### 6. 🛠 DevOps, Git Hooks & Code Quality
- **Git Pre-Commit Hook**: Automated `.githooks/pre-commit` hook running `./gradlew lintDebug` with root `installGitHooks` task to ensure bad commits never reach source control.
- **GitHub Actions**: `.github/workflows/android-ci.yml` (automated lint, unit test matrix, debug APK assembly).
- **Dependabot**: `.github/dependabot.yml` for automated weekly Gradle Version Catalog updates.
- **Detekt**: `config/detekt/detekt.yml` tuned for Jetpack Compose PascalCase functions.
- **`.editorconfig`**: Built-in coding standard compliance across Kotlin, XML, and Gradle files.
- **Pre-configured Run Configurations**: Instant `app` run configuration in `.idea/runConfigurations/app.xml`.

### 7. 🩺 Awesome Project Doctor (Diagnostics & Architecture Guard)
- Integrated diagnostic action accessible via **Tools ➔ Awesome Project Doctor...** or <kbd>Ctrl+Alt+Shift+D</kbd>.
- **Clean Architecture Dependency Guard**: Scans all `feature/*/build.gradle.kts` files to verify that feature modules never violate Clean Architecture (e.g. flagging direct forbidden dependencies on `:core:network` or other `:feature:*` modules).
- **Git Pre-Commit Hook Enforcer**: Checks if `.git/hooks/pre-commit` is installed and offers automated installation guidance.
- Audits Android SDK installation, Version Catalog syntax, Convention Plugin alignment, Gradle Wrapper versions, and unused module resources.

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

---

## 💻 Usage Guide

### 1. Installing the Plugin in Android Studio / IntelliJ IDEA
1. Build the distribution zip:
   ```bash
   gradle buildPlugin
   ```
   *(Or locate the pre-built zip at `build/distributions/awesome-android-wizard-1.0.0.zip`)*
2. In Android Studio or IntelliJ IDEA, open:
   - **Settings** (Windows/Linux) or **Preferences** (macOS) ➔ **Plugins**.
3. Click the gear icon (⚙️) ➔ **Install Plugin from Disk...**
4. Select `awesome-android-wizard-1.0.0.zip` and restart the IDE.

---

### 2. Creating a New Project (The 4-Step Wizard)
1. In Android Studio, go to:
   - **File ➔ New ➔ New Awesome Android Project...**
   - Or press shortcut: <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>A</kbd> (Mac: <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>A</kbd>).
2. **Step 1: Project Basics**:
   - Choose an Archetype (*Consumer App*, *Enterprise / Fintech*, *Minimal Prototype*, or *Custom*).
   - Enter Application Name, Package Name, and Destination Directory.
   - Select Minimum SDK (e.g., API 26+) and Target SDK (API 35).
3. **Step 2: Architecture & Modules**:
   - Select Architecture Pattern (*Modular Clean Architecture (MVI)* or *MVVM*).
   - Toggle Core Modules (`:core:designsystem`, `:core:ui`, `:core:model`, `:core:data`, `:core:network`).
   - Add custom dynamic feature module names (e.g., `auth`, `profile`, `settings`, `checkout`).
4. **Step 3: Tech Stack & Tooling**:
   - Dependency Injection: **Koin** (lightweight DSL) or **Hilt / Dagger** (compile-time code-gen).
   - Networking: **Ktor Client** or **Retrofit 2**.
   - Storage: **Room Database** or **None**.
   - DevOps & CI: Check GitHub Actions, Dependabot, and Detekt.
5. **Step 4: Summary & Generate**:
   - Review the generated module tree and click **Finish**.
   - The plugin scaffolds the complete project, configures Gradle wrapper binaries, and auto-detects `local.properties`.

---

### 3. Day-2 Scaffolding: Adding a Feature to an Existing Project
1. In any existing Android Studio project, right-click the project root or the `feature/` directory:
   - **New ➔ Awesome Feature Module...**
   - Or press shortcut: <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>F</kbd> (Mac: <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>F</kbd>).
2. Enter the feature name (e.g. `billing` or `dashboard`).
3. Click **Create Feature Module**.
4. The wizard will automatically:
   - Generate `:feature:<name>` with `build.gradle.kts`, `<Name>Screen.kt`, and `<Name>ViewModel.kt`.
   - Add `include(":feature:<name>")` into `settings.gradle.kts`.
   - Wire `implementation(project(":feature:<name>"))` into `app/build.gradle.kts`.
   - Prompt you to Sync Gradle.

---

### 4. Day-2 Scaffolding: Adding a Core Module to an Existing Project
1. In any existing Android Studio project, right-click the project root or the `core/` directory:
   - **New ➔ Awesome Core Module...**
   - Or press shortcut: <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd> (Mac: <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd>).
2. Enter the core module name (e.g. `analytics`, `security`, `datastore`).
3. Select Module Type:
   - **JVM Library (Pure Kotlin)**: For domain logic, utilities, rules (zero AGP build overhead).
   - **Android Library**: Standard Android library.
   - **Compose Library**: Android library with Jetpack Compose & Material 3.
4. Click **Create Core Module**.

---

### 5. Running the Project Doctor (Project Health & Architecture Guard)
1. In Android Studio, go to **Tools ➔ Awesome Project Doctor...**
   - Or press shortcut: <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>D</kbd> (Mac: <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>D</kbd>).
2. The dialog audits:
   - **Clean Architecture Boundaries**: Verifies feature modules don't directly reference `:core:network` or sister `:feature:*` modules.
   - **Git Hooks**: Detects if `.git/hooks/pre-commit` is active.
   - **SDK & Environment**: `local.properties`, SDK paths, Gradle version, Version Catalog integrity, Convention Plugins, and unused module resources.
3. Review any warnings and follow the suggested fixes to keep your multi-module architecture running at peak efficiency.

---

## ⌨️ Keyboard Shortcuts Cheatsheet

| Action | Shortcut (macOS) | Shortcut (Windows/Linux) |
| :--- | :--- | :--- |
| **New Awesome Project** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>A</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>A</kbd> |
| **New Feature Module** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>F</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>F</kbd> |
| **New Core Module** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd> |
| **Awesome Project Doctor** | <kbd>Cmd</kbd> + <kbd>Option</kbd> + <kbd>Shift</kbd> + <kbd>D</kbd> | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>D</kbd> |

---

## 🛠 Developing & Building the Plugin

### Prerequisites
- JDK 17 (Amazon Corretto, Eclipse Temurin, or OpenJDK)
- Gradle 8.10+ (or use system `gradle`)

### Common Tasks
```bash
# 1. Run a sandboxed IntelliJ/Android Studio instance with the plugin active
gradle runIde

# 2. Run unit and generator verification tests
gradle test

# 3. Compile Kotlin plugin code
gradle compileKotlin

# 4. Build distribution zip package
gradle buildPlugin
```

---

## 💡 Troubleshooting & FAQ

#### 1. "Could not install Gradle distribution... SocketTimeoutException"
The plugin includes pre-configured `networkTimeout=60000` and `validateDistributionUrl=false` in `gradle-wrapper.properties`. If developing in an offline or restricted environment, ensure Gradle 8.10.2 binary is extracted in `~/.gradle/wrapper/dists/gradle-8.10.2-bin/`.

#### 2. "SDK location not found"
The wizard automatically detects `ANDROID_HOME`, `ANDROID_SDK_ROOT`, or standard paths (`~/Library/Android/sdk` on macOS, `~/AppData/Local/Android/Sdk` on Windows) and generates a `local.properties` file with `sdk.dir` pointing to your local SDK.

#### 3. Why is `:core:model` not an Android library?
Making `:core:model` a pure Kotlin JVM library (`awesome.jvm.library`) removes Android SDK and AAPT2 overhead, reducing compilation time by up to 5x and making models portable across platforms.
