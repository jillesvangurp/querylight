import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import search.BoolQuery
import search.Document
import search.DocumentIndex
import search.Hits
import search.MatchQuery
import search.TextFieldIndex
import search.ids
import kotlin.test.Test

class BoolTest {
    
    @Test
    fun shouldFilterCorrectly() {
        val index = testIndex()

        val results = index.search(BoolQuery(filter = listOf(
            MatchQuery("title", "querylight")
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
            filter = listOf(MatchQuery("title","querylight")),
            must = listOf(esClause)
        )).apply {
            size shouldBe 1
        }
    }

    fun Hits.shouldAppearBefore(id1: String, id2: String) {
        val i1 = this.ids().indexOf(id1)
        val i2 = this.ids().indexOf(id2)
        i1 shouldBeGreaterThanOrEqual 0
        i2 shouldBeGreaterThanOrEqual 0
        i1 shouldBeLessThan i2
    }
    @Test
    fun shouldRank() {
        val index = testIndex()
        val q=BoolQuery(should = listOf(MatchQuery("title","querylight"), MatchQuery("description","querylight")))
        index.search(q).apply {
            forEach {
                println("hit: $it")
            }
            this.ids() shouldContainAll listOf("querylight", "es", "solr")
            // querylight ranks higher because it appears in the title and description
            this.shouldAppearBefore("querylight","es")
            this.shouldAppearBefore("querylight","solr")
        }
    }


    data class Foo(val id: String, val title:String) {
        fun doc() = Document(id, mapOf("title" to listOf(title)))
    }

    @Test
    fun shouldDoBooleanLogic() {
        val idx = DocumentIndex(mapping = mutableMapOf("title" to TextFieldIndex()))


        idx.index(Foo("1","foo").doc())
        idx.index(Foo("2","bar").doc())
        idx.index(Foo("3","foobar").doc())
        idx.index(Foo("4","foo bar").doc())
        idx.index(Foo("5","bar foo").doc())
        idx.index(Foo("6","barfoo").doc())
        idx.index(Foo("7","bar foo baz").doc())

        val fooClause = MatchQuery("title", "foo")
        val barClause = MatchQuery("title", "bar")
        val bazClause = MatchQuery("title", "baz")

        idx.search(BoolQuery(must = listOf(fooClause,barClause))).ids().apply {
            shouldNotContain("1")
            shouldContainAll(listOf("4","5","7"))
        }
        idx.search(BoolQuery(filter = listOf(fooClause,barClause))).ids().apply {
            shouldNotContain("1")
            shouldContainAll(listOf("4","5","7"))
        }
        idx.search(BoolQuery(
            filter = listOf(fooClause,barClause),
            must= listOf(bazClause))).ids().apply {
            shouldNotContainAll(listOf("1","4","5"))
            shouldContainAll(listOf("7"))
        }
        idx.search(BoolQuery(should = listOf(fooClause,barClause))).ids().apply {
            shouldContainAll(listOf("1","2","4","5","7"))
            shouldNotContainAll(listOf("6"))
        }
    }
}
