package prasad.vennam.awesomeandroidwizard.generator.templates

import prasad.vennam.awesomeandroidwizard.model.WizardModel

object RootFilesTemplate {

    fun generateSettingsGradle(model: WizardModel): String {
        val sb = StringBuilder()
        sb.appendLine("""
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "${model.projectName}"
""".trimIndent())

        if (model.useConventionPlugins) {
            sb.appendLine("includeBuild(\"build-logic\")")
        }

        sb.appendLine("include(\":app\")")
        for (module in model.getAllSelectedModules()) {
            val gradlePath = ":" + module.replace("/", ":")
            sb.appendLine("include(\"$gradlePath\")")
        }

        return sb.toString()
    }

    fun generateRootBuildGradle(model: WizardModel): String {
        return """
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// Git hooks installer task
tasks.register("installGitHooks", Copy::class) {
    from(file(".githooks"))
    into(file(".git/hooks"))
    filePermissions {
        unix("rwxr-xr-x")
    }
}
""".trimIndent()
    }

    fun generatePreCommitHook(): String = """
#!/bin/sh
# Awesome Android Project Pre-Commit Hook
echo "🔍 Running pre-commit static analysis & code quality checks..."

./gradlew lintDebug --daemon || {
    echo "❌ Pre-commit checks failed! Fix lint issues before committing."
    exit 1
}

echo "✅ Pre-commit verification passed."
""".trimIndent()


    fun generateGradleProperties(): String = """
# Project-wide Gradle settings.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configuration-cache=true

# AndroidX package structure to make it clearer which packages are bundled with the
# Android operating system, and which are packaged with your app's APK
android.useAndroidX=true
# Automatically convert third-party libraries to use AndroidX
android.enableJetifier=false

# Kotlin code style
kotlin.code.style=official
""".trimIndent()

    fun generateGradleWrapperProperties(): String = """
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip
networkTimeout=60000
validateDistributionUrl=false
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
""".trimIndent()

    fun generateGitignore(): String = """
# Built application files & artifacts
*.apk
*.aar
*.ap_
*.aab
*.dex

# Java & Kotlin classes and binaries
*.class
bin/
gen/
out/

# Gradle & Kotlin build caches across all modules
.gradle/
.kotlin/
build/
**/build/

# Local Android SDK configuration
local.properties
/local.properties

# Proguard / R8 mapping files
proguard/
**/proguard/

# Android Studio & IntelliJ IDEA
*.iml
.idea/caches/
.idea/libraries/
.idea/modules.xml
.idea/workspace.xml
.idea/navEditor.xml
.idea/assetWizardSettings.xml
.idea/deploymentTargetDropDown.xml
.idea/compiler.xml
.idea/misc.xml
.idea/vcs.xml
.idea/shelf/
.idea/tasks.xml
.idea/usage.statistics.xml
.idea/dictionaries/
!/.idea/runConfigurations/

# Keystores & sensitive credentials
*.jks
*.keystore
signing.properties
google-services.json

# C/C++ native builds
.externalNativeBuild
.cxx
cmake_build/

# OS-specific files
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db

# Android Studio profiler & dumps
captures/
*.hprof
*.log

# Fastlane & Reports
fastlane/report.xml
fastlane/Preview.html
fastlane/screenshots
reports/
""".trimIndent()

    fun generateReadme(model: WizardModel): String = """
# ${model.projectName}

Enterprise-grade, modular Android application built with **Jetpack Compose**, **Material 3**, and **${model.architecture.displayName}**.

Scaffolded using the **Awesome Android Project Wizard** plugin.

---

## 🏗 Architecture & Modules

The project is structured according to Google's official Android Architecture Guide and *Now in Android* modularization principles:

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
 ┌──────────────────┐         ┌─────────────┐
 │:core:designsystem│         │:core:network│
 └──────────────────┘         └─────────────┘
```

### Module Breakdown:

| Module | Type | Description | Resources (`res/`) |
| :--- | :--- | :--- | :---: |
| **`:app`** | Android Application | Composition root, `AwesomeApplication`, `MainActivity` with feature navigation. | ✅ Yes |
| **`:core:designsystem`** | Android Library | Design tokens, Material 3 `AwesomeTheme`, Dynamic colors, Typography, Vector assets. | ✅ Yes |
| **`:core:ui`** | Android Library | Reusable UI components (`AwesomeCard`, `AwesomeLoadingWheel`, etc.). | ❌ Compose only |
| **`:core:model`** | **Pure Kotlin JVM** | Data models & domain entities. Zero Android SDK/AGP overhead for blazing builds. | ❌ None |
| **`:core:network`** | Android Library | API services, Ktor/Retrofit data sources, JSON serializers, DTOs. | ❌ None |
| **`:core:data`** | Android Library | Repository pattern, offline-first caching, domain mapping, data sources. | ❌ None |
| **`:feature:*`** | Android Library | Clean feature slices containing MVI/MVVM screens and ViewModels. | ❌ Optional |

${if (model.useConventionPlugins) "### ⚡ Build Logic (`build-logic/`)\nShared Gradle convention plugins eliminate build-script duplication:\n- `awesome.android.application`: Configures Android app plugins, SDK targets, and Kotlin options.\n- `awesome.android.library`: Configures Android library standards.\n- `awesome.android.library.compose`: Configures Jetpack Compose & Compose Compiler.\n- `awesome.jvm.library`: Pure Kotlin/JVM convention plugin for zero-overhead domain/model modules.\n" else ""}

---

## 🚀 Tech Stack

- **UI & Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material 3](https://m3.material.io/) (Edge-to-Edge)
- **Dependency Injection:** ${model.diFramework.displayName}
- **Networking:** ${model.networking.displayName}
- **Database / Storage:** ${model.database.displayName}
- **Asynchronous Flow:** Kotlin Coroutines & `StateFlow`
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with [Version Catalog](gradle/libs.versions.toml)

---

## 🛠 Usage & Build Commands

### 1. Build and Assemble
Compile and assemble the debug APK:
```bash
./gradlew assembleDebug
```
The output APK is generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Run All Unit Tests
Run unit tests across all core and feature modules in parallel:
```bash
./gradlew test
```

### 3. Code Quality & Linting
Run static analysis and linting:
```bash
${if (model.generateDetekt) "./gradlew detekt" else "./gradlew lintDebug"}
```

### 4. Running in Android Studio
1. Open the project folder in **Android Studio Hedgehog / Ladybug** or newer.
2. Wait for Gradle Sync to complete.
3. Select the `app` run configuration and click **Run (▶)** on an emulator or connected device.
""".trimIndent()

