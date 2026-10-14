package prasad.vennam.awesomeandroidwizard.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import prasad.vennam.awesomeandroidwizard.generator.BenchmarkModuleGenerator
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

class NewBenchmarkModuleDialog(private val project: Project) : DialogWrapper(project, true) {

    private val packageField = JBTextField(24)

    init {
        title = "New Awesome Benchmark & Baseline Profile Module"
        detectPackage()
        init()
    }

    private fun detectPackage() {
        val basePath = project.basePath ?: return
        val appBuild = File(basePath, "app/build.gradle.kts")
        if (appBuild.exists()) {
            val content = appBuild.readText()
            val namespaceRegex = """namespace\s*=\s*["']([^"']+)["']""".toRegex()
            val match = namespaceRegex.find(content)
            if (match != null) {
                packageField.text = match.groupValues[1]
                return
            }
        }
        packageField.text = "com.example.app"
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(500, 220)
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
            JBLabel("<html><h3>Add Baseline Profile & Macrobenchmark Module</h3><p color='#777777'>Pre-compiles Compose bytecodes to native machine code on app install, boosting cold start times by 30-40%.</p></html>"),
            gbc
        )

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0
        formPanel.add(JBLabel("Target Package:"), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        formPanel.add(packageField, gbc)

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2
        formPanel.add(
            JBLabel("<html><small color='#777777'>Will create <code>:benchmark</code> module with <code>BaselineProfileGenerator</code>, <code>StartupBenchmark</code>, and wire into <code>settings.gradle.kts</code> and <code>app/build.gradle.kts</code>.</small></html>"),
            gbc
        )

        root.add(formPanel, BorderLayout.CENTER)
        return root
    }

    override fun doOKAction() {
        val pkg = packageField.text.trim()
        val basePath = project.basePath ?: return
        val rootDir = File(basePath)

        val result = BenchmarkModuleGenerator.scaffoldBenchmarkModule(
            projectRootDir = rootDir,
            customPackage = pkg
        )

        if (result.success) {
            super.doOKAction()

            val virtualBase = LocalFileSystem.getInstance().findFileByIoFile(rootDir)
            if (virtualBase != null) {
                VfsUtil.markDirtyAndRefresh(true, true, true, virtualBase)
            }

            Messages.showInfoMessage(
                project,
                "${result.message}\n\nRun './gradlew :benchmark:generateBaselineProfile' on a physical device/emulator to generate your profile.",
                "Benchmark Module Created"
            )
        } else {
            Messages.showErrorDialog(project, result.message, "Benchmark Module Creation Failed")
        }
    }
}
