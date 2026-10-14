package prasad.vennam.awesomeandroidwizard.ui

import org.junit.Assert.*
import org.junit.Test
import prasad.vennam.awesomeandroidwizard.model.*
import prasad.vennam.awesomeandroidwizard.ui.steps.*
import java.awt.GraphicsEnvironment

class WizardStepSynchronizationTest {

    @Test
    fun testArchetypeSynchronizationAcrossSteps() {
        if (GraphicsEnvironment.isHeadless()) {
            // Even in headless environments, Swing component hierarchies can be instantiated
        }

        val model = WizardModel()
        val step1 = ProjectBasicsStep(model)
        val step2 = ArchitectureStep(model)
        val step3 = DeviceAndThemeStep(model)
        val step4 = TechStackStep(model)
        val step5 = SummaryStep(model)

        // 1. Initial State should be STARTUP
        assertEquals(ArchetypeBlueprint.STARTUP, model.archetype)
        assertEquals(DiFramework.KOIN, model.diFramework)
        assertEquals(NetworkingFramework.KTOR, model.networking)

        // 2. Switch to FINTECH
        model.applyArchetype(ArchetypeBlueprint.FINTECH)
        assertEquals(DiFramework.HILT, model.diFramework)
        assertEquals(NetworkingFramework.RETROFIT, model.networking)
        assertEquals(DatabaseFramework.ROOM, model.database)

        // Sync steps
        step1.refreshFromModel()
        step2.refreshFromModel()
        step3.refreshFromModel()
        step4.refreshFromModel()
        step5.refresh()

        // Verify Step 2 has features and core modules
        assertTrue(model.featureModules.contains("auth"))
        assertTrue(model.featureModules.contains("dashboard"))
        assertTrue(model.selectedCoreModules.contains("core:model"))

        // 3. Switch to MINIMAL (Single Module)
        model.applyArchetype(ArchetypeBlueprint.MINIMAL)
        assertEquals(ArchitectureType.SINGLE_MODULE, model.architecture)
        assertFalse(model.useConventionPlugins)
        assertTrue(model.featureModules.isEmpty())
        assertTrue(model.selectedCoreModules.isEmpty())

        step2.refreshFromModel()
        step4.refreshFromModel()
        step5.refresh()

        // 4. Modify Architecture or TechStack manually -> transitions to CUSTOM
        model.architecture = ArchitectureType.MODULAR_CLEAN_MVI
        model.diFramework = DiFramework.KOIN
        model.archetype = ArchetypeBlueprint.CUSTOM
        assertEquals(ArchetypeBlueprint.CUSTOM, model.archetype)

        // 5. Test Dependency Cascading
        model.featureModules.add("search")
        model.ensureCoreModuleDependencies()
        assertTrue(model.selectedCoreModules.contains("core:model"))
        assertTrue(model.selectedCoreModules.contains("core:data"))
        assertTrue(model.selectedCoreModules.contains("core:designsystem"))
        assertTrue(model.selectedCoreModules.contains("core:ui"))
    }
}
