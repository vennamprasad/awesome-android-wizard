package prasad.vennam.awesomeandroidwizard.generator.templates

import prasad.vennam.awesomeandroidwizard.model.DiFramework
import prasad.vennam.awesomeandroidwizard.model.NetworkingFramework
import prasad.vennam.awesomeandroidwizard.model.WizardModel

object ModulesTemplate {

    // -------------------------------------------------------------
    // :app module
    // -------------------------------------------------------------
    fun generateAppBuildGradle(model: WizardModel): String {
        val pluginsBlock = if (model.useConventionPlugins) {
            """
plugins {
    id("awesome.android.application")
    id("awesome.android.library.compose")
    alias(libs.plugins.kotlin.serialization)
${if (model.diFramework == DiFramework.HILT) "    alias(libs.plugins.hilt)\n    alias(libs.plugins.ksp)" else ""}
}

android {
    namespace = "${model.packageName}"
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}
"""
        } else {
            """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
${if (model.diFramework == DiFramework.HILT) "    alias(libs.plugins.hilt)\n    alias(libs.plugins.ksp)" else ""}
}

android {
    namespace = "${model.packageName}"
    compileSdk = ${model.compileSdk}

    defaultConfig {
        applicationId = "${model.packageName}"
        minSdk = ${model.minSdk}
        targetSdk = ${model.targetSdk}
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}
"""
        }

        val dependenciesBlock = StringBuilder("""
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Submodules
""")
        for (mod in model.getAllSelectedModules()) {
            if (mod == "core:testing" || mod == "core/testing") {
                dependenciesBlock.appendLine("    testImplementation(project(\":${mod.replace("/", ":")}\"))")
            } else {
                dependenciesBlock.appendLine("    implementation(project(\":${mod.replace("/", ":")}\"))")
            }
        }

        if (model.diFramework == DiFramework.KOIN) {
            dependenciesBlock.appendLine("    implementation(libs.koin.android)")
            dependenciesBlock.appendLine("    implementation(libs.koin.androidx.compose)")
        } else {
            dependenciesBlock.appendLine("    implementation(libs.hilt.android)")
            dependenciesBlock.appendLine("    ksp(libs.hilt.compiler)")
            dependenciesBlock.appendLine("    implementation(libs.androidx.hilt.navigation.compose)")
        }

        dependenciesBlock.appendLine("""
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
""")
        return pluginsBlock.trimIndent() + "\n" + dependenciesBlock.toString().trimIndent()
    }

    fun generateAppManifest(model: WizardModel): String = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".AwesomeApplication"
        android:allowBackup="true"
        android:icon="@android:drawable/sym_def_app_icon"
        android:label="${model.projectName}"
        android:roundIcon="@android:drawable/sym_def_app_icon"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
""".trimIndent()

    fun generateApplicationClass(model: WizardModel): String {
        return if (model.diFramework == DiFramework.KOIN) {
            """
package ${model.packageName}

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AwesomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@AwesomeApplication)
            // Register app and feature modules
        }
    }
}
""".trimIndent()
        } else {
            """
package ${model.packageName}

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AwesomeApplication : Application()
""".trimIndent()
        }
    }

    fun generateMainActivity(model: WizardModel): String {
        val hiltAnnotation = if (model.diFramework == DiFramework.HILT) "@AndroidEntryPoint\n" else ""
        val features = model.featureModules.map { WizardModel.sanitizeFeatureName(it) }
        val imports = StringBuilder()
        features.forEach { feat ->
            val pascal = WizardModel.toPascalCase(feat)
            imports.appendLine("import ${model.packageName}.feature.$feat.${pascal}Screen")
        }

        val itemsData = features.mapIndexed { index, feat ->
            val pascal = WizardModel.toPascalCase(feat)
            val iconRef = when (feat.lowercase()) {
                "home" -> "AwesomeIcons.Home"
                "search" -> "AwesomeIcons.Search"
                "notifications" -> "AwesomeIcons.Notifications"
                "settings" -> "AwesomeIcons.Settings"
                "profile" -> "AwesomeIcons.Profile"
                else -> "AwesomeIcons.Logo"
            }
            "FeatureTab($index, \"$pascal\", $iconRef) { modifier -> ${pascal}Screen(modifier = modifier) }"
        }.joinToString(",\n                        ")

        return """
