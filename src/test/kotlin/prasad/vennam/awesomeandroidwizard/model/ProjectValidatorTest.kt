package prasad.vennam.awesomeandroidwizard.model

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ProjectValidatorTest {

    @Test
    fun testProjectNameValidation() {
        // Valid
        assertTrue(ProjectValidator.validateProjectName("MyAwesomeApp").isValid)
        assertTrue(ProjectValidator.validateProjectName("App123").isValid)
        assertTrue(ProjectValidator.validateProjectName("Fintech_Banking").isValid)

        // Empty
        val emptyRes = ProjectValidator.validateProjectName("   ")
        assertTrue(emptyRes.isError)
        assertEquals("Project name cannot be empty.", (emptyRes as ProjectValidator.ValidationResult.Error).message)

        // Illegal characters
        assertTrue(ProjectValidator.validateProjectName("App/Name").isError)
        assertTrue(ProjectValidator.validateProjectName("App\\Name").isError)
        assertTrue(ProjectValidator.validateProjectName("App:Name").isError)
        assertTrue(ProjectValidator.validateProjectName("App*Name").isError)
        assertTrue(ProjectValidator.validateProjectName("App?Name").isError)
        assertTrue(ProjectValidator.validateProjectName("App\"Name").isError)
        assertTrue(ProjectValidator.validateProjectName("App<Name").isError)
        assertTrue(ProjectValidator.validateProjectName("App>Name").isError)
        assertTrue(ProjectValidator.validateProjectName("App|Name").isError)

        // Reserved OS device names
        assertTrue(ProjectValidator.validateProjectName("CON").isError)
        assertTrue(ProjectValidator.validateProjectName("aux").isError)
        assertTrue(ProjectValidator.validateProjectName("NUL").isError)
        assertTrue(ProjectValidator.validateProjectName("com1").isError)

        // Control characters
        assertTrue(ProjectValidator.validateProjectName("App\u0000Name").isError)
        assertTrue(ProjectValidator.validateProjectName("App\nName").isError)

        // Non-alphanumeric start
        assertTrue(ProjectValidator.validateProjectName("-InvalidStart").isError)
        assertTrue(ProjectValidator.validateProjectName("!App").isError)
    }

    @Test
    fun testPackageNameValidation() {
        // Valid
        assertTrue(ProjectValidator.validatePackageName("com.example.myawesomeapp").isValid)
        assertTrue(ProjectValidator.validatePackageName("org.company.project_app").isValid)
        assertTrue(ProjectValidator.validatePackageName("io.github.vennamprasad.wizard").isValid)

        // Empty
        assertTrue(ProjectValidator.validatePackageName("").isError)
        assertTrue(ProjectValidator.validatePackageName("   ").isError)

        // Single segment
        val singleRes = ProjectValidator.validatePackageName("myawesomeapp")
        assertTrue(singleRes.isError)
        assertTrue((singleRes as ProjectValidator.ValidationResult.Error).message.contains("at least two segments"))

        // Consecutive or leading/trailing dots
        assertTrue(ProjectValidator.validatePackageName(".com.example.app").isError)
        assertTrue(ProjectValidator.validatePackageName("com.example.app.").isError)
        assertTrue(ProjectValidator.validatePackageName("com..example.app").isError)

        // Starting with digit in a segment
        val digitRes = ProjectValidator.validatePackageName("com.example.123app")
        assertTrue(digitRes.isError)
        assertTrue((digitRes as ProjectValidator.ValidationResult.Error).message.contains("must start with a letter or underscore"))

        // Reserved Java/Kotlin keywords
        val keywordRes1 = ProjectValidator.validatePackageName("com.class.app")
        assertTrue(keywordRes1.isError)
        assertTrue((keywordRes1 as ProjectValidator.ValidationResult.Error).message.contains("reserved keyword"))

        val keywordRes2 = ProjectValidator.validatePackageName("com.fun.app")
        assertTrue(keywordRes2.isError)
        assertTrue((keywordRes2 as ProjectValidator.ValidationResult.Error).message.contains("reserved keyword"))

        val keywordRes3 = ProjectValidator.validatePackageName("com.example.switch")
        assertTrue(keywordRes3.isError)
        assertTrue((keywordRes3 as ProjectValidator.ValidationResult.Error).message.contains("reserved keyword"))

        // Kotlin in, is, as
        assertTrue(ProjectValidator.validatePackageName("com.in.app").isError)
        assertTrue(ProjectValidator.validatePackageName("com.example.is").isError)
        assertTrue(ProjectValidator.validatePackageName("com.as.core").isError)
    }

    @Test
    fun testProjectLocationValidation() {
        // Empty
        assertTrue(ProjectValidator.validateProjectLocation("").isError)
        assertTrue(ProjectValidator.validateProjectLocation("   ").isError)

        // Relative path
        assertTrue(ProjectValidator.validateProjectLocation("relative/path/to/project").isError)

        // New / non-existent directory under existing parent
        val tempParent = File(System.getProperty("java.io.tmpdir"))
        val newTargetDir = File(tempParent, "awesome_test_new_project_${System.currentTimeMillis()}")
        assertTrue(ProjectValidator.validateProjectLocation(newTargetDir.absolutePath).isValid)

        // Existing directory with files produces warning
        val existingNonEmptyDir = File(tempParent, "awesome_test_existing_${System.currentTimeMillis()}").apply {
            mkdirs()
            File(this, "dummy.txt").writeText("hello")
        }
        try {
            val warnRes = ProjectValidator.validateProjectLocation(existingNonEmptyDir.absolutePath)
            assertTrue(warnRes.isWarning)
            assertTrue((warnRes as ProjectValidator.ValidationResult.Warning).message.contains("not empty"))
        } finally {
            existingNonEmptyDir.deleteRecursively()
        }
    }

    @Test
    fun testValidateAll() {
        val valid = ProjectValidator.validateAll(
            "MyAwesomeApp",
            "com.example.myawesomeapp",
            File(System.getProperty("java.io.tmpdir"), "test_proj").absolutePath
        )
        assertTrue(valid.isValid)

        val invalidName = ProjectValidator.validateAll(
            "",
            "com.example.myawesomeapp",
            File(System.getProperty("java.io.tmpdir"), "test_proj").absolutePath
        )
        assertTrue(invalidName.isError)
    }
}
