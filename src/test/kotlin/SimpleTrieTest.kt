import search.SimpleStringTrie
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SimpleStringTrieTest {
    @Test
    fun shouldTriePrefixes() {
        val trie = SimpleStringTrie()
        // these prefixes we want to find
        trie.add("foofo")
        trie.add("foofoo")
        trie.add("fooboooooo")
        trie.add("1234")
        trie.add("123")
        // these inputs have no prefix that was added
        assertNull(trie["foo"])
        assertNull(trie["foobar"])
        assertNull(trie["abc"])
        assertNull(trie["1200"])
        // these inputs have matching prefixes that were added
        assertEquals(trie["foofo"], "foofo")
        assertEquals(trie["foofoxxxxxxxxxx"], "foofo")
        assertEquals(trie["foofoo"], "foofoo")
        assertEquals(trie["foofoooo"], "foofoo")
        assertEquals(trie["fooboooooo"], "fooboooooo")
        assertEquals(trie["1234"], "1234")
        assertEquals(trie["1230"], "123")
    }

    @Test
    fun shouldProduceAllExceptAlbania() {
        val trie = SimpleStringTrie()
        trie.add("australia")
        trie.add("austria")
        trie.add("albania")
        val matches = trie.match("au").joinToString(" ")
        assertTrue {
            matches.contains("austria") && matches.contains("australia")
        }
    }

    @Test
    fun shouldProduceAllNestedPrefixes() {
        val trie = SimpleStringTrie()
        trie.add("ab")
        trie.add("abc")
        trie.add("abcd")
        trie.add("abcde")
        assertEquals(trie.match("ab").size, 4)
        val match = trie.match("a").joinToString(" ")
        listOf("ab", "abc", "abcd", "abcde").forEach {
            assertTrue(match.contains(it))
        }
        assertEquals(trie.match("abc").size, 3)
        val match2 = trie.match("abc").joinToString(" ")
        listOf("abc", "abcd", "abcde").forEach {
            assertTrue(match2.contains(it))
        }
        assertFalse(match2.contains("ab\""))
    }

    @Test
    fun shouldProduceAllPostFixes() {
        val trie = SimpleStringTrie()
        val strings =
            listOf("a", "aa", "aaa", "ab", "abb", "aab")
        strings.forEach { trie.add(it) }
        val matches = trie.match("a")
        assertEquals(matches.size, strings.size)
    }

    @Test
    fun shouldMatch() {
        val trie = SimpleStringTrie()
        trie.add("foofoo")
        trie.add("foobar")
        trie.add("bar")
        val match = trie.match("fo")
        assertTrue(match.contains("foofoo"))
        assertTrue(match.contains("foobar"))
        assertFalse(match.contains("bar"))
        val match2 = trie.match("fff")
        assertEquals(match2.size, 0)
    }

    @Test
    fun shouldReturnMatchingPrefix() {
        val trie = SimpleStringTrie()
        trie.add("a")
        trie.add("b")
        trie.add("c")
        val match = trie.match("abc")
        assertTrue(match.contains("a"))
        assertFalse(match.contains("abc"))
    }
}
