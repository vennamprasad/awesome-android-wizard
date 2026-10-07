package com.developer.awesomeandroidwizard.generator

import com.developer.awesomeandroidwizard.generator.templates.BuildLogicTemplate
import com.developer.awesomeandroidwizard.generator.templates.ModulesTemplate
import com.developer.awesomeandroidwizard.generator.templates.RootFilesTemplate
import com.developer.awesomeandroidwizard.generator.templates.VersionCatalogTemplate
import com.developer.awesomeandroidwizard.model.WizardModel
import java.io.File

object ProjectGenerator {

    fun generate(model: WizardModel): File {
        val rootDir = File(model.projectLocation)
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }

        // 1. Root files
        createFile(rootDir, "settings.gradle.kts", RootFilesTemplate.generateSettingsGradle(model))
        createFile(rootDir, "build.gradle.kts", RootFilesTemplate.generateRootBuildGradle(model))
        createFile(rootDir, "gradle.properties", RootFilesTemplate.generateGradleProperties())
        createFile(rootDir, ".gitignore", RootFilesTemplate.generateGitignore())
        createFile(rootDir, ".editorconfig", RootFilesTemplate.generateEditorConfig())
        createFile(rootDir, "README.md", RootFilesTemplate.generateReadme(model))

        // Pre-configured Android Run Configuration for Android Studio
        val runConfigDir = File(rootDir, ".idea/runConfigurations")
        createFile(runConfigDir, "app.xml", RootFilesTemplate.generateRunConfigurationXml())

        // Pre-commit git hooks
        val githooksDir = File(rootDir, ".githooks")
        createFile(githooksDir, "pre-commit", RootFilesTemplate.generatePreCommitHook())
        File(githooksDir, "pre-commit").setExecutable(true)

        // Gradle wrapper
        val wrapperDir = File(rootDir, "gradle/wrapper")
        wrapperDir.mkdirs()
        createFile(wrapperDir, "gradle-wrapper.properties", RootFilesTemplate.generateGradleWrapperProperties())
        copyWrapperFiles(rootDir)
        detectAndWriteLocalProperties(rootDir)

        // 2. Version Catalog
        val gradleDir = File(rootDir, "gradle")
        gradleDir.mkdirs()
        createFile(gradleDir, "libs.versions.toml", VersionCatalogTemplate.generate(model))

        // 3. Build-logic Convention Plugins
        if (model.useConventionPlugins) {
            val buildLogicDir = File(rootDir, "build-logic")
            createFile(buildLogicDir, "settings.gradle.kts", BuildLogicTemplate.generateSettingsGradle())

            val conventionDir = File(buildLogicDir, "convention")
            createFile(conventionDir, "build.gradle.kts", BuildLogicTemplate.generateConventionBuildGradle())

            val conventionSrc = File(conventionDir, "src/main/kotlin")
            conventionSrc.mkdirs()
            createFile(conventionSrc, "KotlinAndroid.kt", BuildLogicTemplate.generateKotlinAndroidExtension(model))
            createFile(conventionSrc, "AndroidApplicationConventionPlugin.kt", BuildLogicTemplate.generateAndroidApplicationConventionPlugin(model))
            createFile(conventionSrc, "AndroidLibraryConventionPlugin.kt", BuildLogicTemplate.generateAndroidLibraryConventionPlugin())
            createFile(conventionSrc, "AndroidComposeConventionPlugin.kt", BuildLogicTemplate.generateAndroidComposeConventionPlugin())
            createFile(conventionSrc, "JvmLibraryConventionPlugin.kt", BuildLogicTemplate.generateJvmLibraryConventionPlugin())
        }

        // 4. :app module
        val appDir = File(rootDir, "app")
        createFile(appDir, "build.gradle.kts", ModulesTemplate.generateAppBuildGradle(model))
        val appSrcMain = File(appDir, "src/main")
        createFile(appSrcMain, "AndroidManifest.xml", ModulesTemplate.generateAppManifest(model))

        val appPackageDir = File(appSrcMain, "kotlin/${model.getFormattedPackagePath()}")
        appPackageDir.mkdirs()
        createFile(appPackageDir, "AwesomeApplication.kt", ModulesTemplate.generateApplicationClass(model))
        createFile(appPackageDir, "MainActivity.kt", ModulesTemplate.generateMainActivity(model))

        // 5. Core Submodules
        val pkgPath = model.getFormattedPackagePath()

