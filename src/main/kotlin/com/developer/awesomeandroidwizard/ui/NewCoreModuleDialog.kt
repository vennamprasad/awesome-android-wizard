package com.developer.awesomeandroidwizard.ui

import com.developer.awesomeandroidwizard.generator.CoreModuleGenerator
import com.developer.awesomeandroidwizard.model.CoreModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import java.io.File
import javax.swing.*

class NewCoreModuleDialog(private val project: Project) : DialogWrapper(project, true) {

    private val moduleNameField = JBTextField(16)
    private var selectedType: CoreModuleType = CoreModuleType.JVM_LIBRARY
    private val suggestionsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2))

    init {
        title = "New Awesome Core Module"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(520, 320)
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
            JBLabel("<html><h3>Add Core Module to Project</h3><p color='#888888'>Scaffold a new decoupled core submodule with convention plugins.</p></html>"),
            gbc
        )

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("Core Module Name:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        moduleNameField.emptyText.text = "e.g. analytics, storage, location"
        formPanel.add(moduleNameField, gbc)

        // Suggestions
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2
        suggestionsPanel.add(JBLabel("<html><small color='#888888'>Suggestions:</small></html>"))
        val suggestions = listOf("analytics", "storage", "location", "crypto", "auth-token", "notifications")
        for (s in suggestions) {
            val btn = JButton("+$s").apply {
                font = font.deriveFont(11.0f)
                margin = Insets(1, 6, 1, 6)
                addActionListener {
                    moduleNameField.text = s
                }
            }
            suggestionsPanel.add(btn)
        }
        formPanel.add(suggestionsPanel, gbc)

        // Module Type radio options
        gbc.gridy = 3
        val typeGroup = ButtonGroup()
        val typePanel = JPanel(GridLayout(0, 1, 3, 3))
        typePanel.border = BorderFactory.createTitledBorder("Module Type")

        for (type in CoreModuleType.values()) {
            val radio = JRadioButton("<html><b>${type.displayName}</b> - <small color='#888888'>${type.description}</small></html>")
            radio.isSelected = type == selectedType
            radio.addActionListener {
                selectedType = type
            }
            typeGroup.add(radio)
            typePanel.add(radio)
        }
        formPanel.add(typePanel, gbc)

        gbc.gridy = 4
        val infoLabel = JBLabel("<html><small color='#777777'>Will create <code>:core:&lt;name&gt;</code> and register in <code>settings.gradle.kts</code>.</small></html>")
        formPanel.add(infoLabel, gbc)

        root.add(formPanel, BorderLayout.CENTER)
        return root
    }

    override fun doOKAction() {
        val rawName = moduleNameField.text.trim()
        if (rawName.isEmpty()) {
            Messages.showErrorDialog(project, "Please enter a valid core module name.", "Invalid Module Name")
            return
        }

        val basePath = project.basePath ?: return
        val rootDir = File(basePath)

        val result = CoreModuleGenerator.scaffoldCoreModule(
            projectRootDir = rootDir,
            rawCoreName = rawName,
            moduleType = selectedType
        )

        if (result.success) {
            super.doOKAction()

            val virtualBase = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByIoFile(rootDir)
            if (virtualBase != null) {
                VfsUtil.markDirtyAndRefresh(true, true, true, virtualBase)
            }

            Messages.showInfoMessage(
                project,
                "${result.message}\n\nPlease click 'Sync Project with Gradle Files' to finish importing.",
                "Core Module Created"
            )
        } else {
            Messages.showErrorDialog(project, result.message, "Module Creation Failed")
        }
    }
}
