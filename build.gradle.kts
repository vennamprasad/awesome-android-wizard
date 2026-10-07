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
            name = "Awesome Dev Team"
            url = "https://github.com/developer/awesome-android-wizard"
        }
        description = """
            <h3>Awesome Android Project Wizard</h3>
            <p>An interactive, multi-step project scaffolding wizard for Android Studio.</p>
            <ul>
                <li>Modern Multi-Module Clean Architecture</li>
                <li>Gradle Version Catalogs (libs.versions.toml)</li>
                <li>Gradle Convention Plugins (build-logic)</li>
                <li>Jetpack Compose + Material 3</li>
                <li>Choice of Dependency Injection (Hilt or Koin)</li>
                <li>Choice of Networking (Ktor or Retrofit)</li>
                <li>Pre-configured Unit &amp; UI Testing</li>
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
