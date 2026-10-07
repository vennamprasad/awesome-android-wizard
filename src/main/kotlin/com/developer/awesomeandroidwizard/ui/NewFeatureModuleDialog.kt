package com.developer.awesomeandroidwizard.ui

import com.developer.awesomeandroidwizard.generator.FeatureModuleGenerator
import com.developer.awesomeandroidwizard.model.DiFramework
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
import java.awt.Insets
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class NewFeatureModuleDialog(private val project: Project) : DialogWrapper(project, true) {

    private val featureNameField = JBTextField(16)
    private val suggestionsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2))

    init {
        title = "New Awesome Feature Module"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(480, 220)
            border = BorderFactory.createEmptyBorder(15, 20, 15, 20)
        }

        val formPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(6, 6, 6, 6)
            gridx = 0; gridy = 0; gridwidth = 2
        }

        formPanel.add(JBLabel("<html><h3>Add Feature Module to Project</h3><p color='#888888'>Scaffold a new decoupled Jetpack Compose feature with ViewModel & MVI state.</p></html>"), gbc)

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("Feature Name:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        featureNameField.emptyText.text = "e.g. auth, profile, settings"
        formPanel.add(featureNameField, gbc)

        // Quick suggestions
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2
        suggestionsPanel.add(JBLabel("<html><small color='#888888'>Suggestions:</small></html>"))
        val suggestions = listOf("auth", "profile", "settings", "search", "checkout", "details")
        for (s in suggestions) {
            val btn = JButton("+$s").apply {
                font = font.deriveFont(11.0f)
                margin = Insets(1, 6, 1, 6)
                addActionListener {
                    featureNameField.text = s
                }
            }
            suggestionsPanel.add(btn)
        }
        formPanel.add(suggestionsPanel, gbc)

        gbc.gridy = 3
        val infoLabel = JBLabel("<html><small color='#777777'>Will create <code>:feature:&lt;name&gt;</code> and register in <code>settings.gradle.kts</code> and <code>app/build.gradle.kts</code>.</small></html>")
        formPanel.add(infoLabel, gbc)

        root.add(formPanel, BorderLayout.CENTER)
        return root
    }

    override fun doOKAction() {
        val rawName = featureNameField.text.trim()
        if (rawName.isEmpty()) {
            Messages.showErrorDialog(project, "Please enter a valid feature name.", "Invalid Feature Name")
            return
        }

        val basePath = project.basePath ?: return
        val rootDir = File(basePath)

        val result = FeatureModuleGenerator.scaffoldFeature(
            projectRootDir = rootDir,
            rawFeatureName = rawName,
            diFramework = DiFramework.KOIN
        )

        if (result.success) {
            super.doOKAction()

            // Refresh Virtual File System so IDE displays the new directory immediately
            val virtualBase = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByIoFile(rootDir)
            if (virtualBase != null) {
                VfsUtil.markDirtyAndRefresh(true, true, true, virtualBase)
            }

            Messages.showInfoMessage(
                project,
                "${result.message}\n\nPlease sync Gradle to configure the new module.",
                "Feature Created"
            )
        } else {
            Messages.showErrorDialog(project, result.message, "Feature Creation Failed")
        }
    }
}
