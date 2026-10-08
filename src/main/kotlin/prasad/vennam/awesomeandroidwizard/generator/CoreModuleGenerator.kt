package prasad.vennam.awesomeandroidwizard.generator

import prasad.vennam.awesomeandroidwizard.model.CoreModuleType
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import java.io.File

object CoreModuleGenerator {

    data class CoreGenerationResult(
        val success: Boolean,
        val moduleDir: File,
        val message: String
    )

    fun scaffoldCoreModule(
        projectRootDir: File,
        rawCoreName: String,
        moduleType: CoreModuleType = CoreModuleType.ANDROID_LIBRARY
    ): CoreGenerationResult {
        val sanitized = sanitizeCoreName(rawCoreName)
        if (sanitized.isEmpty()) {
            return CoreGenerationResult(false, projectRootDir, "Invalid core module name.")
        }

        val basePackage = detectBasePackage(projectRootDir) ?: "com.example.app"
        val pascal = WizardModel.toPascalCase(sanitized)
        val pkgPath = basePackage.replace(".", "/")
        val useConventionPlugins = File(projectRootDir, "build-logic").exists()

        // 1. Create directory
        val moduleDir = File(projectRootDir, "core/$sanitized")
        if (moduleDir.exists()) {
            return CoreGenerationResult(false, moduleDir, "Core module ':core:$sanitized' already exists.")
        }
        moduleDir.mkdirs()

        // 2. Generate build.gradle.kts
        val buildFile = File(moduleDir, "build.gradle.kts")
        val buildContent = when (moduleType) {
            CoreModuleType.JVM_LIBRARY -> {
                if (useConventionPlugins) {
                    """
plugins {
    id("awesome.jvm.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
""".trimIndent()
                } else {
                    """
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
""".trimIndent()
                }
            }
            CoreModuleType.ANDROID_LIBRARY -> {
                if (useConventionPlugins) {
                    """
plugins {
    id("awesome.android.library")
}

android {
    namespace = "$basePackage.core.$sanitized"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
}
""".trimIndent()
                } else {
                    """
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "$basePackage.core.$sanitized"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
}
""".trimIndent()
                }
            }
            CoreModuleType.COMPOSE_LIBRARY -> {
                if (useConventionPlugins) {
                    """
plugins {
    id("awesome.android.library")
    id("awesome.android.library.compose")
}

android {
    namespace = "$basePackage.core.$sanitized"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
}
""".trimIndent()
                } else {
                    """
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "$basePackage.core.$sanitized"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
}
""".trimIndent()
                }
            }
        }
        buildFile.writeText(buildContent)

        // 3. Generate sample source class
        val srcDir = File(moduleDir, "src/main/kotlin/$pkgPath/core/$sanitized")
        srcDir.mkdirs()
        val classContent = """
package $basePackage.core.$sanitized

/**
 * Standard contract for :core:$sanitized module.
 */
interface ${pascal}Contract {
    fun getModuleName(): String = ":core:$sanitized"
}
""".trimIndent()
        File(srcDir, "${pascal}Contract.kt").writeText(classContent)

        // 4. Update settings.gradle.kts
        val settingsFile = File(projectRootDir, "settings.gradle.kts")
        if (settingsFile.exists()) {
            val settingsText = settingsFile.readText()
            val includeStatement = "include(\":core:$sanitized\")"
            if (!settingsText.contains(includeStatement)) {
                settingsFile.appendText("\n$includeStatement\n")
            }
        }

        return CoreGenerationResult(
            success = true,
            moduleDir = moduleDir,
            message = "Core module ':core:$sanitized' (${moduleType.displayName}) successfully scaffolded!"
        )
    }

    fun sanitizeCoreName(raw: String): String {
        return raw.trim()
            .removePrefix("core:")
            .removePrefix(":")
            .lowercase()
            .replace("[^a-z0-9_-]".toRegex(), "")
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
