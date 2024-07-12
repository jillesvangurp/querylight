package search

import kotlin.math.min

class DocumentIndex(val mapping: MutableMap<String, TextFieldIndex>) {
    // TODO document removal is tricky with current TextIndex implementation

    val documents = mutableMapOf<String, Document>()
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