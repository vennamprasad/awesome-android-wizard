package prasad.vennam.awesomeandroidwizard.generator.templates

import prasad.vennam.awesomeandroidwizard.model.DatabaseFramework
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import prasad.vennam.awesomeandroidwizard.model.NetworkingFramework
import prasad.vennam.awesomeandroidwizard.model.WizardModel

object VersionCatalogTemplate {
    fun generate(model: WizardModel): String {
        val sb = StringBuilder()
        sb.appendLine("""
[versions]
agp = "9.4.1"
kotlin = "2.4.21"
coreKtx = "1.19.1"
lifecycleRuntimeKtx = "2.11.0"
activityCompose = "1.13.0"
composeBom = "2026.09.00"
navigationCompose = "2.10.2"
navigation3 = "1.0.0-alpha01"
coroutines = "1.11.0"
serialization = "1.11.0"
dataStore = "1.2.1"
""".trimIndent())

        if (model.diFramework == DiFramework.KOIN) {
            sb.appendLine("koin = \"4.0.0\"")
        } else {
            sb.appendLine("hilt = \"2.60.1\"")
            sb.appendLine("ksp = \"2.3.12\"")
        }

        if (model.networking == NetworkingFramework.KTOR) {
            sb.appendLine("ktor = \"3.0.0\"")
        } else {
            sb.appendLine("retrofit = \"3.0.0\"")
            sb.appendLine("okhttp = \"5.5.0\"")
        }

        val hasRoom = model.database == DatabaseFramework.ROOM || model.selectedCoreModules.any { it.contains("database") }
        if (hasRoom) {
            sb.appendLine("room = \"2.8.5\"")
            if (model.diFramework != DiFramework.HILT) {
                sb.appendLine("ksp = \"2.3.12\"")
            }
        }

        sb.appendLine("""
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
turbine = "1.2.1"
mockk = "1.14.11"
androidxBenchmark = "1.3.3"
androidxUiautomator = "2.3.0"
baselineprofile = "1.3.3"
wearCompose = "1.4.1"
playServicesWearable = "19.0.0"
carApp = "1.7.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "dataStore" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-navigation3-runtime = { group = "androidx.navigation3", name = "navigation3-runtime", version.ref = "navigation3" }
androidx-navigation3-ui = { group = "androidx.navigation3", name = "navigation3-ui", version.ref = "navigation3" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }
""".trimIndent())

        if (model.diFramework == DiFramework.KOIN) {
            sb.appendLine("""
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-androidx-compose = { group = "io.insert-koin", name = "koin-androidx-compose", version.ref = "koin" }
koin-core = { group = "io.insert-koin", name = "koin-core", version.ref = "koin" }
""".trimIndent())
        } else {
            sb.appendLine("""
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.4.0" }
""".trimIndent())
        }

        if (model.networking == NetworkingFramework.KTOR) {
            sb.appendLine("""
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { group = "io.ktor", name = "ktor-client-logging", version.ref = "ktor" }
""".trimIndent())
        } else {
            sb.appendLine("""
retrofit-core = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-kotlinx-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
""".trimIndent())
        }

        if (hasRoom) {
            sb.appendLine("""
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
""".trimIndent())
        }

        sb.appendLine("""
# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
androidx-benchmark-macro = { group = "androidx.benchmark", name = "benchmark-macro-junit4", version.ref = "androidxBenchmark" }
androidx-uiautomator = { group = "androidx.test.uiautomator", name = "uiautomator", version.ref = "androidxUiautomator" }

# Wear OS
androidx-wear-compose-material3 = { group = "androidx.wear.compose", name = "compose-material3", version.ref = "wearCompose" }
androidx-wear-compose-foundation = { group = "androidx.wear.compose", name = "compose-foundation", version.ref = "wearCompose" }
androidx-wear-compose-navigation = { group = "androidx.wear.compose", name = "compose-navigation", version.ref = "wearCompose" }
play-services-wearable = { group = "com.google.android.gms", name = "play-services-wearable", version.ref = "playServicesWearable" }

# Android Auto / Automotive
androidx-car-app = { group = "androidx.car.app", name = "app", version.ref = "carApp" }
androidx-car-app-projected = { group = "androidx.car.app", name = "app-projected", version.ref = "carApp" }

# Build-logic plugins
android-gradlePlugin = { group = "com.android.tools.build", name = "gradle", version.ref = "agp" }
kotlin-gradlePlugin = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin" }
compose-compiler-gradlePlugin = { group = "org.jetbrains.kotlin", name = "compose-compiler-gradle-plugin", version.ref = "kotlin" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
android-test = { id = "com.android.test", version.ref = "agp" }
androidx-baselineprofile = { id = "androidx.baselineprofile", version.ref = "baselineprofile" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
""".trimIndent())

        if (model.diFramework == DiFramework.HILT) {
            sb.appendLine("hilt = { id = \"com.google.dagger.hilt.android\", version.ref = \"hilt\" }")
            sb.appendLine("ksp = { id = \"com.google.devtools.ksp\", version.ref = \"ksp\" }")
        } else if (hasRoom) {
            sb.appendLine("ksp = { id = \"com.google.devtools.ksp\", version.ref = \"ksp\" }")
        }

        return sb.toString()
    }
}
