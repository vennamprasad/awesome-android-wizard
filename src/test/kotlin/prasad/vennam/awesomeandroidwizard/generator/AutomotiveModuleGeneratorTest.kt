package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AutomotiveModuleGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testScaffoldAutomotiveModule() {
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

        val result = AutomotiveModuleGenerator.scaffoldAutomotiveModule(rootDir)

        assertTrue(result.success)
        assertTrue(result.automotiveDir.exists())

        // Check build.gradle.kts
        val buildFile = File(result.automotiveDir, "build.gradle.kts")
        assertTrue(buildFile.exists())
        val buildText = buildFile.readText()
        assertTrue(buildText.contains("namespace = \"com.example.awesome.automotive\""))
        assertTrue(buildText.contains("implementation(libs.androidx.car.app)"))

        // Check AndroidManifest.xml
        val manifestFile = File(result.automotiveDir, "src/main/AndroidManifest.xml")
        assertTrue(manifestFile.exists())
        val manifestText = manifestFile.readText()
        assertTrue(manifestText.contains("android.hardware.type.automotive"))
        assertTrue(manifestText.contains(".MainCarAppService"))

        // Check CarAppService
        val serviceFile = File(result.automotiveDir, "src/main/kotlin/com/example/awesome/automotive/MainCarAppService.kt")
        assertTrue(serviceFile.exists())
        assertTrue(serviceFile.readText().contains("class MainCarAppService : CarAppService()"))

        // Check Session
        val sessionFile = File(result.automotiveDir, "src/main/kotlin/com/example/awesome/automotive/MainCarSession.kt")
        assertTrue(sessionFile.exists())
        assertTrue(sessionFile.readText().contains("class MainCarSession : Session()"))

        // Check Screen
        val screenFile = File(result.automotiveDir, "src/main/kotlin/com/example/awesome/automotive/MainCarScreen.kt")
        assertTrue(screenFile.exists())
        assertTrue(screenFile.readText().contains("class MainCarScreen"))

        // Check automotive_app_desc.xml
        val descFile = File(result.automotiveDir, "src/main/res/xml/automotive_app_desc.xml")
        assertTrue(descFile.exists())

        // Check settings.gradle.kts update
        val updatedSettings = settingsFile.readText()
        assertTrue(updatedSettings.contains("include(\":automotive\")"))

        // Duplicate test
        val duplicateResult = AutomotiveModuleGenerator.scaffoldAutomotiveModule(rootDir)
        assertFalse(duplicateResult.success)
    }
}
