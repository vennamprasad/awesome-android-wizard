package prasad.vennam.awesomeandroidwizard.model

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ArchitectureAuditorTest {

    @Test
    fun testParseIncludedModulesSingleAndMultiArg() {
        val tempSettings = File.createTempFile("settings", ".gradle.kts")
        try {
            tempSettings.writeText("""
                rootProject.name = "TestApp"
                include(":app")
                include(":core:model", ":core:data", ":core:network")
                // include(":commented:out")
                include(":feature:home")
            """.trimIndent())

            val modules = ArchitectureAuditor.parseIncludedModules(tempSettings)
            assertEquals(5, modules.size)
            assertTrue(modules.contains(":app"))
            assertTrue(modules.contains(":core:model"))
            assertTrue(modules.contains(":core:data"))
            assertTrue(modules.contains(":core:network"))
            assertTrue(modules.contains(":feature:home"))
            assertFalse(modules.contains(":commented:out"))
        } finally {
            tempSettings.delete()
        }
    }

    @Test
    fun testParseModuleDependenciesTraditionalAndTypeSafe() {
        val buildContent = """
            dependencies {
                implementation(project(":core:model"))
                implementation(project(path = ":core:data"))
                implementation(projects.feature.home)
                testImplementation(project(":core:testing"))
            }
        """.trimIndent()

        val deps = ArchitectureAuditor.parseModuleDependencies(buildContent)
        assertTrue(deps.contains(":core:model"))
        assertTrue(deps.contains(":core:data"))
        assertTrue(deps.contains(":feature:home"))
        assertTrue(deps.contains(":core:testing"))
    }
}
