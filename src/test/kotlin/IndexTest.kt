import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IndexTest {
    @Test
    fun shouldAddTerms() {
        val index = Index()
        index.add("foo",1)
        index.add("foo",1)
        index.add("bar",1)
        index.add("bar",1)

        index.add("foo", 2)
        index.add("foobar", 2)
        index.add("foobar", 2)
        index.add("foobar", 2)

        index.add("bar",3)
        index.add("foobar",3)

        val results = index.get("foo")
        assertEquals(results.size, 2)

        assertEquals(results[0].first, 1)
        assertEquals(results[1].first, 2)
    }

}