package ${model.packageName}

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ${model.packageName}.core.designsystem.theme.AwesomeTheme
import ${model.packageName}.core.designsystem.icon.AwesomeIcon
import ${model.packageName}.core.designsystem.icon.AwesomeIcons
import ${model.packageName}.core.designsystem.icon.AwesomeIconSource
$imports
${if (model.diFramework == DiFramework.HILT) "import dagger.hilt.android.AndroidEntryPoint" else ""}

private data class FeatureTab(
    val index: Int,
    val title: String,
    val icon: AwesomeIconSource,
    val content: @Composable (Modifier) -> Unit
)

${hiltAnnotation}class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AwesomeTheme {
                val tabs = remember {
                    listOf(
                        $itemsData
                    )
                }
                var selectedTabIndex by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (tabs.size > 1) {
                            NavigationBar {
                                tabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = selectedTabIndex == tab.index,
                                        onClick = { selectedTabIndex = tab.index },
                                        icon = { AwesomeIcon(icon = tab.icon, contentDescription = tab.title) },
                                        label = { Text(tab.title) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    val currentTab = tabs.getOrNull(selectedTabIndex) ?: tabs.first()
                    currentTab.content(Modifier.padding(innerPadding))
                }
            }
        }
    }
}
""".trimIndent()
    }


    // -------------------------------------------------------------
    // :core:ui
    // -------------------------------------------------------------
    fun generateCoreUiBuildGradle(model: WizardModel): String {
        return if (model.useConventionPlugins) {
            """
plugins {
    id("awesome.android.library")
    id("awesome.android.library.compose")
}

