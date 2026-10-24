package prasad.vennam.awesomeandroidwizard.generator

import prasad.vennam.awesomeandroidwizard.model.NetworkingFramework
import prasad.vennam.awesomeandroidwizard.model.WizardModel
import java.io.File

object RepositorySliceGenerator {

    data class RepositorySliceConfig(
        val entityName: String,
        val packageName: String,
        val targetDir: File,
        val networking: NetworkingFramework = NetworkingFramework.RETROFIT,
        val includeFake: Boolean = true
    )

    data class RepositoryGenerationResult(
        override val success: Boolean,
        val generatedFiles: List<File>,
        override val message: String
    ) : GeneratorResult

    fun scaffoldRepositorySlice(config: RepositorySliceConfig): RepositoryGenerationResult {
        val sanitized = WizardModel.sanitizeFeatureName(config.entityName)
        if (sanitized.isEmpty()) {
            return RepositoryGenerationResult(false, emptyList(), "Invalid entity/repository name.")
        }

        val pascal = WizardModel.toPascalCase(config.entityName)
        val filesCreated = mutableListOf<File>()

        val modelDir = File(config.targetDir, "model")
        val networkDir = File(config.targetDir, "network")
        val repoDir = File(config.targetDir, "repository")

        modelDir.mkdirs()
        networkDir.mkdirs()
        repoDir.mkdirs()

        // 1. Domain Model: <Pascal>.kt
        val modelFile = File(modelDir, "$pascal.kt")
        modelFile.writeText(generateDomainModelContent(config.packageName, pascal))
        filesCreated.add(modelFile)

        // 2. Network DTO & Mapper: Network<Pascal>.kt
        val networkFile = File(networkDir, "Network$pascal.kt")
        networkFile.writeText(generateNetworkDtoContent(config.packageName, pascal))
        filesCreated.add(networkFile)

        // 3. Repository Contract: <Pascal>Repository.kt
        val contractFile = File(repoDir, "${pascal}Repository.kt")
        contractFile.writeText(generateContractContent(config.packageName, pascal))
        filesCreated.add(contractFile)

        // 4. Default Implementation: Default<Pascal>Repository.kt
        val implFile = File(repoDir, "Default${pascal}Repository.kt")
        implFile.writeText(generateImplementationContent(config.packageName, pascal))
        filesCreated.add(implFile)

        // 5. Test Double Fake: Fake<Pascal>Repository.kt
        if (config.includeFake) {
            val fakeFile = File(repoDir, "Fake${pascal}Repository.kt")
            fakeFile.writeText(generateFakeContent(config.packageName, pascal))
            filesCreated.add(fakeFile)
        }

        return RepositoryGenerationResult(
            success = true,
            generatedFiles = filesCreated,
            message = "Repository Slice '$pascal' created (${filesCreated.size} files generated)."
        )
    }

    fun generateDomainModelContent(packageName: String, pascal: String): String = """
package $packageName.model

import kotlinx.serialization.Serializable

/**
 * Domain entity representing a $pascal.
 */
@Serializable
data class $pascal(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
""".trimIndent()

    fun generateNetworkDtoContent(packageName: String, pascal: String): String = """
package $packageName.network

import $packageName.model.$pascal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network Data Transfer Object (DTO) for $pascal.
 */
@Serializable
data class Network$pascal(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("timestamp") val timestamp: Long? = null
)

/**
 * Convert Network$pascal DTO to domain model $pascal.
 */
fun Network$pascal.toDomain(): $pascal = $pascal(
    id = id,
    title = title,
    description = description.orEmpty(),
    timestamp = timestamp ?: System.currentTimeMillis()
)
""".trimIndent()

    fun generateContractContent(packageName: String, pascal: String): String = """
package $packageName.repository

import $packageName.model.$pascal
import kotlinx.coroutines.flow.Flow

/**
 * Clean Architecture repository contract for $pascal data operations.
 */
interface ${pascal}Repository {
    fun get${pascal}s(): Flow<List<$pascal>>
    suspend fun get${pascal}ById(id: String): Result<$pascal>
    suspend fun refresh(): Result<Unit>
}
""".trimIndent()

    fun generateImplementationContent(packageName: String, pascal: String): String = """
package $packageName.repository

import $packageName.model.$pascal
import $packageName.network.Network$pascal
import $packageName.network.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Default production implementation of ${pascal}Repository.
 */
class Default${pascal}Repository : ${pascal}Repository {

    override fun get${pascal}s(): Flow<List<$pascal>> = flow {
        // Emit initial / cached data mapped to domain entities
        val simulatedNetwork = listOf(
            Network$pascal(id = "1", title = "$pascal Alpha", description = "Initial $pascal record"),
            Network$pascal(id = "2", title = "$pascal Beta", description = "Secondary $pascal record")
        )
        emit(simulatedNetwork.map { it.toDomain() })
    }

    override suspend fun get${pascal}ById(id: String): Result<$pascal> = runCatching {
        $pascal(
            id = id,
            title = "$pascal #${'$'}id",
            description = "Detailed record for $pascal ${'$'}id"
        )
    }

    override suspend fun refresh(): Result<Unit> = runCatching {
        // Network synchronization call
    }
}
""".trimIndent()

    fun generateFakeContent(packageName: String, pascal: String): String = """
package $packageName.repository

import $packageName.model.$pascal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Test fake implementation of ${pascal}Repository for reliable unit tests.
 */
class Fake${pascal}Repository : ${pascal}Repository {

    private val dataFlow = MutableStateFlow<List<$pascal>>(emptyList())

    fun emitItems(items: List<$pascal>) {
        dataFlow.value = items
    }

    override fun get${pascal}s(): Flow<List<$pascal>> = dataFlow.asStateFlow()

    override suspend fun get${pascal}ById(id: String): Result<$pascal> {
        val found = dataFlow.value.find { it.id == id }
        return if (found != null) {
            Result.success(found)
        } else {
            Result.failure(NoSuchElementException("$pascal with id '${'$'}id' not found in test fake."))
        }
    }

    override suspend fun refresh(): Result<Unit> = Result.success(Unit)
}
""".trimIndent()
}
