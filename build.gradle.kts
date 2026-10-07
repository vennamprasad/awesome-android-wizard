plugins {
    id("java")
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.developer.awesomeandroidwizard"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2024.1.4")
        bundledPlugin("com.intellij.java")
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
        id = "com.developer.awesomeandroidwizard"
        name = "Awesome Android Wizard"
        version = "1.0.0"
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
    }

    publishing {
        token = providers.environmentVariable("JETBRAINS_MARKETPLACE_TOKEN")
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}
