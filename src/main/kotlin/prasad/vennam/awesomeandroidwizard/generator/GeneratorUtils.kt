package prasad.vennam.awesomeandroidwizard.generator

import prasad.vennam.awesomeandroidwizard.model.ArchitectureAuditor
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import java.io.File

/**
 * Common result contract for all module and slice generators.
 */
interface GeneratorResult {
    val success: Boolean
    val message: String
}

/**
 * Reusable utility methods for Android project introspection, file generation,
 * and build script synchronization used across generators.
 */
object GeneratorUtils {

    /**
     * Creates a file with the given name and content inside directory,
     * ensuring parent directories exist.
     */
    fun createFile(directory: File, fileName: String, content: String): File {
        directory.mkdirs()
        val file = File(directory, fileName)
        file.writeText(content)
        return file
    }

    /**
     * Detects base Android package from `app/build.gradle.kts` (via namespace or applicationId)
     * or fallback to `app/src/main/AndroidManifest.xml`.
     */
    fun detectBasePackage(rootDir: File): String? {
        val appBuildFile = File(rootDir, "app/build.gradle.kts")
        if (appBuildFile.exists()) {
            val content = appBuildFile.readText()
            val namespaceRegex = """namespace\s*=\s*["']([^"']+)["']""".toRegex()
            val match = namespaceRegex.find(content)
            if (match != null) {
                return match.groupValues[1]
            }
            val appIdRegex = """applicationId\s*=\s*["']([^"']+)["']""".toRegex()
            val appMatch = appIdRegex.find(content)
            if (appMatch != null) {
                return appMatch.groupValues[1]
            }
        }

        // Fallback: check AndroidManifest.xml
        val manifestFile = File(rootDir, "app/src/main/AndroidManifest.xml")
        if (manifestFile.exists()) {
            val pkgRegex = """package\s*=\s*["']([^"']+)["']""".toRegex()
            val match = pkgRegex.find(manifestFile.readText())
            if (match != null) {
                return match.groupValues[1]
            }
        }

        return null
    }

    /**
     * Detects the dependency injection framework (Hilt vs Koin) configured in the project.
     */
    fun detectDiFramework(rootDir: File): DiFramework {
        val tomlFile = File(rootDir, "gradle/libs.versions.toml")
        if (tomlFile.exists()) {
            val tomlText = tomlFile.readText()
            if (tomlText.contains("hilt")) return DiFramework.HILT
            if (tomlText.contains("koin")) return DiFramework.KOIN
        }
        val appBuild = File(rootDir, "app/build.gradle.kts")
        if (appBuild.exists()) {
            val text = appBuild.readText()
            if (text.contains("hilt")) return DiFramework.HILT
            if (text.contains("koin")) return DiFramework.KOIN
        }
        return DiFramework.HILT
    }

    /**
     * Checks if the project uses Gradle convention plugins located in `build-logic`.
     */
    fun hasConventionPlugins(rootDir: File): Boolean {
        return File(rootDir, "build-logic").exists()
    }

    /**
     * Adds a module include statement to `settings.gradle.kts` if not already present.
     * Returns true if settings.gradle.kts was modified, false otherwise.
     */
    fun addModuleToSettingsGradle(rootDir: File, moduleGradlePath: String): Boolean {
        val settingsFile = File(rootDir, "settings.gradle.kts")
        if (!settingsFile.exists()) return false

        val normalized = if (moduleGradlePath.startsWith(":")) moduleGradlePath else ":$moduleGradlePath"
        val existingModules = ArchitectureAuditor.parseIncludedModules(settingsFile)
        if (existingModules.contains(normalized)) {
            return false
        }

        val settingsText = settingsFile.readText()
        val includeStatement = "include(\"$normalized\")"
        val updated = if (settingsText.endsWith("\n")) {
            "$settingsText$includeStatement\n"
        } else {
            "$settingsText\n$includeStatement\n"
        }
        settingsFile.writeText(updated)
        return true
    }

    /**
     * Injects a dependency statement into `app/build.gradle.kts` dependencies block if not already present.
     * Returns true if modified, false otherwise.
     */
    fun addDependencyToAppBuildGradle(rootDir: File, dependencyStatement: String): Boolean {
        val appBuildFile = File(rootDir, "app/build.gradle.kts")
        if (!appBuildFile.exists()) return false

        val appBuildText = appBuildFile.readText()
        if (appBuildText.contains(dependencyStatement.trim())) {
            return false
        }

        val updatedText = if (appBuildText.contains("dependencies {")) {
            appBuildText.replaceFirst("dependencies {", "dependencies {\n$dependencyStatement")
        } else {
            appBuildText + "\n\ndependencies {\n$dependencyStatement\n}\n"
        }
        appBuildFile.writeText(updatedText)
        return true
    }
}
