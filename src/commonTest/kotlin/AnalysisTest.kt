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
            analyze(",.foo -bar_\n\tfoo.") shouldContainInOrder listOf("foo", "bar", "foo")
            analyze("foo,bar,foo") shouldContainInOrder listOf("foo", "bar", "foo")
        }
    }

    @Test
    fun shouldStrip() {
        val re = "[\\]\\[]".toRegex()
        re.replace("[]", "") shouldBe ""
    }

    @Test
    fun shouldGenerateNgrams() {
        val tokens = Analyzer().analyze("madam i'm adam")
        val ngramTokenFilter = NgramTokenFilter(3)

        ngramTokenFilter.filter(tokens) shouldBe listOf("mad", "ada", "dam", "ami", "mim", "ima")
        ngramTokenFilter.filter(listOf()) shouldBe listOf()
        ngramTokenFilter.filter(listOf("1")) shouldBe listOf("1")
        ngramTokenFilter.filter(listOf("12")) shouldBe listOf("12")
        ngramTokenFilter.filter(listOf("123")) shouldBe listOf("123")
        ngramTokenFilter.filter(listOf("1234")) shouldBe listOf("123","234")
    }

    @Test
    fun shouldGenerateEdgeNgrams() {
        val tokens = Analyzer().analyze("madam i'm adam")

        val edgeNgramsTokenFilter = EdgeNgramsTokenFilter(2, 4)
        edgeNgramsTokenFilter.filter(tokens) shouldBe listOf(
            "ma", "am", "mad", "dam", "mada", "adam", "i", "m", "ad", "ada"
        )
        edgeNgramsTokenFilter.filter(listOf()) shouldBe listOf()
        edgeNgramsTokenFilter.filter(listOf("1")) shouldBe listOf("1")
        edgeNgramsTokenFilter.filter(listOf("12")) shouldBe listOf("12")
        edgeNgramsTokenFilter.filter(listOf("123")) shouldBe listOf("12", "23", "123")
        edgeNgramsTokenFilter.filter(listOf("1234")) shouldBe listOf("12", "34", "123", "234", "1234")
        edgeNgramsTokenFilter.filter(listOf("12345")) shouldBe listOf("12", "45", "123", "345", "1234", "2345")
    }
}
