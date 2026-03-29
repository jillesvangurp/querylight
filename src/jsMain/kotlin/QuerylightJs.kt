@file:Suppress("NON_EXPORTABLE_TYPE")
package search

import kotlinx.serialization.json.Json

@JsExport
class QuerylightJs private constructor(private val index: DocumentIndex) {
    companion object {
        fun fromJson(json: String): QuerylightJs {
            val state = Json.decodeFromString(DocumentIndexState.serializer(), json)
            val idx = DocumentIndex(mutableMapOf()).loadState(state)
            return QuerylightJs(idx)
        }
    }

    fun search(term: String, limit: Int = 20): Array<String> {
        val hits = index.search {
            this.limit = limit
            query = BoolQuery(
                should = listOf(
                    MatchQuery("title", term, prefixMatch = true),
                    MatchQuery("content", term, prefixMatch = true),
                    MatchQuery("tags", term, prefixMatch = true)
                )
            )
        }
        return hits.map { it.first }.toTypedArray()
    }

    fun document(id: String): Map<String, List<String>>? = index.get(id)?.fields
}
