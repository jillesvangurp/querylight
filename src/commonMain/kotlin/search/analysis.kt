package search

import kotlin.math.min

interface TextFilter {
    fun filter(text: String): String
}

class LowerCaseTextFilter : TextFilter {
    override fun filter(text: String): String {
        return text.lowercase()
    }
}

interface Tokenizer {
    fun tokenize(text: String): List<String>
}


class SplittingTokenizer : Tokenizer {
    val re = Regex("""\s+""", RegexOption.MULTILINE)
    override fun tokenize(text: String): List<String> {
        return re.split(text).filter { it.isNotBlank() }.toList()
    }
}

interface TokenFilter {
    fun filter(tokens: List<String>): List<String>
}

class NgramTokenFilter(val ngramSize: Int) : TokenFilter {
    override fun filter(tokens: List<String>): List<String> {
        val joined = tokens.joinToString("")
        return if(joined.isBlank()) listOf()
        else if (joined.length < ngramSize) {
            listOf(joined)
        } else {
            (0..joined.length - ngramSize).map { i ->
                joined.subSequence(i, i+ngramSize).toString()
            }
        }.distinct()
    }
}

class EdgeNgramsTokenFilter(val minLength:Int, val maxLength:Int): TokenFilter {
    override fun filter(tokens: List<String>): List<String> {
        return tokens.flatMap { token ->
            if(token.length<=minLength)
                listOf(token)
            else {
                (minLength..min(maxLength, token.length)).flatMap { length ->
                    listOf(
                        token.subSequence(0,length).toString(),
                        token.subSequence(token.length-length,token.length).toString()
                    )
                }
            }
        }.distinct()
    }

}

class ElisionTextFilter : TextFilter {
    private val elisionRE = """['’]""".toRegex()
    override fun filter(text: String): String {
        return elisionRE.replace(text, "")
    }
}

class InterpunctionTextFilter : TextFilter {
    private val interpunctionRE = """[\\\]\['"!,.@#$%^&*()_+-={}|><`~±§?]""".toRegex()
    override fun filter(text: String): String {
        return interpunctionRE.replace(text, " ")
    }
}

class Analyzer(
    private val textFilters: List<TextFilter> = listOf(LowerCaseTextFilter(), ElisionTextFilter(), InterpunctionTextFilter()),
    private val tokenizer: Tokenizer = SplittingTokenizer(),
    private val tokenFilter: List<TokenFilter> = emptyList()
) {
    fun analyze(text: String): List<String> {
        var filtered = text
        textFilters.forEach { filtered = it.filter(filtered) }

        var tokens = tokenizer.tokenize(filtered)
        tokenFilter.forEach {
            tokens = it.filter(tokens)
        }
        return tokens
    }
}
