import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.random.Random
import search.VectorFieldIndex
import search.bigramVector
import search.quotesIndex
import search.Analyzer

class SyntacticVectorTest {
    private val analyzer = Analyzer()

    @Test
    fun shouldFindHamletViaVectorSearch() {
        val docs = quotesIndex()
        val index = VectorFieldIndex(4, 36 * 36, Random(42))

        docs.documents.forEach { (id, doc) ->
            val text = doc.fields["description"]?.joinToString(" ") ?: ""
            val vec = bigramVector(analyzer.analyze(text))
            index.insert(id, listOf(vec))
        }

        val queryVec = bigramVector(analyzer.analyze("to be or not to be"))
        val result = index.query(queryVec, 1)

        val hamletId = docs.documents.entries.first { entry ->
            entry.value.fields["title"]?.any { it.contains("Hamlet") } == true
        }.key

        result.first().first shouldBe hamletId
    }
}

