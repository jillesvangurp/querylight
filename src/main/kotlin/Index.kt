import kotlin.math.log10

class Index {
    // docid -> word count so we can calculate tf
    val wordCount = mutableMapOf<Int,Int>()
    val reverseMap = mutableMapOf<String,MutableList<Int>>()

    fun add(term: String, docId: Int) {
        wordCount[docId] = wordCount.getOrElse(docId, {0}) + 1
        (reverseMap.getOrPut(term) { mutableListOf() }).add(docId)
    }

    /**
     * Returns a list of docId to tf/idf score
     */
    fun get(term: String): List<Pair<Int, Double>> {
        // https://en.wikipedia.org/wiki/Tf%E2%80%93idf

        val docIds = reverseMap[term]
        val termCountsPerDoc = mutableMapOf<Int,Int>()
        val matchedDocs = mutableSetOf<Int>()
        docIds?.forEach {
            termCountsPerDoc[it] = termCountsPerDoc.getOrElse(it,{0}) + 1
            matchedDocs.add(it)
        }

        val idf = log10((wordCount.size.toDouble() / matchedDocs.size.toDouble()))
        return termCountsPerDoc.map {(docId, termCount) ->
            val wordCountForDoc = wordCount(docId)

            val tc = termCount.toDouble()
            val wc = wordCountForDoc.toDouble()

            // avoid divide by zero and default to 0
            val tf = if(wordCountForDoc==0) 0.0 else {
                tc / wc
            }

            val tfIdf = tf * idf
            docId to tfIdf
        }.sortedByDescending { (_,tfIdf) -> tfIdf }
    }

    private fun wordCount(docId: Int) =
        (wordCount[docId] ?: throw IllegalStateException("word count not found for $docId"))
}
