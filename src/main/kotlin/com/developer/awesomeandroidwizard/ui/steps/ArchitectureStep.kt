package com.developer.awesomeandroidwizard.ui.steps

import com.developer.awesomeandroidwizard.model.ArchitectureType
import com.developer.awesomeandroidwizard.model.WizardModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JRadioButton

class ArchitectureStep(private val model: WizardModel) : JPanel(BorderLayout()) {

    private val coreModuleCheckboxes = mutableMapOf<String, JBCheckBox>()
    private val conventionPluginsBox = JBCheckBox("Use Gradle Convention Plugins (build-logic)", model.useConventionPlugins)

    // Dynamic Feature Module UI
    private val newFeatureField = JBTextField(14)
    private val addFeatureButton = JButton("Add Feature")
    private val featuresContainer = JPanel(FlowLayout(FlowLayout.LEFT, 6, 6))

    init {
        border = BorderFactory.createEmptyBorder(15, 20, 15, 20)

        val mainPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
            gridx = 0; gridy = 0; gridwidth = 2
        }

        mainPanel.add(JBLabel("<html><h2>Architecture & Modularization</h2><p color='#888888'>Configure module structure and custom feature names</p></html>"), gbc)

        // 1. Architecture Radio options
        gbc.gridy = 1
        val archGroup = ButtonGroup()
        val archPanel = JPanel(GridLayout(0, 1, 3, 3))
        archPanel.border = BorderFactory.createTitledBorder("Architecture Pattern")

        for (arch in ArchitectureType.values()) {
            val radio = JRadioButton("<html><b>${arch.displayName}</b> - <small color='#888888'>${arch.description}</small></html>")
            radio.isSelected = arch == model.architecture
            radio.addActionListener {
                model.architecture = arch
                updateModulesForArchitecture(arch)
            }
            archGroup.add(radio)
            archPanel.add(radio)
        }
        mainPanel.add(archPanel, gbc)

        // 2. Convention plugins checkbox
        gbc.gridy = 2
        conventionPluginsBox.addActionListener {
            model.useConventionPlugins = conventionPluginsBox.isSelected
        }
        mainPanel.add(conventionPluginsBox, gbc)

        // 3. Core Modules selector
        gbc.gridy = 3
        val corePanel = JPanel(GridLayout(0, 2, 6, 4))
        corePanel.border = BorderFactory.createTitledBorder("Core Modules")

        val availableCore = listOf(
            "core:model" to "Domain & Data Models",
            "core:data" to "Repository & Data Sources",
            "core:network" to "HTTP & API Client",
            "core:database" to "Room SQLite Database & DAOs",
            "core:ui" to "Reusable UI & Theming",
            "core:designsystem" to "Design Tokens & Primitives",
            "core:testing" to "Test Rules, Dispatchers & Fakes"
        )

        for ((modId, modTitle) in availableCore) {
            val cb = JBCheckBox(":$modId ($modTitle)", model.selectedCoreModules.contains(modId))
            cb.addActionListener {
                if (cb.isSelected) {
                    model.selectedCoreModules.add(modId)
                } else {
                    model.selectedCoreModules.remove(modId)
                }
            }
            coreModuleCheckboxes[modId] = cb
            corePanel.add(cb)
        }
        mainPanel.add(corePanel, gbc)

        // 4. Feature Modules Custom Naming Section
        gbc.gridy = 4
        val featurePanel = JPanel(BorderLayout(4, 6))
        featurePanel.border = BorderFactory.createTitledBorder("Feature Modules (:feature:<name>)")

        val inputPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2))
        inputPanel.add(JBLabel("Feature Name:"))
        newFeatureField.emptyText.text = "e.g. auth, profile, checkout"
        inputPanel.add(newFeatureField)
        inputPanel.add(addFeatureButton)

        // Quick suggestions
        val suggestionsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2))
        suggestionsPanel.add(JBLabel("<html><small color='#888888'>Quick Add:</small></html>"))
        val suggestions = listOf("auth", "profile", "settings", "search", "checkout", "details", "notifications")
        for (suggest in suggestions) {
            val chipButton = JButton("+$suggest").apply {
                margin = Insets(1, 6, 1, 6)
                font = font.deriveFont(11.0f)
                addActionListener {
                    addFeature(suggest)
                }
            }
            suggestionsPanel.add(chipButton)
        }

        val topFeatureControl = JPanel(GridLayout(2, 1, 2, 2))
        topFeatureControl.add(inputPanel)
        topFeatureControl.add(suggestionsPanel)

        featurePanel.add(topFeatureControl, BorderLayout.NORTH)
        featurePanel.add(featuresContainer, BorderLayout.CENTER)

        mainPanel.add(featurePanel, gbc)

        setupFeatureActions()
        renderFeatureChips()

        add(mainPanel, BorderLayout.NORTH)
    }

    private fun setupFeatureActions() {
        addFeatureButton.addActionListener {
            val text = newFeatureField.text.trim()
            if (text.isNotEmpty()) {
                val names = text.split(",", " ")
                for (name in names) {
                    addFeature(name)
                }
                newFeatureField.text = ""
            }
        }
    }

    private fun addFeature(name: String) {
        if (model.addFeature(name)) {
            renderFeatureChips()
        }
    }

    private fun removeFeature(name: String) {
        model.removeFeature(name)
        renderFeatureChips()
    }

    private fun renderFeatureChips() {
        featuresContainer.removeAll()
        if (model.featureModules.isEmpty()) {
            featuresContainer.add(JBLabel("<html><i color='#888888'>No feature modules configured yet. Type a name above to add one.</i></html>"))
        } else {
            for (feature in model.featureModules) {
                val chip = JPanel(FlowLayout(FlowLayout.CENTER, 4, 2)).apply {
                    border = BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(java.awt.Color(120, 120, 160), 1, true),
                        BorderFactory.createEmptyBorder(2, 6, 2, 4)
                    )
                    add(JBLabel(":feature:$feature"))
                    val removeBtn = JButton("×").apply {
                        margin = Insets(0, 3, 0, 3)
                        isBorderPainted = false
                        isContentAreaFilled = false
                        isFocusPainted = false
                        font = font.deriveFont(12.0f)
                        toolTipText = "Remove :feature:$feature"
                        addActionListener {
                            removeFeature(feature)
                        }
                    }
                    add(removeBtn)
                }
                featuresContainer.add(chip)
            }
        }
        featuresContainer.revalidate()
        featuresContainer.repaint()
    }

    private fun updateModulesForArchitecture(arch: ArchitectureType) {
        val isSingle = arch == ArchitectureType.SINGLE_MODULE
        conventionPluginsBox.isEnabled = !isSingle
        newFeatureField.isEnabled = !isSingle
        addFeatureButton.isEnabled = !isSingle

        for ((id, cb) in coreModuleCheckboxes) {
            cb.isEnabled = !isSingle
            if (isSingle) {
                cb.isSelected = false
                model.selectedCoreModules.remove(id)
            } else {
                cb.isSelected = true
                model.selectedCoreModules.add(id)
            }
        }

        if (isSingle) {
            model.featureModules.clear()
        } else if (model.featureModules.isEmpty()) {
            model.featureModules.add("home")
        }
        renderFeatureChips()
    }
}
