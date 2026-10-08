package prasad.vennam.awesomeandroidwizard.ui.steps

import prasad.vennam.awesomeandroidwizard.model.DatabaseFramework
import prasad.vennam.awesomeandroidwizard.model.DiFramework
import prasad.vennam.awesomeandroidwizard.model.NetworkingFramework
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton

class TechStackStep(private val model: WizardModel) : JPanel(BorderLayout()) {

    init {
        border = BorderFactory.createEmptyBorder(15, 20, 15, 20)

        val mainPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(6, 6, 6, 6)
            gridx = 0; gridy = 0; gridwidth = 2
        }

        mainPanel.add(JBLabel("<html><h2>Tech Stack & Libraries</h2><p color='#888888'>Select your preferred frameworks and toolkits</p></html>"), gbc)

        // UI Framework
        gbc.gridy = 1
        val uiPanel = JPanel(GridLayout(0, 1, 4, 4))
        uiPanel.border = BorderFactory.createTitledBorder("UI Framework")
        val composeBox = JBCheckBox("Jetpack Compose with Material 3 (Edge-to-Edge)", model.useCompose)
        composeBox.isEnabled = false // Default modern standard
        uiPanel.add(composeBox)
        mainPanel.add(uiPanel, gbc)

        // DI Framework
        gbc.gridy = 2
        val diPanel = JPanel(GridLayout(0, 1, 4, 4))
        diPanel.border = BorderFactory.createTitledBorder("Dependency Injection")
        val diGroup = ButtonGroup()

        for (di in DiFramework.values()) {
            val radio = JRadioButton(di.displayName)
            radio.isSelected = di == model.diFramework
            radio.addActionListener { model.diFramework = di }
            diGroup.add(radio)
            diPanel.add(radio)
        }
        mainPanel.add(diPanel, gbc)

        // Networking Framework
        gbc.gridy = 3
        val netPanel = JPanel(GridLayout(0, 1, 4, 4))
        netPanel.border = BorderFactory.createTitledBorder("Networking")
        val netGroup = ButtonGroup()

        for (net in NetworkingFramework.values()) {
            val radio = JRadioButton(net.displayName)
            radio.isSelected = net == model.networking
            radio.addActionListener { model.networking = net }
            netGroup.add(radio)
            netPanel.add(radio)
        }
        mainPanel.add(netPanel, gbc)

        // Database Framework
        gbc.gridy = 4
        val dbPanel = JPanel(GridLayout(0, 1, 4, 4))
        dbPanel.border = BorderFactory.createTitledBorder("Local Storage")
        val dbGroup = ButtonGroup()

        for (db in DatabaseFramework.values()) {
            val radio = JRadioButton(db.displayName)
            radio.isSelected = db == model.database
            radio.addActionListener { model.database = db }
            dbGroup.add(radio)
            dbPanel.add(radio)
        }
        mainPanel.add(dbPanel, gbc)

        add(mainPanel, BorderLayout.NORTH)
    }
}
