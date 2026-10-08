package prasad.vennam.awesomeandroidwizard.ui

import prasad.vennam.awesomeandroidwizard.generator.ProjectGenerator
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import prasad.vennam.awesomeandroidwizard.ui.steps.ArchitectureStep
import prasad.vennam.awesomeandroidwizard.ui.steps.ProjectBasicsStep
import prasad.vennam.awesomeandroidwizard.ui.steps.SummaryStep
import prasad.vennam.awesomeandroidwizard.ui.steps.TechStackStep
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import java.io.File
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class AwesomeWizardDialog(private val project: Project?) : DialogWrapper(project, true) {

    private val model = WizardModel()

    private val cardLayout = CardLayout()
    private val contentPanel = JPanel(cardLayout)

    private val step1 = ProjectBasicsStep(model)
    private val step2 = ArchitectureStep(model)
    private val step3 = TechStackStep(model)
    private val step4 = SummaryStep(model)

    private var currentStep = 0
    private val totalSteps = 4

    private lateinit var prevButton: JButton
    private lateinit var nextButton: JButton

    init {
        title = "Awesome Android Project Wizard"
        init()
        updateStepUI()
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(650, 480)
        }

        contentPanel.add(step1, "0")
        contentPanel.add(step2, "1")
        contentPanel.add(step3, "2")
        contentPanel.add(step4, "3")

        root.add(contentPanel, BorderLayout.CENTER)
        return root
    }

    override fun createActions(): Array<Action> {
        val cancelAction = cancelAction
        val customOkAction = okAction
        return arrayOf(cancelAction, customOkAction)
    }

    override fun createButtonsPanel(buttons: MutableList<out JButton>): JPanel {
        val panel = super.createButtonsPanel(buttons)

        prevButton = JButton("Previous").apply {
            addActionListener {
                if (currentStep > 0) {
                    currentStep--
                    updateStepUI()
                }
            }
        }

        nextButton = JButton("Next").apply {
            addActionListener {
                if (currentStep < totalSteps - 1) {
                    currentStep++
                    updateStepUI()
                }
            }
        }

        panel.add(prevButton, 0)
        panel.add(nextButton, 1)

        return panel
    }

    private fun updateStepUI() {
        cardLayout.show(contentPanel, currentStep.toString())

        if (::prevButton.isInitialized) {
            prevButton.isEnabled = currentStep > 0
        }
        if (::nextButton.isInitialized) {
            nextButton.isEnabled = currentStep < totalSteps - 1
        }

        // Enable OK button only on last step
        isOKActionEnabled = currentStep == totalSteps - 1

        if (currentStep == 3) {
            step4.refresh()
        }

        title = "Awesome Android Wizard (${currentStep + 1}/$totalSteps): " + when (currentStep) {
            0 -> "Project Basics"
            1 -> "Architecture & Modules"
            2 -> "Tech Stack & Frameworks"
            else -> "Review & Generate"
        }
    }

    override fun doOKAction() {
        super.doOKAction()

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generating Awesome Android Project...", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.fraction = 0.2
                indicator.text = "Scaffolding directory structure..."

                val generatedDir = ProjectGenerator.generate(model)

                indicator.fraction = 0.7
                indicator.text = "Configuring Gradle build files and dependencies..."

                Thread.sleep(500) // Brief UI feedback

                indicator.fraction = 1.0
                indicator.text = "Project generated successfully!"

                // Open newly created project in Android Studio
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    val prompt = Messages.showYesNoDialog(
                        project,
                        "Project successfully generated at:\n${generatedDir.absolutePath}\n\nWould you like to open it now?",
                        "Project Created",
                        Messages.getInformationIcon()
                    )

                    if (prompt == Messages.YES) {
                        try {
                            ProjectUtil.openOrImport(generatedDir.toPath(), null, true)
                        } catch (e: Exception) {
                            Messages.showErrorDialog("Could not automatically open project: ${e.message}", "Open Project Error")
                        }
                    }
                }
            }
        })
    }
}
