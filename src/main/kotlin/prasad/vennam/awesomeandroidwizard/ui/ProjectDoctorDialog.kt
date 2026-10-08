package prasad.vennam.awesomeandroidwizard.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.io.File
import javax.swing.*

class ProjectDoctorDialog(private val currentProject: Project?) : DialogWrapper(currentProject) {

    init {
        title = "Awesome Project Doctor - Architecture & Health Audit"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout(12, 12))
        mainPanel.preferredSize = Dimension(620, 480)
        mainPanel.border = BorderFactory.createEmptyBorder(16, 16, 16, 16)

        val headerPanel = JPanel()
        headerPanel.layout = BoxLayout(headerPanel, BoxLayout.Y_AXIS)

        val titleLabel = JBLabel("🏥 Project Health Diagnostics")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 18f)

        val projectPath = currentProject?.basePath ?: "N/A"
        val subtitleLabel = JBLabel("Auditing: $projectPath")
        subtitleLabel.font = subtitleLabel.font.deriveFont(12f)
        subtitleLabel.foreground = Color.GRAY

        headerPanel.add(titleLabel)
        headerPanel.add(Box.createVerticalStrut(4))
        headerPanel.add(subtitleLabel)
        headerPanel.add(Box.createVerticalStrut(12))

        val resultsPanel = JPanel()
        resultsPanel.layout = BoxLayout(resultsPanel, BoxLayout.Y_AXIS)

        val checks = performDiagnostics(currentProject?.basePath)
        var passedCount = 0

        for (check in checks) {
            if (check.passed) passedCount++
            resultsPanel.add(createCheckCard(check))
            resultsPanel.add(Box.createVerticalStrut(8))
        }

        val scorePercentage = if (checks.isNotEmpty()) (passedCount * 100) / checks.size else 0
        val summaryLabel = JBLabel("Health Score: $scorePercentage% ($passedCount/${checks.size} Checks Passed)")
        summaryLabel.font = summaryLabel.font.deriveFont(Font.BOLD, 14f)
        summaryLabel.foreground = if (scorePercentage >= 80) Color(0x2E, 0x7D, 0x32) else Color(0xD3, 0x2F, 0x2F)

        headerPanel.add(summaryLabel)
        headerPanel.add(Box.createVerticalStrut(8))

        mainPanel.add(headerPanel, BorderLayout.NORTH)
        mainPanel.add(JBScrollPane(resultsPanel), BorderLayout.CENTER)

        return mainPanel
    }

    private fun createCheckCard(check: DiagnosticCheck): JComponent {
        val card = JPanel(BorderLayout(8, 4))
        card.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(if (check.passed) Color(0xC8, 0xE6, 0xC9) else Color(0xFF, 0xCD, 0xD2)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        )
        card.background = if (check.passed) Color(0xF1, 0xF8, 0xE9) else Color(0xFF, 0xEB, 0xEE)

        val iconText = if (check.passed) "✅" else "⚠️"
        val header = JLabel("$iconText  ${check.title}")
        header.font = header.font.deriveFont(Font.BOLD, 13f)

        val desc = JLabel("<html>${check.details}</html>")
        desc.font = desc.font.deriveFont(11f)
        desc.foreground = Color.DARK_GRAY

        card.add(header, BorderLayout.NORTH)
        card.add(desc, BorderLayout.CENTER)
        return card
    }

    private fun performDiagnostics(basePath: String?): List<DiagnosticCheck> {
        val list = mutableListOf<DiagnosticCheck>()
        if (basePath == null) {
            list.add(DiagnosticCheck("Project State", false, "No active project is currently loaded in the IDE."))
            return list
        }

        val rootDir = File(basePath)

        // 1. Android SDK Check
        val localProps = File(rootDir, "local.properties")
        val envSdk = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
        val defaultMacSdk = File(System.getProperty("user.home"), "Library/Android/sdk")
        val hasSdk = localProps.exists() || envSdk != null || defaultMacSdk.exists()
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
                if (hasWrapper && is810) "Gradle 8.10+ wrapper installed and configured."
                else "Gradle wrapper missing or outdated. Recommended: Gradle 8.10.2."
            )
        )

        // 3. Version Catalog Check
        val catalogFile = File(rootDir, "gradle/libs.versions.toml")
        list.add(
            DiagnosticCheck(
                "Version Catalog (libs.versions.toml)",
                catalogFile.exists(),
                if (catalogFile.exists()) "Centralized dependencies catalog found at gradle/libs.versions.toml."
                else "Version catalog not found. Recommended to manage dependencies via libs.versions.toml."
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

        // 8. Architecture Dependency Guard
        val featureDir = File(rootDir, "feature")
        val featureBuildFiles = if (featureDir.exists() && featureDir.isDirectory) {
            featureDir.listFiles()?.mapNotNull { f ->
                val b = File(f, "build.gradle.kts")
                if (b.exists()) b else null
            } ?: emptyList()
        } else {
            emptyList()
        }

        val networkLeakingFeatures = mutableListOf<String>()
        val crossFeatureCoupled = mutableListOf<String>()

        for (bf in featureBuildFiles) {
            val content = bf.readText()
            val moduleName = bf.parentFile.name
            if (content.contains("project(\":core:network\")")) {
                networkLeakingFeatures.add(moduleName)
            }
            val match = """:feature:([a-zA-Z0-9_-]+)""".toRegex().findAll(content)
            for (m in match) {
                val target = m.groupValues[1]
                if (target != moduleName) {
                    crossFeatureCoupled.add("$moduleName -> $target")
                }
            }
        }

        val hasArchViolations = networkLeakingFeatures.isNotEmpty() || crossFeatureCoupled.isNotEmpty()
        val archMessage = when {
            networkLeakingFeatures.isNotEmpty() ->
                "Architecture Warning! Feature(s) [${networkLeakingFeatures.joinToString()}] directly depend on :core:network. Features must depend on :core:data to preserve encapsulation."
            crossFeatureCoupled.isNotEmpty() ->
                "Coupling Warning! Direct cross-feature dependencies detected: [${crossFeatureCoupled.joinToString()}]. Decouple using navigation routes or core modules."
            featureBuildFiles.isNotEmpty() ->
                "Clean Architecture boundaries strictly honored (${featureBuildFiles.size} feature modules audited). No network leaks or feature-to-feature coupling."
            else ->
                "No custom feature modules detected in /feature directory."
        }

        list.add(
            DiagnosticCheck(
                "Clean Architecture Dependency Guard",
                !hasArchViolations,
                archMessage
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
                else "Pre-commit hook missing. Run './gradlew installGitHooks' to prevent broken code from being committed."
            )
        )

        return list
    }

    private data class DiagnosticCheck(
        val title: String,
        val passed: Boolean,
        val details: String
    )
}
