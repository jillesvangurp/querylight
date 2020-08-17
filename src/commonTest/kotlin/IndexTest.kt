import io.kotest.matchers.shouldBe
import search.MatchQuery
import search.TextFieldIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexTest {
    @Test
    fun shouldAddTerms() {
        val index = TextFieldIndex()
        index.add("1", "foo")
        index.add("1", "foo")
        index.add("1", "bar")
        index.add("1", "bar")

        index.add("2", "foo")
        index.add("2", "foobar")
        index.add("2", "foobar")
        index.add("2", "foobar")

        index.add("3", "bar")
        index.add("3", "foobar")

        val results = index.searchTerm("foo")
        assertEquals(results.size, 2)

        assertEquals(results[0].first, "1")
        assertEquals(results[1].first, "2")
    }

    @Test
    fun shouldIndexDocuments() {
        val index = testIndex()

        val hits = index.search(MatchQuery("title", "Elasticsearch"))
        hits.size shouldBe 1
    }
}

