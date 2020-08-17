import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import search.Analyzer
import kotlin.test.Test

class AnalysisTest {
    @Test
    fun shouldTokenize() {
        val standardAnalyzer = Analyzer()

        standardAnalyzer.apply {
            analyze("").size shouldBe 0
            analyze("""!@#$%^&*()_+=-{}][\\|'\"';:/?.>,<`~§±""").size shouldBe 0
            analyze("\n\t ").size shouldBe 0
            analyze(",.foo -bar_\n\tfoo.") shouldContainInOrder  listOf("foo","bar","foo")
            analyze("foo,bar,foo") shouldContainInOrder listOf("foo","bar","foo")
        }
    }

    @Test
    fun shouldStrip() {
        val re = "[\\]\\[]".toRegex()
        re.replace("[]","") shouldBe ""
    }
}
