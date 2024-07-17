package search

import kotlin.math.min
import kotlinx.serialization.Serializable

@Serializable
data class DocumentIndexState(val documents: Map<String, Document>, val fieldState: Map<String, IndexState>)

class DocumentIndex(
    val mapping: MutableMap<String, TextFieldIndex>,
    val documents: MutableMap<String, Document> = mutableMapOf(),
) {
    // TODO document removal is tricky with current TextIndex implementation


    val indexState get() = DocumentIndexState(
        documents = documents,
        fieldState = mapping.map { (k, v) ->
            k to v.indexState
        }.toMap()
    )

    fun loadState(documentIndexState: DocumentIndexState): DocumentIndex {
        val loadedMapping = mapping.map { (name, index) ->
            val state = documentIndexState.fieldState[name]
            name to (state?.let { index.loadState(it) } ?: index)
        }.toMap().toMutableMap()
        return DocumentIndex(loadedMapping,documentIndexState.documents.toMutableMap())
    }


    fun index(document: Document) {
        documents.put(document.id,document)
        document.fields.forEach { (field,texts) ->
            // create indices on the fly to emulate dynamic mapping
            val fieldIndex = mapping[field].let {
                if(it == null) {
                    val newIndex = TextFieldIndex()
                    mapping[field] = newIndex
                    newIndex
                } else {
                    it
                }
            }

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