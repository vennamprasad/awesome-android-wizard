package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.*
import org.junit.Test
import prasad.vennam.awesomeandroidwizard.generator.templates.ModulesTemplate
import prasad.vennam.awesomeandroidwizard.model.ArchetypeBlueprint
import prasad.vennam.awesomeandroidwizard.model.ArchitectureType
import prasad.vennam.awesomeandroidwizard.model.WizardModel

class GeneratorTemplateTest {

    @Test
    fun testSingleModuleMainActivityDoesNotCallTabsFirstOrImportDesignSystem() {
        val model = WizardModel().apply {
            applyArchetype(ArchetypeBlueprint.MINIMAL)
        }
        assertEquals(ArchitectureType.SINGLE_MODULE, model.architecture)
        assertTrue(model.featureModules.isEmpty())
        assertTrue(model.selectedCoreModules.isEmpty())

        val mainActivityContent = ModulesTemplate.generateMainActivity(model)
        assertFalse("Must not call tabs.first() when tabs are empty", mainActivityContent.contains("tabs.first()"))
        assertFalse("Must not import core.designsystem when not selected", mainActivityContent.contains("core.designsystem"))
        assertTrue("Must declare MainScreen composable", mainActivityContent.contains("fun MainScreen("))
        assertTrue("Must use MaterialTheme wrapper", mainActivityContent.contains("MaterialTheme {"))
    }

    @Test
    fun testCoreModuleDependencyCascading() {
        val model = WizardModel(
            architecture = ArchitectureType.MODULAR_CLEAN_MVI,
            selectedCoreModules = mutableSetOf("core:testing"),
            featureModules = mutableSetOf()
        )
        model.ensureCoreModuleDependencies()
        assertTrue("Testing module requires core:model", model.selectedCoreModules.contains("core:model"))
        assertTrue("Testing module requires core:data", model.selectedCoreModules.contains("core:data"))

        val featureModel = WizardModel(
            architecture = ArchitectureType.MODULAR_CLEAN_MVI,
            selectedCoreModules = mutableSetOf(),
            featureModules = mutableSetOf("home")
        )
        featureModel.ensureCoreModuleDependencies()
        assertTrue("Features require core:model", featureModel.selectedCoreModules.contains("core:model"))
        assertTrue("Features require core:ui", featureModel.selectedCoreModules.contains("core:ui"))
        assertTrue("Features require core:designsystem", featureModel.selectedCoreModules.contains("core:designsystem"))
        assertTrue("Features require core:data", featureModel.selectedCoreModules.contains("core:data"))
    }
}