        if (model.selectedCoreModules.contains("core:designsystem") || model.selectedCoreModules.contains("core/designsystem")) {
            val coreDesignDir = File(rootDir, "core/designsystem")
            createFile(coreDesignDir, "build.gradle.kts", ModulesTemplate.generateCoreDesignSystemBuildGradle(model))
            val themeDir = File(coreDesignDir, "src/main/kotlin/$pkgPath/core/designsystem/theme")
            themeDir.mkdirs()
            createFile(themeDir, "Theme.kt", ModulesTemplate.generateDesignSystemTheme(model))
            createFile(themeDir, "Color.kt", ModulesTemplate.generateDesignSystemColor(model))
            createFile(themeDir, "Type.kt", ModulesTemplate.generateDesignSystemType(model))
            createFile(themeDir, "Spacing.kt", ModulesTemplate.generateDesignSystemSpacing(model))
            createFile(themeDir, "Radius.kt", ModulesTemplate.generateDesignSystemRadius(model))

            // Icons Catalog & Components
            val iconDir = File(coreDesignDir, "src/main/kotlin/$pkgPath/core/designsystem/icon")
            iconDir.mkdirs()
            createFile(iconDir, "AwesomeIcons.kt", ModulesTemplate.generateDesignSystemIcons(model))

            // DataStore & Theme Preferences
            val dataStoreDir = File(coreDesignDir, "src/main/kotlin/$pkgPath/core/designsystem/datastore")
            dataStoreDir.mkdirs()
            createFile(dataStoreDir, "ThemeConfig.kt", ModulesTemplate.generateDesignSystemThemeConfig(model))
            createFile(dataStoreDir, "ThemeDataStore.kt", ModulesTemplate.generateDesignSystemThemeDataStore(model))

            // Resource: Vector logo in res/drawable
            val resDrawableDir = File(coreDesignDir, "src/main/res/drawable")
            resDrawableDir.mkdirs()
            createFile(resDrawableDir, "ic_awesome_logo.xml", ModulesTemplate.generateDesignSystemLogoVector())
        }

        if (model.selectedCoreModules.contains("core:ui") || model.selectedCoreModules.contains("core/ui")) {
            val coreUiDir = File(rootDir, "core/ui")
            createFile(coreUiDir, "build.gradle.kts", ModulesTemplate.generateCoreUiBuildGradle(model))
            val coreUiComponentsDir = File(coreUiDir, "src/main/kotlin/$pkgPath/core/ui/components")
            coreUiComponentsDir.mkdirs()
            createFile(coreUiComponentsDir, "AwesomeComponents.kt", ModulesTemplate.generateAwesomeComponents(model))
        }

        if (model.selectedCoreModules.contains("core:model") || model.selectedCoreModules.contains("core/model")) {
            val coreModelDir = File(rootDir, "core/model")
            createFile(coreModelDir, "build.gradle.kts", ModulesTemplate.generateCoreModelBuildGradle(model))
            val coreModelSrc = File(coreModelDir, "src/main/kotlin/$pkgPath/core/model")
            coreModelSrc.mkdirs()
            createFile(coreModelSrc, "Item.kt", ModulesTemplate.generateSampleModel(model))
        }

        if (model.selectedCoreModules.contains("core:network") || model.selectedCoreModules.contains("core/network")) {
            val coreNetworkDir = File(rootDir, "core/network")
            createFile(coreNetworkDir, "build.gradle.kts", ModulesTemplate.generateCoreNetworkBuildGradle(model))
            val modelDir = File(coreNetworkDir, "src/main/kotlin/$pkgPath/core/network/model")
            modelDir.mkdirs()
            createFile(modelDir, "NetworkItem.kt", ModulesTemplate.generateNetworkItem(model))
            createFile(modelDir, "NetworkResult.kt", ModulesTemplate.generateNetworkResult(model))
            val dsDir = File(coreNetworkDir, "src/main/kotlin/$pkgPath/core/network/datasource")
            dsDir.mkdirs()
            createFile(dsDir, "AwesomeNetworkDataSource.kt", ModulesTemplate.generateNetworkDataSource(model))
        }

        if (model.selectedCoreModules.contains("core:data") || model.selectedCoreModules.contains("core/data")) {
            val coreDataDir = File(rootDir, "core/data")
            createFile(coreDataDir, "build.gradle.kts", ModulesTemplate.generateCoreDataBuildGradle(model))
            val repoDir = File(coreDataDir, "src/main/kotlin/$pkgPath/core/data/repository")
            repoDir.mkdirs()
            createFile(repoDir, "ItemRepository.kt", ModulesTemplate.generateItemRepository(model))
            createFile(repoDir, "DefaultItemRepository.kt", ModulesTemplate.generateDefaultItemRepository(model))
        }

        if (model.selectedCoreModules.contains("core:database") || model.selectedCoreModules.contains("core/database")) {
            val coreDbDir = File(rootDir, "core/database")
            createFile(coreDbDir, "build.gradle.kts", ModulesTemplate.generateCoreDatabaseBuildGradle(model))
            val dbModelDir = File(coreDbDir, "src/main/kotlin/$pkgPath/core/database/model")
            dbModelDir.mkdirs()
            createFile(dbModelDir, "ItemEntity.kt", ModulesTemplate.generateItemEntity(model))
            val daoDir = File(coreDbDir, "src/main/kotlin/$pkgPath/core/database/dao")
            daoDir.mkdirs()
            createFile(daoDir, "ItemDao.kt", ModulesTemplate.generateItemDao(model))
            val dbSrc = File(coreDbDir, "src/main/kotlin/$pkgPath/core/database")
            createFile(dbSrc, "AwesomeDatabase.kt", ModulesTemplate.generateAwesomeDatabase(model))
        }

