package search

class Document(val id: String,val fields: Map<String, List<String>>)

class DocumentIndex(val mapping: MutableMap<String, TextFieldIndex>): Map<String,TextFieldIndex> by mapping {

    fun index(document: Document) {
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

    fun search(query: Query) = query.hits(this)
}