android {
    namespace = "${model.packageName}.core.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
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
    namespace = "${model.packageName}.core.ui"
    compileSdk = ${model.compileSdk}

    defaultConfig {
        minSdk = ${model.minSdk}
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
}
""".trimIndent()
        }
    }

    fun generateAwesomeComponents(model: WizardModel): String = """
package ${model.packageName}.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ${model.packageName}.core.designsystem.theme.AwesomeTheme

@Composable
fun AwesomeCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AwesomeTheme.radius.medium),
        elevation = CardDefaults.cardElevation(defaultElevation = AwesomeTheme.dimensions.cardElevation)
    ) {
        Column(modifier = Modifier.padding(AwesomeTheme.spacing.medium)) {
            Text(
                text = title,
                style = AwesomeTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(AwesomeTheme.spacing.extraSmall))
            Text(
                text = description,
                style = AwesomeTheme.typography.bodyMedium,
                color = AwesomeTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AwesomeLoadingWheel(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
""".trimIndent()

    // -------------------------------------------------------------
    // :core:designsystem (Theme, Typography, Colors, Resources, Icons, Tokens)
    // -------------------------------------------------------------
    fun generateCoreDesignSystemBuildGradle(model: WizardModel): String {
        return if (model.useConventionPlugins) {
            """
plugins {
    id("awesome.android.library")
    id("awesome.android.library.compose")
}

android {
    namespace = "${model.packageName}.core.designsystem"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
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
    namespace = "${model.packageName}.core.designsystem"
    compileSdk = ${model.compileSdk}

    defaultConfig {
        minSdk = ${model.minSdk}
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
}
""".trimIndent()
        }
    }

    fun generateDesignSystemSpacing(model: WizardModel): String = """
package ${model.packageName}.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard Design System Spacing Tokens (4dp/8dp harmonic grid system).
 */
@Immutable
data class Spacing(
    val default: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val mediumSmall: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val mediumLarge: Dp = 20.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val giant: Dp = 64.dp
)

/**
 * Standard Component Dimensions & Sizes Tokens (.dp standards).
 */
@Immutable
data class Dimensions(
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val iconExtraLarge: Dp = 48.dp,
    val minTouchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 48.dp,
    val cardElevation: Dp = 2.dp,
    val modalElevation: Dp = 6.dp,
    val dividerThickness: Dp = 1.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
val LocalDimensions = staticCompositionLocalOf { Dimensions() }
""".trimIndent()

    fun generateDesignSystemRadius(model: WizardModel): String = """
package ${model.packageName}.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard Corner Radius Tokens for consistent shape geometry across the project.
 */
@Immutable
data class CornerRadius(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 28.dp,
    val full: Dp = 9999.dp
)

val LocalRadius = staticCompositionLocalOf { CornerRadius() }

val AwesomeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
""".trimIndent()

    fun generateDesignSystemType(model: WizardModel): String = """
package ${model.packageName}.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Standard Typography Scale Tokens (.sp font sizes and line heights).
 */
object FontTokens {
    val DisplayLargeSize: TextUnit = 57.sp
    val DisplayLargeLineHeight: TextUnit = 64.sp

    val DisplayMediumSize: TextUnit = 45.sp
    val DisplayMediumLineHeight: TextUnit = 52.sp

    val DisplaySmallSize: TextUnit = 36.sp
    val DisplaySmallLineHeight: TextUnit = 44.sp

    val HeadlineLargeSize: TextUnit = 32.sp
    val HeadlineLargeLineHeight: TextUnit = 40.sp

    val HeadlineMediumSize: TextUnit = 28.sp
    val HeadlineMediumLineHeight: TextUnit = 36.sp

    val HeadlineSmallSize: TextUnit = 24.sp
    val HeadlineSmallLineHeight: TextUnit = 32.sp

    val TitleLargeSize: TextUnit = 22.sp
    val TitleLargeLineHeight: TextUnit = 28.sp

    val TitleMediumSize: TextUnit = 16.sp
    val TitleMediumLineHeight: TextUnit = 24.sp

    val TitleSmallSize: TextUnit = 14.sp
    val TitleSmallLineHeight: TextUnit = 20.sp

    val BodyLargeSize: TextUnit = 16.sp
    val BodyLargeLineHeight: TextUnit = 24.sp

    val BodyMediumSize: TextUnit = 14.sp
    val BodyMediumLineHeight: TextUnit = 20.sp

    val BodySmallSize: TextUnit = 12.sp
    val BodySmallLineHeight: TextUnit = 16.sp

    val LabelLargeSize: TextUnit = 14.sp
    val LabelLargeLineHeight: TextUnit = 20.sp

    val LabelMediumSize: TextUnit = 12.sp
    val LabelMediumLineHeight: TextUnit = 16.sp

    val LabelSmallSize: TextUnit = 11.sp
    val LabelSmallLineHeight: TextUnit = 16.sp
}

val AwesomeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = FontTokens.DisplayLargeSize,
        lineHeight = FontTokens.DisplayLargeLineHeight
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = FontTokens.HeadlineMediumSize,
        lineHeight = FontTokens.HeadlineMediumLineHeight
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = FontTokens.TitleLargeSize,
        lineHeight = FontTokens.TitleLargeLineHeight
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = FontTokens.TitleMediumSize,
        lineHeight = FontTokens.TitleMediumLineHeight
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = FontTokens.BodyLargeSize,
        lineHeight = FontTokens.BodyLargeLineHeight
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = FontTokens.BodyMediumSize,
        lineHeight = FontTokens.BodyMediumLineHeight
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = FontTokens.BodySmallSize,
        lineHeight = FontTokens.BodySmallLineHeight
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = FontTokens.LabelLargeSize,
        lineHeight = FontTokens.LabelLargeLineHeight
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = FontTokens.LabelMediumSize,
        lineHeight = FontTokens.LabelMediumLineHeight
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = FontTokens.LabelSmallSize,
        lineHeight = FontTokens.LabelSmallLineHeight
    )
)
""".trimIndent()

    fun generateDesignSystemColor(model: WizardModel): String = """
package ${model.packageName}.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Brand Palette
val PrimaryLight = Color(0xFF6750A4)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFEADDFF)
val OnPrimaryContainerLight = Color(0xFF21005D)

val SecondaryLight = Color(0xFF625B71)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE8DEF8)
val OnSecondaryContainerLight = Color(0xFF1D192B)

val TertiaryLight = Color(0xFF7D5260)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFD8E4)
val OnTertiaryContainerLight = Color(0xFF31111D)

val BackgroundLight = Color(0xFFFFFBFE)
val OnBackgroundLight = Color(0xFF1C1B1F)
val SurfaceLight = Color(0xFFFFFBFE)
val OnSurfaceLight = Color(0xFF1C1B1F)
val SurfaceVariantLight = Color(0xFFE7E0EC)
val OnSurfaceVariantLight = Color(0xFF49454F)
val OutlineLight = Color(0xFF79747E)

val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF381E72)
val PrimaryContainerDark = Color(0xFF4F378B)
val OnPrimaryContainerDark = Color(0xFFEADDFF)

