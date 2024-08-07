package search

import kotlinx.serialization.Serializable

@Serializable
class Document(val id: String,val fields: Map<String, List<String>>)

@DslMarker
annotation class SearchQuery

@SearchQuery
class QueryDsl {
    var from=0
    var limit=200
    var query: Query = MatchAll()
}

fun DocumentIndex.search(block: QueryDsl.() -> Unit = {}) =
    QueryDsl().apply(block).let { query ->
        search(
            query.query,
            from = query.from,
            limit = query.limit
        )
    }

fun DocumentIndex.count( block: QueryDsl.() -> Unit = {}) = search(block).size
