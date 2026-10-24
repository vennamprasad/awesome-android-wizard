package prasad.vennam.awesomeandroidwizard.generator

import prasad.vennam.awesomeandroidwizard.generator.templates.ModulesTemplate
import prasad.vennam.awesomeandroidwizard.model.ArchitectureType
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import java.io.File
object FeatureModuleGenerator {

    data class FeatureGenerationResult(
        override val success: Boolean,
        val featureDir: File,
        override val message: String
    ) : GeneratorResult

    fun scaffoldFeature(
        projectRootDir: File,
        rawFeatureName: String,
        diFramework: DiFramework? = null
    ): FeatureGenerationResult {
        val sanitized = WizardModel.sanitizeFeatureName(rawFeatureName)
        if (sanitized.isEmpty()) {
            return FeatureGenerationResult(false, projectRootDir, "Invalid feature name.")
        }

        val basePackage = GeneratorUtils.detectBasePackage(projectRootDir) ?: "com.example.app"
        val effectiveDi = diFramework ?: GeneratorUtils.detectDiFramework(projectRootDir)
        val pascal = WizardModel.toPascalCase(sanitized)
        val pkgPath = basePackage.replace(".", "/")

        val dummyModel = WizardModel(
            projectName = projectRootDir.name,
            packageName = basePackage,
            projectLocation = projectRootDir.absolutePath,
            architecture = ArchitectureType.MODULAR_CLEAN_MVI,
            diFramework = effectiveDi,
            useConventionPlugins = GeneratorUtils.hasConventionPlugins(projectRootDir)
        )

        // 1. Create module directory
        val featureDir = File(projectRootDir, "feature/$sanitized")
        if (featureDir.exists()) {
            return FeatureGenerationResult(false, featureDir, "Feature module ':feature:$sanitized' already exists.")
        }
        featureDir.mkdirs()

        // 2. Generate build.gradle.kts
        GeneratorUtils.createFile(featureDir, "build.gradle.kts", ModulesTemplate.generateFeatureBuildGradle(dummyModel, sanitized))

        // 3. Generate Contract, Screen, ViewModel, and Navigation
        val srcDir = File(featureDir, "src/main/kotlin/$pkgPath/feature/$sanitized")
        GeneratorUtils.createFile(srcDir, "${pascal}Contract.kt", ModulesTemplate.generateFeatureContract(dummyModel, sanitized))
        GeneratorUtils.createFile(srcDir, "${pascal}Screen.kt", ModulesTemplate.generateFeatureScreen(dummyModel, sanitized))
        GeneratorUtils.createFile(srcDir, "${pascal}ViewModel.kt", ModulesTemplate.generateFeatureViewModel(dummyModel, sanitized))
        GeneratorUtils.createFile(srcDir, "${pascal}Navigation.kt", ModulesTemplate.generateFeatureNavigation(dummyModel, sanitized))

        // 4. Update settings.gradle.kts
        GeneratorUtils.addModuleToSettingsGradle(projectRootDir, ":feature:$sanitized")

        // 5. Update app/build.gradle.kts dependencies block
        GeneratorUtils.addDependencyToAppBuildGradle(projectRootDir, "    implementation(project(\":feature:$sanitized\"))")

        return FeatureGenerationResult(
            success = true,
            featureDir = featureDir,
            message = "Feature module ':feature:$sanitized' successfully generated and wired!"
        )
    }
}
