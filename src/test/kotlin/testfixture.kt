import search.Document
import search.DocumentIndex
import search.TextFieldIndex
import kotlin.random.Random

data class SampleObject(
    val title: String,
    val description: String,
    val id: String = Random.nextLong(0, Long.MAX_VALUE).toString()
) {
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

            id = "lorem",
            title = "Lorem ipsum",
            description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
                """.trimIndent()
        ),
        SampleObject(
            id = "hamlet",
            title = "Hamlet",
            description = """A famous play by Shakespeare that contains a nice edge case for search engines "To Be, Or Not To Be" consisting of stop words."""
        ),
        SampleObject(
            id="ktjsearch",
            title = "Ktjsearch",
            description = "Ktjsearch is an alternative to both solr and elasticsearch that does not use lucene."
        ),
        SampleObject(
            id="solr",
            title = "Apache Solr & Lucene",
            description = "An alternative to Elasticsearch that lives in the same OSS project as Apache Lucene, which is used by both."
        ),
        SampleObject(
            id="es",
            title = "Elasticsearch, you know for search",
            description = "Elasticsearch is something you should consider using instead of Ktjsearch. Unless you need offline search of course."
        )
    ).map(SampleObject::toDoc).forEach(documentIndex::index)
    return documentIndex
}
