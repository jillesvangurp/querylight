package search

import kotlin.math.log10
import kotlinx.serialization.Serializable

typealias Hit = Pair<String, Double>
typealias Hits = List<Hit>

@Serializable
data class TermPos(val id: String, val position:Int)

@Serializable
data class IndexState(
    val termCounts: Map<String,Int>,
    val reverseMap: Map<String,List<TermPos>>,
    val trie: TrieNode,
)

class TextFieldIndex(
    val analyzer: Analyzer = Analyzer(),
    val queryAnalyzer: Analyzer = Analyzer(),
    private val termCounts: MutableMap<String, Int> = mutableMapOf(),
    private val reverseMap: MutableMap<String, MutableList<TermPos>> = mutableMapOf(),
    private val trie: SimpleStringTrie = SimpleStringTrie()
) {
    val indexState get() = IndexState(termCounts, reverseMap, trie.root)

    fun loadState(indexState: IndexState): TextFieldIndex {
        return TextFieldIndex(
            analyzer = analyzer, queryAnalyzer = queryAnalyzer,
            termCounts = indexState.termCounts.toMutableMap(),
            reverseMap = indexState.reverseMap.map { (k,v)-> k to v.toMutableList() }.toMap().toMutableMap(),
            trie = SimpleStringTrie(indexState.trie)
        )
    }

    // docid -> word count so we can calculate tf

    fun add(docId: String, text: String) {
        val tokens = analyzer.analyze(text)
        val termPositions = mutableMapOf<String, MutableList<Int>>()

        tokens.forEachIndexed { index, term ->
            termPositions.getOrPut(term) { mutableListOf() }.add(index)
        }

        termPositions.forEach { (term, positions) ->
            termCounts[docId] = termCounts.getOrElse(docId) { 0 } + positions.size
            reverseMap.getOrPut(term) { mutableListOf() }.addAll(positions.map { TermPos(docId, it) })
            trie.add(term)
        }
    }

    /**
     * Returns a list of docId to tf/idf score
     */
    fun searchTerm(term: String, allowPrefixMatch: Boolean = false): List<Hit> {
        val matches = termMatches(term) ?: if (allowPrefixMatch) {
            trie.match(term).flatMap { t -> termMatches(t) ?: listOf() }.distinct().takeIf { it.isNotEmpty() }
        } else null

        return matches?.let { calculateTfIdf(it.map { it.id }) } ?: emptyList()
    }

    fun searchPhrase(terms: List<String>, slop: Int = 0): List<Hit> {
        if (terms.isEmpty()) return emptyList()

        val initialMatches = reverseMap[terms[0]]?.toMutableList() ?: return emptyList()
        val phraseMatches = mutableListOf<String>()

        initialMatches.forEach { (docId, startPos) ->
            var match = true
            for (i in 1 until terms.size) {
                val term = terms[i]
                val termPositions = reverseMap[term]?.filter { it.id == docId }?.map { it.position } ?: listOf()
                if (!termPositions.any { pos -> pos == startPos + i || (slop > 0 && pos in (startPos + i - slop)..(startPos + i + slop)) }) {
                    match = false
                    break
                }
            }
            if (match) {
                phraseMatches.add(docId)
            }
        }

        return calculateTfIdf(phraseMatches)
    }

    fun searchPrefix(prefix: String): List<Hit> {
        val terms = trie.match(prefix)
        val docIds = terms.flatMap { termMatches(it)?.map { it.id } ?: listOf() }.distinct()
        return calculateTfIdf(docIds)
    }

    fun termMatches(term: String): List<TermPos>? {
        return reverseMap[term]
    }

    fun filterTermsByRange(
        lt: String? = null,
        lte: String? = null,
        gt: String? = null,
        gte: String? = null,

        ): List<Hit> {
        val lower = gt ?: gte
        val lowerInclusive = gte != null
        val upper = lt ?: lte
        val upperInclusive = lte != null

        return reverseMap.keys.asSequence().filter { term ->
            val lowerClause = if (lower != null) {
                if (lowerInclusive) {
                    term >= lower
                } else {
                    term > lower
                }
            } else {
                true
            }
            val upperClause = if (upper != null) {
                if (upperInclusive) {
                    term <= upper
                } else {
                    term < upper
                }
            } else {
                true
            }
            lowerClause && upperClause
        }.flatMap {
            reverseMap[it].orEmpty()
        }.map {
            it.id
        }.distinct().map { it to 1.0 }.toList()
    }

    private fun calculateTfIdf(docIds: List<String>?): List<Pair<String, Double>> {
        val termCountsPerDoc = mutableMapOf<String, Int>()
        val matchedDocs = mutableSetOf<String>()
        docIds?.forEach { docId ->
            termCountsPerDoc[docId] = termCountsPerDoc.getOrElse(docId) { 0 } + 1
            matchedDocs.add(docId)
        }
        val idf = if (matchedDocs.isEmpty()) 0.0 else log10((termCounts.size.toDouble() / matchedDocs.size.toDouble()))
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
     * where the Double is the significance score and the Int is the document count
     */
    fun getTopSignificantTerms(n: Int, subsetDocIds: Set<String>): Map<String, Pair<Double, Int>> {
        val subsetTermCounts = mutableMapOf<String, Int>()
        val subsetTermDocs = mutableMapOf<String, MutableSet<String>>()

        // Calculate term frequencies in the subset
        subsetDocIds.forEach { docId ->
            reverseMap.forEach { (term, docIds) ->
                if (docIds.any { it.id == docId }) {
                    subsetTermCounts[term] = subsetTermCounts.getOrElse(term) { 0 } + 1
                    subsetTermDocs.getOrPut(term) { mutableSetOf() }.add(docId)
                }
            }
        }

        val totalDocs = termCounts.size.toDouble()
        val subsetSize = subsetDocIds.size.toDouble()
        val backgroundTermCounts = reverseMap.mapValues { it.value.size }

        // Calculate significance scores
        val termScores = mutableMapOf<String, Pair<Double, Int>>()

        subsetTermCounts.forEach { (term, subsetCount) ->
            val subsetFrequency = subsetCount / subsetSize
            val backgroundFrequency = backgroundTermCounts[term]?.div(totalDocs) ?: 0.0

            // Use a simple significance score: subset frequency divided by background frequency
            val significance = if (backgroundFrequency > 0) {
                subsetFrequency / backgroundFrequency
            } else {
                subsetFrequency
            }

            val docCount = subsetTermDocs[term]?.size ?: 0
            termScores[term] = Pair(significance, docCount)
        }

        // Sort terms by significance score and take the top N
        return termScores.entries
            .sortedByDescending { it.value.first }
            .take(n)
            .associate { it.key to it.value }
    }

    /**
     * Returns the top N terms by frequency as a Map<String, Int>
     * for the entire corpus or a specified subset of documents
     */
    fun termsAggregation(n: Int, subsetDocIds: Set<String>? = null): Map<String, Int> {
        val termCounts = mutableMapOf<String, Int>()

        if (subsetDocIds == null) {
            // Calculate term frequencies for the entire corpus
            reverseMap.forEach { (term, docIds) ->
                termCounts[term] = docIds.size
            }
        } else {
            // Calculate term frequencies for the specified subset
            subsetDocIds.forEach { docId ->
                reverseMap.forEach { (term, docIds) ->
                    if (docIds.any { it.id == docId }) {
                        termCounts[term] = termCounts.getOrElse(term) { 0 } + 1
                    }
                }
            }
        }

        // Sort terms by frequency and take the top N
        return termCounts.entries
            .sortedByDescending { it.value }
            .take(n)
            .associate { it.key to it.value }
    }
}