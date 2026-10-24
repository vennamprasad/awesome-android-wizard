package prasad.vennam.awesomeandroidwizard.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import prasad.vennam.awesomeandroidwizard.generator.GeneratorUtils
import prasad.vennam.awesomeandroidwizard.generator.WearModuleGenerator
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

class NewWearModuleDialog(private val project: Project) : DialogWrapper(project, true) {

    private val packageField = JBTextField(24)

    init {
        title = "New Awesome Wear OS Companion Module"
        detectPackage()
        init()
    }

    private fun detectPackage() {
        val basePath = project.basePath?.let { File(it) } ?: return
        packageField.text = GeneratorUtils.detectBasePackage(basePath) ?: "com.example.app"
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(520, 240)
            border = BorderFactory.createEmptyBorder(15, 20, 15, 20)
        }

        val formPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(6, 6, 6, 6)
            gridx = 0; gridy = 0; gridwidth = 2
        }

        formPanel.add(
            JBLabel("<html><b>Scaffold Wear OS Companion (:wear)</b><br>" +
                    "<span color='gray'>Generates Wear Compose Material 3 UI, TileService, and wires into settings.gradle.kts.</span></html>"),
            gbc
        )

        gbc.gridy++
        gbc.gridwidth = 1
        formPanel.add(JBLabel("Base Application Package:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        formPanel.add(packageField, gbc)

        gbc.gridx = 0
        gbc.gridy++
        gbc.gridwidth = 2
        gbc.weightx = 0.0
        formPanel.add(
            JBLabel("<html><small color='gray'>" +
                    "• Target SDK: 37, Min SDK: 30 (Wear OS 3+)<br>" +
                    "• Wear Compose M3 (AppScaffold, TimeText, ScreenScaffold)<br>" +
                    "• Glanceable watch face Tile with MainTileService" +
                    "</small></html>"),
            gbc
        )

        root.add(formPanel, BorderLayout.CENTER)
        return root
    }

    override fun doOKAction() {
        val basePath = project.basePath ?: return
        val rootDir = File(basePath)
        val customPkg = packageField.text.trim()

        val result = WearModuleGenerator.scaffoldWearModule(rootDir, customPkg)

        if (result.success) {
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(rootDir)?.let {
                VfsUtil.markDirtyAndRefresh(true, true, true, it)
            }
            Messages.showInfoMessage(
                project,
                "${result.message}\n\nSync Gradle to start building your Wear OS app!",
                "Wear OS Module Created"
            )
            super.doOKAction()
        } else {
            Messages.showErrorDialog(project, result.message, "Scaffolding Failed")
        }
    }
}
