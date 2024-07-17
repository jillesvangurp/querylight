import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import search.count

class IndexStateSerializationTest {
    @Test
    fun shouldLoadSavedStateAndStillWork() {
        val originalIndex = quotesIndex()
        val ogCount = originalIndex.count { }

        val state =  originalIndex.indexState
        val loadedIndex = originalIndex.loadState(state)
        loadedIndex.mapping["description"]?.textFieldIndexState?.reverseMap?.size shouldNotBe 0
        loadedIndex.count {  } shouldBe ogCount
    }
}