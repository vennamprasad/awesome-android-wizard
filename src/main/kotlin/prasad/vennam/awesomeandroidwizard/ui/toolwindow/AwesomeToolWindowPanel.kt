package prasad.vennam.awesomeandroidwizard.ui.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.treeStructure.Tree
import prasad.vennam.awesomeandroidwizard.config.AwesomeVersions
import prasad.vennam.awesomeandroidwizard.model.ArchitectureAuditor
import prasad.vennam.awesomeandroidwizard.ui.NewAutomotiveModuleDialog
import prasad.vennam.awesomeandroidwizard.ui.NewBenchmarkModuleDialog
import prasad.vennam.awesomeandroidwizard.ui.NewCoreModuleDialog
import prasad.vennam.awesomeandroidwizard.ui.NewFeatureModuleDialog
import prasad.vennam.awesomeandroidwizard.ui.NewMviSliceDialog
import prasad.vennam.awesomeandroidwizard.ui.NewRepositorySliceDialog
import prasad.vennam.awesomeandroidwizard.ui.NewWearModuleDialog
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel

class AwesomeToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val tabbedPane = JBTabbedPane()
    private val archTreeRoot = DefaultMutableTreeNode("Modules")
    private val archTreeModel = DefaultTreeModel(archTreeRoot)
    private val archTree = Tree(archTreeModel)
    private val archStatusLabel = JBLabel("Scanning project architecture...")

    init {
        setupUI()
        refreshArchitectureAudit()
    }

    private fun setupUI() {
        tabbedPane.addTab("Architecture Guard", AllIcons.Nodes.ModuleGroup, createArchitecturePanel())
        tabbedPane.addTab("Version Catalog", AllIcons.Nodes.DataTables, createVersionCatalogPanel())
        tabbedPane.addTab("Quick Scaffolders", AllIcons.Actions.Compile, createQuickActionsPanel())
        add(tabbedPane, BorderLayout.CENTER)
    }

    private fun createArchitecturePanel(): JComponent {
        val panel = JPanel(BorderLayout(8, 8)).apply {
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        }

        // Header
        val headerPanel = JPanel(BorderLayout(6, 6))
        archStatusLabel.font = archStatusLabel.font.deriveFont(Font.BOLD, 12f)
        headerPanel.add(archStatusLabel, BorderLayout.CENTER)

        val refreshBtn = JButton("Audit Graph", AllIcons.Actions.Refresh).apply {
            margin = Insets(2, 8, 2, 8)
            addActionListener {
                refreshArchitectureAudit()
            }
        }
        headerPanel.add(refreshBtn, BorderLayout.EAST)
        panel.add(headerPanel, BorderLayout.NORTH)

        // Tree
        archTree.cellRenderer = object : DefaultTreeCellRenderer() {
            override fun getTreeCellRendererComponent(
                tree: JTree?,
                value: Any?,
                sel: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ): Component {
                val comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
                val node = value as? DefaultMutableTreeNode
                val text = node?.userObject?.toString() ?: ""

                when {
                    text.startsWith("VIOLATION") -> {
                        icon = AllIcons.General.Error
                        foreground = com.intellij.ui.JBColor(Color(0xC0, 0x39, 0x2B), Color(0xEF, 0x53, 0x50))
                    }
                    text.startsWith("WARNING") -> {
                        icon = AllIcons.General.Warning
                        foreground = com.intellij.ui.JBColor(Color(0xD3, 0x54, 0x00), Color(0xFF, 0xA7, 0x26))
                    }
                    text.startsWith(":app") -> {
                        icon = AllIcons.Nodes.Artifact
                    }
                    text.startsWith(":feature:") -> {
                        icon = AllIcons.Nodes.Module
                    }
                    text.startsWith(":core:") -> {
                        icon = AllIcons.Nodes.Plugin
                    }
                    text.startsWith("Depends on:") -> {
                        icon = AllIcons.Actions.Forward
                    }
                }
                return comp
            }
        }

        panel.add(JBScrollPane(archTree), BorderLayout.CENTER)
        return panel
    }

    private fun createVersionCatalogPanel(): JComponent {
        val panel = JPanel(BorderLayout(8, 8)).apply {
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        }

        val basePath = project.basePath
        val catalogFile = if (basePath != null) File(basePath, "gradle/libs.versions.toml") else null

        val catalogText = JTextArea().apply {
            isEditable = false
            font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            margin = Insets(8, 8, 8, 8)
        }

        val headerPanel = JPanel(BorderLayout(6, 6))

        fun reloadCatalog() {
            headerPanel.removeAll()
            if (catalogFile != null && catalogFile.exists()) {
                val rawText = catalogFile.readText()
                catalogText.text = rawText
                catalogText.caretPosition = 0

                val statuses = AwesomeVersions.auditTomlVersions(rawText)
                val outdated = statuses.filter { it.isOutdated }

                val topInfoPanel = JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                }

                val titleLabel = JBLabel("<html><b>gradle/libs.versions.toml</b> (${catalogFile.length()} bytes) &nbsp;|&nbsp; Standard: <b>AGP ${AwesomeVersions.AGP}</b>, <b>Kotlin ${AwesomeVersions.KOTLIN}</b>, <b>SDK ${AwesomeVersions.COMPILE_SDK}</b></html>")
                topInfoPanel.add(titleLabel)
                topInfoPanel.add(Box.createVerticalStrut(6))

                if (outdated.isNotEmpty()) {
                    val alertPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
                    val warnLabel = JBLabel("<html><font color='#d97706'>⚠️ <b>${outdated.size} version(s) differ from Awesome Android Standard</b></font></html>", AllIcons.General.Warning, SwingConstants.LEFT)
                    val upgradeBtn = JButton("Upgrade to Awesome Stack", AllIcons.Actions.Execute).apply {
                        margin = Insets(2, 8, 2, 8)
                        addActionListener {
                            val confirm = Messages.showYesNoDialog(
                                project,
                                "Update ${outdated.size} dependency versions in libs.versions.toml to the latest verified Awesome Stack?\n\nUpdated items: ${outdated.joinToString { it.key }}",
                                "Upgrade Version Catalog",
                                Messages.getQuestionIcon()
                            )
                            if (confirm == Messages.YES) {
                                val upgradedContent = AwesomeVersions.upgradeTomlContent(rawText)
                                WriteCommandAction.runWriteCommandAction(project) {
                                    catalogFile.writeText(upgradedContent)
                                    LocalFileSystem.getInstance().refreshIoFiles(listOf(catalogFile))
                                }
                                reloadCatalog()
                                Messages.showInfoMessage(
                                    project,
                                    "Successfully upgraded ${outdated.size} dependency versions in gradle/libs.versions.toml!\nPlease run 'Sync Project with Gradle Files'.",
                                    "Awesome Stack Updated"
                                )
                            }
                        }
                    }
                    alertPanel.add(warnLabel)
                    alertPanel.add(Box.createHorizontalStrut(12))
                    alertPanel.add(upgradeBtn)
                    topInfoPanel.add(alertPanel)
                } else {
                    val okLabel = JBLabel("<html><font color='#16a34a'>✅ All core dependencies and plugins match latest Awesome Android Standard.</font></html>", AllIcons.General.InspectionsOK, SwingConstants.LEFT)
                    topInfoPanel.add(okLabel)
                }

                headerPanel.add(topInfoPanel, BorderLayout.CENTER)
            } else {
                catalogText.text = "# No libs.versions.toml found in project root."
                headerPanel.add(JBLabel("No Version Catalog detected in gradle/libs.versions.toml"), BorderLayout.NORTH)
            }
            headerPanel.revalidate()
            headerPanel.repaint()
        }

        reloadCatalog()

        panel.add(headerPanel, BorderLayout.NORTH)
        panel.add(JBScrollPane(catalogText), BorderLayout.CENTER)
        return panel
    }

    private fun createQuickActionsPanel(): JComponent {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
        }

        panel.add(JBLabel("<html><h3>Rapid Architecture Generators</h3><p color='#777777'>One-click Clean Architecture scaffolders for multi-module projects.</p></html>"))
        panel.add(Box.createVerticalStrut(10))

        // MVI Slice
        val mviBtn = JButton("New Awesome MVI Feature Slice...", AllIcons.Actions.Compile).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(380, 36)
            addActionListener {
                NewMviSliceDialog(project).show()
                refreshArchitectureAudit()
            }
        }
        panel.add(mviBtn)
        panel.add(Box.createVerticalStrut(8))

        // Repository Slice
        val repoBtn = JButton("New Awesome Repository Slice...", AllIcons.Nodes.DataTables).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(380, 36)
            addActionListener {
                NewRepositorySliceDialog(project).show()
                refreshArchitectureAudit()
            }
        }
        panel.add(repoBtn)
        panel.add(Box.createVerticalStrut(8))

        // Feature Module
        val featBtn = JButton("New Awesome Feature Module...", AllIcons.Nodes.Module).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(380, 36)
            addActionListener {
                NewFeatureModuleDialog(project).show()
                refreshArchitectureAudit()
            }
        }
        panel.add(featBtn)
        panel.add(Box.createVerticalStrut(8))

        // Core Module
        val coreBtn = JButton("New Awesome Core Module...", AllIcons.Nodes.Plugin).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(380, 36)
            addActionListener {
                NewCoreModuleDialog(project).show()
                refreshArchitectureAudit()
            }
        }
        panel.add(coreBtn)
        panel.add(Box.createVerticalStrut(8))

        // Benchmark Module
        val benchBtn = JButton("New Awesome Benchmark Module...", AllIcons.Actions.Profile).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(380, 36)
            addActionListener {
                NewBenchmarkModuleDialog(project).show()
                refreshArchitectureAudit()
            }
        }
        panel.add(benchBtn)
        panel.add(Box.createVerticalStrut(8))

        // Wear OS Companion Module
        val wearBtn = JButton("New Awesome Wear OS Companion...", AllIcons.Nodes.PpWeb).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(380, 36)
            addActionListener {
                NewWearModuleDialog(project).show()
                refreshArchitectureAudit()
            }
        }
        panel.add(wearBtn)
        panel.add(Box.createVerticalStrut(8))

        // Automotive Module
        val autoBtn = JButton("New Awesome Android Auto & Automotive...", AllIcons.Nodes.Services).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(380, 36)
            addActionListener {
                NewAutomotiveModuleDialog(project).show()
                refreshArchitectureAudit()
            }
        }
        panel.add(autoBtn)

        panel.add(Box.createVerticalGlue())
        return panel
    }

    fun refreshArchitectureAudit() {
        val basePath = project.basePath ?: return
        val rootDir = File(basePath)
        val report = ArchitectureAuditor.auditProject(rootDir)

        archTreeRoot.removeAllChildren()
        archTreeRoot.userObject = "Project Modules (${report.totalModules})"

        for (node in report.modules) {
            val modNode = DefaultMutableTreeNode(node.gradlePath)

            // Add violations first
            for (v in node.violations) {
                modNode.add(DefaultMutableTreeNode("VIOLATION: $v"))
            }

            // Add dependencies
            if (node.dependencies.isNotEmpty()) {
                val depsNode = DefaultMutableTreeNode("Depends on: (${node.dependencies.size})")
                for (dep in node.dependencies) {
                    depsNode.add(DefaultMutableTreeNode(dep))
                }
                modNode.add(depsNode)
            } else {
                modNode.add(DefaultMutableTreeNode("No project dependencies (Independent)"))
            }

            archTreeRoot.add(modNode)
        }

        // Global cycles
        for (gv in report.globalViolations) {
            archTreeRoot.add(DefaultMutableTreeNode("WARNING: $gv"))
        }

        archTreeModel.reload()

        // Expand root
        archTree.expandRow(0)

        if (report.isClean) {
            archStatusLabel.text = "<html><span color='#27AE60'>● Clean Architecture Compliant</span> (${report.totalModules} modules)</html>"
        } else {
            val violationCount = report.modules.sumOf { it.violations.size } + report.globalViolations.size
            archStatusLabel.text = "<html><span color='#C0392B'>● $violationCount Boundary Violations Detected</span></html>"
        }
    }
}
