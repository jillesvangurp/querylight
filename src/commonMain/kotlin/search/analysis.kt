package search

interface TextFilter {
    fun filter(text: String): String
}

class LowerCaseTextFilter: TextFilter {
    override fun filter(text: String): String {
        return text.toLowerCase()
    }
}

interface Tokenizer {
    fun tokenize(text: String): List<String>
}


class SplittingTokenizer: Tokenizer {
    override fun tokenize(text: String): List<String> {
        return text.split(' ').toList()
    }
}

interface TokenFilter {
    fun filter(tokens: List<String>): List<String>
}

class InterpunctionTextFilter: TextFilter {
    private val interpunctionRE = """[-_.,\?\!\+=]""".toRegex()
    override fun filter(text: String): String {
        return interpunctionRE.replace(text, " ")
    }
}

class Analyzer(
    private val textFilters: List<TextFilter> = listOf(LowerCaseTextFilter(), InterpunctionTextFilter()),
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
