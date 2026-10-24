package prasad.vennam.awesomeandroidwizard.config

/**
 * Single source of truth for all build toolchain, SDK, and dependency versions
 * used across the Awesome Android Wizard plugin, generators, and project diagnostics.
 *
 * Upgrading a dependency or Gradle version across the entire plugin ecosystem only
 * requires changing the corresponding constant in this file.
 */
object AwesomeVersions {

    // =========================================================================
    // 1. Build Toolchain & Compilers
    // =========================================================================
    const val GRADLE_WRAPPER: String = "9.8.1"
    const val AGP: String = "9.4.1"
    const val KOTLIN: String = "2.4.21"
    const val KSP: String = "2.3.12"
    const val DETEKT: String = "1.23.8"

    // =========================================================================
    // 2. Android SDK Boundaries
    // =========================================================================
    const val COMPILE_SDK: Int = 37
    const val TARGET_SDK: Int = 36
    const val MIN_SDK: Int = 26
    const val WEAR_MIN_SDK: Int = 30
    const val AUTOMOTIVE_MIN_SDK: Int = 26

    // =========================================================================
    // 3. AndroidX & Jetpack Core
    // =========================================================================
    const val CORE_KTX: String = "1.19.1"
    const val LIFECYCLE_RUNTIME_KTX: String = "2.11.0"
    const val ACTIVITY_COMPOSE: String = "1.13.0"
    const val DATASTORE: String = "1.2.1"

    // =========================================================================
    // 4. Jetpack Compose & Navigation
    // =========================================================================
    const val COMPOSE_BOM: String = "2026.09.00"
    const val NAVIGATION_COMPOSE: String = "2.10.2"
    const val NAVIGATION_3: String = "1.0.0-alpha01"

    // =========================================================================
    // 5. Coroutines & Serialization
    // =========================================================================
    const val COROUTINES: String = "1.11.0"
    const val SERIALIZATION: String = "1.11.0"

    // =========================================================================
    // 6. Dependency Injection
    // =========================================================================
    const val KOIN: String = "4.0.0"
    const val HILT: String = "2.60.1"
    const val HILT_NAVIGATION_COMPOSE: String = "1.4.0"

    // =========================================================================
    // 7. Networking & HTTP
    // =========================================================================
    const val KTOR: String = "3.0.0"
    const val RETROFIT: String = "3.0.0"
    const val OKHTTP: String = "5.5.0"

    // =========================================================================
    // 8. Offline Persistence & Database
    // =========================================================================
    const val ROOM: String = "2.8.5"

    // =========================================================================
    // 9. Unit & UI Testing
    // =========================================================================
    const val JUNIT: String = "4.13.2"
    const val JUNIT_VERSION: String = "1.3.0"
    const val ESPRESSO_CORE: String = "3.7.0"
    const val TURBINE: String = "1.2.1"
    const val MOCKK: String = "1.14.11"

    // =========================================================================
    // 10. Macrobenchmark & Baseline Profiles
    // =========================================================================
    const val ANDROIDX_BENCHMARK: String = "1.3.3"
    const val ANDROIDX_UIAUTOMATOR: String = "2.3.0"
    const val BASELINE_PROFILE: String = "1.3.3"

    // =========================================================================
    // 11. Multiplatform Companions (Wear OS & Automotive)
    // =========================================================================
    const val WEAR_COMPOSE: String = "1.4.1"
    const val WEAR_TILES: String = "1.4.1"
    const val GUAVA: String = "33.4.0-android"
    const val PLAY_SERVICES_WEARABLE: String = "19.0.0"
    const val CAR_APP: String = "1.7.0"

    // =========================================================================
    // Utility & URL Helpers
    // =========================================================================

    /**
     * Produces the official distribution URL for Gradle Wrapper properties.
     */
    fun gradleWrapperDistributionUrl(): String =
        "https\\://services.gradle.org/distributions/gradle-$GRADLE_WRAPPER-bin.zip"

