package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import java.io.File

class MviSliceGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testScaffoldMviSliceWithHiltAndAllOptions() {
        val targetDir = tempFolder.newFolder("order")
        val testDir = tempFolder.newFolder("orderTest")

        val config = MviSliceGenerator.MviSliceConfig(
            sliceName = "OrderHistory",
            packageName = "com.example.app.feature.order",
            targetDir = targetDir,
            testDir = testDir,
            diFramework = DiFramework.HILT,
            includeNavigation = true,
            includeTest = true
        )

        val result = MviSliceGenerator.scaffoldMviSlice(config)

        assertTrue(result.success)
        assertEquals(5, result.generatedFiles.size)

        // 1. Contract
        val contractFile = File(targetDir, "OrderHistoryContract.kt")
        assertTrue(contractFile.exists())
        val contractText = contractFile.readText()
        assertTrue(contractText.contains("sealed interface OrderHistoryUiState"))
        assertTrue(contractText.contains("sealed interface OrderHistoryUiIntent"))
        assertTrue(contractText.contains("sealed interface OrderHistoryUiEffect"))

        // 2. ViewModel
        val viewModelFile = File(targetDir, "OrderHistoryViewModel.kt")
        assertTrue(viewModelFile.exists())
        val viewModelText = viewModelFile.readText()
        assertTrue(viewModelText.contains("@HiltViewModel"))
        assertTrue(viewModelText.contains("class OrderHistoryViewModel @Inject constructor()"))
        assertTrue(viewModelText.contains("val uiState: StateFlow<OrderHistoryUiState>"))
        assertTrue(viewModelText.contains("val uiEffect = _uiEffect.receiveAsFlow()"))

        // 3. Screen
        val screenFile = File(targetDir, "OrderHistoryScreen.kt")
        assertTrue(screenFile.exists())
        val screenText = screenFile.readText()
        assertTrue(screenText.contains("fun OrderHistoryScreen("))
        assertTrue(screenText.contains("fun OrderHistoryContent("))
        assertTrue(screenText.contains("hiltViewModel()"))

        // 4. Navigation
        val navFile = File(targetDir, "OrderHistoryNavigation.kt")
        assertTrue(navFile.exists())
        val navText = navFile.readText()
        assertTrue(navText.contains("data object OrderHistoryRoute"))
        assertTrue(navText.contains("fun NavController.navigateToOrderHistory("))
        assertTrue(navText.contains("fun NavGraphBuilder.orderHistoryScreen("))

        // 5. ViewModelTest
        val testFile = File(testDir, "OrderHistoryViewModelTest.kt")
        assertTrue(testFile.exists())
        val testText = testFile.readText()
        assertTrue(testText.contains("class OrderHistoryViewModelTest"))
        assertTrue(testText.contains("initialUiState_loadsSuccessContent"))
    }

    @Test
    fun testScaffoldMviSliceWithKoinAndNoNavigation() {
        val targetDir = tempFolder.newFolder("profile")

        val config = MviSliceGenerator.MviSliceConfig(
            sliceName = "UserProfile",
            packageName = "com.example.app.feature.profile",
            targetDir = targetDir,
            testDir = null,
            diFramework = DiFramework.KOIN,
            includeNavigation = false,
            includeTest = false
        )

        val result = MviSliceGenerator.scaffoldMviSlice(config)

        assertTrue(result.success)
        assertEquals(3, result.generatedFiles.size)

        val viewModelFile = File(targetDir, "UserProfileViewModel.kt")
        assertTrue(viewModelFile.exists())
        val viewModelText = viewModelFile.readText()
        org.junit.Assert.assertFalse(viewModelText.contains("@HiltViewModel"))
        assertTrue(viewModelText.contains("class UserProfileViewModel : ViewModel()"))

        val navFile = File(targetDir, "UserProfileNavigation.kt")
        org.junit.Assert.assertFalse(navFile.exists())
    }

    @Test
    fun testScaffoldMviSliceWithNavigation3AndDependencyAlignment() {
        val rootDir = tempFolder.newFolder("Nav3Project")
        File(rootDir, "settings.gradle.kts").writeText("""
            rootProject.name = "Nav3Project"
            include(":feature:profile")
        """.trimIndent())

        val gradleDir = File(rootDir, "gradle")
        gradleDir.mkdirs()
        val tomlFile = File(gradleDir, "libs.versions.toml")
        tomlFile.writeText("""
[versions]
kotlin = "2.4.21"

[libraries]
androidx-core = { group = "androidx.core", name = "core-ktx", version = "1.19.0" }

[plugins]
android-library = { id = "com.android.library", version = "9.4.1" }
        """.trimIndent())

        val featureDir = File(rootDir, "feature/profile")
        val featureSrcDir = File(featureDir, "src/main/kotlin/com/example/app/feature/profile")
        featureSrcDir.mkdirs()

        val featureBuild = File(featureDir, "build.gradle.kts")
        featureBuild.writeText("""
plugins {
    alias(libs.plugins.android.library)
}
dependencies {
    implementation(libs.androidx.core)
}
        """.trimIndent())

        val config = MviSliceGenerator.MviSliceConfig(
            sliceName = "UserProfile",
            packageName = "com.example.app.feature.profile",
            targetDir = featureSrcDir,
            testDir = null,
            diFramework = DiFramework.HILT,
            includeNavigation = true,
            navigationFramework = MviSliceGenerator.NavigationFramework.NAVIGATION_3,
            includeTest = false
        )

        val result = MviSliceGenerator.scaffoldMviSlice(config)
        assertTrue(result.success)

        // 1. Navigation 3 code check
        val navFile = File(featureSrcDir, "UserProfileNavigation.kt")
        assertTrue(navFile.exists())
        val navText = navFile.readText()
        assertTrue(navText.contains("import androidx.navigation3.Record"))
        assertTrue(navText.contains("data object UserProfileRoute"))
        assertTrue(navText.contains("fun userProfileRecord("))

        // 2. libs.versions.toml updated
        val updatedToml = tomlFile.readText()
        assertTrue(updatedToml.contains("navigation3 = \"1.0.0-alpha01\""))
        assertTrue(updatedToml.contains("androidx-navigation3-ui"))
        assertTrue(updatedToml.contains("androidx-navigation3-runtime"))

        // 3. feature/profile/build.gradle.kts updated
        val updatedBuild = featureBuild.readText()
        assertTrue(updatedBuild.contains("alias(libs.plugins.kotlin.serialization)"))
        assertTrue(updatedBuild.contains("implementation(libs.androidx.navigation3.ui)"))
        assertTrue(updatedBuild.contains("implementation(libs.androidx.navigation3.runtime)"))
        assertTrue(updatedBuild.contains("implementation(libs.kotlinx.serialization.json)"))
    }
}
