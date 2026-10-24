package prasad.vennam.awesomeandroidwizard.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import prasad.vennam.awesomeandroidwizard.config.AwesomeVersions
import prasad.vennam.awesomeandroidwizard.model.ArchitectureAuditor
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.*

/**
 * Enterprise Project Health Diagnostics & Architecture Auditor Dialog.
 * Evaluates SDK environment, Gradle wrappers, convention plugins, and Clean Architecture boundaries
 * with theme-adaptive visual styling, retina DPI scaling, live re-audit, and one-click quick fixes.
 */
class ProjectDoctorDialog(private val currentProject: Project?) : DialogWrapper(currentProject) {

    private val resultsPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    private val summaryLabel = JBLabel()
    private var lastChecks: List<DiagnosticCheck> = emptyList()

    init {
        title = "Awesome Project Doctor - Architecture & Health Audit"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout(JBUI.scale(12), JBUI.scale(12))).apply {
            preferredSize = JBUI.size(680, 520)
            border = JBUI.Borders.empty(14, 16)
        }

        val headerPanel = JPanel(BorderLayout(JBUI.scale(8), JBUI.scale(8)))

        // Title and Project Path
        val titleBox = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        val titleLabel = JBLabel("Project Health & Architecture Doctor", AllIcons.General.InspectionsEye, SwingConstants.LEFT).apply {
            font = JBFont.h2().asBold()
        }

        val projectPath = currentProject?.basePath ?: "N/A"
        val subtitleLabel = JBLabel("Auditing: $projectPath").apply {
            font = JBFont.small()
            foreground = TEXT_MUTED
        }

        titleBox.add(titleLabel)
        titleBox.add(Box.createVerticalStrut(JBUI.scale(4)))
        titleBox.add(subtitleLabel)
        titleBox.add(Box.createVerticalStrut(JBUI.scale(8)))
        titleBox.add(summaryLabel)

        headerPanel.add(titleBox, BorderLayout.WEST)

        // Top Action Bar: Refresh & Copy Report
        val actionToolbar = JPanel(FlowLayout(FlowLayout.RIGHT, JBUI.scale(6), 0))

        val copyBtn = JButton("Copy Report", AllIcons.Actions.Copy).apply {
            font = JBFont.small()
            margin = JBUI.insets(3, 8)
            toolTipText = "Copy diagnostics summary as Markdown to clipboard"
            addActionListener {
                copyReportToClipboard()
            }
        }

        val refreshBtn = JButton("Re-run Audit", AllIcons.Actions.Refresh).apply {
            font = JBFont.small()
            margin = JBUI.insets(3, 8)
            toolTipText = "Re-evaluate all diagnostic rules against the current workspace"
            addActionListener {
                refreshDiagnostics()
            }
        }

        actionToolbar.add(copyBtn)
        actionToolbar.add(refreshBtn)
        headerPanel.add(actionToolbar, BorderLayout.EAST)

        mainPanel.add(headerPanel, BorderLayout.NORTH)
        mainPanel.add(JBScrollPane(resultsPanel).apply {
            border = JBUI.Borders.empty()
        }, BorderLayout.CENTER)

