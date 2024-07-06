package search

import kotlin.math.log10

typealias Hit = Pair<String,Double>
typealias Hits = List<Hit>

class TextFieldIndex(val analyzer: Analyzer = Analyzer(), val queryAnalyzer: Analyzer = Analyzer()) {
    // docid -> word count so we can calculate tf
    private val termCounts = mutableMapOf<String,Int>()
    private val reverseMap = mutableMapOf<String,MutableList<String>>()
    private val trie = SimpleStringTrie()

    fun add(docId: String, text: String) {
        analyzer.analyze(text).forEach { term ->
            termCounts[docId] = termCounts.getOrElse(docId) { 0 } + 1
            (reverseMap.getOrPut(term) { mutableListOf() }).add(docId)
            trie.add(term)
        }
    }

    /**
     * Returns a list of docId to tf/idf score
     */
    fun searchTerm(term: String, allowPrefixMatch: Boolean = false): List<Pair<String, Double>> {
        // https://en.wikipedia.org/wiki/Tf%E2%80%93idf

        val matches = termMatches(term) ?: if(allowPrefixMatch) trie.match(term).flatMap { t -> termMatches(t) ?: listOf() }
            .distinct().takeIf { it.isNotEmpty() } else null
        return when {
            matches != null -> {
                calculateTfIdf(matches)
            }
            allowPrefixMatch -> {
                calculateTfIdf(trie.match(term)).boost(0.1)
            }
            else -> {
                emptyList()
            }
        }
    }

    fun searchPrefix(prefix: String): List<Pair<String, Double>> {
        val terms = trie.match(prefix)
        val docIds = terms.flatMap { termMatches(it) ?: listOf() }.distinct()
        return calculateTfIdf(docIds)
    }

    fun termMatches(term: String) = reverseMap[term]

    private fun calculateTfIdf(docIds: List<String>?): List<Pair<String, Double>> {
        // https://en.wikipedia.org/wiki/Tf%E2%80%93idf

        val termCountsPerDoc = mutableMapOf<String, Int>()
        val matchedDocs = mutableSetOf<String>()
        docIds?.forEach { docId ->
            termCountsPerDoc[docId] = termCountsPerDoc.getOrElse(docId) { 0 } + 1
            matchedDocs.add(docId)
        }
        val idf = if(matchedDocs.isEmpty()) 0.0 else log10((termCounts.size.toDouble() / matchedDocs.size.toDouble()))
        val unsorted = termCountsPerDoc.map { (docId, termCount) ->
            val wordCountForDoc = wordCount(docId)

            val tc = termCount.toDouble()
            val wc = wordCountForDoc.toDouble()

            // avoid divide by zero and default to 0
            val tf = if (wordCountForDoc == 0) 0.0 else {
                tc / wc
            }

            val tfIdf = tf * idf
            docId to tfIdf
        }
        return unsorted.sortedByDescending { (_, tfIdf) -> tfIdf }
    }


    private fun wordCount(docId: String) = termCounts[docId] ?: 0

    /**
     * Returns the top N significant terms as a Map<String, Pair<Double, Int>>
     * where the Double is the aggregated TF-IDF score and the Int is the document count
     */
    fun getTopSignificantTerms(n: Int): Map<String, Pair<Double, Int>> {
        val termScores = mutableMapOf<String, Pair<Double, Int>>()

        // Calculate TF-IDF for each term and count documents
        reverseMap.forEach { (term, docIds) ->
            val tfIdfList = calculateTfIdf(docIds)
            val totalTfIdf = tfIdfList.sumOf { it.second }
            val docCount = docIds.distinct().size
            termScores[term] = Pair(totalTfIdf, docCount)
        }

        // Sort terms by TF-IDF score and take the top N
        return termScores.entries
            .sortedByDescending { it.value.first }
            .take(n)
            .associate { it.key to it.value }
    }
}
