package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import prasad.vennam.awesomeandroidwizard.model.ArchitectureAuditor
import java.io.File

class ArchitectureAuditorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testCleanArchitectureCompliantProject() {
        val projectDir = tempFolder.newFolder("CleanApp")

        // 1. settings.gradle.kts
        File(projectDir, "settings.gradle.kts").writeText("""
            include(":app")
            include(":core:model")
            include(":feature:home")
        """.trimIndent())

        // 2. app build file
        val appDir = File(projectDir, "app").apply { mkdirs() }
        File(appDir, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":core:model"))
                implementation(project(":feature:home"))
            }
        """.trimIndent())

        // 3. feature:home build file
        val homeDir = File(projectDir, "feature/home").apply { mkdirs() }
        File(homeDir, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":core:model"))
            }
        """.trimIndent())

        // 4. core:model build file
        val modelDir = File(projectDir, "core/model").apply { mkdirs() }
        File(modelDir, "build.gradle.kts").writeText("""
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        """.trimIndent())

        val report = ArchitectureAuditor.auditProject(projectDir)

        assertTrue(report.isClean)
        assertEquals(3, report.totalModules)
        assertTrue(report.globalViolations.isEmpty())
    }

    @Test
    fun testCoreDependingOnFeatureViolation() {
        val projectDir = tempFolder.newFolder("ViolatingApp")

        File(projectDir, "settings.gradle.kts").writeText("""
            include(":core:data")
            include(":feature:auth")
        """.trimIndent())

        val dataDir = File(projectDir, "core/data").apply { mkdirs() }
        // Core data erroneously depending on feature auth
        File(dataDir, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":feature:auth"))
            }
        """.trimIndent())

        val authDir = File(projectDir, "feature/auth").apply { mkdirs() }
        File(authDir, "build.gradle.kts").writeText("""
            dependencies {
            }
        """.trimIndent())

        val report = ArchitectureAuditor.auditProject(projectDir)

        assertFalse(report.isClean)
        val dataNode = report.modules.find { it.gradlePath == ":core:data" }
        assertTrue(dataNode != null)
        assertTrue(dataNode!!.violations.any { it.contains("Boundary Violation") })
    }

    @Test
    fun testCircularDependencyDetection() {
        val projectDir = tempFolder.newFolder("CyclicApp")

        File(projectDir, "settings.gradle.kts").writeText("""
            include(":core:data")
            include(":core:network")
        """.trimIndent())

        val dataDir = File(projectDir, "core/data").apply { mkdirs() }
        File(dataDir, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":core:network"))
            }
        """.trimIndent())

        val netDir = File(projectDir, "core/network").apply { mkdirs() }
        File(netDir, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":core:data"))
            }
        """.trimIndent())

        val report = ArchitectureAuditor.auditProject(projectDir)

        assertFalse(report.isClean)
        assertTrue(report.globalViolations.any { it.contains("Circular dependency") })
    }

    @Test
    fun testFeatureEncapsulationLeak() {
        val projectDir = tempFolder.newFolder("FeatureLeakApp")

        File(projectDir, "settings.gradle.kts").writeText("""
            include(":feature:checkout")
            include(":core:network")
        """.trimIndent())

        val checkoutDir = File(projectDir, "feature/checkout").apply { mkdirs() }
        File(checkoutDir, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":core:network"))
            }
        """.trimIndent())

        val report = ArchitectureAuditor.auditProject(projectDir)
        assertFalse(report.isClean)
        val checkoutNode = report.modules.find { it.gradlePath == ":feature:checkout" }
        assertTrue(checkoutNode != null)
        assertTrue(checkoutNode!!.violations.any { it.contains("Encapsulation Leak") })
    }

    @Test
    fun testCrossFeatureCouplingViolation() {
        val projectDir = tempFolder.newFolder("CrossFeatureApp")

        File(projectDir, "settings.gradle.kts").writeText("""
            include(":feature:feed")
            include(":feature:profile")
        """.trimIndent())

        val feedDir = File(projectDir, "feature/feed").apply { mkdirs() }
        File(feedDir, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":feature:profile"))
            }
        """.trimIndent())

        val report = ArchitectureAuditor.auditProject(projectDir)
        assertFalse(report.isClean)
        val feedNode = report.modules.find { it.gradlePath == ":feature:feed" }
        assertTrue(feedNode != null)
        assertTrue(feedNode!!.violations.any { it.contains("Coupling Warning") })
    }
}
