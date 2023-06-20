import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import search.BoolQuery
import search.MatchAll
import search.MatchQuery
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

    @Test
    fun shouldFindShakespeare() {
        val index = quotesIndex()
        index.documents.size shouldBeGreaterThan 0
        val results = index.search {
            query = BoolQuery(
                should = listOf(
                    MatchQuery(SampleObject::description.name, "to be")
                )
            )
        }
        results.size shouldBeGreaterThan 0
        results.forEach { (id,score) ->
            println(id + " " + score + " " + index.documents[id]?.let { "${it.fields["description"]} (${it.fields["title"]})" })
        }
    }

}