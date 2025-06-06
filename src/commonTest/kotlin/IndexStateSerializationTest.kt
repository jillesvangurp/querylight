import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import search.TextFieldIndexState
import search.TextFieldIndex
import search.RankingAlgorithm
import search.Bm25Config
import search.DocumentIndex
import search.count

class IndexStateSerializationTest {
    @Test
    fun shouldLoadSavedStateAndStillWork() {
        val originalIndex = quotesIndex()
        val ogCount = originalIndex.count { }

        val state =  originalIndex.indexState
        val loadedIndex = originalIndex.loadState(state)
        loadedIndex.mapping["description"]?.indexState?.let {
            it as TextFieldIndexState
            it.reverseMap
        }?.size shouldNotBe 0
        loadedIndex.count {  } shouldBe ogCount
    }

    @Test
    fun shouldPreserveRankingSettings() {
        val index = DocumentIndex(
            mutableMapOf(
                "title" to TextFieldIndex(rankingAlgorithm = RankingAlgorithm.BM25, bm25Config = Bm25Config(k1 = 2.0, b = 0.6)),
                "description" to TextFieldIndex(rankingAlgorithm = RankingAlgorithm.BM25, bm25Config = Bm25Config(k1 = 2.0, b = 0.6))
            )
        )

        listOf(
            SampleObject("foo", "bar"),
            SampleObject("bar", "foo")
        ).map(SampleObject::toDoc).forEach(index::index)

        val state = index.indexState
        val loaded = index.loadState(state)
        val loadedField = loaded.mapping["title"] as TextFieldIndex
        loadedField.rankingAlgorithm shouldBe RankingAlgorithm.BM25
        loadedField.bm25Config.k1 shouldBe 2.0
        loadedField.bm25Config.b shouldBe 0.6
    }
}