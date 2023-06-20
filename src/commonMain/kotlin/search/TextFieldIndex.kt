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
    fun searchTerm(term: String): List<Pair<String, Double>> {
        // https://en.wikipedia.org/wiki/Tf%E2%80%93idf

        return calculateTfIdf(termMatches(term))
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


    private fun wordCount(docId: String) =
        (termCounts[docId] ?: throw IllegalStateException("word count not found for $docId"))
}
