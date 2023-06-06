package search

class QueryContext(
) {
    private var score: Boolean = true
    private var excludeIds: MutableSet<String>? = null
    private var includeIds: MutableSet<String>? = null

    fun exclude(ids: List<String>) {
        if (excludeIds == null) {
            excludeIds = mutableSetOf()
        }
        excludeIds?.addAll(ids)
    }

    fun include(ids: List<String>) {
        if (includeIds == null) {
            includeIds = mutableSetOf()
        }
        includeIds?.addAll(ids)
    }

    fun setIncludeIds(ids: List<String>) {
        if (includeIds == null) {
            includeIds = mutableSetOf()
        } else {
            includeIds?.clear()
        }
        includeIds?.addAll(ids)
    }

    fun <T> withFilterMode(block: QueryContext.() -> T): T {
        this.score = false
        val result = block.invoke(this)
        this.score = true
        return result
    }

    fun hits(): Hits = includeIds?.filter { true != excludeIds?.contains(it) }?.map { (it to 1.0) }
        ?: throw IllegalStateException("cannot get hits from uninitialized context")

    fun keep(id: String) = true != excludeIds?.contains(id) && true == includeIds?.contains(id)

    override fun toString(): String {
        return "i:$includeIds e:$excludeIds $score"
    }

}