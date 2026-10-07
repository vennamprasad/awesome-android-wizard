package com.developer.awesomeandroidwizard.generator

import com.developer.awesomeandroidwizard.model.ArchetypeBlueprint
import com.developer.awesomeandroidwizard.model.ArchitectureType
import com.developer.awesomeandroidwizard.model.DiFramework
import com.developer.awesomeandroidwizard.model.NetworkingFramework
import com.developer.awesomeandroidwizard.model.WizardModel
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
            compileSdk = 35,
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
            moduleType = com.developer.awesomeandroidwizard.model.CoreModuleType.JVM_LIBRARY
        )
        assertTrue(jvmResult.success)
        assertTrue(File(projectDir, "core/analytics/build.gradle.kts").exists())
        assertTrue(File(projectDir, "core/analytics/src/main/kotlin/com/example/existingcore/core/analytics/AnalyticsContract.kt").exists())

        // 2. Scaffold Android core module ':core:security'
        val androidResult = CoreModuleGenerator.scaffoldCoreModule(
            projectRootDir = projectDir,
            rawCoreName = "security",
            moduleType = com.developer.awesomeandroidwizard.model.CoreModuleType.ANDROID_LIBRARY
        )
        assertTrue(androidResult.success)
        assertTrue(File(projectDir, "core/security/build.gradle.kts").exists())
        assertTrue(File(projectDir, "core/security/src/main/kotlin/com/example/existingcore/core/security/SecurityContract.kt").exists())

        // Verify settings.gradle.kts was updated
        val settingsText = File(projectDir, "settings.gradle.kts").readText()
        assertTrue(settingsText.contains("include(\":core:analytics\")"))
        assertTrue(settingsText.contains("include(\":core:security\")"))
    }
}
