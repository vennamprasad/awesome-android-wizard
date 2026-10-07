package com.developer.awesomeandroidwizard.generator.templates

import com.developer.awesomeandroidwizard.model.DatabaseFramework
import com.developer.awesomeandroidwizard.model.DiFramework
import com.developer.awesomeandroidwizard.model.NetworkingFramework
import com.developer.awesomeandroidwizard.model.WizardModel

object VersionCatalogTemplate {
    fun generate(model: WizardModel): String {
        val sb = StringBuilder()
        sb.appendLine("""
[versions]
agp = "8.6.0"
kotlin = "2.0.21"
coreKtx = "1.13.1"
lifecycleRuntimeKtx = "2.8.6"
activityCompose = "1.9.3"
composeBom = "2024.10.00"
navigationCompose = "2.8.3"
coroutines = "1.9.0"
serialization = "1.7.3"
dataStore = "1.1.1"
""".trimIndent())

        if (model.diFramework == DiFramework.KOIN) {
            sb.appendLine("koin = \"4.0.0\"")
        } else {
            sb.appendLine("hilt = \"2.52\"")
            sb.appendLine("ksp = \"2.0.21-1.0.25\"")
        }

        if (model.networking == NetworkingFramework.KTOR) {
            sb.appendLine("ktor = \"3.0.0\"")
        } else {
            sb.appendLine("retrofit = \"2.11.0\"")
            sb.appendLine("okhttp = \"4.12.0\"")
        }

        val hasRoom = model.database == DatabaseFramework.ROOM || model.selectedCoreModules.any { it.contains("database") }
        if (hasRoom) {
            sb.appendLine("room = \"2.6.1\"")
            if (model.diFramework != DiFramework.HILT) {
                sb.appendLine("ksp = \"2.0.21-1.0.25\"")
            }
        }

        sb.appendLine("""
junit = "4.13.2"
junitVersion = "1.2.1"
espressoCore = "3.6.1"
turbine = "1.2.0"
mockk = "1.13.13"

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
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
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

# Build-logic plugins
android-gradlePlugin = { group = "com.android.tools.build", name = "gradle", version.ref = "agp" }
kotlin-gradlePlugin = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin" }
compose-compiler-gradlePlugin = { group = "org.jetbrains.kotlin", name = "compose-compiler-gradle-plugin", version.ref = "kotlin" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
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
