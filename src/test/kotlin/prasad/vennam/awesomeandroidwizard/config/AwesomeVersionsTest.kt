package prasad.vennam.awesomeandroidwizard.config

import org.junit.Assert.*
import org.junit.Test

class AwesomeVersionsTest {

    @Test
    fun testAllVersionsAreValidAndNonEmpty() {
        assertFalse(AwesomeVersions.GRADLE_WRAPPER.isBlank())
        assertFalse(AwesomeVersions.AGP.isBlank())
        assertFalse(AwesomeVersions.KOTLIN.isBlank())
        assertFalse(AwesomeVersions.COMPOSE_BOM.isBlank())
        assertTrue(AwesomeVersions.COMPILE_SDK >= 37)
        assertTrue(AwesomeVersions.TARGET_SDK >= 35)
        assertTrue(AwesomeVersions.MIN_SDK >= 24)

        val managed = AwesomeVersions.allManagedVersions()
        assertTrue(managed.isNotEmpty())
        managed.forEach { (key, version) ->
            assertFalse("Version for key $key must not be blank", version.isBlank())
        }
    }

    @Test
    fun testGradleWrapperUrl() {
        val url = AwesomeVersions.gradleWrapperDistributionUrl()
        assertTrue(url.contains(AwesomeVersions.GRADLE_WRAPPER))
        assertTrue(url.startsWith("https\\://services.gradle.org/distributions/gradle-"))
    }

    @Test
    fun testAuditTomlVersionsDetectsOutdatedEntries() {
        val sampleToml = """
[versions]
agp = "8.2.0"
kotlin = "1.9.20"
composeBom = "2024.01.00"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.12.0" }
""".trimIndent()

        val results = AwesomeVersions.auditTomlVersions(sampleToml)
        val agpStatus = results.firstOrNull { it.key == "agp" }
        assertNotNull(agpStatus)
        assertEquals("8.2.0", agpStatus?.currentVersion)
        assertEquals(AwesomeVersions.AGP, agpStatus?.recommendedVersion)
        assertTrue(agpStatus?.isOutdated == true)

        val kotlinStatus = results.firstOrNull { it.key == "kotlin" }
        assertNotNull(kotlinStatus)
        assertEquals("1.9.20", kotlinStatus?.currentVersion)
        assertTrue(kotlinStatus?.isOutdated == true)
    }

    @Test
    fun testUpgradeTomlContentUpdatesVersionsPreservingFormatting() {
        val sampleToml = """
# Project dependencies
[versions]
agp = "8.2.0" # old AGP
kotlin = "1.9.20"

[libraries]
some-lib = { group = "com.example", name = "lib" }
""".trimIndent()

        val upgraded = AwesomeVersions.upgradeTomlContent(sampleToml)
        assertTrue(upgraded.contains("""agp = "${AwesomeVersions.AGP}" # old AGP"""))
        assertTrue(upgraded.contains("""kotlin = "${AwesomeVersions.KOTLIN}""""))
        assertTrue(upgraded.contains("[libraries]"))
        assertTrue(upgraded.contains("some-lib = { group = \"com.example\", name = \"lib\" }"))
    }
}