    fun generateEditorConfig(): String = """
# http://editorconfig.org
root = true

[*]
charset = utf-8
end_of_line = lf
indent_size = 4
indent_style = space
insert_final_newline = true
trim_trailing_whitespace = true

[*.{kt,kts}]
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
ij_kotlin_imports_layout = *,|,import javax.**,import java.**,|,import *
max_line_length = 120

[*.{xml,json}]
indent_size = 4

[*.md]
trim_trailing_whitespace = false
""".trimIndent()

    fun generateRunConfigurationXml(): String = """
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="app" type="AndroidRunConfigurationType" factoryName="Android App" activateToolWindowBeforeRun="false">
    <module name="app" />
    <option name="DEPLOY" value="true" />
    <option name="DEPLOY_APK_FROM_BUNDLE" value="false" />
    <option name="DEPLOY_AS_INSTANT" value="false" />
    <option name="ARTIFACT_NAME" value="" />
    <option name="PM_INSTALL_OPTIONS" value="" />
    <option name="ALL_USERS" value="false" />
    <option name="ALWAYS_INSTALL_WITH_PM" value="false" />
    <option name="CLEAR_APP_STORAGE" value="false" />
    <option name="ACTIVITY_EXTRA_FLAGS" value="" />
    <option name="MODE" value="default_activity" />
    <option name="CLEAR_LOGCAT" value="false" />
    <option name="SHOW_LOGCAT_AUTOMATICALLY" value="false" />
    <option name="TARGET_SELECTION_MODE" value="DEVICE_AND_SNAPSHOT_COMBO_BOX" />
    <option name="SELECTED_CLOUD_MATRIX_CONFIGURATION_ID" value="-1" />
    <option name="SELECTED_CLOUD_MATRIX_PROJECT_ID" value="" />
    <option name="DEBUGGER_TYPE" value="Auto" />
    <Auto>
      <option name="USE_JAVA_AWARE_DEBUGGER" value="false" />
      <option name="SHOW_STATIC_VARS" value="true" />
      <option name="WORKING_DIR" value="" />
      <option name="TARGET_LOGGING_CHANNELS" value="lldb process:gdb-remote packets" />
      <option name="SHOW_OPTIMIZED_WARNING" value="true" />
      <option name="ATTACH_ON_WAIT_FOR_DEBUGGER" value="false" />
      <option name="DEBUG_SANDBOX_SDK" value="false" />
    </Auto>
    <Hybrid>
      <option name="USE_JAVA_AWARE_DEBUGGER" value="false" />
      <option name="SHOW_STATIC_VARS" value="true" />
      <option name="WORKING_DIR" value="" />
      <option name="TARGET_LOGGING_CHANNELS" value="lldb process:gdb-remote packets" />
      <option name="SHOW_OPTIMIZED_WARNING" value="true" />
      <option name="ATTACH_ON_WAIT_FOR_DEBUGGER" value="false" />
      <option name="DEBUG_SANDBOX_SDK" value="false" />
    </Hybrid>
    <Java>
      <option name="ATTACH_ON_WAIT_FOR_DEBUGGER" value="false" />
      <option name="DEBUG_SANDBOX_SDK" value="false" />
    </Java>
    <Native>
      <option name="USE_JAVA_AWARE_DEBUGGER" value="false" />
      <option name="SHOW_STATIC_VARS" value="true" />
      <option name="WORKING_DIR" value="" />
      <option name="TARGET_LOGGING_CHANNELS" value="lldb process:gdb-remote packets" />
      <option name="SHOW_OPTIMIZED_WARNING" value="true" />
      <option name="ATTACH_ON_WAIT_FOR_DEBUGGER" value="false" />
      <option name="DEBUG_SANDBOX_SDK" value="false" />
    </Native>
    <Profilers>
      <option name="ADVANCED_PROFILING_ENABLED" value="false" />
      <option name="STARTUP_PROFILING_ENABLED" value="false" />
      <option name="STARTUP_CPU_PROFILING_ENABLED" value="false" />
      <option name="STARTUP_CPU_PROFILING_CONFIGURATION_NAME" value="Java/Kotlin Method Sample (legacy)" />
      <option name="STARTUP_NATIVE_MEMORY_PROFILING_ENABLED" value="false" />
      <option name="NATIVE_MEMORY_SAMPLE_RATE_BYTES" value="2048" />
    </Profilers>
    <option name="DEEP_LINK" value="" />
    <option name="ACTIVITY_CLASS" value="" />
    <option name="SEARCH_ACTIVITY_IN_GLOBAL_SCOPE" value="false" />
    <option name="SKIP_ACTIVITY_VALIDATION" value="false" />
    <method v="2">
      <option name="Android.Gradle.BeforeRunTask" enabled="true" />
    </method>
  </configuration>
</component>
""".trimIndent()
}
