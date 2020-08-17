import io.kotest.matchers.shouldBe
import search.BoolQuery
import search.MatchQuery
import kotlin.test.Test

class BoolTest {
    
    @Test
    fun shouldFilterCorrectly() {
        val index = testIndex()

        val results = index.search(BoolQuery(filter = listOf(
            MatchQuery("title", "ktjsearch")
        )))

        results.forEach { println(it) }
        results.size shouldBe 1
    }

    @Test
    fun shouldOnlyFindKtsearch() {
        val index = testIndex()

        val esClause = MatchQuery("description", "elasticsearch")
        index.search(esClause).apply {
            size shouldBe 3
        }
        index.search(BoolQuery(
            must = listOf(esClause)
        )).apply {
            size shouldBe 3
        }
        index.search(BoolQuery(
            filter = listOf(MatchQuery("title","ktjsearch")),
            must = listOf(esClause)
        )).apply {
            size shouldBe 1
        }
    }

}
