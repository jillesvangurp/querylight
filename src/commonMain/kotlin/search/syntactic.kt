package search

/**
 * Generate a [Vector] with counts for all character bigrams.
 * Tokens are normalized to base36 (0-9,a-z) before processing.
 */
fun bigramVector(tokens: List<String>): Vector {
    val dims = 36 * 36
    val vector = MutableList(dims) { 0.0 }

    fun idx(c: Char): Int? = when(c) {
        in '0'..'9' -> c - '0'
        in 'a'..'z' -> c - 'a' + 10
        in 'A'..'Z' -> c - 'A' + 10
        else -> null
    }

    tokens.forEach { token ->
        val chars = token.filter { it.isLetterOrDigit() }.lowercase()
        if (chars.length > 1) {
            val codes = chars.mapNotNull { idx(it) }
            for(i in 0 until codes.size - 1) {
                val index = codes[i] * 36 + codes[i + 1]
                vector[index] = vector[index] + 1.0
            }
        }
    }
    return vector
}

fun bigramVector(text: String, analyzer: Analyzer = Analyzer()): Vector =
    bigramVector(analyzer.analyze(text))


