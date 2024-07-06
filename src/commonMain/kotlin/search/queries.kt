@file:Suppress("unused")

package search

interface Query {
    val boost: Double?
    fun hits(documentIndex: DocumentIndex, context: QueryContext = QueryContext()): Hits
}

internal val Query.normalizedBoost get() = boost ?: 1.0

internal fun Hits.boost(boost: Double) = if (boost != 1.0) {
    map { it.first to it.second * boost }
} else {
    this
}

enum class OP {
    AND, OR
}

class BoolQuery(
    private val should: List<Query> = listOf(),
    private val must: List<Query> = listOf(),
    private val filter: List<Query> = listOf(),
    private val mustNot: List<Query> = listOf(),
    override val boost: Double? = null
) : Query {
    override fun hits(documentIndex: DocumentIndex, context: QueryContext): Hits {
        if (filter.isEmpty() && should.isEmpty() && must.isEmpty()) {
            throw IllegalArgumentException("should specify at least one of filter, must, or should")
        }
        context.withFilterMode {
            val excludedHits = mustNot.map {
                val excluded = it.hits(documentIndex, this)
                context.exclude(excluded.ids())
                excluded
            }
            context.exclude(
                if (excludedHits.isNotEmpty()) excludedHits.reduce(Hits::and).map { it.first } else emptyList())

            val filtered = filter.map { it.hits(documentIndex, this) }
            if (filtered.isNotEmpty()) {
                val reduced = filtered.reduce(Hits::and)
                context.include(reduced.ids())
            }
        }
        val mustHits = if (must.isEmpty() && filter.isNotEmpty()) {
            context.hits()
        } else {
            val mappedMusts = must.map { it.hits(documentIndex, context) }
            if (mappedMusts.isNotEmpty()) {
                if (filter.isNotEmpty()) {
                    (listOf(context.hits()) + mappedMusts).reduce(Hits::and)

                } else {
                    mappedMusts.reduce(Hits::and)
                }
            } else {
                emptyList()
            }
        }

        if (must.isNotEmpty()) {
            context.setIncludeIds(mustHits.ids())
        }
        val mappedShoulds = should.map { it.hits(documentIndex, context) }
        val shouldHits = if (mappedShoulds.isNotEmpty()) mappedShoulds.reduce(Hits::or) else emptyList()
        return when {
            must.isEmpty() && should.isEmpty() -> mustHits // results from the filter are put here
            filter.isEmpty() && should.isEmpty() -> mustHits // whatever came out of evaluating the must clauses
            must.isEmpty() && filter.isEmpty() -> shouldHits // whatever came out of the
            filter.isEmpty() -> {
                when {
                    should.isEmpty() -> mustHits
                    must.isEmpty() -> shouldHits
                    else -> mustHits.and(shouldHits)
                }
            }

            else -> if (shouldHits.isEmpty()) mustHits else mustHits.and(shouldHits)
        }.boost(normalizedBoost)
    }
}

class TermQuery(
    private val field: String,
    private val text: String,
    override val boost: Double? = null,
) : Query {
    override fun hits(documentIndex: DocumentIndex, context: QueryContext): Hits {
        return (documentIndex.getFieldIndex(field)?.let {
            it.termMatches(text)?.map { Hit(it.first, 1.0) }
        } ?: emptyList()).boost(normalizedBoost)
    }
}

class MatchQuery(
    private val field: String,
    private val text: String,
    private val operation: OP = OP.AND,
    private val prefixMatch: Boolean = false,
    override val boost: Double? = null,
) : Query {
    override fun hits(documentIndex: DocumentIndex, context: QueryContext): Hits {
        val fieldIndex = documentIndex.getFieldIndex(field)
        if (fieldIndex != null) {
            val searchTerms = fieldIndex.queryAnalyzer.analyze(text)
            val collectedHits = mutableMapOf<String, Double>()

            if (operation == OP.AND) {
                // start with the smallest list
                val termHits = searchTerms.map {
                    fieldIndex.searchTerm(it,prefixMatch)
                }.sortedBy { it.size }
                // quick check to see if we can return right away (if one of the terms did not match, we have no hits)
                if (termHits.isEmpty() || termHits[0].isEmpty()) {
                    listOf<Hit>()
                } else {
                    termHits.first().forEach { (docId, score) ->
                        collectedHits[docId] = score
                    }
                    // plenty of potential to optimize this later
                    // if we have an efficient way to look up keys from sub lists, simply looking up each
                    // of the keys in the smallest list in the other lists is potentially faster if we have
                    // some terms with a lot of hits.
                    termHits.subList(1, termHits.size).forEach {
                        it.forEach { hit ->
                            if (collectedHits.containsKey(hit.first)) {
                                collectedHits[hit.first] = hit.second + (collectedHits[hit.first] ?: 0.0)
                            }
                        }
                    }
                }
            } else {
                val termHits = searchTerms.map { fieldIndex.searchTerm(it,prefixMatch) }
                termHits.first().forEach {
                    collectedHits[it.first] = it.second
                }
                termHits.subList(1, termHits.size).forEach {
                    it.forEach { hit ->
                        collectedHits[hit.first] = hit.second + (collectedHits[hit.first] ?: 0.0)
                    }
                }
            }
            return collectedHits.map { it.key to it.value }.sortedByDescending { it.second }.boost(normalizedBoost)
        } else {
            return emptyList()
        }
    }
}

class MatchPhrase(
    private val field: String,
    private val text: String,
    private val slop: Int=0,
    override val boost: Double? = null,
): Query {
    override fun hits(documentIndex: DocumentIndex, context: QueryContext): Hits {
        val fieldIndex = documentIndex.getFieldIndex(field)
        if (fieldIndex != null) {
            val searchTerms = fieldIndex.queryAnalyzer.analyze(text)

            return fieldIndex.searchPhrase(searchTerms,slop)
        } else {
            return  emptyList()
        }
    }

}

class MatchAll(
    override val boost: Double? = null,
) : Query {
    override fun hits(documentIndex: DocumentIndex, context: QueryContext): Hits =
        documentIndex.ids().map { it to 1.0 }.boost(normalizedBoost)
}

fun Hits.ids() = this.map { it.first }

fun Hits.and(other: Hits): Hits {
    val (left, right) = if (this.size <= other.size) {
        this to other
    } else {
        other to this
    }
    val rightMap = right.toMap()
    return left.mapNotNull {

        val rightValue = rightMap[it.first]
        if (rightValue == null) {
            null
        } else {
            it.first to it.second + rightValue
        }
    }.filter { it.second > 0.0 }.sortedByDescending { it.second }
}

fun Hits.or(other: Hits): Hits {
    val collectedHits = mutableMapOf<String, Double>()
    collectedHits.putAll(this)
    other.forEach { hit ->
        collectedHits[hit.first] = hit.second + (collectedHits[hit.first] ?: 0.0)
    }
    return collectedHits.entries.map { it.key to it.value }.filter { it.second > 0.0 }.sortedByDescending { it.second }
}

