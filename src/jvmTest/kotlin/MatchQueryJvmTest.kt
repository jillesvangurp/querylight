import kotlin.test.Test
import kotlin.test.assertEquals
import search.Document
import search.DocumentIndex
import search.MatchQuery
import search.OP
import search.RankingAlgorithm
import search.TextFieldIndex

class MatchQueryJvmTest {
    private fun buildIndex(
        rankingAlgorithm: RankingAlgorithm,
        documents: List<Document>
    ): DocumentIndex {
        val index = DocumentIndex(mutableMapOf("text" to TextFieldIndex(rankingAlgorithm = rankingAlgorithm)))
        documents.forEach(index::index)
        return index
    }

    @Test
    fun andOnlyReturnsDocsContainingAllTerms() {
        val documents = listOf(
            Document("1", mapOf("text" to listOf("alpha beta"))),
            Document("2", mapOf("text" to listOf("alpha gamma"))),
            Document("3", mapOf("text" to listOf("beta gamma")))
        )

        listOf(RankingAlgorithm.TFIDF, RankingAlgorithm.BM25).forEach { algorithm ->
            val index = buildIndex(algorithm, documents)
            val hits = index.search(MatchQuery("text", "alpha beta", operation = OP.AND))
            assertEquals(listOf("1"), hits.map { it.first })
        }
    }

    @Test
    fun andAggregatesScoresForIntersectedDocs() {
        val documents = listOf(
            Document("1", mapOf("text" to listOf("alpha beta alpha"))),
            Document("2", mapOf("text" to listOf("alpha alpha alpha"))),
            Document("3", mapOf("text" to listOf("beta beta beta")))
        )

        listOf(RankingAlgorithm.TFIDF, RankingAlgorithm.BM25).forEach { algorithm ->
            val index = buildIndex(algorithm, documents)
            val hits = index.search(MatchQuery("text", "alpha beta", operation = OP.AND))

            assertEquals(1, hits.size)
            assertEquals("1", hits.first().first)

            val fieldIndex = index.getFieldIndex("text") as TextFieldIndex
            val expectedScore = listOf("alpha", "beta").sumOf { term ->
                fieldIndex.searchTerm(term).firstOrNull { it.first == "1" }?.second ?: 0.0
            }

            assertEquals(expectedScore, hits.first().second, 1e-10)
        }
    }
}
