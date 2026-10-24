package prasad.vennam.awesomeandroidwizard.generator

import prasad.vennam.awesomeandroidwizard.model.CoreModuleType
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import java.io.File

object CoreModuleGenerator {

    data class CoreGenerationResult(
        override val success: Boolean,
        val moduleDir: File,
        override val message: String
    ) : GeneratorResult

    fun scaffoldCoreModule(
        projectRootDir: File,
        rawCoreName: String,
        moduleType: CoreModuleType = CoreModuleType.ANDROID_LIBRARY
    ): CoreGenerationResult {
        val sanitized = sanitizeCoreName(rawCoreName)
        if (sanitized.isEmpty()) {
            return CoreGenerationResult(false, projectRootDir, "Invalid core module name.")
        }

        val basePackage = GeneratorUtils.detectBasePackage(projectRootDir) ?: "com.example.app"
        val pascal = WizardModel.toPascalCase(sanitized)
        val pkgPath = basePackage.replace(".", "/")
        val useConventionPlugins = GeneratorUtils.hasConventionPlugins(projectRootDir)

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
}

android {
    namespace = "$basePackage.core.$sanitized"
    compileSdk = ${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.COMPILE_SDK}
    defaultConfig { minSdk = ${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.MIN_SDK} }
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
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "$basePackage.core.$sanitized"
    compileSdk = ${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.COMPILE_SDK}
    defaultConfig { minSdk = ${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.MIN_SDK} }
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
        val classContent = """
package $basePackage.core.$sanitized

/**
 * Standard contract for :core:$sanitized module.
 */
interface ${pascal}Contract {
    fun getModuleName(): String = ":core:$sanitized"
}
""".trimIndent()
        GeneratorUtils.createFile(srcDir, "${pascal}Contract.kt", classContent)

        // 4. Update settings.gradle.kts
        GeneratorUtils.addModuleToSettingsGradle(projectRootDir, ":core:$sanitized")

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
}
