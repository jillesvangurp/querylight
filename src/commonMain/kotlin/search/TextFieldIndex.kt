package search

import kotlin.math.log10

typealias Hit = Pair<String,Double>
typealias Hits = List<Hit>

class TextFieldIndex(val analyzer: Analyzer = Analyzer(), val queryAnalyzer: Analyzer = Analyzer()) {
    // docid -> word count so we can calculate tf
    val termCounts = mutableMapOf<String,Int>()
    val reverseMap = mutableMapOf<String,MutableList<String>>()

    fun add(docId: String, text: String) {
        analyzer.analyze(text).forEach {
            termCounts[docId] = termCounts.getOrElse(docId, {0}) + 1
            (reverseMap.getOrPut(it) { mutableListOf() }).add(docId)
        }
    }

    /**
     * Returns a list of docId to tf/idf score
     */
    fun searchTerm(term: String): List<Pair<String, Double>> {
        // https://en.wikipedia.org/wiki/Tf%E2%80%93idf

        val docIds = reverseMap[term]
        val termCountsPerDoc = mutableMapOf<String, Int>()
        val matchedDocs = mutableSetOf<String>()
        docIds?.forEach {
            termCountsPerDoc[it] = termCountsPerDoc.getOrElse(it, { 0 }) + 1
            matchedDocs.add(it)
        }
        val idf = log10((termCounts.size.toDouble() / matchedDocs.size.toDouble()))
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
        // println(term + " in " + reverseMap.map { it.key + " -> " + it.value }.joinToString("\n") + ": " + unsorted.size)
        return unsorted.sortedByDescending { (_,tfIdf) -> tfIdf }
    }

    private fun wordCount(docId: String) =
        (termCounts[docId] ?: throw IllegalStateException("word count not found for $docId"))
}
