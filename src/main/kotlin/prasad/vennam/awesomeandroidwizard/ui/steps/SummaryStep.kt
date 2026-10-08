package prasad.vennam.awesomeandroidwizard.ui.steps

import prasad.vennam.awesomeandroidwizard.model.WizardModel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode

class SummaryStep(private val model: WizardModel) : JPanel(BorderLayout()) {

    private val summaryLabel = JBLabel()
    private val treeRoot = DefaultMutableTreeNode("Project")
    private val previewTree = Tree(treeRoot)

    init {
        border = BorderFactory.createEmptyBorder(15, 20, 15, 20)

        val headerPanel = JPanel()
        headerPanel.layout = BoxLayout(headerPanel, BoxLayout.Y_AXIS)
        headerPanel.add(JBLabel("<html><h2>Review & Scaffold</h2><p color='#888888'>Confirm your configuration before generating the project</p></html>"))
        headerPanel.add(summaryLabel)

        add(headerPanel, BorderLayout.NORTH)

        val scrollPane = JBScrollPane(previewTree).apply {
            preferredSize = Dimension(450, 220)
            border = BorderFactory.createTitledBorder("Project Hierarchy Preview")
        }
        add(scrollPane, BorderLayout.CENTER)
    }

    fun refresh() {
        val featuresList = if (model.featureModules.isEmpty()) "None" else model.featureModules.joinToString(", ") { ":feature:$it" }

        summaryLabel.text = """
            <html>
            <table cellpadding='3' style='margin-top: 8px;'>
                <tr><td><b>Project:</b></td><td>${model.projectName} (${model.packageName})</td></tr>
                <tr><td><b>Architecture:</b></td><td>${model.architecture.displayName}</td></tr>
                <tr><td><b>Features:</b></td><td>$featuresList</td></tr>
                <tr><td><b>Tech Stack:</b></td><td>Compose M3 | ${model.diFramework.displayName} | ${model.networking.displayName}</td></tr>
                <tr><td><b>Target Location:</b></td><td><code>${model.projectLocation}</code></td></tr>
            </table>
            </html>
        """.trimIndent()

        // Rebuild tree preview
        treeRoot.removeAllChildren()
        treeRoot.userObject = model.projectName

        val gradleNode = DefaultMutableTreeNode("gradle")
        gradleNode.add(DefaultMutableTreeNode("libs.versions.toml"))
        val wrapperNode = DefaultMutableTreeNode("wrapper")
        wrapperNode.add(DefaultMutableTreeNode("gradle-wrapper.properties"))
        gradleNode.add(wrapperNode)
        treeRoot.add(gradleNode)

        if (model.useConventionPlugins) {
            val buildLogicNode = DefaultMutableTreeNode("build-logic")
            val conventionNode = DefaultMutableTreeNode("convention")
            conventionNode.add(DefaultMutableTreeNode("AndroidApplicationConventionPlugin.kt"))
            conventionNode.add(DefaultMutableTreeNode("AndroidLibraryConventionPlugin.kt"))
            conventionNode.add(DefaultMutableTreeNode("AndroidComposeConventionPlugin.kt"))
            buildLogicNode.add(conventionNode)
            treeRoot.add(buildLogicNode)
        }

        val appNode = DefaultMutableTreeNode(":app")
        appNode.add(DefaultMutableTreeNode("build.gradle.kts"))
        appNode.add(DefaultMutableTreeNode("AndroidManifest.xml"))
        appNode.add(DefaultMutableTreeNode("MainActivity.kt"))
        appNode.add(DefaultMutableTreeNode("AwesomeApplication.kt"))
        treeRoot.add(appNode)

        // Core Modules
        for (mod in model.selectedCoreModules) {
            val modNode = DefaultMutableTreeNode(":$mod")
            modNode.add(DefaultMutableTreeNode("build.gradle.kts"))
            treeRoot.add(modNode)
        }

        // Custom Feature Modules
        for (feat in model.featureModules) {
            val pascal = WizardModel.toPascalCase(feat)
            val featNode = DefaultMutableTreeNode(":feature:$feat")
            featNode.add(DefaultMutableTreeNode("build.gradle.kts"))
            featNode.add(DefaultMutableTreeNode("${pascal}Screen.kt"))
            featNode.add(DefaultMutableTreeNode("${pascal}ViewModel.kt"))
            treeRoot.add(featNode)
        }

        treeRoot.add(DefaultMutableTreeNode("settings.gradle.kts"))
        treeRoot.add(DefaultMutableTreeNode("build.gradle.kts"))
        treeRoot.add(DefaultMutableTreeNode("README.md"))

        previewTree.model?.let {
            (it as javax.swing.tree.DefaultTreeModel).reload()
        }
        for (i in 0 until previewTree.rowCount) {
            previewTree.expandRow(i)
        }
    }
}
