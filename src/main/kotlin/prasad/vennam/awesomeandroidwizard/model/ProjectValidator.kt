package prasad.vennam.awesomeandroidwizard.model

import java.io.File

/**
 * Validates project name, package name, and destination filesystem path
 * to prevent build failures, file overwrites, and invalid Android project generation.
 */
object ProjectValidator {

    private val RESERVED_KEYWORDS = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
        "volatile", "while", "true", "false", "null", "val", "var", "fun", "when", "object", "typealias",
        "in", "is", "as", "typeof"
    )

    private val WINDOWS_RESERVED_NAMES = setOf(
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    )

    private val ILLEGAL_NAME_CHARS = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|', '\'', '%')

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Warning(val message: String) : ValidationResult()
        data class Error(val message: String) : ValidationResult()

        val isError: Boolean get() = this is Error
        val isWarning: Boolean get() = this is Warning
        val isValid: Boolean get() = this is Valid

        val messageOrNull: String?
            get() = when (this) {
                is Error -> message
                is Warning -> message
                Valid -> null
            }
    }

    fun validateProjectName(name: String): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("Project name cannot be empty.")
        }
        if (trimmed.uppercase() in WINDOWS_RESERVED_NAMES) {
            return ValidationResult.Error("'$trimmed' is a reserved OS device name on Windows.")
        }
        if (trimmed.any { it in ILLEGAL_NAME_CHARS }) {
            return ValidationResult.Error("Project name contains illegal filesystem characters (e.g. / \\ : * ? \" < > |).")
        }
        if (trimmed.any { it.isISOControl() }) {
            return ValidationResult.Error("Project name contains invalid control characters.")
        }
        if (!trimmed.first().isLetterOrDigit()) {
            return ValidationResult.Error("Project name must start with an alphanumeric letter or digit.")
        }
        return ValidationResult.Valid
    }

    fun validatePackageName(pkg: String): ValidationResult {
        val trimmed = pkg.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("Package name cannot be empty.")
        }
        if (!trimmed.contains(".")) {
            return ValidationResult.Error("Package name must contain at least two segments (e.g. com.example.app).")
        }
        if (trimmed.startsWith(".") || trimmed.endsWith(".")) {
            return ValidationResult.Error("Package name cannot start or end with a dot.")
        }

        val segments = trimmed.split(".")
        for (segment in segments) {
            if (segment.isEmpty()) {
                return ValidationResult.Error("Package name cannot contain consecutive dots ('..').")
            }
            if (!segment.first().isLetter() && segment.first() != '_') {
                return ValidationResult.Error("Package segment '$segment' must start with a letter or underscore.")
            }
            if (!segment.all { it.isLetterOrDigit() || it == '_' }) {
                return ValidationResult.Error("Package segment '$segment' contains invalid characters.")
            }
            if (segment.lowercase() in RESERVED_KEYWORDS) {
                return ValidationResult.Error("Package segment '$segment' is a reserved keyword.")
            }
        }
        return ValidationResult.Valid
    }

    fun validateProjectLocation(path: String): ValidationResult {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("Project location cannot be empty.")
        }
        val file = File(trimmed)
        if (!file.isAbsolute) {
            return ValidationResult.Error("Project location must be an absolute path.")
        }

        if (file.exists() && !file.isDirectory) {
            return ValidationResult.Error("The specified location is an existing file, not a directory.")
        }

        if (file.exists() && file.isDirectory) {
            if (!file.canWrite()) {
                return ValidationResult.Error("Permission denied: directory is not writable (${file.absolutePath}).")
            }
            val children = file.listFiles()
            if (children != null && children.isNotEmpty()) {
                return ValidationResult.Warning("Directory already exists and is not empty (${children.size} items found).")
            }
        } else {
            var parent: File? = file.parentFile
            while (parent != null && !parent.exists()) {
                parent = parent.parentFile
            }
            if (parent != null && !parent.canWrite()) {
                return ValidationResult.Error("Permission denied: cannot write to ${parent.absolutePath}")
            }
        }

        return ValidationResult.Valid
    }

    fun validateAll(projectName: String, packageName: String, projectLocation: String): ValidationResult {
        val nameRes = validateProjectName(projectName)
        if (nameRes is ValidationResult.Error) return nameRes

        val pkgRes = validatePackageName(packageName)
        if (pkgRes is ValidationResult.Error) return pkgRes

        val locRes = validateProjectLocation(projectLocation)
        if (locRes is ValidationResult.Error) return locRes
        if (locRes is ValidationResult.Warning) return locRes

        return ValidationResult.Valid
    }
}
