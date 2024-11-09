import io.kotest.matchers.shouldBe
import kotlin.test.Test
import search.cosineSimilarity
import search.hashFunction
import search.populateLSHBuckets

class AknnTest {

    // Sample vectors for testing
    private val vectors = mapOf(
        "id1" to listOf(1.0, 2.0, 3.0),
        "id2" to listOf(4.0, 5.0, 6.0),
        "id3" to listOf(7.0, 8.0, 9.0),
        "id4" to listOf(1.1, 2.1, 3.1)
    )

    @Test
    fun testCosineSimilarity() {
        val v1 = listOf(1.0, 2.0, 3.0)
        val v2 = listOf(1.0, 2.0, 3.0)
        val similarity = cosineSimilarity(v1, v2)
        similarity shouldBe 1.0f
    }

    @Test
    fun testHashFunction() {
        val vector = listOf(1.0, 2.0, 3.0)
        val randomVectors = listOf(
            listOf(0.1, 0.3, 0.5),
            listOf(0.7, 0.2, 0.6)
        )
        val hash = hashFunction(vector, randomVectors)
        hash shouldBe hashFunction(vector, randomVectors) // Hash should be consistent
    }

    @Test
    fun testPopulateLSHBuckets() {
        val randomVectors = listOf(
            listOf(0.1, 0.3, 0.5),
            listOf(0.7, 0.2, 0.6)
        )
        val buckets = populateLSHBuckets(vectors, randomVectors)
        buckets.isNotEmpty() shouldBe true
    }
}