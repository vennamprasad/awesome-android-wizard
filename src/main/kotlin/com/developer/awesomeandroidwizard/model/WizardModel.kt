package com.developer.awesomeandroidwizard.model

enum class ArchitectureType(val displayName: String, val description: String) {
    MODULAR_CLEAN_MVI("Modular Clean Architecture (MVI)", "Decoupled layers (:core:*, :feature:*) with immutable UI State and Unidirectional Data Flow."),
    MODULAR_MVVM("Modular Feature-First (MVVM)", "Modular features with ViewModel and StateFlow data binding."),
    SINGLE_MODULE("Single Module (Lightweight)", "Standard monolithic :app structure with Jetpack Compose.")
}

enum class DiFramework(val displayName: String) {
    KOIN("Koin (Lightweight Kotlin DSL)"),
    HILT("Hilt / Dagger (Google Standard Annotation Processing)")
}

enum class NetworkingFramework(val displayName: String) {
    KTOR("Ktor Client (Modern, multiplatform-ready)"),
    RETROFIT("Retrofit 2 + OkHttp (Industry veteran)")
}

enum class CoreModuleType(val displayName: String, val pluginId: String, val description: String) {
    JVM_LIBRARY("Pure Kotlin JVM Library", "awesome.jvm.library", "Zero Android SDK/AGP overhead. Ideal for domain models, business logic, math engines."),
    ANDROID_LIBRARY("Android Library", "awesome.android.library", "Standard Android library with Android context and system APIs."),
    COMPOSE_LIBRARY("Compose UI Library", "awesome.android.library.compose", "Android library configured with Jetpack Compose & Compose Compiler.")
}

enum class DatabaseFramework(val displayName: String) {
    ROOM("AndroidX Room (SQLite ORM)"),
    NONE("None (In-memory / Network only)")
}

enum class ArchetypeBlueprint(val title: String, val subtitle: String) {
    STARTUP("🚀 Modern Consumer / Startup", "Compose M3, Koin, Ktor, Navigation Compose, Clean MVI, GitHub Actions"),
    FINTECH("🏦 Enterprise / Fintech", "Compose M3, Hilt, Retrofit 2, Room DB, Proguard, Strict Detekt, GitHub Actions"),
    MINIMAL("⚡ Minimal / Spike Prototype", "Single module, Compose M3, Koin, ultra-fast builds for POCs"),
    CUSTOM("🛠 Custom Configuration", "Fine-tune every library, module, and architecture option individually")
}

data class WizardModel(
    var projectName: String = "MyAwesomeApp",
    var packageName: String = "com.example.myawesomeapp",
    var projectLocation: String = System.getProperty("user.home") + "/AndroidStudioProjects/MyAwesomeApp",
    var minSdk: Int = 26,
    var targetSdk: Int = 35,
    var compileSdk: Int = 35,
    var archetype: ArchetypeBlueprint = ArchetypeBlueprint.STARTUP,
    var architecture: ArchitectureType = ArchitectureType.MODULAR_CLEAN_MVI,
    var diFramework: DiFramework = DiFramework.KOIN,
    var networking: NetworkingFramework = NetworkingFramework.KTOR,
    var database: DatabaseFramework = DatabaseFramework.ROOM,
    var useCompose: Boolean = true,
    var useVersionCatalog: Boolean = true,
    var useConventionPlugins: Boolean = true,
    var generateCiWorkflow: Boolean = true,
    var generateDetekt: Boolean = true,
    var generateDependabot: Boolean = true,
    var selectedCoreModules: MutableSet<String> = mutableSetOf(
        "core:model",
        "core:data",
        "core:network",
        "core:database",
        "core:ui",
        "core:designsystem",
        "core:testing"
    ),
    var featureModules: MutableSet<String> = mutableSetOf(
        "home"
    )
) {
    fun getFormattedPackagePath(): String {
        return packageName.replace(".", "/")
    }

    fun applyArchetype(blueprint: ArchetypeBlueprint) {
        archetype = blueprint
        when (blueprint) {
            ArchetypeBlueprint.STARTUP -> {
                architecture = ArchitectureType.MODULAR_CLEAN_MVI
                diFramework = DiFramework.KOIN
                networking = NetworkingFramework.KTOR
                database = DatabaseFramework.ROOM
                useCompose = true
                useConventionPlugins = true
                generateCiWorkflow = true
                generateDetekt = true
                generateDependabot = true
                selectedCoreModules = mutableSetOf("core:model", "core:data", "core:network", "core:database", "core:ui", "core:designsystem", "core:testing")
                if (featureModules.isEmpty()) featureModules.add("home")
            }
            ArchetypeBlueprint.FINTECH -> {
                architecture = ArchitectureType.MODULAR_CLEAN_MVI
                diFramework = DiFramework.HILT
                networking = NetworkingFramework.RETROFIT
                database = DatabaseFramework.ROOM
                useCompose = true
                useConventionPlugins = true
                generateCiWorkflow = true
                generateDetekt = true
                generateDependabot = true
                selectedCoreModules = mutableSetOf("core:model", "core:data", "core:network", "core:database", "core:ui", "core:designsystem", "core:testing")
                if (featureModules.isEmpty()) {
                    featureModules.addAll(listOf("auth", "dashboard"))
                }
            }
            ArchetypeBlueprint.MINIMAL -> {
                architecture = ArchitectureType.SINGLE_MODULE
                diFramework = DiFramework.KOIN
                networking = NetworkingFramework.KTOR
                database = DatabaseFramework.NONE
                useCompose = true
                useConventionPlugins = false
                generateCiWorkflow = true
                generateDetekt = false
                generateDependabot = false
                selectedCoreModules.clear()
                featureModules.clear()
            }
            ArchetypeBlueprint.CUSTOM -> {
                // Keep manual config
            }
        }
    }

    fun addFeature(rawName: String): Boolean {
        val sanitized = sanitizeFeatureName(rawName)
        if (sanitized.isNotBlank()) {
            return featureModules.add(sanitized)
        }
        return false
    }

    fun removeFeature(rawName: String) {
        val sanitized = sanitizeFeatureName(rawName)
        featureModules.remove(sanitized)
    }

    fun getAllSelectedModules(): List<String> {
        val list = mutableListOf<String>()
        list.addAll(selectedCoreModules)
        list.addAll(featureModules.map { "feature:$it" })
        return list
    }

    companion object {
        fun sanitizeFeatureName(raw: String): String {
            return raw.trim()
                .removePrefix("feature:")
                .removePrefix(":")
                .lowercase()
                .replace("[^a-z0-9_-]".toRegex(), "")
        }

        fun toPascalCase(name: String): String {
            val sanitized = sanitizeFeatureName(name)
            if (sanitized.isEmpty()) return "Feature"
            return sanitized.split("-", "_")
                .filter { it.isNotEmpty() }
                .joinToString("") { part ->
                    part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
        }
    }
}
