package prasad.vennam.awesomeandroidwizard.model

import java.io.File

object ArchitectureAuditor {

    data class ModuleNode(
        val gradlePath: String,
        val directory: File,
        val dependencies: List<String>,
        val violations: List<String>
    )

    data class AuditReport(
        val isClean: Boolean,
        val totalModules: Int,
        val modules: List<ModuleNode>,
        val globalViolations: List<String>
    )

    fun auditProject(rootDir: File): AuditReport {
        val settingsFile = File(rootDir, "settings.gradle.kts")
        if (!settingsFile.exists()) {
            return AuditReport(
                isClean = false,
                totalModules = 0,
                modules = emptyList(),
                globalViolations = listOf("Missing settings.gradle.kts in root directory.")
            )
        }

        val modulePaths = parseIncludedModules(settingsFile)
        val moduleNodes = mutableListOf<ModuleNode>()

        for (modPath in modulePaths) {
            val relPath = modPath.removePrefix(":").replace(":", "/")
            val modDir = File(rootDir, relPath)
            val buildFile = File(modDir, "build.gradle.kts")

            val deps = if (buildFile.exists()) {
                parseModuleDependencies(buildFile.readText())
            } else {
                emptyList()
            }

            val violations = mutableListOf<String>()

            // Clean Architecture Rule 1: :core:* cannot depend on :feature:*
            if (modPath.startsWith(":core:")) {
                for (dep in deps) {
                    if (dep.startsWith(":feature:")) {
                        violations.add("Boundary Violation: Core module '$modPath' must not depend on feature module '$dep'.")
                    }
                    if (dep == ":app") {
                        violations.add("Boundary Violation: Core module '$modPath' must not depend on ':app'.")
                    }
                }
            }

            // Clean Architecture Rule 2: :core:model should not depend on UI or Data
            if (modPath == ":core:model") {
                for (dep in deps) {
                    if (dep.startsWith(":core:ui") || dep.startsWith(":core:designsystem") || dep.startsWith(":core:data")) {
                        violations.add("Purity Warning: ':core:model' should remain pure business/domain logic with zero UI or data dependencies (depends on '$dep').")
                    }
                }
            }

            moduleNodes.add(
                ModuleNode(
                    gradlePath = modPath,
                    directory = modDir,
                    dependencies = deps,
                    violations = violations
                )
            )
        }

        // Circular Dependency Check
        val circularViolations = mutableListOf<String>()
        val depMap = moduleNodes.associate { it.gradlePath to it.dependencies }

        for (node in moduleNodes) {
            val visited = mutableSetOf<String>()
            val cycle = detectCycle(node.gradlePath, depMap, visited, mutableListOf())
            if (cycle != null && !circularViolations.contains("Circular dependency: ${cycle.joinToString(" -> ")}")) {
                circularViolations.add("Circular dependency: ${cycle.joinToString(" -> ")}")
            }
        }

        val allViolations = moduleNodes.flatMap { it.violations } + circularViolations
        return AuditReport(
            isClean = allViolations.isEmpty(),
            totalModules = moduleNodes.size,
            modules = moduleNodes,
            globalViolations = circularViolations
        )
    }

    fun parseIncludedModules(settingsFile: File): List<String> {
        val content = settingsFile.readText()
        val cleanContent = content.lines().filterNot {
            val t = it.trim()
            t.startsWith("//") || t.startsWith("/*") || t.startsWith("*")
        }.joinToString("\n")

        val modules = mutableListOf<String>()
        val includeBlockRegex = """include\s*(?:\(([^)]+)\)|([^\n]+))""".toRegex()
        val quoteRegex = """["'](:[^"']+)["']""".toRegex()

        for (match in includeBlockRegex.findAll(cleanContent)) {
            val block = match.value
            for (quoted in quoteRegex.findAll(block)) {
                val mod = quoted.groupValues[1].trim()
                if (mod.isNotEmpty() && !modules.contains(mod)) {
                    modules.add(mod)
                }
            }
        }
        return modules
    }

    fun parseModuleDependencies(buildContent: String): List<String> {
        val list = mutableListOf<String>()

        // 1. Traditional project(":name") or project(path = ":name")
        val projectFuncRegex = """project\(\s*(?:path\s*=\s*)?["']([^"']+)["']\s*\)""".toRegex()
        for (match in projectFuncRegex.findAll(buildContent)) {
            val dep = match.groupValues[1].trim()
            if (dep.isNotEmpty() && !list.contains(dep)) {
                list.add(dep)
            }
        }

        // 2. Type-safe accessors: projects.core.model -> :core:model
        val accessorRegex = """projects\.([a-zA-Z0-9_.]+)""".toRegex()
        for (match in accessorRegex.findAll(buildContent)) {
            val rawPath = match.groupValues[1]
            val dep = ":" + rawPath.replace(".", ":")
            if (!list.contains(dep)) {
                list.add(dep)
            }
        }

        return list
    }

    private fun detectCycle(
        current: String,
        graph: Map<String, List<String>>,
        visited: MutableSet<String>,
        path: MutableList<String>
    ): List<String>? {
        if (path.contains(current)) {
            val cycleStart = path.indexOf(current)
            return path.subList(cycleStart, path.size) + current
        }
        if (visited.contains(current)) return null

        visited.add(current)
        path.add(current)

        val neighbors = graph[current] ?: emptyList()
        for (n in neighbors) {
            val cycle = detectCycle(n, graph, visited, path)
            if (cycle != null) return cycle
        }

        path.removeAt(path.size - 1)
        return null
    }
}
