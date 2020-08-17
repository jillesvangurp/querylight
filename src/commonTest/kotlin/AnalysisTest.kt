import io.kotest.matchers.shouldBe
import search.Analyzer
import kotlin.test.Test

class AnalysisTest {
    @Test
    fun shouldTokenize() {
        val standardAnalyzer = Analyzer()

        standardAnalyzer.apply {
            analyze(",.foo -bar_\n\tfoo.") shouldBe listOf("foo","bar","foo")
        }
    }
}
