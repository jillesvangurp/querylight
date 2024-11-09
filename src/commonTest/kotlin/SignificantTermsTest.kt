import kotlin.test.Test
import search.TermQuery
import search.TextFieldIndex
import search.search

class SignificantTermsTest {
    @Test
    fun shouldCalculateMostSignificantTerms() {
        val index = quotesIndex()

        index.getFieldIndex("tags")!!.let { it as TextFieldIndex }.termsAggregation(10).forEach { (term, _)->
            val hits = index.search {
                query = TermQuery("tags",term)
            }
            val ids = hits.map {
                it.first
            }.toSet()
            println("""
                ## $term
                
            """.trimIndent())
            val terms = index.getFieldIndex("title")!!.let { it as TextFieldIndex }.getTopSignificantTerms(5,ids)
            terms.forEach {(t,s) ->
                println("$t: $s")
            }
        }
    }
}