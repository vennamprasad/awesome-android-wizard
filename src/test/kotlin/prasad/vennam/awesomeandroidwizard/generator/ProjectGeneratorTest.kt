package prasad.vennam.awesomeandroidwizard.generator

import prasad.vennam.awesomeandroidwizard.model.ArchetypeBlueprint
import prasad.vennam.awesomeandroidwizard.model.ArchitectureType
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import prasad.vennam.awesomeandroidwizard.model.NetworkingFramework
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ProjectGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testGenerateMultiModuleCleanArchitectureWithCustomFeatures() {
        val targetDir = tempFolder.newFolder("TestApp")

        val model = WizardModel(
            projectName = "TestApp",
            packageName = "com.example.testapp",
            projectLocation = targetDir.absolutePath,
            minSdk = 26,
            targetSdk = 35,
            compileSdk = 37,
            architecture = ArchitectureType.MODULAR_CLEAN_MVI,
            diFramework = DiFramework.KOIN,
            networking = NetworkingFramework.KTOR,
            useCompose = true,
            useVersionCatalog = true,
            useConventionPlugins = true,
            selectedCoreModules = mutableSetOf("core:model", "core:ui"),
            featureModules = mutableSetOf("home", "profile", "auth-flow")
        )

        val resultDir = ProjectGenerator.generate(model)

        // Verify root files
        assertTrue(File(resultDir, "settings.gradle.kts").exists())
        assertTrue(File(resultDir, "build.gradle.kts").exists())
        assertTrue(File(resultDir, "gradle.properties").exists())
        assertTrue(File(resultDir, ".gitignore").exists())
        assertTrue(File(resultDir, ".editorconfig").exists())
        assertTrue(File(resultDir, ".idea/runConfigurations/app.xml").exists())
        assertTrue(File(resultDir, "README.md").exists())
        assertTrue(File(resultDir, "gradle/wrapper/gradle-wrapper.properties").exists())
        assertTrue(File(resultDir, "gradle/libs.versions.toml").exists())

        // Verify build-logic convention plugins
        assertTrue(File(resultDir, "build-logic/settings.gradle.kts").exists())
        assertTrue(File(resultDir, "build-logic/convention/build.gradle.kts").exists())
        assertTrue(File(resultDir, "build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt").exists())
        assertTrue(File(resultDir, "build-logic/convention/src/main/kotlin/AndroidLibraryConventionPlugin.kt").exists())
        assertTrue(File(resultDir, "build-logic/convention/src/main/kotlin/AndroidComposeConventionPlugin.kt").exists())
        assertTrue(File(resultDir, "build-logic/convention/src/main/kotlin/JvmLibraryConventionPlugin.kt").exists())

        // Verify :app module
        assertTrue(File(resultDir, "app/build.gradle.kts").exists())
        assertTrue(File(resultDir, "app/src/main/AndroidManifest.xml").exists())
        assertTrue(File(resultDir, "app/src/main/kotlin/com/example/testapp/MainActivity.kt").exists())
        assertTrue(File(resultDir, "app/src/main/kotlin/com/example/testapp/AwesomeApplication.kt").exists())

        // Verify custom named feature: home
        assertTrue(File(resultDir, "feature/home/build.gradle.kts").exists())
        assertTrue(File(resultDir, "feature/home/src/main/kotlin/com/example/testapp/feature/home/HomeScreen.kt").exists())

        // Verify custom named feature: auth-flow
        assertTrue(File(resultDir, "feature/auth-flow/build.gradle.kts").exists())
        assertTrue(File(resultDir, "feature/auth-flow/src/main/kotlin/com/example/testapp/feature/auth-flow/AuthFlowScreen.kt").exists())

        // Verify settings.gradle.kts contains all custom features
        val settingsContent = File(resultDir, "settings.gradle.kts").readText()
        assertTrue(settingsContent.contains("include(\":feature:home\")"))
        assertTrue(settingsContent.contains("include(\":feature:profile\")"))
        assertTrue(settingsContent.contains("include(\":feature:auth-flow\")"))
    }

    @Test
    fun testFintechArchetypeAndDevOpsGeneration() {
        val targetDir = tempFolder.newFolder("FintechApp")

        val model = WizardModel(
            projectName = "FintechApp",
            packageName = "com.company.fintech",
            projectLocation = targetDir.absolutePath
        ).apply {
            applyArchetype(ArchetypeBlueprint.FINTECH)
        }

        val resultDir = ProjectGenerator.generate(model)

        // Verify Fintech archetypes (Hilt + Retrofit)
        val libsContent = File(resultDir, "gradle/libs.versions.toml").readText()
        assertTrue(libsContent.contains("hilt"))
        assertTrue(libsContent.contains("retrofit"))

        // Verify DevOps files
        assertTrue(File(resultDir, ".github/workflows/android-ci.yml").exists())
        assertTrue(File(resultDir, ".github/dependabot.yml").exists())
        assertTrue(File(resultDir, "config/detekt/detekt.yml").exists())

        // Verify Design System Tokens, Icons, and DataStore
        assertTrue(File(resultDir, "core/designsystem/src/main/kotlin/com/company/fintech/core/designsystem/theme/Spacing.kt").exists())
        assertTrue(File(resultDir, "core/designsystem/src/main/kotlin/com/company/fintech/core/designsystem/theme/Radius.kt").exists())
        assertTrue(File(resultDir, "core/designsystem/src/main/kotlin/com/company/fintech/core/designsystem/icon/AwesomeIcons.kt").exists())
        assertTrue(File(resultDir, "core/designsystem/src/main/kotlin/com/company/fintech/core/designsystem/datastore/ThemeConfig.kt").exists())
        assertTrue(File(resultDir, "core/designsystem/src/main/kotlin/com/company/fintech/core/designsystem/datastore/ThemeDataStore.kt").exists())

        // Verify Git Hooks
        val preCommitHook = File(resultDir, ".githooks/pre-commit")
        assertTrue(preCommitHook.exists())
        assertTrue(preCommitHook.canExecute())

        // Verify :core:database
        assertTrue(File(resultDir, "core/database/build.gradle.kts").exists())
        assertTrue(File(resultDir, "core/database/src/main/kotlin/com/company/fintech/core/database/model/ItemEntity.kt").exists())
        assertTrue(File(resultDir, "core/database/src/main/kotlin/com/company/fintech/core/database/dao/ItemDao.kt").exists())
        assertTrue(File(resultDir, "core/database/src/main/kotlin/com/company/fintech/core/database/AwesomeDatabase.kt").exists())

        // Verify :core:testing
        assertTrue(File(resultDir, "core/testing/build.gradle.kts").exists())
        assertTrue(File(resultDir, "core/testing/src/main/kotlin/com/company/fintech/core/testing/util/MainDispatcherRule.kt").exists())
        assertTrue(File(resultDir, "core/testing/src/main/kotlin/com/company/fintech/core/testing/repository/FakeItemRepository.kt").exists())
        assertTrue(File(resultDir, "core/testing/src/main/kotlin/com/company/fintech/core/testing/tags/AwesomeTestTags.kt").exists())
    }

    @Test
    fun testExistingProjectFeatureScaffolder() {
        val projectDir = tempFolder.newFolder("ExistingApp")

        // 1. Initial minimal project
        val initialModel = WizardModel(
            projectName = "ExistingApp",
            packageName = "com.example.existing",
            projectLocation = projectDir.absolutePath,
            featureModules = mutableSetOf("home")
        )
        ProjectGenerator.generate(initialModel)

        // 2. Use FeatureModuleGenerator to add ':feature:checkout'
        val result = FeatureModuleGenerator.scaffoldFeature(
            projectRootDir = projectDir,
            rawFeatureName = "checkout"
        )

        assertTrue(result.success)
        assertTrue(File(projectDir, "feature/checkout/build.gradle.kts").exists())
        assertTrue(File(projectDir, "feature/checkout/src/main/kotlin/com/example/existing/feature/checkout/CheckoutScreen.kt").exists())
        assertTrue(File(projectDir, "feature/checkout/src/main/kotlin/com/example/existing/feature/checkout/CheckoutViewModel.kt").exists())

        // Verify settings.gradle.kts was updated
        val settingsText = File(projectDir, "settings.gradle.kts").readText()
        assertTrue(settingsText.contains("include(\":feature:checkout\")"))

        // Verify app/build.gradle.kts dependencies was updated
        val appBuildText = File(projectDir, "app/build.gradle.kts").readText()
        assertTrue(appBuildText.contains("implementation(project(\":feature:checkout\"))"))
    }

    @Test
    fun testExistingProjectCoreScaffolder() {
        val projectDir = tempFolder.newFolder("ExistingAppForCore")

        val initialModel = WizardModel(
            projectName = "ExistingAppForCore",
            packageName = "com.example.existingcore",
            projectLocation = projectDir.absolutePath,
            featureModules = mutableSetOf("home")
        )
        ProjectGenerator.generate(initialModel)

        // 1. Scaffold JVM core module ':core:analytics'
        val jvmResult = CoreModuleGenerator.scaffoldCoreModule(
            projectRootDir = projectDir,
            rawCoreName = "analytics",
            moduleType = prasad.vennam.awesomeandroidwizard.model.CoreModuleType.JVM_LIBRARY
        )
        assertTrue(jvmResult.success)
        assertTrue(File(projectDir, "core/analytics/build.gradle.kts").exists())
        assertTrue(File(projectDir, "core/analytics/src/main/kotlin/com/example/existingcore/core/analytics/AnalyticsContract.kt").exists())

        // 2. Scaffold Android core module ':core:security'
        val androidResult = CoreModuleGenerator.scaffoldCoreModule(
            projectRootDir = projectDir,
            rawCoreName = "security",
            moduleType = prasad.vennam.awesomeandroidwizard.model.CoreModuleType.ANDROID_LIBRARY
        )
        assertTrue(androidResult.success)
        assertTrue(File(projectDir, "core/security/build.gradle.kts").exists())
        assertTrue(File(projectDir, "core/security/src/main/kotlin/com/example/existingcore/core/security/SecurityContract.kt").exists())

        // Verify settings.gradle.kts was updated
        val settingsText = File(projectDir, "settings.gradle.kts").readText()
        assertTrue(settingsText.contains("include(\":core:analytics\")"))
        assertTrue(settingsText.contains("include(\":core:security\")"))
    }

    @Test
    fun testDeviceFormFactorAndThemeCustomization() {
        val projectDir = tempFolder.newFolder("TvOceanApp")
        val model = WizardModel(
            projectName = "TvOceanApp",
            packageName = "com.example.tvocean",
            projectLocation = projectDir.absolutePath,
            targetFormFactor = prasad.vennam.awesomeandroidwizard.model.TargetFormFactor.TV,
            colorPreset = prasad.vennam.awesomeandroidwizard.model.ColorPreset.OCEAN_BLUE,
            cornerStyle = prasad.vennam.awesomeandroidwizard.model.CornerStyle.SHARP,
            featureModules = mutableSetOf("home")
        )

        val resultDir = ProjectGenerator.generate(model)

        // 1. Verify AndroidManifest has TV Leanback entries
        val manifestText = File(resultDir, "app/src/main/AndroidManifest.xml").readText()
        assertTrue(manifestText.contains("android.software.leanback"))
        assertTrue(manifestText.contains("LEANBACK_LAUNCHER"))

        // 2. Verify MainActivity has TV Tab layout
        val mainActivityText = File(resultDir, "app/src/main/kotlin/com/example/tvocean/MainActivity.kt").readText()
        assertTrue(mainActivityText.contains("PrimaryTabRow"))

        // 3. Verify Design System Color has Ocean Blue hex
        val colorText = File(resultDir, "core/designsystem/src/main/kotlin/com/example/tvocean/core/designsystem/theme/Color.kt").readText()
        assertTrue(colorText.contains("0xFF0061A4"))

        // 4. Verify Design System Radius has Sharp corner constants (2.dp, 4.dp)
        val radiusText = File(resultDir, "core/designsystem/src/main/kotlin/com/example/tvocean/core/designsystem/theme/Radius.kt").readText()
        assertTrue(radiusText.contains("small: Dp = 2.dp"))
        assertTrue(radiusText.contains("medium: Dp = 4.dp"))
    }

    @Test
    fun testAgp9KotlinBuiltinAndCompileSdkInCoreModules() {
        val projectDir = tempFolder.newFolder("Agp9App")
        val model = WizardModel(
            projectName = "Agp9App",
            packageName = "com.example.agp9",
            projectLocation = projectDir.absolutePath,
            useConventionPlugins = false,
            compileSdk = 37,
            minSdk = 24,
            selectedCoreModules = mutableSetOf(
                "core:data", "core:network", "core:database",
                "core:ui", "core:designsystem", "core:testing"
            ),
            featureModules = mutableSetOf("home")
        )

        val resultDir = ProjectGenerator.generate(model)

        val appBuild = File(resultDir, "app/build.gradle.kts").readText()
        val coreDataBuild = File(resultDir, "core/data/build.gradle.kts").readText()
        val coreNetworkBuild = File(resultDir, "core/network/build.gradle.kts").readText()
        val coreDbBuild = File(resultDir, "core/database/build.gradle.kts").readText()
        val coreUiBuild = File(resultDir, "core/ui/build.gradle.kts").readText()
        val coreDsBuild = File(resultDir, "core/designsystem/build.gradle.kts").readText()
        val coreTestingBuild = File(resultDir, "core/testing/build.gradle.kts").readText()
        val featureBuild = File(resultDir, "feature/home/build.gradle.kts").readText()
        val rootBuild = File(resultDir, "build.gradle.kts").readText()

        // 1. Verify AGP 9 built-in Kotlin: alias(libs.plugins.kotlin.android) is NEVER declared
        val modules = listOf(rootBuild, appBuild, coreDataBuild, coreNetworkBuild, coreDbBuild, coreUiBuild, coreDsBuild, coreTestingBuild, featureBuild)
        for (moduleContent in modules) {
            org.junit.Assert.assertFalse(moduleContent.contains("alias(libs.plugins.kotlin.android)"))
        }

        // 2. Verify compileSdk and minSdk are configured in :core:data and :core:network
        assertTrue(coreDataBuild.contains("compileSdk = 37"))
        assertTrue(coreDataBuild.contains("minSdk = 24"))
        assertTrue(coreNetworkBuild.contains("compileSdk = 37"))
        assertTrue(coreNetworkBuild.contains("minSdk = 24"))
    }
}
