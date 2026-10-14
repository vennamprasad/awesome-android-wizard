package prasad.vennam.awesomeandroidwizard.model

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

enum class TargetFormFactor(val displayName: String, val description: String) {
    MOBILE("Mobile (Phone)", "Edge-to-edge touch UI with bottom navigation"),
    TABLET_FOLDABLE("Tablet & Foldables", "Dual-pane & Navigation Rail large-screen layouts"),
    TV("Android TV", "D-pad navigation, Leanback manifest flags, and TV focus styles"),
    UNIVERSAL_ADAPTIVE("Adaptive (Universal)", "Responsive layout dynamically adapting to Phone, Tablet & Foldable")
}

enum class ColorPreset(
    val displayName: String,
    val description: String,
    val primaryLightHex: String,
    val onPrimaryLightHex: String,
    val primaryContainerLightHex: String,
    val onPrimaryContainerLightHex: String,
    val secondaryLightHex: String,
    val primaryDarkHex: String,
    val onPrimaryDarkHex: String,
    val primaryContainerDarkHex: String,
    val onPrimaryContainerDarkHex: String,
    val secondaryDarkHex: String
) {
    DYNAMIC(
        "Dynamic Material You",
        "Uses user wallpaper palette on Android 12+ (falls back to Purple)",
        "0xFF6750A4", "0xFFFFFFFF", "0xFFEADDFF", "0xFF21005D", "0xFF625B71",
        "0xFFD0BCFF", "0xFF381E72", "0xFF4F378B", "0xFFEADDFF", "0xFFCCC2DC"
    ),
    OCEAN_BLUE(
        "Ocean Blue",
        "Calm and professional corporate blue tones",
        "0xFF0061A4", "0xFFFFFFFF", "0xFFD1E4FF", "0xFF001D36", "0xFF535F70",
        "0xFF9ECAFF", "0xFF003258", "0xFF00497D", "0xFFD1E4FF", "0xFFBBC7DB"
    ),
    EMERALD_GREEN(
        "Emerald Green",
        "Fresh, vibrant, eco and fintech-oriented green",
        "0xFF006D44", "0xFFFFFFFF", "0xFF90F7BE", "0xFF002111", "0xFF4E6354",
        "0xFF73DAA3", "0xFF003920", "0xFF005232", "0xFF90F7BE", "0xFFB4CCB8"
    ),
    VIBRANT_PURPLE(
        "Vibrant Purple",
        "Default Material 3 expressive purple aesthetic",
        "0xFF6750A4", "0xFFFFFFFF", "0xFFEADDFF", "0xFF21005D", "0xFF625B71",
        "0xFFD0BCFF", "0xFF381E72", "0xFF4F378B", "0xFFEADDFF", "0xFFCCC2DC"
    ),
    SUNSET_ORANGE(
        "Sunset Orange",
        "Warm, energetic social and consumer brand palette",
        "0xFF984715", "0xFFFFFFFF", "0xFFFFDBCF", "0xFF360F00", "0xFF77574A",
        "0xFFFFB598", "0xFF591D00", "0xFF793000", "0xFFFFDBCF", "0xFFE6BEAD"
    ),
    MIDNIGHT_MONO(
        "Midnight Monochrome",
        "Sleek, minimalist dark-slate & titanium palette",
        "0xFF2B3137", "0xFFFFFFFF", "0xFFE1E4E8", "0xFF141618", "0xFF586069",
        "0xFFC9D1D9", "0xFF161B22", "0xFF30363D", "0xFFE1E4E8", "0xFF8B949E"
    )
}

enum class CornerStyle(
    val displayName: String,
    val description: String,
    val smallDp: Int,
    val mediumDp: Int,
    val largeDp: Int,
    val extraLargeDp: Int
) {
    ROUNDED("Rounded (Default Material 3)", "Standard balanced corners: 8dp, 12dp, 16dp, 28dp", 8, 12, 16, 28),
    SOFT_PILL("Soft & Pill (Playful)", "Friendly, extra rounded contours: 12dp, 18dp, 24dp, 36dp", 12, 18, 24, 36),
    SHARP("Sharp & Geometric (Technical)", "Crisp, square edges for modern tech apps: 2dp, 4dp, 6dp, 10dp", 2, 4, 6, 10)
}

data class WizardModel(
    var projectName: String = "MyAwesomeApp",
    var packageName: String = "com.example.myawesomeapp",
    var projectLocation: String = System.getProperty("user.home") + "/AndroidStudioProjects/MyAwesomeApp",
    var minSdk: Int = 26,
    var targetSdk: Int = 35,
    var compileSdk: Int = 37,
    var targetFormFactor: TargetFormFactor = TargetFormFactor.MOBILE,
    var colorPreset: ColorPreset = ColorPreset.DYNAMIC,
    var cornerStyle: CornerStyle = CornerStyle.ROUNDED,
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
            val clean = name.trim()
                .removePrefix("feature:")
                .removePrefix(":")
                .replace("[^a-zA-Z0-9_-]".toRegex(), "")
            if (clean.isEmpty()) return "Feature"
            val words = clean.split("(?<=[a-z])(?=[A-Z])|[-_]".toRegex()).filter { it.isNotEmpty() }
            return words.joinToString("") { part ->
                part.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }
}
