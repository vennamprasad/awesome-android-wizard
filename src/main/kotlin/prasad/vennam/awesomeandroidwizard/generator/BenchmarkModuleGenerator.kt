package prasad.vennam.awesomeandroidwizard.generator

import java.io.File

object BenchmarkModuleGenerator {

    data class BenchmarkGenerationResult(
        val success: Boolean,
        val benchmarkDir: File,
        val message: String
    )

    fun scaffoldBenchmarkModule(projectRootDir: File, customPackage: String? = null): BenchmarkGenerationResult {
        val benchmarkDir = File(projectRootDir, "benchmark")
        if (benchmarkDir.exists()) {
            return BenchmarkGenerationResult(false, benchmarkDir, "Module ':benchmark' already exists in this project.")
        }

        val basePackage = customPackage?.takeIf { it.isNotBlank() } ?: detectBasePackage(projectRootDir) ?: "com.example.app"
        val pkgPath = basePackage.replace(".", "/")

        // 1. Create module directory
        benchmarkDir.mkdirs()

        // 2. Generate benchmark/build.gradle.kts
        val buildFile = File(benchmarkDir, "build.gradle.kts")
        val buildContent = """
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "$basePackage.benchmark"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro)
}
""".trimIndent()
        buildFile.writeText(buildContent)

        // 3. AndroidManifest.xml
        val manifestDir = File(benchmarkDir, "src/main")
        manifestDir.mkdirs()
        File(manifestDir, "AndroidManifest.xml").writeText("""
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
""".trimIndent())

        // 4. Source directory
        val srcDir = File(manifestDir, "kotlin/$pkgPath/benchmark")
        srcDir.mkdirs()

        // 5. BaselineProfileGenerator.kt
        val baselineContent = """
package $basePackage.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates an optimized Baseline Profile for the application.
 * Run via: `./gradlew :benchmark:generateBaselineProfile`
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        baselineRule.collect(
            packageName = "$basePackage",
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}
""".trimIndent()
        File(srcDir, "BaselineProfileGenerator.kt").writeText(baselineContent)

        // 6. StartupBenchmark.kt
        val startupContent = """
package $basePackage.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark measuring cold startup latency before and after Baseline Profile optimization.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupCompilationNone() = startup(CompilationMode.None())

    @Test
    fun startupCompilationBaselineProfiles() = startup(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)
    )

    private fun startup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = "$basePackage",
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            iterations = 5,
            startupMode = StartupMode.COLD
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}
""".trimIndent()
        File(srcDir, "StartupBenchmark.kt").writeText(startupContent)

        // 7. Update settings.gradle.kts
        val settingsFile = File(projectRootDir, "settings.gradle.kts")
        if (settingsFile.exists()) {
            val settingsText = settingsFile.readText()
            val includeStatement = "include(\":benchmark\")"
            if (!settingsText.contains(includeStatement)) {
                settingsFile.appendText("\n$includeStatement\n")
            }
        }

        // 8. Update app/build.gradle.kts
        val appBuild = File(projectRootDir, "app/build.gradle.kts")
        if (appBuild.exists()) {
            var text = appBuild.readText()
            val pluginLine = "    alias(libs.plugins.androidx.baselineprofile)"
            val depLine = "    baselineProfile(project(\":benchmark\"))"

            if (!text.contains("alias(libs.plugins.androidx.baselineprofile)")) {
                text = text.replaceFirst("plugins {", "plugins {\n$pluginLine")
            }
            if (!text.contains("baselineProfile(project(\":benchmark\"))")) {
                text = text.replaceFirst("dependencies {", "dependencies {\n$depLine")
            }
            appBuild.writeText(text)
        }

        return BenchmarkGenerationResult(
            success = true,
            benchmarkDir = benchmarkDir,
            message = "Baseline Profile & Macrobenchmark module ':benchmark' successfully created!"
        )
    }

    private fun detectBasePackage(rootDir: File): String? {
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
        return null
    }
}
