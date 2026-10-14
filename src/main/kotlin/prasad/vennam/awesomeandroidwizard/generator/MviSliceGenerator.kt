package prasad.vennam.awesomeandroidwizard.generator

import prasad.vennam.awesomeandroidwizard.model.DiFramework
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import java.io.File

object MviSliceGenerator {

    enum class NavigationFramework(val displayName: String) {
        NAV_COMPOSE("Navigation Compose (AndroidX Nav 2.8+)"),
        NAVIGATION_3("Navigation 3 (androidx.navigation3 Compose)"),
        NONE("None")
    }

    data class MviSliceConfig(
        val sliceName: String,
        val packageName: String,
        val targetDir: File,
        val testDir: File? = null,
        val diFramework: DiFramework = DiFramework.HILT,
        val includeNavigation: Boolean = true,
        val navigationFramework: NavigationFramework = NavigationFramework.NAV_COMPOSE,
        val includeTest: Boolean = true
    )

    data class MviGenerationResult(
        val success: Boolean,
        val generatedFiles: List<File>,
        val message: String
    )

    fun scaffoldMviSlice(config: MviSliceConfig): MviGenerationResult {
        val sanitized = WizardModel.sanitizeFeatureName(config.sliceName)
        if (sanitized.isEmpty()) {
            return MviGenerationResult(false, emptyList(), "Invalid feature slice name.")
        }

        val pascal = WizardModel.toPascalCase(config.sliceName)
        val filesCreated = mutableListOf<File>()

        if (!config.targetDir.exists()) {
            config.targetDir.mkdirs()
        }

        // 1. Contract.kt
        val contractFile = File(config.targetDir, "${pascal}Contract.kt")
        contractFile.writeText(generateContractContent(config.packageName, pascal))
        filesCreated.add(contractFile)

        // 2. ViewModel.kt
        val viewModelFile = File(config.targetDir, "${pascal}ViewModel.kt")
        viewModelFile.writeText(generateViewModelContent(config.packageName, pascal, config.diFramework))
        filesCreated.add(viewModelFile)

        // 3. Screen.kt
        val screenFile = File(config.targetDir, "${pascal}Screen.kt")
        screenFile.writeText(generateScreenContent(config.packageName, pascal, config.diFramework))
        filesCreated.add(screenFile)

        // 4. Navigation.kt
        if (config.includeNavigation && config.navigationFramework != NavigationFramework.NONE) {
            val navFile = File(config.targetDir, "${pascal}Navigation.kt")
            val navContent = when (config.navigationFramework) {
                NavigationFramework.NAVIGATION_3 -> generateNavigation3Content(config.packageName, pascal)
                NavigationFramework.NAV_COMPOSE -> generateNavigationContent(config.packageName, pascal)
                NavigationFramework.NONE -> ""
            }
            if (navContent.isNotBlank()) {
                navFile.writeText(navContent)
                filesCreated.add(navFile)
            }
        }

        // 5. ViewModelTest.kt
        if (config.includeTest && config.testDir != null) {
            if (!config.testDir.exists()) {
                config.testDir.mkdirs()
            }
            val testFile = File(config.testDir, "${pascal}ViewModelTest.kt")
            testFile.writeText(generateViewModelTestContent(config.packageName, pascal))
            filesCreated.add(testFile)
        }

        alignDependencies(
            targetDir = config.targetDir,
            navigationFramework = if (config.includeNavigation) config.navigationFramework else NavigationFramework.NONE,
            diFramework = config.diFramework,
            includeTest = config.includeTest
        )

        return MviGenerationResult(
            success = true,
            generatedFiles = filesCreated,
            message = "MVI Feature Slice '$pascal' created (${filesCreated.size} files generated)."
        )
    }

    fun generateContractContent(packageName: String, pascal: String): String = """
package $packageName

import androidx.compose.runtime.Immutable

/**
 * Immutable UI State for $pascal screen.
 */
@Immutable
sealed interface ${pascal}UiState {
    data object Loading : ${pascal}UiState

    data class Success(
        val items: List<String> = emptyList(),
        val isRefreshing: Boolean = false
    ) : ${pascal}UiState

    data class Error(
        val message: String
    ) : ${pascal}UiState
}

/**
 * User intents / actions dispatched to ${pascal}ViewModel.
 */
sealed interface ${pascal}UiIntent {
    data object LoadData : ${pascal}UiIntent
    data object Refresh : ${pascal}UiIntent
    data class ItemClicked(val id: String) : ${pascal}UiIntent
    data object Retry : ${pascal}UiIntent
}

/**
 * Single-shot side effects (navigation, snackbars, dialogues) for $pascal.
 */
sealed interface ${pascal}UiEffect {
    data class ShowSnackbar(val message: String) : ${pascal}UiEffect
    data class NavigateTo(val route: String) : ${pascal}UiEffect
}
""".trimIndent()

