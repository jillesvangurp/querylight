import io.kotest.matchers.shouldBe
import io.kotest.matchers.doubles.plusOrMinus
import search.*
import kotlin.test.Test

class RankingTest {
    private fun createDocs(): List<Document> {
        return listOf(
            Document("1", mapOf("text" to listOf("foo foo foo bar"))),
            Document("2", mapOf("text" to listOf("foo bar bar bar"))),
            Document("3", mapOf("text" to listOf("bar bar bar bar")))
        )
    }

    private fun index(algorithm: RankingAlgorithm): DocumentIndex {
        val index = DocumentIndex(mutableMapOf("text" to TextFieldIndex(rankingAlgorithm = algorithm)))
        createDocs().forEach(index::index)
        return index
    }

    @Test
    fun rankingShouldWorkForBothAlgorithms() {
        val tfidf = index(RankingAlgorithm.TFIDF)
        val bm25 = index(RankingAlgorithm.BM25)
        tfidf.search(MatchQuery("text", "foo")).first().first shouldBe "1"
        bm25.search(MatchQuery("text", "foo")).first().first shouldBe "1"
    }

    @Test
    fun bm25ScoresShouldMatchLucene() {
        val bm25 = index(RankingAlgorithm.BM25)
        val results = bm25.search(MatchQuery("text", "foo"))
        // expected scores calculated with Lucene's BM25 formula
        val expected = listOf(
            "1" to 0.783339382076226,
            "2" to 0.4700036292457356
        )
        results.size shouldBe expected.size
        results[0].first shouldBe expected[0].first
        results[0].second shouldBe (expected[0].second plusOrMinus 1e-6)
        results[1].first shouldBe expected[1].first
        results[1].second shouldBe (expected[1].second plusOrMinus 1e-6)
    }
}
