package prasad.vennam.awesomeandroidwizard.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import prasad.vennam.awesomeandroidwizard.config.AwesomeVersions
import java.io.File

class ProjectDoctorDialogTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testPerformDiagnosticsWithNullBasePath() {
        val checks = ProjectDoctorDialog.performDiagnostics(null)
        assertEquals(1, checks.size)
        assertEquals("Project State", checks[0].title)
        assertFalse(checks[0].passed)
    }

    @Test
    fun testPerformDiagnosticsWithFullyConfiguredProject() {
        val rootDir = tempFolder.newFolder("HealthyApp")

        // 1. Android SDK
        File(rootDir, "local.properties").writeText("sdk.dir=/Users/test/Library/Android/sdk")

        // 2. Gradle wrapper
        val wrapperDir = File(rootDir, "gradle/wrapper").apply { mkdirs() }
        File(wrapperDir, "gradle-wrapper.properties").writeText(
            "distributionUrl=https\\://services.gradle.org/distributions/gradle-${AwesomeVersions.GRADLE_WRAPPER}-bin.zip"
        )
        File(rootDir, "gradlew").apply {
            writeText("#!/bin/sh")
            setExecutable(true)
        }

        // 3. Version catalog
        val gradleDir = File(rootDir, "gradle").apply { mkdirs() }
        File(gradleDir, "libs.versions.toml").writeText("""
            [versions]
            agp = "${AwesomeVersions.AGP}"
        """.trimIndent())

        // 4. Build logic convention
        File(rootDir, "build-logic/convention").mkdirs()

        // 5. core:model
        val modelDir = File(rootDir, "core/model").apply { mkdirs() }
        File(modelDir, "build.gradle.kts").writeText("""
            plugins {
                alias(libs.plugins.awesome.jvm.library)
            }
        """.trimIndent())

        // 6. core:designsystem
        val designDir = File(rootDir, "core/designsystem/src/main/res/drawable").apply { mkdirs() }
        File(designDir, "ic_launcher_foreground.xml").writeText("<vector/>")

        // 7. CI and code quality
        File(rootDir, ".github/workflows").mkdirs()
        File(rootDir, ".editorconfig").writeText("root = true")

        // 8. Architecture clean
        File(rootDir, "settings.gradle.kts").writeText("""
            include(":app")
            include(":core:model")
            include(":feature:profile")
        """.trimIndent())
        File(rootDir, "app").mkdirs()
        File(rootDir, "app/build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":feature:profile"))
            }
        """.trimIndent())
        File(rootDir, "feature/profile").mkdirs()
        File(rootDir, "feature/profile/build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":core:model"))
            }
        """.trimIndent())

        // 9. Git pre-commit hooks
        val hookDir = File(rootDir, ".githooks").apply { mkdirs() }
        File(hookDir, "pre-commit").writeText("#!/bin/sh\n./gradlew detekt")

        val checks = ProjectDoctorDialog.performDiagnostics(rootDir.absolutePath)
        assertEquals(9, checks.size)

        for (c in checks) {
            assertTrue("Expected check '${c.title}' to pass, details: ${c.details}", c.passed)
        }
    }

    @Test
    fun testPerformDiagnosticsDetectsArchitectureViolation() {
        val rootDir = tempFolder.newFolder("ViolatingApp")

        File(rootDir, "local.properties").writeText("sdk.dir=/Users/test/Library/Android/sdk")
        File(rootDir, "gradle/libs.versions.toml").apply {
            parentFile.mkdirs()
            writeText("[versions]\n")
        }

        // Feature directly leaking :core:network
        File(rootDir, "settings.gradle.kts").writeText("""
            include(":app")
            include(":feature:feed")
            include(":core:network")
        """.trimIndent())

        val feedDir = File(rootDir, "feature/feed").apply { mkdirs() }
        File(feedDir, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":core:network"))
            }
        """.trimIndent())

        val networkDir = File(rootDir, "core/network").apply { mkdirs() }
        File(networkDir, "build.gradle.kts").writeText("dependencies { }")

        val checks = ProjectDoctorDialog.performDiagnostics(rootDir.absolutePath)
        val archCheck = checks.find { it.title == "Clean Architecture Dependency Guard" }

        assertTrue(archCheck != null)
        assertFalse(archCheck!!.passed)
        assertTrue(archCheck.details.contains("Encapsulation Leak"))
    }
}
