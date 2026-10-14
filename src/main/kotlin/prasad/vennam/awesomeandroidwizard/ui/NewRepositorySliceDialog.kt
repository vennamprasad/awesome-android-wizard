package prasad.vennam.awesomeandroidwizard.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import prasad.vennam.awesomeandroidwizard.generator.RepositorySliceGenerator
import prasad.vennam.awesomeandroidwizard.model.NetworkingFramework
import java.awt.*
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class NewRepositorySliceDialog(
    private val project: Project,
    private val selectedFile: VirtualFile? = null
) : DialogWrapper(project, true) {

    private val entityNameField = JBTextField(16)
    private val packageField = JBTextField(24)
    private val networkCombo = ComboBox(NetworkingFramework.entries.toTypedArray()).apply {
        selectedItem = NetworkingFramework.RETROFIT
    }
    private val fakeRepoCheck = JBCheckBox("Include Fake Repository for Testing (in :core:testing or local)", true)
    private val suggestionsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2))

    init {
        title = "New Awesome Repository Slice"
        detectInitialPackage()
        init()
    }

    private fun detectInitialPackage() {
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
                packageField.text = "com.example.app.core.data"
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(520, 290)
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
            JBLabel("<html><h3>Create Repository Slice</h3><p color='#888888'>Generates Domain Model, Network DTO & Mapper, Repository Contract, Implementation, and Fake.</p></html>"),
            gbc
        )

        // 1. Entity Name
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("Entity Name:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        entityNameField.emptyText.text = "e.g. Order, Product, Transaction"
        formPanel.add(entityNameField, gbc)

        // Suggestions
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2
        suggestionsPanel.add(JBLabel("<html><small color='#888888'>Suggestions:</small></html>"))
        val suggestions = listOf("Order", "Product", "User", "Transaction", "CartItem", "Notification")
        for (s in suggestions) {
            val btn = JButton("+$s").apply {
                font = font.deriveFont(11.0f)
                margin = Insets(1, 5, 1, 5)
                addActionListener {
                    entityNameField.text = s
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

        // 3. Networking
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("Networking:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(networkCombo, gbc)

        // 4. Options
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2
        formPanel.add(fakeRepoCheck, gbc)

        root.add(formPanel, BorderLayout.CENTER)
        return root
    }

    override fun doOKAction() {
        val entityName = entityNameField.text.trim()
        val pkg = packageField.text.trim()

        if (entityName.isEmpty()) {
            Messages.showErrorDialog(project, "Please enter an entity name.", "Invalid Entity Name")
            return
        }
        if (pkg.isEmpty()) {
            Messages.showErrorDialog(project, "Please enter a target package.", "Invalid Package")
            return
        }

        val basePath = project.basePath ?: return
        val targetDir = resolveTargetDirectory(basePath, pkg)

        val config = RepositorySliceGenerator.RepositorySliceConfig(
            entityName = entityName,
            packageName = pkg,
            targetDir = targetDir,
            networking = networkCombo.selectedItem as NetworkingFramework,
            includeFake = fakeRepoCheck.isSelected
        )

        val result = RepositorySliceGenerator.scaffoldRepositorySlice(config)

        if (result.success) {
            super.doOKAction()

            val virtualBase = LocalFileSystem.getInstance().findFileByIoFile(File(basePath))
            if (virtualBase != null) {
                VfsUtil.markDirtyAndRefresh(true, true, true, virtualBase)
            }

            Messages.showInfoMessage(
                project,
                "${result.message}\n\nFiles placed in: ${targetDir.absolutePath}",
                "Repository Slice Generated"
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

        // Try :core:data if exists, else app
        val coreData = File(basePath, "core/data/src/main/kotlin/$relPkgPath")
        if (File(basePath, "core/data").exists()) {
            return coreData
        }
        return File(basePath, "app/src/main/kotlin/$relPkgPath")
    }
}