val SecondaryDark = Color(0xFFCCC2DC)
val OnSecondaryDark = Color(0xFF332D41)
val SecondaryContainerDark = Color(0xFF4A4458)
val OnSecondaryContainerDark = Color(0xFFE8DEF8)

val TertiaryDark = Color(0xFFEFB8C8)
val OnTertiaryDark = Color(0xFF492532)
val TertiaryContainerDark = Color(0xFF633B48)
val OnTertiaryContainerDark = Color(0xFFFFD8E4)

val BackgroundDark = Color(0xFF141218)
val OnBackgroundDark = Color(0xFFE6E1E5)
val SurfaceDark = Color(0xFF141218)
val OnSurfaceDark = Color(0xFFE6E1E5)
val SurfaceVariantDark = Color(0xFF49454F)
val OnSurfaceVariantDark = Color(0xFFCAC4D0)
val OutlineDark = Color(0xFF938F99)

/**
 * Extended Semantic Colors (Success, Warning, Info).
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color
)

val ExtendedColorsLight = ExtendedColors(
    success = Color(0xFF2E7D32),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFC8E6C9),
    onSuccessContainer = Color(0xFF1B5E20),
    warning = Color(0xFFED6C02),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFE0B2),
    onWarningContainer = Color(0xFFE65100),
    info = Color(0xFF0288D1),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFB3E5FC),
    onInfoContainer = Color(0xFF01579B)
)

val ExtendedColorsDark = ExtendedColors(
    success = Color(0xFF81C784),
    onSuccess = Color(0xFF1B5E20),
    successContainer = Color(0xFF2E7D32),
    onSuccessContainer = Color(0xFFA5D6A7),
    warning = Color(0xFFFFB74D),
    onWarning = Color(0xFFE65100),
    warningContainer = Color(0xFFF57C00),
    onWarningContainer = Color(0xFFFFE0B2),
    info = Color(0xFF4FC3F7),
    onInfo = Color(0xFF01579B),
    infoContainer = Color(0xFF0288D1),
    onInfoContainer = Color(0xFFB3E5FC)
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColorsLight }

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)
""".trimIndent()

    fun generateDesignSystemTheme(model: WizardModel): String = """
package ${model.packageName}.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

@Composable
fun AwesomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) ExtendedColorsDark else ExtendedColorsLight

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalDimensions provides Dimensions(),
        LocalRadius provides CornerRadius(),
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AwesomeTypography,
            shapes = AwesomeShapes,
            content = content
        )
    }
}

/**
 * Accessor object for the design system tokens across Composables.
 * Usage:
 * - AwesomeTheme.spacing.medium
 * - AwesomeTheme.radius.large
 * - AwesomeTheme.dimensions.iconMedium
 * - AwesomeTheme.extendedColors.success
 * - AwesomeTheme.colorScheme.primary
 */
