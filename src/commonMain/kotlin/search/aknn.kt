package search

import kotlin.math.sqrt
import kotlin.random.Random

// Define a typealias for better readability
typealias Vector = List<Double>

class VectorFieldIndex(
    private val numHashTables: Int,
    private val dimensions: Int

) : FieldIndex {

    private val vectors = mutableMapOf<String, List<Vector>>()
    private val allBuckets = mutableListOf<MutableMap<Int, MutableList<Pair<String, Vector>>>>()
    private val randomVectorsList = mutableListOf<List<Vector>>()

    override val indexState: IndexState
        get() = TODO("Not yet implemented")

    override fun loadState(fieldIndexState: IndexState): FieldIndex {
        TODO("Not yet implemented")
    }

    init {
        // Generate multiple hash tables with different random vectors
        repeat(numHashTables) {
            val randomVectors = List(dimensions) { normalizeVector(generateRandomVector(dimensions)) }
            randomVectorsList.add(randomVectors)
            allBuckets.add(mutableMapOf())
        }
    }

    /**
     * Add embeddings for a document id.
     */
    fun insert(id: String, embeddings: List<Vector>) {
        vectors[id] = embeddings
        for(vector in embeddings) {
            for (i in 0 until numHashTables) {
                val randomVectors = randomVectorsList[i]
                val buckets = allBuckets[i]
                val hash = hashFunction(vector, randomVectors)

                buckets.getOrPut(hash) { mutableListOf() }.add(Pair(id, vector))
            }
        }
    }

    /**
     * Method to lookup best matching document IDs
     */
    fun query(vector: Vector, k: Int, filterIds: List<String>? = null): List<Hit> {
        val candidates = mutableSetOf<Pair<String, Vector>>()

        for (i in 0 until numHashTables) {
            val randomVectors = randomVectorsList[i]
            val hash = hashFunction(vector, randomVectors)
            val buckets = allBuckets[i]
            buckets[hash]?.let {
                if (filterIds != null) {
                    candidates.addAll(it.filter { pair -> pair.first in filterIds })
                } else {
                    candidates.addAll(it)
                }
            }
        }

        val similarities = candidates.map { Pair(it.first, cosineSimilarity(vector, it.second)) }
        return similarities.sortedByDescending { it.second }.take(k).map { it.first to it.second }
    }
}

// Calculate cosine similarity between two vectors
fun cosineSimilarity(v1: Vector, v2: Vector): Double {
    require(v1.size == v2.size) { "Vectors must be of the same size" }
    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0
    for (i in v1.indices) {
        dotProduct += v1[i] * v2[i]
        normA += v1[i] * v1[i]
        normB += v2[i] * v2[i]
    }
    return dotProduct / (sqrt(normA) * sqrt(normB))
}

// Generate random vector
fun generateRandomVector(dimensions: Int): Vector {
    return List(dimensions) { Random.nextDouble() }
}

// Normalize a vector
fun normalizeVector(vector: Vector): Vector {
    val norm = sqrt(vector.sumOf { it * it })
    return vector.map { it / norm }
}

// Simple hash function for LSH using random projections
fun hashFunction(vector: Vector, randomVectors: List<Vector>): Int {
    var hash = 0
    for (i in randomVectors.indices) {
        val dotProduct = vector.zip(randomVectors[i]).sumOf { it.first * it.second }
        if (dotProduct > 0) hash = hash or (1 shl i)
    }
    return hash
}

// Populate LSH buckets
fun populateLSHBuckets(vectors: Map<String, Vector>, randomVectors: List<Vector>): Map<Int, MutableList<Pair<String, Vector>>> {
    val buckets = mutableMapOf<Int, MutableList<Pair<String, Vector>>>()
    for ((id, vector) in vectors) {
        val hash = hashFunction(vector, randomVectors)

        buckets.getOrPut(hash) { mutableListOf() }.add(Pair(id, vector))
    }
    return buckets
}
