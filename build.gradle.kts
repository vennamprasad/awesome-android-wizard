plugins {
    id("java")
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "prasad.vennam.awesomeandroidwizard"
version = "1.0.7"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2024.1.4")
        pluginVerifier()
        zipSigner()
    }

    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    pluginConfiguration {
        id = "prasad.vennam.awesomeandroidwizard"
        name = "Awesome Android Wizard"
        version = "1.0.7"
        vendor {
            name = "Prasad Vennam"
            email = "vennamprasad@gmail.com"
            url = "https://github.com/vennamprasad/awesome-android-wizard"
        }
        description = """
            <h3>Awesome Android Project Wizard</h3>
            <p>An interactive, enterprise-grade project and module scaffolding platform for <b>Android Studio</b> and <b>IntelliJ IDEA</b>.</p>
            <p>Scaffolds production-ready, highly decoupled Android architectures in seconds with zero boilerplate.</p>
            <h4>Key Features:</h4>
            <ul>
                <li><b>Production Blueprints</b>: Instant archetypes for Startup/Consumer apps, Fintech/Enterprise, and Spike prototypes.</li>
                <li><b>Day-2 Feature Scaffolder</b>: Right-click <code>New ➔ Awesome Feature Module...</code> to instantly generate feature slices with Compose UI & ViewModel.</li>
                <li><b>Day-2 Core Scaffolder</b>: Right-click <code>New ➔ Awesome Core Module...</code> to add pure JVM, Android, or Compose libraries.</li>
                <li><b>Offline-First Room Persistence</b>: Pre-configured <code>:core:database</code> with Room, KSP, reactive Flow DAOs, and Entity models.</li>
                <li><b>Shared Test Fixtures</b>: <code>:core:testing</code> with <code>MainDispatcherRule</code> and fake repositories.</li>
                <li><b>Design System Ecosystem</b>: <code>:core:designsystem</code> with 4dp/8dp spacing tokens, corner radius, type scale, icons, and DataStore theme persistence.</li>
                <li><b>Gradle Convention Plugins</b>: Modern <code>build-logic</code> setup sharing compiler flags, SDK bounds, and packaging rules.</li>
                <li><b>Project Doctor & Architecture Guard</b>: Integrated health diagnostics, Clean Architecture boundary auditing, and Git pre-commit hook enforcement.</li>
            </ul>
        """.trimIndent()
        changeNotes = """
            <h3>1.0.7 - AGP 9.0+ Built-in Kotlin Support & Submodule SDK Alignment</h3>
            <ul>
                <li>Migrated all generated Android application and library module templates to AGP 9 built-in Kotlin support, removing legacy <code>alias(libs.plugins.kotlin.android)</code> collisions.</li>
                <li>Added explicit <code>compileSdk</code> and <code>minSdk</code> configuration to <code>:core:data</code> and <code>:core:network</code> module templates.</li>
                <li>Registered <code>kotlin.jvm</code>, <code>hilt</code>, and <code>ksp</code> plugins in root <code>build.gradle.kts</code> for clean subproject resolution.</li>
            </ul>
            <h3>1.0.6 - Device Form Factor (Mobile/Tablet/TV/Adaptive) & Theme Customization</h3>
            <ul>
                <li>Added interactive 'Device & Theme Setup' wizard step.</li>
                <li>Support for target form factors: Mobile (Phone), Tablet & Foldables (Navigation Rail), Android TV (D-pad Leanback & TabRow), and Universal Adaptive (BoxWithConstraints responsive layouts).</li>
                <li>Customizable brand color palettes: Dynamic Material You, Ocean Blue, Emerald Green, Vibrant Purple, Sunset Orange, and Midnight Monochrome.</li>
                <li>Configurable UI shape tokens: Rounded (Standard M3), Soft & Pill (Playful), and Sharp & Geometric (Technical).</li>
            </ul>
            <h3>1.0.5 - Android SDK 37 (Android 17) & Modern AndroidX AAR Compatibility</h3>
            <ul>
                <li>Updated default compileSdk to 37 to support latest AndroidX libraries (Compose BOM 2026.09, Navigation 2.10, Core-KTX 1.19, OkHttp 5.5, Activity 1.13, Hilt Navigation 1.4).</li>
                <li>Resolved AAR metadata compileSdk 37 minimum version check failures across all generated modules and convention plugins.</li>
                <li>Expanded Target SDK dropdown options to include Android 16 (36) and Android 17 (37).</li>
            </ul>
            <h3>1.0.4 - Gradle 9 & AGP 9.4 Compatibility & Modern Build Logic</h3>
            <ul>
                <li>Upgraded Gradle wrapper to 9.8.1 and AGP to 9.4.1 for out-of-the-box build reliability.</li>
                <li>Modernized build-logic convention plugins using ApplicationExtension/LibraryExtension and Kotlin 2.x compilerOptions.</li>
                <li>Eliminated redundant kotlin.android plugin declarations and resolved extension type conflicts.</li>
                <li>Updated Version Catalog with latest dependencies: Kotlin 2.4.21, Compose BOM 2026.09, Hilt 2.60, KSP 2.3, Retrofit 3, Room 2.8.</li>
                <li>Optimized gradle.properties with 8GB heap, configuration caching, and parallel sync tooling.</li>
            </ul>
            <h3>1.0.3 - Deprecated API Modernization</h3>
            <ul>
                <li>Replaced deprecated addBrowseFolderListener with modern TextBrowseFolderListener.</li>
                <li>Clean, 100% verified compatibility across IntelliJ Platform 2024.1 through 2026.3+ with 0 API removal warnings.</li>
            </ul>
            <h3>1.0.2 - Universal Compatibility & Android Studio Native Support</h3>
            <ul>
                <li>Removed strict Java module requirement, ensuring native compatibility with Android Studio (Koala, Ladybug, Meerkat, Rabbit 1 2026.2+) and IntelliJ IDEA (2024.1 - 2026.x+).</li>
                <li>Updated namespace and action bindings to prasad.vennam.awesomeandroidwizard.</li>
                <li>Zero upper-bound limit for forward compatibility across all future IDE releases.</li>
            </ul>
            <h3>1.0.1 - Compatibility Update</h3>
            <ul>
                <li>Expanded IDE compatibility across all modern IDE releases: IntelliJ IDEA (2024.1+, 2024.2+, 2024.3+, 2025.x, 2026.x) and Android Studio (Koala, Ladybug, Meerkat, Rabbit).</li>
                <li>Removed until-build barrier for forward compatibility.</li>
                <li>Added custom vector icon and design system refinements.</li>
            </ul>
            <h3>1.0.0 - Initial Release</h3>
            <ul>
                <li>Initial release of Awesome Android Wizard on JetBrains Marketplace.</li>
                <li>Multi-step interactive wizard supporting Clean Architecture MVI & MVVM.</li>
                <li>Choice of Dependency Injection (Hilt or Koin) and Networking (Ktor or Retrofit).</li>
                <li>Day-2 Feature and Core module scaffolders with automatic Gradle wiring.</li>
                <li>Integrated Awesome Project Doctor for diagnostics and architectural boundary enforcement.</li>
                <li>Pre-configured Git pre-commit hooks and GitHub Actions CI workflow.</li>
            </ul>
        """.trimIndent()
        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
        }
    }

    publishing {
        token = providers.environmentVariable("JETBRAINS_MARKETPLACE_TOKEN")
            .orElse(providers.gradleProperty("publishToken"))
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}
