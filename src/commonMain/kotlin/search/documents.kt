package search

import kotlin.math.min

class Document(val id: String,val fields: Map<String, List<String>>)

class DocumentIndex(val mapping: MutableMap<String, TextFieldIndex>) {
    // TODO document removal is tricky with current TextIndex implementation

    val documents = mutableMapOf<String,Document>()
    fun index(document: Document) {
        documents.put(document.id,document)
        document.fields.forEach { (field,texts) ->
            val fieldIndex: TextFieldIndex? = mapping[field]
            if(fieldIndex == null) {
                mapping[field] = TextFieldIndex()
            }
            fieldIndex as TextFieldIndex
            texts.forEach {

                fieldIndex.add(document.id, it)
            }
        }
    }

    fun getFieldIndex(field: String) = mapping[field]

    fun get(id: String) = documents[id]

    internal fun search(query: Query, from: Int = 0, limit: Int = 200) = query.hits(this).let {
        it.subList(from, min(limit, it.size))
    }

    fun ids() = documents.keys as Set<String>
}

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