    /**
     * Map of Version Catalog version keys to current recommended versions.
     */
    fun allManagedVersions(): Map<String, String> = mapOf(
        "agp" to AGP,
        "kotlin" to KOTLIN,
        "ksp" to KSP,
        "coreKtx" to CORE_KTX,
        "lifecycleRuntimeKtx" to LIFECYCLE_RUNTIME_KTX,
        "activityCompose" to ACTIVITY_COMPOSE,
        "composeBom" to COMPOSE_BOM,
        "navigationCompose" to NAVIGATION_COMPOSE,
        "navigation3" to NAVIGATION_3,
        "coroutines" to COROUTINES,
        "serialization" to SERIALIZATION,
        "dataStore" to DATASTORE,
        "koin" to KOIN,
        "hilt" to HILT,
        "hiltNavigationCompose" to HILT_NAVIGATION_COMPOSE,
        "ktor" to KTOR,
        "retrofit" to RETROFIT,
        "okhttp" to OKHTTP,
        "room" to ROOM,
        "junit" to JUNIT,
        "junitVersion" to JUNIT_VERSION,
        "espressoCore" to ESPRESSO_CORE,
        "turbine" to TURBINE,
        "mockk" to MOCKK,
        "androidxBenchmark" to ANDROIDX_BENCHMARK,
        "androidxUiautomator" to ANDROIDX_UIAUTOMATOR,
        "baselineprofile" to BASELINE_PROFILE,
        "wearCompose" to WEAR_COMPOSE,
        "wearTiles" to WEAR_TILES,
        "guava" to GUAVA,
        "playServicesWearable" to PLAY_SERVICES_WEARABLE,
        "carApp" to CAR_APP
    )

    data class VersionStatus(
        val key: String,
        val currentVersion: String?,
        val recommendedVersion: String,
        val isOutdated: Boolean
    )

    /**
     * Inspects a raw `libs.versions.toml` content and detects version differences.
     */
    fun auditTomlVersions(tomlContent: String): List<VersionStatus> {
        val managed = allManagedVersions()
        val currentMap = mutableMapOf<String, String>()

        var inVersionsSection = false
        val regex = Regex("""^([a-zA-Z0-9_\-]+)\s*=\s*["']([^"']+)["']""")

        tomlContent.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("[versions]")) {
                inVersionsSection = true
            } else if (trimmed.startsWith("[") && inVersionsSection) {
                inVersionsSection = false
            } else if (inVersionsSection) {
                val match = regex.find(trimmed)
                if (match != null) {
                    val key = match.groupValues[1]
                    val value = match.groupValues[2]
                    currentMap[key] = value
                }
            }
        }

        return managed.map { (key, recommended) ->
            val current = currentMap[key]
            val isOutdated = current != null && current != recommended
            VersionStatus(key, current, recommended, isOutdated)
        }
    }

    /**
     * Safely updates known versions in a `libs.versions.toml` content to the latest recommended versions,
     * preserving all formatting, custom entries, and comments.
     */
    fun upgradeTomlContent(tomlContent: String): String {
        val managed = allManagedVersions()
        var inVersionsSection = false
        val regex = Regex("""^(\s*)([a-zA-Z0-9_\-]+)(\s*=\s*)["']([^"']+)["'](.*)""")

        val resultLines = tomlContent.lines().map { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("[versions]")) {
                inVersionsSection = true
                line
            } else if (trimmed.startsWith("[") && inVersionsSection) {
                inVersionsSection = false
                line
            } else if (inVersionsSection) {
                val match = regex.matchEntire(line)
                if (match != null) {
                    val indent = match.groupValues[1]
                    val key = match.groupValues[2]
                    val equals = match.groupValues[3]
                    val trailing = match.groupValues[5]
                    val recommended = managed[key]
                    if (recommended != null) {
                        """$indent$key$equals"$recommended"$trailing"""
                    } else {
                        line
                    }
                } else {
                    line
                }
            } else {
                line
            }
        }

        return resultLines.joinToString("\n")
    }
}
