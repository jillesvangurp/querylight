import search.Document
import search.DocumentIndex
import search.TextFieldIndex
import kotlin.random.Random
import search.Analyzer
import search.KeywordTokenizer

data class SampleObject(
    val title: String,
    val description: String,
    val tags: List<String> = listOf(),
    val id: String = Random.nextLong(0, Long.MAX_VALUE).toString()
) {
    fun toDoc() = Document(
        id, mapOf(
            "title" to listOf(title),
            "description" to listOf(description),
            "tags" to tags,
        )
    )
}

fun testIndex(): DocumentIndex {
    val documentIndex = DocumentIndex(
        mutableMapOf(
            "title" to TextFieldIndex(),
            "description" to TextFieldIndex(),
            "tags" to TextFieldIndex(),
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
            id = "querylight",
            title = "querylight",
            description = "querylight is an alternative to both solr and elasticsearch that does not use lucene."
        ),
        SampleObject(
            id = "solr",
            title = "Apache Solr & Lucene",
            description = "An alternative to Elasticsearch that lives in the same OSS project as Apache Lucene, which is used by both but not by querylight."
        ),
        SampleObject(
            id = "es",
            title = "Elasticsearch, you know for search",
            description = "Elasticsearch is something you should consider using instead of querylight. Unless you need offline search of course."
        )
    ).map(SampleObject::toDoc).forEach(documentIndex::index)
    return documentIndex
}


fun quotesIndex(): DocumentIndex {
    val documentIndex = DocumentIndex(
        mutableMapOf(
            "title" to TextFieldIndex(),
            "description" to TextFieldIndex(),
            "tags" to TextFieldIndex(
                analyzer = Analyzer(
                    textFilters = listOf(),
                    tokenizer = KeywordTokenizer()
                )
            ),
        )
    )
    listOf(
        SampleObject(
            "George Orwell, 1984",
            "War is peace. Freedom is slavery. Ignorance is strength.",
            tags = listOf("book")
        ),
        SampleObject(
            "Jane Austen, Pride and Prejudice",
            "It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife.",
            tags = listOf("book")
        ),
        SampleObject(
            "F. Scott Fitzgerald, The Great Gatsby",
            "So we beat on, boats against the current, borne back ceaselessly into the past.",
            tags = listOf("book")
        ),
        SampleObject(
            "William Shakespeare, Hamlet",
            "To be, or not to be: that is the question.",
            tags = listOf("book")
        ),
        SampleObject(
            "Douglas Adams, The Hitchhiker's Guide to the Galaxy",
            "The ships hung in the sky in much the same way that bricks don't.",
            tags = listOf("book", "science fiction","funny")
        ),
        SampleObject("Douglas Adams, The Hitchhiker's Guide to the Galaxy", "Don't Panic.", tags = listOf("book")),
        SampleObject(
            "Douglas Adams, The Restaurant at the End of the Universe",
            "Time is an illusion. Lunchtime doubly so.",
            tags = listOf("book","science fiction","funny")
        ),
        SampleObject(
            "Douglas Adams, Last Chance to See",
            "Human beings, who are almost unique in having the ability to learn from the experience of others, are also remarkable for their apparent disinclination to do so.",
            tags = listOf("book","science fiction","funny")
        ),
        SampleObject(
            "Terry Gilliam and Terry Jones, Monty Python and the Holy Grail",
            "Tis but a scratch.",
            tags = listOf("movie","funny")
        ),
        SampleObject(
            "Terry Gilliam and Terry Jones, Monty Python and the Holy Grail",
            "Nobody expects the Spanish Inquisition!",
            tags = listOf("movie","funny")
        ),
        SampleObject(
            "Terry Gilliam and Terry Jones, Monty Python and the Holy Grail",
            "Your mother was a hamster and your father smelt of elderberries.",
            tags = listOf("movie","funny")
        ),
        SampleObject(
            "Terry Gilliam and Terry Jones, Monty Python's Life of Brian",
            "He's not the Messiah, he's a very naughty boy!",
            tags = listOf("movie","funny")
        ),
        SampleObject(
            "Philip K. Dick, Do Androids Dream of Electric Sheep?",
            "You will be required to do wrong no matter where you go. It is the basic condition of life, to be required to violate your own identity.",
            tags = listOf("book","science fiction")
        ),
        SampleObject(
            "Orson Scott Card, Ender's Game",
            "In the moment when I truly understand my enemy, understand him well enough to defeat him, then in that very moment, I also love him.",
            tags = listOf("book","science fiction")
        ),
    ).map(SampleObject::toDoc).forEach(documentIndex::index)
    return documentIndex
}
