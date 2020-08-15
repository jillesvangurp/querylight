import io.kotest.matchers.shouldBe
import search.Document
import search.DocumentIndex
import search.MatchQuery
import search.TextFieldIndex
import kotlin.random.Random
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
        val documentIndex = documentIndex()

        val hits = documentIndex.search(MatchQuery("title", "Elasticsearch"))
        hits.size shouldBe 1

    }

}

data class SampleObject(val title: String, val description: String, val id: String = Random.nextLong(0,Long.MAX_VALUE).toString()) {
    fun toDoc() = Document(
        id, mapOf(
            "title" to listOf(title),
            "description" to listOf(description)
        )
    )
}

fun documentIndex(): DocumentIndex {
    val documentIndex = DocumentIndex(
        mutableMapOf(
            "title" to TextFieldIndex(),
            "description" to TextFieldIndex()
        )
    )

    listOf(
        SampleObject(
            "Lorem ipsum", """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
            """.trimIndent()
        ),
        SampleObject(
            "Hamlet",
            """A famous play by Shakespeare that contains a nice edge case for search engines "To Be, Or Not To Be" consisting of stop words."""
        ),
        SampleObject("Ktjsearch", "Ktjsearch is an alternative to both solr and elasticsearch that does not use lucene."),
        SampleObject(
            "Solr",
            "An alternative to Elasticsearch that lives in the same OSS project as Apache Lucene, which is used by both."
        ),
        SampleObject(
            "Elasticsearch",
            "Something you should consider using instead of this. Unless you need offline search of course."
        )
    ).map(SampleObject::toDoc).forEach(documentIndex::index)
    return documentIndex
}