        if (model.selectedCoreModules.contains("core:testing") || model.selectedCoreModules.contains("core/testing")) {
            val coreTestDir = File(rootDir, "core/testing")
            createFile(coreTestDir, "build.gradle.kts", ModulesTemplate.generateCoreTestingBuildGradle(model))
            val utilDir = File(coreTestDir, "src/main/kotlin/$pkgPath/core/testing/util")
            utilDir.mkdirs()
            createFile(utilDir, "MainDispatcherRule.kt", ModulesTemplate.generateMainDispatcherRule(model))
            val repoTestDir = File(coreTestDir, "src/main/kotlin/$pkgPath/core/testing/repository")
            repoTestDir.mkdirs()
            createFile(repoTestDir, "FakeItemRepository.kt", ModulesTemplate.generateFakeItemRepository(model))
            val tagsDir = File(coreTestDir, "src/main/kotlin/$pkgPath/core/testing/tags")
            tagsDir.mkdirs()
            createFile(tagsDir, "AwesomeTestTags.kt", ModulesTemplate.generateAwesomeTestTags(model))
        }

        // 6. Custom Feature Modules (:feature:<name>)
        for (featureName in model.featureModules) {
            val sanitized = WizardModel.sanitizeFeatureName(featureName)
            val pascal = WizardModel.toPascalCase(sanitized)

            val featureDir = File(rootDir, "feature/$sanitized")
            createFile(featureDir, "build.gradle.kts", ModulesTemplate.generateFeatureBuildGradle(model, sanitized))

            val featureSrc = File(featureDir, "src/main/kotlin/$pkgPath/feature/$sanitized")
            featureSrc.mkdirs()
            createFile(featureSrc, "${pascal}Screen.kt", ModulesTemplate.generateFeatureScreen(model, sanitized))
            createFile(featureSrc, "${pascal}ViewModel.kt", ModulesTemplate.generateFeatureViewModel(model, sanitized))
        }

        // 7. DevOps & CI/CD Tooling
        if (model.generateCiWorkflow) {
            val workflowsDir = File(rootDir, ".github/workflows")
            createFile(workflowsDir, "android-ci.yml", com.developer.awesomeandroidwizard.generator.templates.DevOpsTemplate.generateGitHubActionsWorkflow(model))
        }

        if (model.generateDependabot) {
            val githubDir = File(rootDir, ".github")
            createFile(githubDir, "dependabot.yml", com.developer.awesomeandroidwizard.generator.templates.DevOpsTemplate.generateDependabot())
        }

        if (model.generateDetekt) {
            val detektDir = File(rootDir, "config/detekt")
            createFile(detektDir, "detekt.yml", com.developer.awesomeandroidwizard.generator.templates.DevOpsTemplate.generateDetektConfig())
        }

        return rootDir
    }

    private fun createFile(directory: File, fileName: String, content: String) {
        directory.mkdirs()
        val file = File(directory, fileName)
        file.writeText(content)
    }

    private fun copyWrapperFiles(rootDir: File) {
        val candidates = listOf(
            File("/Users/prasadvennam/MY WORK/IDEA-PROJECTS/awesome-android-wizard"),
            File("/Users/prasadvennam/AndroidStudioProjects/nowinandroid")
        )
        for (candidate in candidates) {
            val gradlewSrc = File(candidate, "gradlew")
            val jarSrc = File(candidate, "gradle/wrapper/gradle-wrapper.jar")
            if (gradlewSrc.exists() && jarSrc.exists()) {
                val gradlewDest = File(rootDir, "gradlew")
                val gradlewBatDest = File(rootDir, "gradlew.bat")
                val jarDest = File(rootDir, "gradle/wrapper/gradle-wrapper.jar")

                gradlewSrc.copyTo(gradlewDest, overwrite = true)
                gradlewDest.setExecutable(true)

                val batSrc = File(candidate, "gradlew.bat")
                if (batSrc.exists()) {
                    batSrc.copyTo(gradlewBatDest, overwrite = true)
                }

                jarSrc.copyTo(jarDest, overwrite = true)
                break
            }
        }
    }

    private fun detectAndWriteLocalProperties(rootDir: File) {
        val userHome = System.getProperty("user.home")
        val candidates = listOfNotNull(
            System.getenv("ANDROID_HOME"),
            System.getenv("ANDROID_SDK_ROOT"),
            "$userHome/Library/Android/sdk",
            "$userHome/AppData/Local/Android/Sdk",
            "$userHome/Android/Sdk"
        )
        for (candidatePath in candidates) {
            val sdkDir = File(candidatePath)
            if (sdkDir.exists() && File(sdkDir, "platforms").exists()) {
                val localProps = File(rootDir, "local.properties")
                if (!localProps.exists()) {
                    localProps.writeText("sdk.dir=${sdkDir.absolutePath.replace("\\", "/")}\n")
                }
                break
            }
        }
    }
}
