package prasad.vennam.awesomeandroidwizard.generator

import prasad.vennam.awesomeandroidwizard.model.ArchitectureType
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import prasad.vennam.awesomeandroidwizard.model.NetworkingFramework
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import org.junit.Test
import java.io.File

class GenerateMyAwesomeAppTask {

    @Test
    fun generateMyAwesomeApp() {
        val model = WizardModel(
            projectName = "MyAwesomeApp",
            packageName = "com.example.myawesomeapp",
            projectLocation = "/Users/prasadvennam/AndroidStudioProjects/MyAwesomeApp",
            minSdk = 26,
            targetSdk = 35,
            compileSdk = 35,
            architecture = ArchitectureType.MODULAR_CLEAN_MVI,
            diFramework = DiFramework.KOIN,
            networking = NetworkingFramework.KTOR,
            useCompose = true,
            useVersionCatalog = true,
            useConventionPlugins = true,
            generateCiWorkflow = true,
            generateDetekt = true,
            generateDependabot = true,
            selectedCoreModules = mutableSetOf(
                "core:model",
                "core:data",
                "core:network",
                "core:ui",
                "core:designsystem",
                "core:database",
                "core:testing"
            ),
            featureModules = mutableSetOf(
                "home",
                "auth",
                "profile",
                "settings",
                "notifications",
                "search"
            )
        )

        val dir = ProjectGenerator.generate(model)
        println("Generated MyAwesomeApp at: " + dir.absolutePath)
    }
}