    fun generateViewModelContent(packageName: String, pascal: String, diFramework: DiFramework): String {
        val diImport = if (diFramework == DiFramework.HILT) {
            "import dagger.hilt.android.lifecycle.HiltViewModel\nimport javax.inject.Inject\n"
        } else ""

        val diAnnotation = if (diFramework == DiFramework.HILT) {
            "@HiltViewModel\nclass ${pascal}ViewModel @Inject constructor() : ViewModel()"
        } else {
            "class ${pascal}ViewModel : ViewModel()"
        }

        return """
package $packageName

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
$diImport
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

$diAnnotation {

    private val _uiState = MutableStateFlow<${pascal}UiState>(${pascal}UiState.Loading)
    val uiState: StateFlow<${pascal}UiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<${pascal}UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        onIntent(${pascal}UiIntent.LoadData)
    }

    fun onIntent(intent: ${pascal}UiIntent) {
        when (intent) {
            is ${pascal}UiIntent.LoadData -> loadContent()
            is ${pascal}UiIntent.Refresh -> refreshContent()
            is ${pascal}UiIntent.ItemClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(${pascal}UiEffect.ShowSnackbar("Selected: ${'$'}{intent.id}"))
                }
            }
            is ${pascal}UiIntent.Retry -> loadContent()
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            _uiState.value = ${pascal}UiState.Loading
            _uiState.value = ${pascal}UiState.Success(
                items = listOf(
                    "$pascal Item 1",
                    "$pascal Item 2",
                    "$pascal Item 3"
                )
            )
        }
    }

    private fun refreshContent() {
        val current = _uiState.value
        if (current is ${pascal}UiState.Success) {
            _uiState.value = current.copy(isRefreshing = true)
            viewModelScope.launch {
                _uiState.value = current.copy(isRefreshing = false)
                _uiEffect.send(${pascal}UiEffect.ShowSnackbar("Data updated successfully"))
            }
        }
    }
}
""".trimIndent()
    }

    fun generateScreenContent(packageName: String, pascal: String, diFramework: DiFramework): String {
        val vmImport = if (diFramework == DiFramework.HILT) {
            "import androidx.hilt.navigation.compose.hiltViewModel"
        } else {
            "import org.koin.androidx.compose.koinViewModel"
        }
        val defaultVm = if (diFramework == DiFramework.HILT) "hiltViewModel()" else "koinViewModel()"

        return """
package $packageName

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
$vmImport

/**
 * Stateful entry point for $pascal screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ${pascal}Screen(
    onNavigateBack: () -> Unit = {},
    viewModel: ${pascal}ViewModel = $defaultVm,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ${pascal}UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is ${pascal}UiEffect.NavigateTo -> { /* Handle navigation route */ }
            }
        }
    }

    ${pascal}Content(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Stateless pure content composable for $pascal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ${pascal}Content(
    uiState: ${pascal}UiState,
    onIntent: (${pascal}UiIntent) -> Unit,
    onNavigateBack: () -> Unit = {},
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$pascal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is ${pascal}UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ${pascal}UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { onIntent(${pascal}UiIntent.Retry) }) {
                            Text("Retry")
                        }
                    }
                }
                is ${pascal}UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items) { item ->
                            Card(
                                onClick = { onIntent(${pascal}UiIntent.ItemClicked(item)) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ${pascal}ContentSuccessPreview() {
    MaterialTheme {
        ${pascal}Content(
            uiState = ${pascal}UiState.Success(
                items = listOf("Sample Item A", "Sample Item B")
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ${pascal}ContentLoadingPreview() {
    MaterialTheme {
        ${pascal}Content(
            uiState = ${pascal}UiState.Loading,
            onIntent = {}
        )
    }
}
""".trimIndent()
    }

    fun generateNavigationContent(packageName: String, pascal: String): String {
        val funName = pascal.replaceFirstChar { it.lowercase() }
        return """
package $packageName

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation route for $pascal.
 */
@Serializable
data object ${pascal}Route

/**
 * Navigate to $pascal destination.
 */
fun NavController.navigateTo$pascal(navOptions: NavOptions? = null) {
    navigate(route = ${pascal}Route, navOptions = navOptions)
}

/**
 * Compose destination wiring for $pascal in NavGraphBuilder.
 */
fun NavGraphBuilder.${funName}Screen(
    onNavigateBack: () -> Unit = {}
) {
    composable<${pascal}Route> {
        ${pascal}Screen(onNavigateBack = onNavigateBack)
    }
}
""".trimIndent()
    }

