package prasad.vennam.awesomeandroidwizard.ui.steps

import prasad.vennam.awesomeandroidwizard.model.ArchetypeBlueprint
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import java.io.File
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ProjectBasicsStep(private val model: WizardModel) : JPanel(BorderLayout()) {

    private val nameField = JBTextField(model.projectName)
    private val packageField = JBTextField(model.packageName)
    private val locationField = TextFieldWithBrowseButton()
    private val minSdkCombo = ComboBox(arrayOf(24, 26, 28, 30, 33, 34, 35))
    private val targetSdkCombo = ComboBox(arrayOf(34, 35))

    private val ciCheckBox = JBCheckBox("Scaffold GitHub Actions CI (.github/workflows)", model.generateCiWorkflow)
    private val detektCheckBox = JBCheckBox("Configure Detekt & Compose Lints", model.generateDetekt)

    init {
        border = BorderFactory.createEmptyBorder(15, 20, 15, 20)

        locationField.text = model.projectLocation
        val folderDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().apply {
            title = "Select Project Location"
            description = "Choose directory where the project will be created"
        }
        locationField.addBrowseFolderListener(TextBrowseFolderListener(folderDescriptor))

        minSdkCombo.selectedItem = model.minSdk
        targetSdkCombo.selectedItem = model.targetSdk

        setupListeners()

        val formPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
        }

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2
        formPanel.add(JBLabel("<html><h2>Project Basics & Archetype</h2><p color='#888888'>Choose an architecture blueprint and configure project identity</p></html>"), gbc)

        // 1. Archetype Blueprints
        gbc.gridy = 1
        val archetypePanel = JPanel(GridLayout(0, 1, 3, 3)).apply {
            border = BorderFactory.createTitledBorder("Architecture Blueprint (One-Click Setup)")
        }
        val group = ButtonGroup()
        for (arch in ArchetypeBlueprint.values()) {
            val radio = JRadioButton("<html><b>${arch.title}</b> &mdash; <small color='#888888'>${arch.subtitle}</small></html>")
            radio.isSelected = arch == model.archetype
            radio.addActionListener {
                model.applyArchetype(arch)
                ciCheckBox.isSelected = model.generateCiWorkflow
                detektCheckBox.isSelected = model.generateDetekt
            }
            group.add(radio)
            archetypePanel.add(radio)
        }
        formPanel.add(archetypePanel, gbc)

        // 2. Project Name
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("Application Name:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(nameField, gbc)

        // 3. Package Name
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0
        formPanel.add(JBLabel("Package Name:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(packageField, gbc)

        // 4. Location
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0
        formPanel.add(JBLabel("Save Location:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(locationField, gbc)

        // 5. Min SDK
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0
        formPanel.add(JBLabel("Minimum SDK:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(minSdkCombo, gbc)

        // 6. Target SDK
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0
        formPanel.add(JBLabel("Target / Compile SDK:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(targetSdkCombo, gbc)

        // 7. DevOps Checkboxes
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2
        val devOpsPanel = JPanel(GridLayout(1, 2, 4, 4))
        devOpsPanel.border = BorderFactory.createTitledBorder("DevOps & Code Quality")
        ciCheckBox.addActionListener { model.generateCiWorkflow = ciCheckBox.isSelected }
        detektCheckBox.addActionListener { model.generateDetekt = detektCheckBox.isSelected }
        devOpsPanel.add(ciCheckBox)
        devOpsPanel.add(detektCheckBox)
        formPanel.add(devOpsPanel, gbc)

        add(formPanel, BorderLayout.NORTH)
    }

    private fun setupListeners() {
        nameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = update()
            override fun removeUpdate(e: DocumentEvent?) = update()
            override fun changedUpdate(e: DocumentEvent?) = update()

            private fun update() {
                val name = nameField.text.trim()
                model.projectName = name
                val sanitized = name.lowercase().replace(" ", "")
                model.packageName = "com.example.$sanitized"
                packageField.text = model.packageName

                val parent = File(model.projectLocation).parent ?: (System.getProperty("user.home") + "/AndroidStudioProjects")
                model.projectLocation = "$parent/$name"
                locationField.text = model.projectLocation
            }
        })

        packageField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) { model.packageName = packageField.text.trim() }
            override fun removeUpdate(e: DocumentEvent?) { model.packageName = packageField.text.trim() }
            override fun changedUpdate(e: DocumentEvent?) { model.packageName = packageField.text.trim() }
        })

        locationField.textField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) { model.projectLocation = locationField.text.trim() }
            override fun removeUpdate(e: DocumentEvent?) { model.projectLocation = locationField.text.trim() }
            override fun changedUpdate(e: DocumentEvent?) { model.projectLocation = locationField.text.trim() }
        })

        minSdkCombo.addActionListener {
            model.minSdk = minSdkCombo.selectedItem as? Int ?: 26
        }

        targetSdkCombo.addActionListener {
            val target = targetSdkCombo.selectedItem as? Int ?: 35
            model.targetSdk = target
            model.compileSdk = target
        }
    }
}