object AwesomeTheme {
    val colorScheme: ColorScheme
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes

    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current

    val dimensions: Dimensions
        @Composable @ReadOnlyComposable get() = LocalDimensions.current

    val radius: CornerRadius
        @Composable @ReadOnlyComposable get() = LocalRadius.current

    val extendedColors: ExtendedColors
        @Composable @ReadOnlyComposable get() = LocalExtendedColors.current
}
""".trimIndent()

    fun generateDesignSystemIcons(model: WizardModel): String = """
package ${model.packageName}.core.designsystem.icon

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import ${model.packageName}.core.designsystem.R

/**
 * Universal source representation for icons in the design system.
 */
sealed interface AwesomeIconSource {
    data class Vector(val imageVector: ImageVector) : AwesomeIconSource
    data class Resource(@DrawableRes val resId: Int) : AwesomeIconSource
}

/**
 * Standard Design System Icon Catalog.
 */
object AwesomeIcons {
    val Logo: AwesomeIconSource = AwesomeIconSource.Resource(R.drawable.ic_awesome_logo)
    val Home: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Home)
    val Search: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Search)
    val Settings: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Settings)
    val Profile: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Person)
    val Notifications: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Notifications)
    val Check: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Check)
    val Close: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Close)
    val Warning: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Warning)
    val Info: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Info)
    val Refresh: AwesomeIconSource = AwesomeIconSource.Vector(Icons.Default.Refresh)
    val ArrowBack: AwesomeIconSource = AwesomeIconSource.Vector(Icons.AutoMirrored.Filled.ArrowBack)
}

/**
 * Centralized, standardized Composable for rendering Design System icons.
 */