    fun generateNavigation3Content(packageName: String, pascal: String): String {
        val funName = pascal.replaceFirstChar { it.lowercase() }
        return """
package $packageName

import androidx.navigation3.Record
import kotlinx.serialization.Serializable

/**
 * Navigation 3 type-safe route key for $pascal.
 */
@Serializable
data object ${pascal}Route

/**
 * Navigation 3 Record entry for $pascal.
 *
 * Example NavDisplay wiring:
 * ```kotlin
 * val backStack = remember { mutableStateListOf<Any>(${pascal}Route) }
 * NavDisplay(
 *     backstack = backStack,
 *     onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) }
 * ) { key ->
 *     when (key) {
 *         is ${pascal}Route -> ${funName}Record(onNavigateBack = { backStack.removeAt(backStack.size - 1) })
 *         else -> null
 *     }
 * }
 * ```
 */
fun ${funName}Record(
    onNavigateBack: () -> Unit = {}
): Record = Record(
    key = ${pascal}Route,
    content = {
        ${pascal}Screen(onNavigateBack = onNavigateBack)
    }
)
""".trimIndent()
    }

    fun alignDependencies(
        targetDir: File,
        navigationFramework: NavigationFramework = NavigationFramework.NONE,
        diFramework: DiFramework = DiFramework.KOIN,
        includeTest: Boolean = false
    ) {
        // 1. Locate module build.gradle.kts
        var curr: File? = targetDir
        var moduleBuildFile: File? = null
        while (curr != null) {
            val candidate = File(curr, "build.gradle.kts")
            if (candidate.exists()) {
                moduleBuildFile = candidate
                break
            }
            curr = curr.parentFile
        }

        // 2. Locate project root (containing settings.gradle.kts)
        var rootDir: File? = targetDir
        while (rootDir != null) {
            if (File(rootDir, "settings.gradle.kts").exists()) {
                break
            }
            rootDir = rootDir.parentFile
        }

        // 3. Ensure dependencies in gradle/libs.versions.toml
        if (rootDir != null) {
            val tomlFile = File(rootDir, "gradle/libs.versions.toml")
            if (tomlFile.exists()) {
                var toml = tomlFile.readText()
                var modified = false

                if (navigationFramework == NavigationFramework.NAVIGATION_3) {
                    if (!toml.contains("navigation3 =")) {
                        toml = toml.replaceFirst("[versions]", "[versions]\nnavigation3 = \"1.0.0-alpha01\"")
                        modified = true
                    }
                    if (!toml.contains("androidx-navigation3-ui =")) {
                        val libsInsert = "androidx-navigation3-runtime = { group = \"androidx.navigation3\", name = \"navigation3-runtime\", version.ref = \"navigation3\" }\nandroidx-navigation3-ui = { group = \"androidx.navigation3\", name = \"navigation3-ui\", version.ref = \"navigation3\" }"
                        toml = toml.replaceFirst("[libraries]", "[libraries]\n$libsInsert")
                        modified = true
                    }
                } else if (navigationFramework == NavigationFramework.NAV_COMPOSE) {
                    if (!toml.contains("navigationCompose =")) {
                        toml = toml.replaceFirst("[versions]", "[versions]\nnavigationCompose = \"2.10.2\"")
                        modified = true
                    }
                    if (!toml.contains("androidx-navigation-compose =")) {
                        toml = toml.replaceFirst(
                            "[libraries]",
                            "[libraries]\nandroidx-navigation-compose = { group = \"androidx.navigation\", name = \"navigation-compose\", version.ref = \"navigationCompose\" }"
                        )
                        modified = true
                    }
                }

                if (navigationFramework != NavigationFramework.NONE) {
                    if (!toml.contains("serialization =")) {
                        toml = toml.replaceFirst("[versions]", "[versions]\nserialization = \"1.11.0\"")
                        modified = true
                    }
                    if (!toml.contains("kotlinx-serialization-json =")) {
                        toml = toml.replaceFirst(
                            "[libraries]",
                            "[libraries]\nkotlinx-serialization-json = { group = \"org.jetbrains.kotlinx\", name = \"kotlinx-serialization-json\", version.ref = \"serialization\" }"
                        )
                        modified = true
                    }
                    if (!toml.contains("kotlin-serialization =")) {
                        if (toml.contains("[plugins]")) {
                            toml = toml.replaceFirst(
                                "[plugins]",
                                "[plugins]\nkotlin-serialization = { id = \"org.jetbrains.kotlin.plugin.serialization\", version.ref = \"kotlin\" }"
                            )
                            modified = true
                        }
                    }
                }

                if (diFramework == DiFramework.KOIN) {
                    if (!toml.contains("koin-androidx-compose =")) {
                        if (!toml.contains("koin =")) {
                            toml = toml.replaceFirst("[versions]", "[versions]\nkoin = \"4.0.0\"")
                            modified = true
                        }
                        toml = toml.replaceFirst(
                            "[libraries]",
                            "[libraries]\nkoin-androidx-compose = { group = \"io.insert-koin\", name = \"koin-androidx-compose\", version.ref = \"koin\" }"
                        )
                        modified = true
                    }
                }

                if (includeTest) {
                    if (!toml.contains("turbine =")) {
                        if (!toml.contains("turbine = \"")) {
                            toml = toml.replaceFirst("[versions]", "[versions]\nturbine = \"1.2.1\"")
                            modified = true
                        }
                        toml = toml.replaceFirst(
                            "[libraries]",
                            "[libraries]\nturbine = { group = \"app.cash.turbine\", name = \"turbine\", version.ref = \"turbine\" }"
                        )
                        modified = true
                    }
                }

                if (modified) {
                    tomlFile.writeText(toml)
                }
            }
        }

        // 4. Ensure dependencies in target module build.gradle.kts
        if (moduleBuildFile != null && moduleBuildFile.exists()) {
            var buildText = moduleBuildFile.readText()
            var modified = false

            // Ensure serialization plugin if navigation is included
            if (navigationFramework != NavigationFramework.NONE) {
                if (!buildText.contains("kotlin.serialization") && !buildText.contains("kotlin(\"plugin.serialization\")")) {
                    if (buildText.contains("plugins {")) {
                        buildText = buildText.replaceFirst("plugins {", "plugins {\n    alias(libs.plugins.kotlin.serialization)")
                        modified = true
                    }
                }

                // Ensure navigation dependencies
                if (navigationFramework == NavigationFramework.NAVIGATION_3) {
                    if (!buildText.contains("libs.androidx.navigation3.ui")) {
                        if (buildText.contains("dependencies {")) {
                            buildText = buildText.replaceFirst(
                                "dependencies {",
                                "dependencies {\n    implementation(libs.androidx.navigation3.ui)\n    implementation(libs.androidx.navigation3.runtime)"
                            )
                            modified = true
                        }
                    }
                } else if (navigationFramework == NavigationFramework.NAV_COMPOSE) {
                    if (!buildText.contains("libs.androidx.navigation.compose")) {
                        if (buildText.contains("dependencies {")) {
                            buildText = buildText.replaceFirst(
                                "dependencies {",
                                "dependencies {\n    implementation(libs.androidx.navigation.compose)"
                            )
                            modified = true
                        }
                    }
                }

                // Ensure serialization json dependency
                if (!buildText.contains("libs.kotlinx.serialization.json")) {
                    if (buildText.contains("dependencies {")) {
                        buildText = buildText.replaceFirst(
                            "dependencies {",
                            "dependencies {\n    implementation(libs.kotlinx.serialization.json)"
                        )
                        modified = true
                    }
                }
            }

            // Ensure DI compose dependency
            if (diFramework == DiFramework.KOIN && !buildText.contains("koin.androidx.compose")) {
                if (buildText.contains("dependencies {")) {
                    buildText = buildText.replaceFirst(
                        "dependencies {",
                        "dependencies {\n    implementation(libs.koin.androidx.compose)"
                    )
                    modified = true
                }
            }

            // Ensure test dependencies
            if (includeTest && !buildText.contains(":core:testing") && !buildText.contains("turbine")) {
                if (buildText.contains("dependencies {")) {
                    buildText = buildText.replaceFirst(
                        "dependencies {",
                        "dependencies {\n    testImplementation(libs.junit)\n    testImplementation(libs.kotlinx.coroutines.test)\n    testImplementation(libs.turbine)"
                    )
                    modified = true
                }
            }

            if (modified) {
                moduleBuildFile.writeText(buildText)
            }
        }
    }

    fun alignNavigationDependencies(targetDir: File, navigationFramework: NavigationFramework) {
        alignDependencies(targetDir, navigationFramework)
    }

    fun generateViewModelTestContent(packageName: String, pascal: String): String = """
package $packageName

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ${pascal}ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiState_loadsSuccessContent() = runTest(testDispatcher) {
        val viewModel = ${pascal}ViewModel()

        viewModel.uiState.test {
            val first = awaitItem()
            if (first is ${pascal}UiState.Loading) {
                val success = awaitItem()
                assertTrue(success is ${pascal}UiState.Success)
            } else {
                assertTrue(first is ${pascal}UiState.Success)
            }
        }
    }

    @Test
    fun itemClickedIntent_emitsShowSnackbarEffect() = runTest(testDispatcher) {
        val viewModel = ${pascal}ViewModel()

        viewModel.uiEffect.test {
            viewModel.onIntent(${pascal}UiIntent.ItemClicked("test-item"))
            val effect = awaitItem()
            assertTrue(effect is ${pascal}UiEffect.ShowSnackbar)
            assertEquals("Selected: test-item", (effect as ${pascal}UiEffect.ShowSnackbar).message)
        }
    }
}
""".trimIndent()
}
