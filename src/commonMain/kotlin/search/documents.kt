package search

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

    fun search(query: Query) = query.hits(this)

    fun ids() = documents.keys as Set<String>
}
