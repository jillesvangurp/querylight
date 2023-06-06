import io.kotest.matchers.shouldBe
import search.MatchAll
import search.search
import kotlin.test.Test

class QueryTest {

    @Test
    fun shouldReturnDocs() {
        val index = testIndex()
        val results = index.search {
            from=0
            limit=3
            query = MatchAll()
        }
        results.size shouldBe 3
    }
}