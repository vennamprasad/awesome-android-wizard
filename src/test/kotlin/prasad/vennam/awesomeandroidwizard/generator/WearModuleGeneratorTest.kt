package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class WearModuleGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testScaffoldWearModule() {
        val rootDir = tempFolder.newFolder("MyAndroidApp")

        val settingsFile = File(rootDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "MyAndroidApp"
            include(":app")
        """.trimIndent())

        val appDir = File(rootDir, "app")
        appDir.mkdirs()
        File(appDir, "build.gradle.kts").writeText("""
            android {
                namespace = "com.example.awesome"
            }
        """.trimIndent())

        val result = WearModuleGenerator.scaffoldWearModule(rootDir)

        assertTrue(result.success)
        assertTrue(result.wearDir.exists())

        // Check build.gradle.kts
        val buildFile = File(result.wearDir, "build.gradle.kts")
        assertTrue(buildFile.exists())
        val buildText = buildFile.readText()
        assertTrue(buildText.contains("namespace = \"com.example.awesome.wear\""))
        assertTrue(buildText.contains("implementation(libs.androidx.wear.compose.material3)"))

        // Check AndroidManifest.xml
        val manifestFile = File(result.wearDir, "src/main/AndroidManifest.xml")
        assertTrue(manifestFile.exists())
        val manifestText = manifestFile.readText()
        assertTrue(manifestText.contains("android.hardware.type.watch"))
        assertTrue(manifestText.contains(".presentation.MainActivity"))
        assertTrue(manifestText.contains(".tile.MainTileService"))

        // Check MainActivity.kt
        val mainActivity = File(result.wearDir, "src/main/kotlin/com/example/awesome/wear/presentation/MainActivity.kt")
        assertTrue(mainActivity.exists())
        assertTrue(mainActivity.readText().contains("class MainActivity : ComponentActivity()"))

        // Check TileService
        val tileService = File(result.wearDir, "src/main/kotlin/com/example/awesome/wear/tile/MainTileService.kt")
        assertTrue(tileService.exists())
        assertTrue(tileService.readText().contains("class MainTileService : TileService()"))

        // Check settings.gradle.kts update
        val updatedSettings = settingsFile.readText()
        assertTrue(updatedSettings.contains("include(\":wear\")"))

        // Duplicate test
        val duplicateResult = WearModuleGenerator.scaffoldWearModule(rootDir)
        assertFalse(duplicateResult.success)
    }
}
