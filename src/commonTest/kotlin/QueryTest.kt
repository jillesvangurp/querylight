import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import search.BoolQuery
import search.MatchAll
import search.MatchQuery
import search.RankingAlgorithm
import search.search
import kotlin.test.Test
import search.MatchPhrase

class QueryTest {

    @Test
    fun shouldReturnDocs() {
        RankingAlgorithm.entries.forEach { alg ->
            val index = testIndex(alg)
            val results = index.search {
                from=0
                limit=3
                query = MatchAll()
            }
            results.size shouldBe 3
        }
    }

    @Test
    fun shouldFindShakespeare() {
        RankingAlgorithm.entries.forEach { alg ->
            val index = quotesIndex(alg)
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

    @Test
    fun shouldDoPhraseSearch() {
        RankingAlgorithm.entries.forEach { alg ->
            val index = quotesIndex(alg)
            val results= index.search {
                query = MatchPhrase("description", "to be or not to be")
            }
            // should find the shakespeare quote and nothing else
            results.size shouldBe 1
        }
    }

    @Test
    fun shouldBoostThings() {
        RankingAlgorithm.entries.forEach { alg ->
            val index = quotesIndex(alg)
            index.search {
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

    @Test
    fun shouldIncludePrefixes() {
        RankingAlgorithm.entries.forEach { alg ->
            val index = quotesIndex(alg)
            index.search {
                query = MatchQuery(SampleObject::description.name, "ba")
            }.size shouldBe 0
            index.search {
                query = MatchQuery(SampleObject::description.name, "ba", prefixMatch = true)
            }.size shouldBeGreaterThan 0
        }
    }
}