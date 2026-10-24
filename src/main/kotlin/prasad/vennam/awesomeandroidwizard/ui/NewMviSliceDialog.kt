package prasad.vennam.awesomeandroidwizard.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import prasad.vennam.awesomeandroidwizard.generator.GeneratorUtils
import prasad.vennam.awesomeandroidwizard.generator.MviSliceGenerator
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import java.awt.*
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class NewMviSliceDialog(
    private val project: Project,
    private val selectedFile: VirtualFile? = null
) : DialogWrapper(project, true) {

    private val sliceNameField = JBTextField(16)
    private val packageField = JBTextField(24)
    private val diCombo = ComboBox(DiFramework.entries.toTypedArray()).apply {
        selectedItem = DiFramework.HILT
    }
    private val navFrameworkCombo = ComboBox(MviSliceGenerator.NavigationFramework.entries.toTypedArray()).apply {
        selectedItem = MviSliceGenerator.NavigationFramework.NAVIGATION_3
        renderer = SimpleListCellRenderer.create("") { it?.displayName ?: "" }
    }
    private val testCheck = JBCheckBox("Include Turbine ViewModel Unit Test in src/test", true)
    private val suggestionsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2))

    init {
        title = "New Awesome MVI Feature Slice"
        detectInitialPackage()
        init()
    }

    private fun detectInitialPackage() {
        val basePath = project.basePath ?: return
        val rootDir = File(basePath)

        // 1. Detect DI Framework from libs.versions.toml or build files
        val tomlFile = File(rootDir, "gradle/libs.versions.toml")
        if (tomlFile.exists()) {
            val tomlText = tomlFile.readText()
            if (tomlText.contains("koin") && !tomlText.contains("hilt")) {
                diCombo.selectedItem = DiFramework.KOIN
            } else if (tomlText.contains("hilt")) {
                diCombo.selectedItem = DiFramework.HILT
            }
        }

        val path = selectedFile?.path ?: ""
        val kotlinMarker = "/src/main/kotlin/"
        val javaMarker = "/src/main/java/"

        when {
            path.contains(kotlinMarker) -> {
                val subPath = path.substringAfter(kotlinMarker)
                val cleanSubPath = if (selectedFile?.isDirectory == true) subPath else subPath.substringBeforeLast("/")
                packageField.text = cleanSubPath.replace("/", ".").trimEnd('.')
            }
            path.contains(javaMarker) -> {
                val subPath = path.substringAfter(javaMarker)
                val cleanSubPath = if (selectedFile?.isDirectory == true) subPath else subPath.substringBeforeLast("/")
                packageField.text = cleanSubPath.replace("/", ".").trimEnd('.')
            }
            else -> {
                val basePkg = GeneratorUtils.detectBasePackage(rootDir) ?: "com.example.app"
                // Check if feature modules exist
                val featDir = File(rootDir, "feature")
                val firstFeat = featDir.listFiles()?.firstOrNull { it.isDirectory }?.name
                if (firstFeat != null) {
                    packageField.text = "$basePkg.feature.$firstFeat"
                } else {
                    packageField.text = "$basePkg.feature"
                }
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(520, 310)
            border = BorderFactory.createEmptyBorder(15, 20, 15, 20)
        }

        val formPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 6, 5, 6)
            gridx = 0; gridy = 0; gridwidth = 2
        }

        formPanel.add(
            JBLabel("<html><h3>Create MVI Feature Slice</h3><p color='#888888'>Generates Contract, ViewModel with StateFlow & Effect Channel, Screen with Previews, and Navigation.</p></html>"),
            gbc
        )

        // 1. Slice Name
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("Slice Name:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        sliceNameField.emptyText.text = "e.g. OrderHistory, UserProfile"
        formPanel.add(sliceNameField, gbc)

        // Suggestions
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2
        suggestionsPanel.add(JBLabel("<html><small color='#888888'>Suggestions:</small></html>"))
        val suggestions = listOf("Feed", "Details", "Profile", "Checkout", "Cart", "Analytics")
        for (s in suggestions) {
            val btn = JButton("+$s").apply {
                font = font.deriveFont(11.0f)
                margin = Insets(1, 5, 1, 5)
                addActionListener {
                    sliceNameField.text = s
                }
            }
            suggestionsPanel.add(btn)
        }
        formPanel.add(suggestionsPanel, gbc)

        // 2. Target Package
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("Target Package:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(packageField, gbc)

        // 3. DI Framework
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("DI Framework:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(diCombo, gbc)

        // 4. Navigation
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("Navigation:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(navFrameworkCombo, gbc)

        // 5. Options
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2
        formPanel.add(testCheck, gbc)

        gbc.gridy = 7
        formPanel.add(
            JBLabel("<html><small color='gray'>Auto-aligns navigation & serialization in libs.versions.toml and target build.gradle.kts.</small></html>"),
            gbc
        )

        root.add(formPanel, BorderLayout.CENTER)
        return root
    }

    override fun doOKAction() {
        val sliceName = sliceNameField.text.trim()
        val pkg = packageField.text.trim()

        if (sliceName.isEmpty()) {
            Messages.showErrorDialog(project, "Please enter a slice name.", "Invalid Slice Name")
            return
        }
        if (pkg.isEmpty()) {
            Messages.showErrorDialog(project, "Please enter a target package.", "Invalid Package")
            return
        }

        val basePath = project.basePath ?: return
        val targetDir = resolveTargetDirectory(basePath, pkg)
        val testDir = resolveTestDirectory(basePath, pkg)
        val selectedNav = navFrameworkCombo.selectedItem as MviSliceGenerator.NavigationFramework

        val config = MviSliceGenerator.MviSliceConfig(
            sliceName = sliceName,
            packageName = pkg,
            targetDir = targetDir,
            testDir = testDir,
            diFramework = diCombo.selectedItem as DiFramework,
            includeNavigation = selectedNav != MviSliceGenerator.NavigationFramework.NONE,
            navigationFramework = selectedNav,
            includeTest = testCheck.isSelected
        )

        val result = MviSliceGenerator.scaffoldMviSlice(config)

        if (result.success) {
            super.doOKAction()

            val virtualBase = LocalFileSystem.getInstance().findFileByIoFile(File(basePath))
            if (virtualBase != null) {
                VfsUtil.markDirtyAndRefresh(true, true, true, virtualBase)
            }

            Messages.showInfoMessage(
                project,
                "${result.message}\n\nFiles placed in: ${targetDir.absolutePath}",
                "MVI Feature Slice Generated"
            )
        } else {
            Messages.showErrorDialog(project, result.message, "Scaffolding Failed")
        }
    }

    private fun resolveTargetDirectory(basePath: String, pkg: String): File {
        val relPkgPath = pkg.replace(".", "/")
        val selectedPath = selectedFile?.path ?: ""

        if (selectedPath.contains("/src/main/kotlin") || selectedPath.contains("/src/main/java")) {
            val rootModuleDir = if (selectedFile?.isDirectory == true) selectedFile.path else selectedFile?.parent?.path ?: basePath
            return File(rootModuleDir)
        }

        // Try to match an existing feature module: feature/<name>
        val rootDir = File(basePath)
        val featureDir = File(rootDir, "feature")
        if (featureDir.exists() && featureDir.isDirectory) {
            featureDir.listFiles()?.forEach { feat ->
                if (feat.isDirectory && pkg.contains("feature.${feat.name}")) {
                    return File(feat, "src/main/kotlin/$relPkgPath")
                }
            }
        }

        // Default to app module
        return File(basePath, "app/src/main/kotlin/$relPkgPath")
    }

    private fun resolveTestDirectory(basePath: String, pkg: String): File {
        val targetDir = resolveTargetDirectory(basePath, pkg)
        val path = targetDir.absolutePath
        if (path.contains("/src/main/kotlin/")) {
            return File(path.replace("/src/main/kotlin/", "/src/test/kotlin/"))
        }
        val relPkgPath = pkg.replace(".", "/")
        return File(basePath, "app/src/test/kotlin/$relPkgPath")
    }
}
