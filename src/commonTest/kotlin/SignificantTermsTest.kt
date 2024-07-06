import kotlin.test.Test

class SignificantTermsTest {
    @Test
    fun shouldCalculateMostSignificantTerms() {
        val index = quotesIndex()
        val terms = index.getFieldIndex("description")!!.getTopSignificantTerms(50)
        terms.forEach {(t,s) ->
            println("$t: $s")
        }
    }
}