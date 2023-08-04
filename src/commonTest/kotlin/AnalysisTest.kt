import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import search.Analyzer
import search.EdgeNgramsTokenFilter
import search.NgramTokenFilter
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

    @Test
    fun shouldGenerateNgrams() {
        val tokens = Analyzer().analyze("madam i'm adam")
        val ngramTokenFilter = NgramTokenFilter(3)
        val ngrams = ngramTokenFilter.filter(tokens)
        println(ngrams)

        ngrams shouldBe listOf("mad", "ada", "dam", "ami", "mim", "ima", "mad", "ada", "dam")
    }
    @Test
    fun shouldGenerateEdgeNgrams() {
        val tokens = Analyzer().analyze("madam i'm adam")
        val ngramTokenFilter = EdgeNgramsTokenFilter(2,4)
        val ngrams = ngramTokenFilter.filter(tokens)
        println(ngrams)

        ngrams shouldBe listOf("ma", "am", "mad", "dam", "mada", "adam", "i", "m", "ad", "am", "ada", "dam", "adam", "adam")
    }
}
