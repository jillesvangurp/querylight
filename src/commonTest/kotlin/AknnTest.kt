import io.kotest.matchers.shouldBe
import kotlin.test.Test
import search.cosineSimilarity
import search.hashFunction
import search.populateLSHBuckets
import search.VectorFieldIndex
import search.VectorFieldIndexState
import kotlin.random.Random
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

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

    @Test
    fun testVectorFieldIndexQuery() {
        val index = VectorFieldIndex(2, 3, Random(42))
        index.insert("id1", listOf(listOf(1.0, 2.0, 3.0)))
        index.insert("id2", listOf(listOf(4.0, 5.0, 6.0)))
        index.insert("id3", listOf(listOf(1.1, 2.1, 3.1)))

        val result = index.query(listOf(1.0, 2.0, 3.0), 1)
        result.first().first shouldBe "id1"
    }

    @Test
    fun testVectorFieldIndexStateRoundTrip() {
        val index = VectorFieldIndex(2, 3, Random(42))
        index.insert("id1", listOf(listOf(1.0, 2.0, 3.0)))
        index.insert("id2", listOf(listOf(4.0, 5.0, 6.0)))

        val state = index.indexState as VectorFieldIndexState
        val loaded = index.loadState(state) as VectorFieldIndex

        val result = loaded.query(listOf(1.0, 2.0, 3.0), 1)
        result.first().first shouldBe "id1"
    }

    @Test
    fun testVectorFieldIndexSerialization() {
        val index = VectorFieldIndex(2, 3, Random(42))
        index.insert("id1", listOf(listOf(1.0, 2.0, 3.0)))

        val json = Json.encodeToString(index.indexState as VectorFieldIndexState)
        val state = Json.decodeFromString<VectorFieldIndexState>(json)
        val loaded = index.loadState(state) as VectorFieldIndex

        val result = loaded.query(listOf(1.0, 2.0, 3.0), 1)
        result.first().first shouldBe "id1"
    }
}