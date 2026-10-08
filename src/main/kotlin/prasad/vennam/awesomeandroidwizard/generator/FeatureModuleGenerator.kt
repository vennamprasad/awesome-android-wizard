package prasad.vennam.awesomeandroidwizard.generator

import prasad.vennam.awesomeandroidwizard.generator.templates.ModulesTemplate
import prasad.vennam.awesomeandroidwizard.model.ArchitectureType
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import java.io.File

object FeatureModuleGenerator {

    data class FeatureGenerationResult(
        val success: Boolean,
        val featureDir: File,
        val message: String
    )

    fun scaffoldFeature(
        projectRootDir: File,
        rawFeatureName: String,
        diFramework: DiFramework = DiFramework.KOIN
    ): FeatureGenerationResult {
        val sanitized = WizardModel.sanitizeFeatureName(rawFeatureName)
        if (sanitized.isEmpty()) {
            return FeatureGenerationResult(false, projectRootDir, "Invalid feature name.")
        }

        val basePackage = detectBasePackage(projectRootDir) ?: "com.example.app"
        val pascal = WizardModel.toPascalCase(sanitized)
        val pkgPath = basePackage.replace(".", "/")

        val dummyModel = WizardModel(
            projectName = projectRootDir.name,
            packageName = basePackage,
            projectLocation = projectRootDir.absolutePath,
            architecture = ArchitectureType.MODULAR_CLEAN_MVI,
            diFramework = diFramework,
            useConventionPlugins = File(projectRootDir, "build-logic").exists()
        )

        // 1. Create module directory
        val featureDir = File(projectRootDir, "feature/$sanitized")
        if (featureDir.exists()) {
            return FeatureGenerationResult(false, featureDir, "Feature module ':feature:$sanitized' already exists.")
        }
        featureDir.mkdirs()

        // 2. Generate build.gradle.kts
        val buildFile = File(featureDir, "build.gradle.kts")
        buildFile.writeText(ModulesTemplate.generateFeatureBuildGradle(dummyModel, sanitized))

        // 3. Generate Screen and ViewModel
        val srcDir = File(featureDir, "src/main/kotlin/$pkgPath/feature/$sanitized")
        srcDir.mkdirs()
        File(srcDir, "${pascal}Screen.kt").writeText(ModulesTemplate.generateFeatureScreen(dummyModel, sanitized))
        File(srcDir, "${pascal}ViewModel.kt").writeText(ModulesTemplate.generateFeatureViewModel(dummyModel, sanitized))

        // 4. Update settings.gradle.kts
        val settingsFile = File(projectRootDir, "settings.gradle.kts")
        if (settingsFile.exists()) {
            val settingsText = settingsFile.readText()
            val includeStatement = "include(\":feature:$sanitized\")"
            if (!settingsText.contains(includeStatement)) {
                settingsFile.appendText("\n$includeStatement\n")
            }
        }

        // 5. Update app/build.gradle.kts dependencies block
        val appBuildFile = File(projectRootDir, "app/build.gradle.kts")
        if (appBuildFile.exists()) {
            val appBuildText = appBuildFile.readText()
            val depStatement = "    implementation(project(\":feature:$sanitized\"))"
            if (!appBuildText.contains(depStatement)) {
                val updatedText = if (appBuildText.contains("dependencies {")) {
                    appBuildText.replaceFirst("dependencies {", "dependencies {\n$depStatement")
                } else {
                    appBuildText + "\n\ndependencies {\n$depStatement\n}\n"
                }
                appBuildFile.writeText(updatedText)
            }
        }

        return FeatureGenerationResult(
            success = true,
            featureDir = featureDir,
            message = "Feature module ':feature:$sanitized' successfully generated and wired!"
        )
    }

    private fun detectBasePackage(rootDir: File): String? {
        val appBuildFile = File(rootDir, "app/build.gradle.kts")
        if (appBuildFile.exists()) {
            val content = appBuildFile.readText()
            val namespaceRegex = """namespace\s*=\s*["']([^"']+)["']""".toRegex()
            val match = namespaceRegex.find(content)
            if (match != null) {
                return match.groupValues[1]
            }
            val appIdRegex = """applicationId\s*=\s*["']([^"']+)["']""".toRegex()
            val appMatch = appIdRegex.find(content)
            if (appMatch != null) {
                return appMatch.groupValues[1]
            }
        }
        return null
    }
}
