package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import prasad.vennam.awesomeandroidwizard.model.CoreModuleType
import java.io.File

class CoreModuleGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testScaffoldJvmCoreModule() {
        val root = tempFolder.newFolder("JvmCoreProject")
        val appDir = File(root, "app").apply { mkdirs() }
        File(appDir, "build.gradle.kts").writeText("android { namespace = \"com.sample.coreapp\" }")
        val settings = File(root, "settings.gradle.kts")
        settings.writeText("""include(":app")""")

        val result = CoreModuleGenerator.scaffoldCoreModule(
            projectRootDir = root,
            rawCoreName = "analytics",
            moduleType = CoreModuleType.JVM_LIBRARY
        )

        assertTrue(result.success)
        assertTrue(result.message.contains(":core:analytics"))

        val moduleDir = File(root, "core/analytics")
        val buildFile = File(moduleDir, "build.gradle.kts")
        assertTrue(buildFile.exists())
        assertTrue(buildFile.readText().contains("libs.plugins.kotlin.jvm"))

        val contractFile = File(moduleDir, "src/main/kotlin/com/sample/coreapp/core/analytics/AnalyticsContract.kt")
        assertTrue(contractFile.exists())
        assertTrue(contractFile.readText().contains("interface AnalyticsContract"))

        assertTrue(settings.readText().contains("include(\":core:analytics\")"))
    }

    @Test
    fun testScaffoldAndroidCoreModule() {
        val root = tempFolder.newFolder("AndroidCoreProject")
        val appDir = File(root, "app").apply { mkdirs() }
        File(appDir, "build.gradle.kts").writeText("android { namespace = \"com.sample.coreapp\" }")
        val settings = File(root, "settings.gradle.kts")
        settings.writeText("""include(":app")""")

        val result = CoreModuleGenerator.scaffoldCoreModule(
            projectRootDir = root,
            rawCoreName = "security",
            moduleType = CoreModuleType.ANDROID_LIBRARY
        )

        assertTrue(result.success)
        val buildFile = File(root, "core/security/build.gradle.kts")
        assertTrue(buildFile.exists())
        assertTrue(buildFile.readText().contains("namespace = \"com.sample.coreapp.core.security\""))
    }

    @Test
    fun testScaffoldComposeCoreModule() {
        val root = tempFolder.newFolder("ComposeCoreProject")
        val appDir = File(root, "app").apply { mkdirs() }
        File(appDir, "build.gradle.kts").writeText("android { namespace = \"com.sample.coreapp\" }")
        val settings = File(root, "settings.gradle.kts")
        settings.writeText("""include(":app")""")

        val result = CoreModuleGenerator.scaffoldCoreModule(
            projectRootDir = root,
            rawCoreName = "widget",
            moduleType = CoreModuleType.COMPOSE_LIBRARY
        )

        assertTrue(result.success)
        val buildFile = File(root, "core/widget/build.gradle.kts")
        assertTrue(buildFile.exists())
        assertTrue(buildFile.readText().contains("libs.androidx.compose.bom"))
    }

    @Test
    fun testScaffoldCoreModuleInvalidName() {
        val root = tempFolder.newFolder("InvalidCoreProject")
        val result = CoreModuleGenerator.scaffoldCoreModule(root, "   ")
        assertFalse(result.success)
        assertEquals("Invalid core module name.", result.message)
    }

    @Test
    fun testScaffoldCoreModuleAlreadyExists() {
        val root = tempFolder.newFolder("ExistingCoreProject")
        File(root, "core/network").mkdirs()

        val result = CoreModuleGenerator.scaffoldCoreModule(root, "network")
        assertFalse(result.success)
        assertTrue(result.message.contains("already exists"))
    }
}
