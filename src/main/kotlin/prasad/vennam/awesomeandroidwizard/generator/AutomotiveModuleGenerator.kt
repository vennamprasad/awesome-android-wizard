package prasad.vennam.awesomeandroidwizard.generator

import java.io.File

object AutomotiveModuleGenerator {

    data class AutomotiveGenerationResult(
        val success: Boolean,
        val automotiveDir: File,
        val message: String
    )

    fun scaffoldAutomotiveModule(projectRootDir: File, customPackage: String? = null): AutomotiveGenerationResult {
        val autoDir = File(projectRootDir, "automotive")
        if (autoDir.exists()) {
            return AutomotiveGenerationResult(false, autoDir, "Module ':automotive' already exists in this project.")
        }

        val basePackage = customPackage?.takeIf { it.isNotBlank() } ?: detectBasePackage(projectRootDir) ?: "com.example.app"
        val autoPackage = "$basePackage.automotive"
        val pkgPath = autoPackage.replace(".", "/")

        // 1. Create module directory
        autoDir.mkdirs()

        // 2. automotive/build.gradle.kts
        val buildFile = File(autoDir, "build.gradle.kts")
        val buildContent = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "$autoPackage"
    compileSdk = ${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.COMPILE_SDK}

    defaultConfig {
        applicationId = "$autoPackage"
        minSdk = ${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.AUTOMOTIVE_MIN_SDK}
        targetSdk = ${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.COMPILE_SDK}
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.car.app)
    implementation(libs.androidx.car.app.projected)
}
""".trimIndent()
        buildFile.writeText(buildContent)

        // 3. Proguard rules
        File(autoDir, "proguard-rules.pro").writeText("# Android Auto / Automotive Proguard Rules\n")

        // 4. Resources
        val resXmlDir = File(autoDir, "src/main/res/xml")
        resXmlDir.mkdirs()
        File(resXmlDir, "automotive_app_desc.xml").writeText("""
<?xml version="1.0" encoding="utf-8"?>
<automotiveApp>
    <uses name="media" />
</automotiveApp>
""".trimIndent())

        // 5. AndroidManifest.xml
        val mainDir = File(autoDir, "src/main")
        mainDir.mkdirs()
        File(mainDir, "AndroidManifest.xml").writeText("""
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-feature
        android:name="android.hardware.type.automotive"
        android:required="true" />

    <application
        android:allowBackup="true"
        android:label="Automotive Companion"
        android:theme="@android:style/Theme.DeviceDefault">

        <meta-data
            android:name="com.android.automotive"
            android:resource="@xml/automotive_app_desc" />

        <service
            android:name=".MainCarAppService"
            android:exported="true">
            <intent-filter>
                <action android:name="androidx.car.app.CarAppService" />
                <category android:name="androidx.car.app.category.IOT" />
            </intent-filter>
        </service>
    </application>
</manifest>
""".trimIndent())

        // 6. Source files
        val srcDir = File(mainDir, "kotlin/$pkgPath")
        srcDir.mkdirs()

        // MainCarAppService.kt
        File(srcDir, "MainCarAppService.kt").writeText("""
package $autoPackage

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class MainCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return MainCarSession()
    }
}
""".trimIndent())

        // MainCarSession.kt
        File(srcDir, "MainCarSession.kt").writeText("""
package $autoPackage

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class MainCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return MainCarScreen(carContext)
    }
}
""".trimIndent())

        // MainCarScreen.kt
        File(srcDir, "MainCarScreen.kt").writeText("""
package $autoPackage

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class MainCarScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val row = Row.Builder()
            .setTitle("Awesome Android Auto")
            .addText("Connected and running in vehicle console.")
            .build()

        val pane = Pane.Builder()
            .addRow(row)
            .addAction(
                Action.Builder()
                    .setTitle("Refresh")
                    .setOnClickListener { invalidate() }
                    .build()
            )
            .build()

        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.APP_ICON)
            .setTitle("Awesome Vehicle Hub")
            .build()
    }
}
""".trimIndent())

        // 7. Update settings.gradle.kts
        val settingsFile = File(projectRootDir, "settings.gradle.kts")
        if (settingsFile.exists()) {
            val settingsText = settingsFile.readText()
            val includeStatement = "include(\":automotive\")"
            if (!settingsText.contains(includeStatement)) {
                settingsFile.appendText("\n$includeStatement\n")
            }
        }

        return AutomotiveGenerationResult(
            success = true,
            automotiveDir = autoDir,
            message = "Android Auto / Automotive module ':automotive' successfully created!"
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
