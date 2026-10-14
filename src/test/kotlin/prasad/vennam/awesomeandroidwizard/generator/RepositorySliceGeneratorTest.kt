package prasad.vennam.awesomeandroidwizard.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import prasad.vennam.awesomeandroidwizard.model.NetworkingFramework
import java.io.File

class RepositorySliceGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testScaffoldRepositorySliceWithFake() {
        val targetDir = tempFolder.newFolder("productData")

        val config = RepositorySliceGenerator.RepositorySliceConfig(
            entityName = "Product",
            packageName = "com.example.app.core.data",
            targetDir = targetDir,
            networking = NetworkingFramework.RETROFIT,
            includeFake = true
        )

        val result = RepositorySliceGenerator.scaffoldRepositorySlice(config)

        assertTrue(result.success)
        assertEquals(5, result.generatedFiles.size)

        // 1. Model
        val modelFile = File(targetDir, "model/Product.kt")
        assertTrue(modelFile.exists())
        val modelText = modelFile.readText()
        assertTrue(modelText.contains("data class Product("))

        // 2. Network DTO
        val netFile = File(targetDir, "network/NetworkProduct.kt")
        assertTrue(netFile.exists())
        val netText = netFile.readText()
        assertTrue(netText.contains("data class NetworkProduct("))
        assertTrue(netText.contains("fun NetworkProduct.toDomain(): Product"))

        // 3. Contract
        val contractFile = File(targetDir, "repository/ProductRepository.kt")
        assertTrue(contractFile.exists())
        val contractText = contractFile.readText()
        assertTrue(contractText.contains("interface ProductRepository"))
        assertTrue(contractText.contains("fun getProducts(): Flow<List<Product>>"))

        // 4. Default Implementation
        val implFile = File(targetDir, "repository/DefaultProductRepository.kt")
        assertTrue(implFile.exists())
        val implText = implFile.readText()
        assertTrue(implText.contains("class DefaultProductRepository : ProductRepository"))

        // 5. Fake Implementation
        val fakeFile = File(targetDir, "repository/FakeProductRepository.kt")
        assertTrue(fakeFile.exists())
        val fakeText = fakeFile.readText()
        assertTrue(fakeText.contains("class FakeProductRepository : ProductRepository"))
    }

    @Test
    fun testScaffoldRepositorySliceWithoutFake() {
        val targetDir = tempFolder.newFolder("userData")

        val config = RepositorySliceGenerator.RepositorySliceConfig(
            entityName = "User",
            packageName = "com.example.app.core.data",
            targetDir = targetDir,
            networking = NetworkingFramework.KTOR,
            includeFake = false
        )

        val result = RepositorySliceGenerator.scaffoldRepositorySlice(config)

        assertTrue(result.success)
        assertEquals(4, result.generatedFiles.size)

        val fakeFile = File(targetDir, "repository/FakeUserRepository.kt")
        org.junit.Assert.assertFalse(fakeFile.exists())
    }
}