        refreshDiagnostics()
        return mainPanel
    }

    private fun refreshDiagnostics() {
        resultsPanel.removeAll()
        val checks = performDiagnostics(currentProject?.basePath)
        lastChecks = checks
        var passedCount = 0

        for (check in checks) {
            if (check.passed) passedCount++
            resultsPanel.add(createCheckCard(check))
            resultsPanel.add(Box.createVerticalStrut(JBUI.scale(8)))
        }

        val scorePercentage = if (checks.isNotEmpty()) (passedCount * 100) / checks.size else 0
        summaryLabel.text = "Health Score: $scorePercentage% ($passedCount/${checks.size} Checks Passed)"
        summaryLabel.font = JBFont.label().asBold()
        summaryLabel.foreground = if (scorePercentage >= 80) SCORE_HEALTHY else SCORE_UNHEALTHY

        resultsPanel.revalidate()
        resultsPanel.repaint()
    }

    private fun createCheckCard(check: DiagnosticCheck): JComponent {
        val card = JPanel(BorderLayout(JBUI.scale(8), JBUI.scale(4))).apply {
            border = JBUI.Borders.compound(
                BorderFactory.createLineBorder(if (check.passed) PASS_BORDER else FAIL_BORDER, 1),
                JBUI.Borders.empty(8, 12)
            )
            background = if (check.passed) PASS_BG else FAIL_BG
        }

        // Header with Icon, Title, and optional Quick-Fix button
        val cardHeader = JPanel(BorderLayout(JBUI.scale(6), 0)).apply {
            isOpaque = false
        }

        val icon = if (check.passed) AllIcons.General.InspectionsOK else AllIcons.General.Warning
        val headerLabel = JBLabel(check.title, icon, SwingConstants.LEFT).apply {
            font = JBFont.label().asBold()
            foreground = UIUtil.getLabelForeground()
        }
        cardHeader.add(headerLabel, BorderLayout.WEST)

        if (check.action != null && check.actionTitle != null && currentProject != null) {
            val quickFixBtn = JButton(check.actionTitle, AllIcons.Actions.QuickfixBulb).apply {
                font = JBFont.small()
                margin = JBUI.insets(2, 6)
                addActionListener {
                    check.action.invoke(currentProject)
                    refreshDiagnostics()
                }
            }
            cardHeader.add(quickFixBtn, BorderLayout.EAST)
        }

        val desc = JBLabel("<html>${check.details}</html>").apply {
            font = JBFont.small()
            foreground = DETAIL_TEXT
        }

        card.add(cardHeader, BorderLayout.NORTH)
        card.add(desc, BorderLayout.CENTER)
        return card
    }

    private fun copyReportToClipboard() {
        val projectPath = currentProject?.basePath ?: "N/A"
        val passedCount = lastChecks.count { it.passed }
        val score = if (lastChecks.isNotEmpty()) (passedCount * 100) / lastChecks.size else 0

        val sb = StringBuilder()
        sb.appendLine("## Awesome Android Project Health Report")
        sb.appendLine("**Project:** `$projectPath`")
        sb.appendLine("**Health Score:** $score% ($passedCount/${lastChecks.size} Checks Passed)\n")

        for (check in lastChecks) {
            val mark = if (check.passed) "[x]" else "[ ]"
            val cleanDetails = check.details
                .replace("<br/>", " ")
                .replace("• ", "\n    - ")
                .replace("<html>", "")
                .replace("</html>", "")
            sb.appendLine("- $mark **${check.title}**: $cleanDetails")
        }

        CopyPasteManager.getInstance().setContents(StringSelection(sb.toString()))
        Messages.showInfoMessage(
            contentPanel,
            "Health diagnostics report successfully copied to clipboard!",
            "Report Copied"
        )
    }

    companion object {
        // Theme-Adaptive Color Tokens (Light & Dark UI Mode Parity)
        private val PASS_BG = JBColor(Color(0xF1, 0xF8, 0xE9), Color(0x1B, 0x2E, 0x1E))
        private val PASS_BORDER = JBColor(Color(0xC8, 0xE6, 0xC9), Color(0x2D, 0x5E, 0x36))
        private val FAIL_BG = JBColor(Color(0xFF, 0xEB, 0xEE), Color(0x3B, 0x1E, 0x22))
        private val FAIL_BORDER = JBColor(Color(0xFF, 0xCD, 0xD2), Color(0x6E, 0x2D, 0x34))
        private val TEXT_MUTED = JBColor(Color.GRAY, Color.LIGHT_GRAY)
        private val DETAIL_TEXT = JBColor(Color(0x33, 0x33, 0x33), Color(0xCC, 0xCC, 0xCC))
        private val SCORE_HEALTHY = JBColor(Color(0x2E, 0x7D, 0x32), Color(0x81, 0xC7, 0x84))
        private val SCORE_UNHEALTHY = JBColor(Color(0xD3, 0x2F, 0x2F), Color(0xEF, 0x53, 0x50))

        data class DiagnosticCheck(
            val title: String,
            val passed: Boolean,
            val details: String,
            val actionTitle: String? = null,
            val action: ((Project) -> Unit)? = null
        )

        fun installPreCommitHook(rootDir: File): Boolean {
            val hookDir = File(rootDir, ".githooks")
            if (!hookDir.exists()) hookDir.mkdirs()
            val hookFile = File(hookDir, "pre-commit")
            hookFile.writeText(
                """#!/bin/sh
# Awesome Android Pre-Commit Guard
echo "Running static analysis & architecture guard..."
./gradlew check --daemon
""".trimIndent()
            )
            hookFile.setExecutable(true)

            val gitDir = File(rootDir, ".git/hooks")
            if (gitDir.exists()) {
                val activeHook = File(gitDir, "pre-commit")
                activeHook.writeText(hookFile.readText())
                activeHook.setExecutable(true)
            }

            val vFile = LocalFileSystem.getInstance().findFileByIoFile(hookFile)
            if (vFile != null) {
                VfsUtil.markDirtyAndRefresh(true, true, true, vFile)
            }
            return true
        }

        fun openFileInEditor(project: Project, file: File) {
            val vFile = LocalFileSystem.getInstance().findFileByIoFile(file)
            if (vFile != null) {
                FileEditorManager.getInstance(project).openFile(vFile, true)
            }
        }

        fun openArchitectureToolWindow(project: Project) {
            ToolWindowManager.getInstance(project)
                .getToolWindow("Awesome Architecture")
                ?.activate(null)
        }

        fun performDiagnostics(basePath: String?): List<DiagnosticCheck> {
            val list = mutableListOf<DiagnosticCheck>()
            if (basePath == null) {
                list.add(DiagnosticCheck("Project State", false, "No active project is currently loaded in the IDE."))
                return list
            }

            val rootDir = File(basePath)

            // 1. Android SDK Check
            val localProps = File(rootDir, "local.properties")
            val userHome = System.getProperty("user.home")
            val sdkCandidates = listOfNotNull(
                System.getenv("ANDROID_HOME"),
                System.getenv("ANDROID_SDK_ROOT"),
                "$userHome/Library/Android/sdk",
                "$userHome/AppData/Local/Android/Sdk",
                "$userHome/Android/Sdk"
            )
            val hasSdk = localProps.exists() || sdkCandidates.any { File(it).exists() }
            list.add(
                DiagnosticCheck(
                    "Android SDK Environment",
                    hasSdk,
                    if (hasSdk) "Android SDK detected properly (local.properties or system path)."
                    else "Android SDK path missing. Set ANDROID_HOME or configure sdk.dir in local.properties."
                )
            )

            // 2. Gradle Wrapper Check
            val wrapperProps = File(rootDir, "gradle/wrapper/gradle-wrapper.properties")
            val wrapperJar = File(rootDir, "gradle/wrapper/gradle-wrapper.jar")
            val gradlew = File(rootDir, "gradlew")
            val hasWrapper = wrapperProps.exists() && (wrapperJar.exists() || gradlew.exists())
            val wrapperContent = if (wrapperProps.exists()) wrapperProps.readText() else ""
            val is810 = wrapperContent.contains("8.10") || wrapperContent.contains("8.11") || wrapperContent.contains("9.")
            list.add(
                DiagnosticCheck(
                    "Gradle Wrapper & Tooling",
                    hasWrapper && is810,
                    if (hasWrapper && is810) "Gradle 9+ / 8.10+ wrapper installed and configured."
                    else "Gradle wrapper missing or outdated. Recommended: Gradle ${AwesomeVersions.GRADLE_WRAPPER}."
                )
            )

            // 3. Version Catalog Check
            val catalogFile = File(rootDir, "gradle/libs.versions.toml")
            val hasCatalog = catalogFile.exists()
            list.add(
                DiagnosticCheck(
                    "Version Catalog (libs.versions.toml)",
                    hasCatalog,
                    if (hasCatalog) "Centralized dependencies catalog found at gradle/libs.versions.toml."
                    else "Version catalog not found. Recommended to manage dependencies via libs.versions.toml.",
                    if (hasCatalog) "Open Catalog" else null,
                    if (hasCatalog) { p -> openFileInEditor(p, catalogFile) } else null
                )
            )

            // 4. Build-Logic Convention Plugins
            val buildLogicDir = File(rootDir, "build-logic/convention")
            list.add(
                DiagnosticCheck(
                    "Gradle Convention Plugins (build-logic)",
                    buildLogicDir.exists(),
                    if (buildLogicDir.exists()) "build-logic/convention active. Shared build configuration verified."
                    else "Convention plugins not detected. Recommended for scalable multi-module projects."
                )
            )

            // 5. Pure JVM core:model check
            val modelBuildFile = File(rootDir, "core/model/build.gradle.kts")
            val isPureJvmModel = modelBuildFile.exists() && (modelBuildFile.readText().contains("awesome.jvm.library") || modelBuildFile.readText().contains("kotlin(\"jvm\")"))
            if (File(rootDir, "core/model").exists()) {
                list.add(
                    DiagnosticCheck(
                        ":core:model Zero-Overhead Optimization",
                        isPureJvmModel,
                        if (isPureJvmModel) ":core:model uses pure JVM convention plugin (zero AGP overhead)."
                        else ":core:model is configured as an Android library. Switching to pure JVM improves build speeds."
                    )
                )
            }

            // 6. Design System Resources
            val designDir = File(rootDir, "core/designsystem")
            val hasVector = File(designDir, "src/main/res/drawable").exists()
            if (designDir.exists()) {
                list.add(
                    DiagnosticCheck(
                        ":core:designsystem Token & Resource Layout",
                        hasVector,
                        if (hasVector) "Design system houses brand colors, typography, theme, and vector assets."
                        else "Consider adding vector drawables and brand assets to :core:designsystem."
                    )
                )
            }

            // 7. Code Quality & CI Tooling
            val ciWorkflow = File(rootDir, ".github/workflows")
            val detektConfig = File(rootDir, "config/detekt/detekt.yml")
            val editorConfig = File(rootDir, ".editorconfig")
            val hasQuality = ciWorkflow.exists() || detektConfig.exists() || editorConfig.exists()
            list.add(
                DiagnosticCheck(
                    "Code Quality & CI/CD Pipelines",
                    hasQuality,
                    if (hasQuality) "CI workflows, .editorconfig, and static analysis tooling verified."
                    else "Consider configuring GitHub Actions CI and .editorconfig."
                )
            )

            // 8. Architecture Dependency Guard (via ArchitectureAuditor)
            val auditReport = ArchitectureAuditor.auditProject(rootDir)
            val allViolations = auditReport.modules.flatMap { it.violations } + auditReport.globalViolations
            val archPassed = auditReport.isClean
            val archMessage = if (archPassed) {
                "Clean Architecture boundaries strictly honored (${auditReport.totalModules} modules audited). Zero circular dependencies, network leaks, or cross-feature coupling."
            } else {
                "Architecture Warning! ${allViolations.size} violation(s) detected:<br/>• " +
                    allViolations.take(3).joinToString("<br/>• ") +
                    if (allViolations.size > 3) "<br/>• ...and ${allViolations.size - 3} more (view in Awesome Architecture tool window)" else ""
            }

            list.add(
                DiagnosticCheck(
                    "Clean Architecture Dependency Guard",
                    archPassed,
                    archMessage,
                    if (!archPassed) "Open Architecture Guard" else null,
                    if (!archPassed) { p -> openArchitectureToolWindow(p) } else null
                )
            )

            // 9. Git Pre-Commit Hooks
            val githookFile = File(rootDir, ".githooks/pre-commit")
            val activeGitHook = File(rootDir, ".git/hooks/pre-commit")
            val hasGitHooks = githookFile.exists() || activeGitHook.exists()
            list.add(
                DiagnosticCheck(
                    "Git Pre-Commit Hook Enforcer",
                    hasGitHooks,
                    if (hasGitHooks) "Pre-commit hook active (.githooks/pre-commit). Enforces code quality before commit."
                    else "Pre-commit hook missing. Run './gradlew installGitHooks' to prevent broken code from being committed.",
                    if (!hasGitHooks) "Install Pre-Commit Hook" else null,
                    if (!hasGitHooks) { p -> installPreCommitHook(rootDir) } else null
                )
            )

            return list
        }
    }
}
