package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BenchmarkModuleGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testScaffoldBenchmarkModule() {
        val rootDir = tempFolder.newFolder("MyAndroidApp")

        // Setup mock settings.gradle.kts and app/build.gradle.kts
        val settingsFile = File(rootDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "MyAndroidApp"
            include(":app")
        """.trimIndent())

        val appDir = File(rootDir, "app")
        appDir.mkdirs()
        val appBuild = File(appDir, "build.gradle.kts")
        appBuild.writeText("""
            plugins {
                alias(libs.plugins.android.application)
            }
            android {
                namespace = "com.example.awesome"
            }
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        """.trimIndent())

        val result = BenchmarkModuleGenerator.scaffoldBenchmarkModule(rootDir)

        assertTrue(result.success)
        assertTrue(result.benchmarkDir.exists())

        // Check benchmark build.gradle.kts
        val benchmarkBuild = File(result.benchmarkDir, "build.gradle.kts")
        assertTrue(benchmarkBuild.exists())
        val buildText = benchmarkBuild.readText()
        assertTrue(buildText.contains("namespace = \"com.example.awesome.benchmark\""))
        assertTrue(buildText.contains("targetProjectPath = \":app\""))
        assertTrue(buildText.contains("implementation(libs.androidx.benchmark.macro)"))

        // Check generated sources
        val baselineFile = File(result.benchmarkDir, "src/main/kotlin/com/example/awesome/benchmark/BaselineProfileGenerator.kt")
        assertTrue(baselineFile.exists())
        assertTrue(baselineFile.readText().contains("class BaselineProfileGenerator"))

        val startupFile = File(result.benchmarkDir, "src/main/kotlin/com/example/awesome/benchmark/StartupBenchmark.kt")
        assertTrue(startupFile.exists())
        assertTrue(startupFile.readText().contains("class StartupBenchmark"))

        // Check settings.gradle.kts update
        val updatedSettings = settingsFile.readText()
        assertTrue(updatedSettings.contains("include(\":benchmark\")"))

        // Check app/build.gradle.kts update
        val updatedAppBuild = appBuild.readText()
        assertTrue(updatedAppBuild.contains("alias(libs.plugins.androidx.baselineprofile)"))
        assertTrue(updatedAppBuild.contains("baselineProfile(project(\":benchmark\"))"))

        // Attempting to scaffold again should fail gracefully
        val duplicateResult = BenchmarkModuleGenerator.scaffoldBenchmarkModule(rootDir)
        assertFalse(duplicateResult.success)
        assertTrue(duplicateResult.message.contains("already exists"))
    }
}
