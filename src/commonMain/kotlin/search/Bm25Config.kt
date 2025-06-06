package search

import kotlinx.serialization.Serializable

@Serializable
data class Bm25Config(
    val k1: Double = 1.2,
    val b: Double = 0.75
)
