import io.kotest.matchers.shouldBe
import search.BoolQuery
import search.MatchQuery
import kotlin.test.Test

class BoolTest {
    @Test
    fun shouldFilterCorrectly() {
        val index = documentIndex()

        val results = index.search(BoolQuery(filter = listOf(
            MatchQuery("description", "ktjsearch")
        )))

        results.forEach { println(it) }
        results.size shouldBe 1
    }

}
