package prasad.vennam.awesomeandroidwizard.generator

import java.io.File

object WearModuleGenerator {

    data class WearGenerationResult(
        override val success: Boolean,
        val wearDir: File,
        override val message: String
    ) : GeneratorResult

    fun scaffoldWearModule(projectRootDir: File, customPackage: String? = null): WearGenerationResult {
        val wearDir = File(projectRootDir, "wear")
        if (wearDir.exists()) {
            return WearGenerationResult(false, wearDir, "Module ':wear' already exists in this project.")
        }

        val basePackage = customPackage?.takeIf { it.isNotBlank() } ?: GeneratorUtils.detectBasePackage(projectRootDir) ?: "com.example.app"
        val wearPackage = "$basePackage.wear"
        val pkgPath = wearPackage.replace(".", "/")

        // 1. Create module directories
        wearDir.mkdirs()

        // 2. wear/build.gradle.kts
        val buildFile = File(wearDir, "build.gradle.kts")
        val buildContent = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "$wearPackage"
    compileSdk = ${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.COMPILE_SDK}

    defaultConfig {
        applicationId = "$wearPackage"
        minSdk = ${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.WEAR_MIN_SDK}
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation)
    implementation(libs.androidx.wear.tiles)
    implementation(libs.play.services.wearable)
    implementation(libs.guava)
}
""".trimIndent()
        buildFile.writeText(buildContent)
        ensureWearDependenciesInCatalog(projectRootDir)

        // 3. Proguard rules
        File(wearDir, "proguard-rules.pro").writeText("# Wear OS Proguard Rules\n")

        // 4. AndroidManifest.xml
        val mainDir = File(wearDir, "src/main")
        mainDir.mkdirs()
        File(mainDir, "AndroidManifest.xml").writeText("""
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-feature android:name="android.hardware.type.watch" />

    <application
        android:allowBackup="true"
        android:label="Wear Companion"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.DeviceDefault">
        
        <activity
            android:name=".presentation.MainActivity"
            android:exported="true"
            android:taskAffinity=""
            android:theme="@android:style/Theme.DeviceDefault">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".tile.MainTileService"
            android:exported="true"
            android:label="Quick Glance"
            android:permission="com.google.android.wearable.permission.BIND_TILE_PROVIDER">
            <intent-filter>
                <action android:name="androidx.wear.tiles.action.BIND_TILE_PROVIDER" />
            </intent-filter>
        </service>
    </application>
</manifest>
""".trimIndent())

        // 5. Source files
        val srcDir = File(mainDir, "kotlin/$pkgPath")
        val presentationDir = File(srcDir, "presentation")
        val themeDir = File(presentationDir, "theme")
        val tileDir = File(srcDir, "tile")
        presentationDir.mkdirs()
        themeDir.mkdirs()
        tileDir.mkdirs()

        // MainActivity.kt
        File(presentationDir, "MainActivity.kt").writeText("""
package $wearPackage.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import $wearPackage.presentation.theme.WearAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearAppTheme {
                AppScaffold {
                    TimeText()
                    WearHomeScreen()
                }
            }
        }
    }
}

@Composable
fun WearHomeScreen() {
    var count by remember { mutableIntStateOf(0) }

    ScreenScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Awesome Wear OS",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Taps: ${'$'}count",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { count++ },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Tap Me")
            }
        }
    }
}
""".trimIndent())

        // Theme.kt
        File(themeDir, "Theme.kt").writeText("""
package $wearPackage.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun WearAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}
""".trimIndent())

        // MainTileService.kt
        File(tileDir, "MainTileService.kt").writeText("""
package $wearPackage.tile

import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * Wear OS Tile providing glanceable metrics right from the watch face carousel.
 */
class MainTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        val tile = TileBuilders.Tile.Builder()
            .setFreshnessIntervalMillis(60 * 1000)
            .build()

        return Futures.immediateFuture(tile)
    }
}
""".trimIndent())

        // 6. Update settings.gradle.kts
        GeneratorUtils.addModuleToSettingsGradle(projectRootDir, ":wear")

        return WearGenerationResult(
            success = true,
            wearDir = wearDir,
            message = "Wear OS Companion module ':wear' successfully created!"
        )
    }

    private fun ensureWearDependenciesInCatalog(rootDir: File) {
        val tomlFile = File(rootDir, "gradle/libs.versions.toml")
        if (!tomlFile.exists()) return
        var toml = tomlFile.readText()

        if (!toml.contains("wearTiles")) {
            toml = toml.replaceFirst("[versions]", "[versions]\nwearTiles = \"${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.WEAR_TILES}\"")
        }
        if (!toml.contains("guava = \"") && !toml.contains("guava = '")) {
            toml = toml.replaceFirst("[versions]", "[versions]\nguava = \"${prasad.vennam.awesomeandroidwizard.config.AwesomeVersions.GUAVA}\"")
        }
        if (!toml.contains("androidx-wear-tiles")) {
            toml = toml.replaceFirst("[libraries]", "[libraries]\nandroidx-wear-tiles = { group = \"androidx.wear.tiles\", name = \"tiles\", version.ref = \"wearTiles\" }")
        }
        if (!toml.contains("guava = { group = \"com.google.guava\"")) {
            toml = toml.replaceFirst("[libraries]", "[libraries]\nguava = { group = \"com.google.guava\", name = \"guava\", version.ref = \"guava\" }")
        }
        tomlFile.writeText(toml)
    }
}
