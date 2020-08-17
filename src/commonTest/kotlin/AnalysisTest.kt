import io.kotest.matchers.shouldBe
import search.Analyzer
import kotlin.test.Test

class AnalysisTest {
    @Test
    fun shouldTokenize() {
        val standardAnalyzer = Analyzer()

        standardAnalyzer.apply {
            analyze("") shouldBe emptyList()
            analyze("""!@#$%^&*()_+=-{}][\\|'\"';:/?.>,<`~§±""") shouldBe emptyList()
            analyze("\n\t ") shouldBe emptyList()
            analyze(",.foo -bar_\n\tfoo.") shouldBe listOf("foo","bar","foo")
            analyze("foo,bar,foo") shouldBe listOf("foo","bar","foo")
        }
    }

    @Test
    fun shouldStrip() {
        val re = "[\\]\\[]".toRegex()
        re.replace("[]","") shouldBe ""
    }
}
