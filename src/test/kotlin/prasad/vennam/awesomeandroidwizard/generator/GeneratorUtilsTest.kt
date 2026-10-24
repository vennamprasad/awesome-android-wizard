package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import java.io.File

class GeneratorUtilsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testDetectBasePackageFromNamespace() {
        val root = tempFolder.newFolder("pkgNamespaceTest")
        val appDir = File(root, "app").apply { mkdirs() }
        File(appDir, "build.gradle.kts").writeText(
            """
            plugins {
                alias(libs.plugins.android.application)
            }
            android {
                namespace = "com.example.awesome"
                compileSdk = 35
            }
            """.trimIndent()
        )

        val detected = GeneratorUtils.detectBasePackage(root)
        assertEquals("com.example.awesome", detected)
    }

    @Test
    fun testDetectBasePackageFromApplicationId() {
        val root = tempFolder.newFolder("pkgAppIdTest")
        val appDir = File(root, "app").apply { mkdirs() }
        File(appDir, "build.gradle.kts").writeText(
            """
            android {
                defaultConfig {
                    applicationId = "com.sample.app"
                }
            }
            """.trimIndent()
        )

        val detected = GeneratorUtils.detectBasePackage(root)
        assertEquals("com.sample.app", detected)
    }

    @Test
    fun testDetectBasePackageFromManifestFallback() {
        val root = tempFolder.newFolder("pkgManifestTest")
        val mainDir = File(root, "app/src/main").apply { mkdirs() }
        File(mainDir, "AndroidManifest.xml").writeText(
            """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.fallback.manifest">
            </manifest>
            """.trimIndent()
        )

        val detected = GeneratorUtils.detectBasePackage(root)
        assertEquals("com.fallback.manifest", detected)
    }

    @Test
    fun testDetectDiFramework() {
        val rootKoin = tempFolder.newFolder("diKoinTest")
        val gradleDir = File(rootKoin, "gradle").apply { mkdirs() }
        File(gradleDir, "libs.versions.toml").writeText(
            """
            [versions]
            koin = "4.0.0"
            """.trimIndent()
        )
        assertEquals(DiFramework.KOIN, GeneratorUtils.detectDiFramework(rootKoin))

        val rootHilt = tempFolder.newFolder("diHiltTest")
        val gradleDirHilt = File(rootHilt, "gradle").apply { mkdirs() }
        File(gradleDirHilt, "libs.versions.toml").writeText(
            """
            [versions]
            hilt = "2.60.1"
            """.trimIndent()
        )
        assertEquals(DiFramework.HILT, GeneratorUtils.detectDiFramework(rootHilt))
    }

    @Test
    fun testHasConventionPlugins() {
        val root = tempFolder.newFolder("conventionTest")
        assertFalse(GeneratorUtils.hasConventionPlugins(root))

        File(root, "build-logic").mkdirs()
        assertTrue(GeneratorUtils.hasConventionPlugins(root))
    }

    @Test
    fun testAddModuleToSettingsGradle() {
        val root = tempFolder.newFolder("settingsTest")
        val settings = File(root, "settings.gradle.kts")
        settings.writeText(
            """
            rootProject.name = "TestApp"
            include(":app")
            """.trimIndent()
        )

        // First addition should succeed
        val modified = GeneratorUtils.addModuleToSettingsGradle(root, ":feature:profile")
        assertTrue(modified)
        assertTrue(settings.readText().contains("include(\":feature:profile\")"))

        // Duplicate addition should be ignored
        val duplicate = GeneratorUtils.addModuleToSettingsGradle(root, ":feature:profile")
        assertFalse(duplicate)

        // Without leading colon should also normalize
        val noColon = GeneratorUtils.addModuleToSettingsGradle(root, "core:network")
        assertTrue(noColon)
        assertTrue(settings.readText().contains("include(\":core:network\")"))
    }

    @Test
    fun testAddDependencyToAppBuildGradle() {
        val root = tempFolder.newFolder("depTest")
        val appDir = File(root, "app").apply { mkdirs() }
        val appBuild = File(appDir, "build.gradle.kts")
        appBuild.writeText(
            """
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
            """.trimIndent()
        )

        val dep = "    implementation(project(\":feature:feed\"))"
        val added = GeneratorUtils.addDependencyToAppBuildGradle(root, dep)
        assertTrue(added)
        assertTrue(appBuild.readText().contains("implementation(project(\":feature:feed\"))"))

        // Duplicate should not be added again
        val duplicate = GeneratorUtils.addDependencyToAppBuildGradle(root, dep)
        assertFalse(duplicate)
    }

    @Test
    fun testCreateFile() {
        val root = tempFolder.newFolder("createFileTest")
        val nested = File(root, "a/b/c")
        val file = GeneratorUtils.createFile(nested, "Sample.kt", "class Sample")

        assertTrue(file.exists())
        assertEquals("class Sample", file.readText())
    }
}
