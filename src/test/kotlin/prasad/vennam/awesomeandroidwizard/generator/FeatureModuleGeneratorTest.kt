package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import java.io.File

class FeatureModuleGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testScaffoldFeatureSuccess() {
        val root = tempFolder.newFolder("FeatureProject")
        val appDir = File(root, "app").apply { mkdirs() }
        File(appDir, "build.gradle.kts").writeText(
            """
            android {
                namespace = "com.sample.featureapp"
            }
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
            """.trimIndent()
        )
        val settings = File(root, "settings.gradle.kts")
        settings.writeText("""include(":app")""")

        val result = FeatureModuleGenerator.scaffoldFeature(
            projectRootDir = root,
            rawFeatureName = "notifications",
            diFramework = DiFramework.KOIN
        )

        assertTrue(result.success)
        assertTrue(result.message.contains(":feature:notifications"))

        val featureDir = File(root, "feature/notifications")
        assertTrue(File(featureDir, "build.gradle.kts").exists())

        val srcDir = File(featureDir, "src/main/kotlin/com/sample/featureapp/feature/notifications")
        assertTrue(File(srcDir, "NotificationsContract.kt").exists())
        assertTrue(File(srcDir, "NotificationsScreen.kt").exists())
        assertTrue(File(srcDir, "NotificationsViewModel.kt").exists())
        assertTrue(File(srcDir, "NotificationsNavigation.kt").exists())

        // Verify settings.gradle.kts include
        assertTrue(settings.readText().contains("include(\":feature:notifications\")"))

        // Verify app/build.gradle.kts dependency wiring
        assertTrue(File(appDir, "build.gradle.kts").readText().contains("implementation(project(\":feature:notifications\"))"))
    }

    @Test
    fun testScaffoldFeatureInvalidName() {
        val root = tempFolder.newFolder("InvalidFeatureProject")
        val result = FeatureModuleGenerator.scaffoldFeature(root, "  !@#$  ")
        assertFalse(result.success)
        assertEquals("Invalid feature name.", result.message)
    }

    @Test
    fun testScaffoldFeatureAlreadyExists() {
        val root = tempFolder.newFolder("ExistingFeatureProject")
        val appDir = File(root, "app").apply { mkdirs() }
        File(appDir, "build.gradle.kts").writeText("android { namespace = \"com.test\" }")
        File(root, "feature/dashboard").mkdirs()

        val result = FeatureModuleGenerator.scaffoldFeature(root, "dashboard")
        assertFalse(result.success)
        assertTrue(result.message.contains("already exists"))
    }
}
