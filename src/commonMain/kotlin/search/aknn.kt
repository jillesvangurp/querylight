package search

import kotlin.math.sqrt
import kotlin.random.Random

// Define a typealias for better readability
typealias Vector = List<Double>

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
