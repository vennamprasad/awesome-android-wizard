package prasad.vennam.awesomeandroidwizard.ui.steps

import prasad.vennam.awesomeandroidwizard.model.ColorPreset
import prasad.vennam.awesomeandroidwizard.model.CornerStyle
import prasad.vennam.awesomeandroidwizard.model.TargetFormFactor
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JRadioButton

class DeviceAndThemeStep(private val model: WizardModel) : JPanel(BorderLayout()) {

    init {
        border = BorderFactory.createEmptyBorder(15, 20, 15, 20)

        val mainPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(6, 6, 6, 6)
            gridx = 0; gridy = 0; gridwidth = 2
            weightx = 1.0
        }

        mainPanel.add(
            JBLabel("<html><h2>Device & Theme Customization</h2><p color='#888888'>Configure target form factor, brand palette, and UI geometry</p></html>"),
            gbc
        )

        // 1. Target Form Factor
        gbc.gridy = 1
        val devicePanel = JPanel(GridLayout(0, 1, 4, 4))
        devicePanel.border = BorderFactory.createTitledBorder("Target Form Factor & Device Layout")
        val deviceGroup = ButtonGroup()

        for (device in TargetFormFactor.values()) {
            val radio = JRadioButton("<html><b>${device.displayName}</b> &mdash; <font color='#888888'>${device.description}</font></html>")
            radio.isSelected = device == model.targetFormFactor
            radio.addActionListener { model.targetFormFactor = device }
            deviceGroup.add(radio)
            devicePanel.add(radio)
        }
        mainPanel.add(devicePanel, gbc)

        // 2. Theme & Brand Color Preset
        gbc.gridy = 2
        val themePanel = JPanel(GridBagLayout())
        themePanel.border = BorderFactory.createTitledBorder("Brand Palette & Theme Preset")
        val tGbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(4, 8, 4, 8)
            gridx = 0; gridy = 0
            weightx = 0.0
        }

        themePanel.add(JBLabel("Color Palette:"), tGbc)

        val colorCombo = ComboBox(ColorPreset.values()).apply {
            selectedItem = this@DeviceAndThemeStep.model.colorPreset
            renderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>?,
                    value: Any?,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean
                ): java.awt.Component {
                    val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    if (value is ColorPreset) {
                        text = "${value.displayName} - ${value.description}"
                    }
                    return comp
                }
            }
            addActionListener {
                val selected = selectedItem as? ColorPreset ?: ColorPreset.DYNAMIC
                this@DeviceAndThemeStep.model.colorPreset = selected
            }
        }
        tGbc.gridx = 1; tGbc.weightx = 1.0
        themePanel.add(colorCombo, tGbc)
        mainPanel.add(themePanel, gbc)

        // 3. Corner / Shape Geometry Style
        gbc.gridy = 3
        val shapePanel = JPanel(GridBagLayout())
        shapePanel.border = BorderFactory.createTitledBorder("Corner Geometry & Shape Tokens")
        val sGbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(4, 8, 4, 8)
            gridx = 0; gridy = 0
            weightx = 0.0
        }

        shapePanel.add(JBLabel("Corner Style:"), sGbc)

        val cornerCombo = ComboBox(CornerStyle.values()).apply {
            selectedItem = this@DeviceAndThemeStep.model.cornerStyle
            renderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>?,
                    value: Any?,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean
                ): java.awt.Component {
                    val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    if (value is CornerStyle) {
                        text = "${value.displayName} - ${value.description}"
                    }
                    return comp
                }
            }
            addActionListener {
                val selected = selectedItem as? CornerStyle ?: CornerStyle.ROUNDED
                this@DeviceAndThemeStep.model.cornerStyle = selected
            }
        }
        sGbc.gridx = 1; sGbc.weightx = 1.0
        shapePanel.add(cornerCombo, sGbc)
        mainPanel.add(shapePanel, gbc)

        add(mainPanel, BorderLayout.NORTH)
    }
}
