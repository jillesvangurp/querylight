import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
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

    @Test
    fun shouldBoostThings() {
        val index = quotesIndex()
        val results = index.search {
            query = BoolQuery(
                should = listOf(
                    MatchQuery(SampleObject::description.name, "to be", boost = 0.5),
                    MatchQuery(SampleObject::description.name, "basic", boost = 20.0)
                )
            )
        }.first().let { (id,_) ->
            // partial match for to be wins here because the massive boost on basic
            index.get(id)?.fields?.get(SampleObject::title.name)?.first() shouldStartWith "Philip K. Dick"
        }
    }
}