private class TrieNode {
    val children: MutableMap<Char, TrieNode> = mutableMapOf()
    var isLeaf = false

    fun strings(): List<String> {
        return children.entries.flatMap { entry ->
            val n = entry.value
            if (n.isLeaf) {
                listOf("" + entry.key) +
                    n.strings().map { s: String -> "" + entry.key + s }

            } else {
                n.strings()
                    .map { s: String -> "" + entry.key + s }
            }
        }
    }
}

/**
 * Simple implementation of a Trie that may be used to match input strings to the longest matching prefix.
 */
class SimpleStringTrie {
    private val root: TrieNode = TrieNode()

    /**
     * Add a string to the trie.
     * @param input any String
     */
    fun add(input: String) {
        var currentNode = root
        for (c in input) {
            val children = currentNode.children
            val matchingNode = children[c]
            if (matchingNode != null) {
                currentNode = matchingNode
            } else {
                val newNode = TrieNode()
                children[c] = newNode
                currentNode = newNode
            }
        }
        currentNode.isLeaf = true // this is the end of an input that was added, there may be more children
    }

    /**
     * Return the longest matching prefix of the input string that was added to the trie.
     * @param input a string
     * @return Optional of longest matching prefix that was added to the trie
     */
    operator fun get(input: String): String? {
        var currentNode = root
        var i = 0
        for (c in input) {
            val nextNode = currentNode.children[c]
            if (nextNode != null) {
                i++
                currentNode = nextNode
            } else {
                if (i > 0 && currentNode.isLeaf) {
                    return input.substring(0, i)
                }
            }
        }
        return if (i > 0 && currentNode.isLeaf) {
            input.substring(0, i)
        } else null
    }

    fun match(input: String): List<String> {
        return addMoreStrings(input, "", root)
    }

    private fun addMoreStrings(
        input: String,
        prefix: String,
        startNode: TrieNode
    ): List<String> {
        var currentNode = startNode
        var i = 0
        val results: MutableList<String> = mutableListOf()
        for (c in input) {
            val nextNode = currentNode.children[c]
            if (nextNode != null) {
                i++
                currentNode = nextNode
            }
        }
        val matched = input.substring(0, i)
        if (i > 0 && currentNode.isLeaf) {
            results.add(prefix + matched) // fully matched against something
        }
        if (currentNode != root && i == input.length) {
            results.addAll(
                currentNode.strings()
                    .map { s: String -> prefix + matched + s }
                    .toList()
            )
        }
        return results
    }

    companion object {
        /**
         * Useful if you want to build a trie for an existing map so you can figure out a matching prefix that has an entry
         * @param map a map
         * @return a SimpleStringTrie for the map.
         */
        fun from(map: Map<String, *>): SimpleStringTrie {
            val st = SimpleStringTrie()
            map.keys.forEach { key: String -> st.add(key) }
            return st
        }
    }
}