@Composable
fun AwesomeIcon(
    icon: AwesomeIconSource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    when (icon) {
        is AwesomeIconSource.Vector -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
        is AwesomeIconSource.Resource -> {
            Icon(
                painter = painterResource(id = icon.resId),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
    }
}
""".trimIndent()

    fun generateDesignSystemThemeConfig(model: WizardModel): String = """
package ${model.packageName}.core.designsystem.datastore

/**
 * Supported Theme Modes across the app.
 */
enum class DarkThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK
}

/**
 * User theme preferences state.
 */
data class UserThemePreferences(
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = true
)
""".trimIndent()

    fun generateDesignSystemThemeDataStore(model: WizardModel): String = """
package ${model.packageName}.core.designsystem.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

/**
 * Jetpack DataStore repository managing persistent user theme preferences.
 */
class ThemeDataStore(private val context: Context) {
    private object PreferencesKeys {
        val DARK_THEME_CONFIG = stringPreferencesKey("dark_theme_config")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    val userThemePreferences: Flow<UserThemePreferences> = context.themeDataStore.data.map { preferences ->
        val darkThemeConfigName = preferences[PreferencesKeys.DARK_THEME_CONFIG] ?: DarkThemeConfig.FOLLOW_SYSTEM.name
        val darkThemeConfig = try {
            DarkThemeConfig.valueOf(darkThemeConfigName)
        } catch (e: IllegalArgumentException) {
            DarkThemeConfig.FOLLOW_SYSTEM
        }
        val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
        UserThemePreferences(darkThemeConfig, dynamicColor)
    }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME_CONFIG] = darkThemeConfig.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }
}
""".trimIndent()

    fun generateDesignSystemLogoVector(): String = """
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="48"
    android:viewportHeight="48">
    <path
        android:fillColor="#6750A4"
        android:pathData="M24,4 L42,14 L42,34 L24,44 L6,34 L6,14 Z"/>
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M24,12 L34,28 L14,28 Z"/>
</vector>
""".trimIndent()


    // -------------------------------------------------------------
    // :core:model (Pure Kotlin JVM module)
    // -------------------------------------------------------------
    fun generateCoreModelBuildGradle(model: WizardModel): String {
        return if (model.useConventionPlugins) {
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

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
""".trimIndent()
        }
    }

    fun generateSampleModel(model: WizardModel): String = """
package ${model.packageName}.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
""".trimIndent()

    // -------------------------------------------------------------
    // :core:network
    // -------------------------------------------------------------
    fun generateCoreNetworkBuildGradle(model: WizardModel): String {
        val netDeps = if (model.networking == NetworkingFramework.KTOR) {
            """
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
"""
        } else {
            """
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging)
"""
        }
        return """
plugins {
    ${if (model.useConventionPlugins) "id(\"awesome.android.library\")" else "alias(libs.plugins.android.library)\n    alias(libs.plugins.kotlin.android)"}
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "${model.packageName}.core.network"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
$netDeps
}
""".trimIndent()
    }

    fun generateNetworkItem(model: WizardModel): String = """
package ${model.packageName}.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkItem(
    val id: String,
    val title: String,
    val description: String
)
""".trimIndent()

    fun generateNetworkResult(model: WizardModel): String = """
package ${model.packageName}.core.network.model

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : NetworkResult<Nothing>
    object Loading : NetworkResult<Nothing>
}
""".trimIndent()

    fun generateNetworkDataSource(model: WizardModel): String = """
package ${model.packageName}.core.network.datasource

import ${model.packageName}.core.network.model.NetworkItem

interface AwesomeNetworkDataSource {
    suspend fun getItems(): List<NetworkItem>
}

class FakeAwesomeNetworkDataSource : AwesomeNetworkDataSource {
    override suspend fun getItems(): List<NetworkItem> = listOf(
        NetworkItem("net-1", "Cloud Sync Verified", "Fetched via :core:network data source"),
        NetworkItem("net-2", "Architecture Certified", "Clean Architecture with MVI/MVVM pattern"),
        NetworkItem("net-3", "Performance Tuned", "Zero-overhead pure JVM and Android convention plugins")
    )
}
""".trimIndent()

    // -------------------------------------------------------------
    // :core:data
    // -------------------------------------------------------------
    fun generateCoreDataBuildGradle(model: WizardModel): String {
        return """
plugins {
    ${if (model.useConventionPlugins) "id(\"awesome.android.library\")" else "alias(libs.plugins.android.library)\n    alias(libs.plugins.kotlin.android)"}
}

android {
    namespace = "${model.packageName}.core.data"
}

dependencies {
    implementation(project(":core:model"))
    ${if (model.selectedCoreModules.contains("core:network") || model.selectedCoreModules.contains("core/network")) "implementation(project(\":core:network\"))" else ""}
    implementation(libs.kotlinx.coroutines.core)
}
""".trimIndent()
    }

    fun generateItemRepository(model: WizardModel): String = """
package ${model.packageName}.core.data.repository

import ${model.packageName}.core.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems(): Flow<List<Item>>

    companion object {
        fun create(): ItemRepository = DefaultItemRepository()
    }
}
""".trimIndent()

    fun generateDefaultItemRepository(model: WizardModel): String = """
package ${model.packageName}.core.data.repository

import ${model.packageName}.core.model.Item
import ${model.packageName}.core.network.datasource.AwesomeNetworkDataSource
import ${model.packageName}.core.network.datasource.FakeAwesomeNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class DefaultItemRepository(
    private val networkDataSource: AwesomeNetworkDataSource = FakeAwesomeNetworkDataSource()
) : ItemRepository {
    override fun getItems(): Flow<List<Item>> = flow {
        val networkItems = networkDataSource.getItems()
        emit(networkItems.map { Item(it.id, it.title, it.description) })
    }
}
""".trimIndent()

    // -------------------------------------------------------------
    // Generic :feature:<featureName> module
    // -------------------------------------------------------------
    fun generateFeatureBuildGradle(model: WizardModel, featureName: String): String {
        val sanitized = WizardModel.sanitizeFeatureName(featureName)
        return """
plugins {
    ${if (model.useConventionPlugins) "id(\"awesome.android.library\")\n    id(\"awesome.android.library.compose\")" else "alias(libs.plugins.android.library)\n    alias(libs.plugins.kotlin.android)\n    alias(libs.plugins.kotlin.compose)"}
${if (model.diFramework == DiFramework.HILT) "    alias(libs.plugins.hilt)\n    alias(libs.plugins.ksp)" else ""}
}

android {
    namespace = "${model.packageName}.feature.$sanitized"
    ${if (!model.useConventionPlugins) "compileSdk = ${model.compileSdk}\n    defaultConfig { minSdk = ${model.minSdk} }\n    buildFeatures { compose = true }" else ""}
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)

${if (model.diFramework == DiFramework.KOIN) "    implementation(libs.koin.androidx.compose)" else "    implementation(libs.hilt.android)\n    ksp(libs.hilt.compiler)\n    implementation(libs.androidx.hilt.navigation.compose)"}
}
""".trimIndent()
    }

    fun generateFeatureScreen(model: WizardModel, featureName: String): String {
        val sanitized = WizardModel.sanitizeFeatureName(featureName)
        val pascal = WizardModel.toPascalCase(sanitized)

        return """
package ${model.packageName}.feature.$sanitized

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import ${model.packageName}.core.designsystem.theme.AwesomeTheme
import ${model.packageName}.core.ui.components.AwesomeCard
import ${model.packageName}.core.ui.components.AwesomeLoadingWheel

@Composable
fun ${pascal}Screen(
    modifier: Modifier = Modifier,
    viewModel: ${pascal}ViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AwesomeTheme.spacing.medium)
    ) {
        Text(
            text = "🚀 Feature: $pascal",
            style = AwesomeTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Module: :feature:$sanitized | Clean Architecture Flow",
            style = AwesomeTheme.typography.bodyMedium,
            color = AwesomeTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = AwesomeTheme.spacing.small)
        )

        Spacer(modifier = Modifier.height(AwesomeTheme.spacing.medium))

        when (val state = uiState) {
            is ${pascal}UiState.Loading -> {
                AwesomeLoadingWheel()
            }
            is ${pascal}UiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(AwesomeTheme.spacing.small)) {
                    items(state.items) { item ->
                        AwesomeCard(title = item.title, description = item.description)
                    }
                }
            }
        }
    }
}
""".trimIndent()
    }

    fun generateFeatureViewModel(model: WizardModel, featureName: String): String {
        val sanitized = WizardModel.sanitizeFeatureName(featureName)
        val pascal = WizardModel.toPascalCase(sanitized)

        return """
package ${model.packageName}.feature.$sanitized

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ${model.packageName}.core.data.repository.ItemRepository
import ${model.packageName}.core.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ${pascal}UiState {
    object Loading : ${pascal}UiState
    data class Success(val items: List<Item>) : ${pascal}UiState
}

class ${pascal}ViewModel(
    private val repository: ItemRepository = ItemRepository.create()
) : ViewModel() {
    private val _uiState = MutableStateFlow<${pascal}UiState>(${pascal}UiState.Loading)
    val uiState: StateFlow<${pascal}UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getItems().collect { networkItems ->
                val featureItem = Item(
                    id = "feat-$sanitized",
                    title = "$pascal Active",
                    description = "Decoupled feature module :feature:$sanitized wired with :core:data repository"
                )
                _uiState.value = ${pascal}UiState.Success(listOf(featureItem) + networkItems)
            }
        }
    }
}
""".trimIndent()
    }

    // Backward-compatibility shortcuts
    fun generateFeatureHomeBuildGradle(model: WizardModel) = generateFeatureBuildGradle(model, "home")
    fun generateHomeScreen(model: WizardModel) = generateFeatureScreen(model, "home")
    fun generateHomeViewModel(model: WizardModel) = generateFeatureViewModel(model, "home")

    // -------------------------------------------------------------
    // :core:database (Room SQLite Persistence)
    // -------------------------------------------------------------
    fun generateCoreDatabaseBuildGradle(model: WizardModel): String {
        return """
plugins {
    ${if (model.useConventionPlugins) "id(\"awesome.android.library\")" else "alias(libs.plugins.android.library)\n    alias(libs.plugins.kotlin.android)"}
    alias(libs.plugins.ksp)
}

android {
    namespace = "${model.packageName}.core.database"
    ${if (!model.useConventionPlugins) "compileSdk = ${model.compileSdk}\n    defaultConfig { minSdk = ${model.minSdk} }" else ""}
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.core)
}
""".trimIndent()
    }

    fun generateItemEntity(model: WizardModel): String = """
package ${model.packageName}.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String
)
""".trimIndent()

    fun generateItemDao(model: WizardModel): String = """
package ${model.packageName}.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ${model.packageName}.core.database.model.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun observeItems(): Flow<List<ItemEntity>>

    @Upsert
    suspend fun upsertItems(items: List<ItemEntity>)

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
""".trimIndent()

    fun generateAwesomeDatabase(model: WizardModel): String = """
package ${model.packageName}.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ${model.packageName}.core.database.dao.ItemDao
import ${model.packageName}.core.database.model.ItemEntity

@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AwesomeDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: AwesomeDatabase? = null

        fun getInstance(context: Context): AwesomeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AwesomeDatabase::class.java,
                    "awesome_app.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
""".trimIndent()

    // -------------------------------------------------------------
    // :core:testing (Shared Test Rules, Fakes, Semantic Tags)
    // -------------------------------------------------------------
    fun generateCoreTestingBuildGradle(model: WizardModel): String {
        return """
plugins {
    ${if (model.useConventionPlugins) "id(\"awesome.android.library\")" else "alias(libs.plugins.android.library)\n    alias(libs.plugins.kotlin.android)"}
}

android {
    namespace = "${model.packageName}.core.testing"
    ${if (!model.useConventionPlugins) "compileSdk = ${model.compileSdk}\n    defaultConfig { minSdk = ${model.minSdk} }" else ""}
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.turbine)
    api(libs.mockk)
}
""".trimIndent()
    }

    fun generateMainDispatcherRule(model: WizardModel): String = """
package ${model.packageName}.core.testing.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Reusable JUnit Rule that overrides [Dispatchers.Main] with a [TestDispatcher]
 * for testing Kotlin Coroutines and StateFlows.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
""".trimIndent()

    fun generateFakeItemRepository(model: WizardModel): String = """
package ${model.packageName}.core.testing.repository

import ${model.packageName}.core.data.repository.ItemRepository
import ${model.packageName}.core.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of [ItemRepository] for testing ViewModels and UI screens.
 */
class FakeItemRepository : ItemRepository {
    private val itemsFlow = MutableStateFlow<List<Item>>(emptyList())

    override fun getItems(): Flow<List<Item>> = itemsFlow.asStateFlow()

    fun emitItems(items: List<Item>) {
        itemsFlow.value = items
    }
}
""".trimIndent()

    fun generateAwesomeTestTags(model: WizardModel): String = """
package ${model.packageName}.core.testing.tags

/**
 * Shared Compose Semantics test tags for automated UI testing and screenshot tests.
 */
object AwesomeTestTags {
    const val LOADING_WHEEL = "awesome:loading_wheel"
    const val ITEM_CARD = "awesome:item_card"
    const val NAVIGATION_BAR = "awesome:navigation_bar"
}
""".trimIndent()
}

