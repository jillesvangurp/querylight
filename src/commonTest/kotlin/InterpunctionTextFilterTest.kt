import io.kotest.matchers.shouldBe
import kotlin.test.Test
import search.InterpunctionTextFilter

class InterpunctionTextFilterTest {

    private val filter = InterpunctionTextFilter()

    @Test
    fun testFilterRemovesInterpunctionCharacters() {
        val input = "Hello, World! 123"
        val expected = "Hello  World  123"
        filter.filter(input) shouldBe expected
    }

    @Test
    fun testFilterHandlesMultipleInterpunctionCharacters() {
        val input = "Test@String#2021"
        val expected = "Test String 2021"
        filter.filter(input) shouldBe expected
    }

    @Test
    fun testFilterHandlesSpecialCharacters() {
        val input = "Special\$Chars%^&*()"
        val expected = "Special Chars"
        filter.filter(input) shouldBe expected
    }

    @Test
    fun testFilterHandlesEmptyString() {
        val input = ""
        val expected = ""
        filter.filter(input) shouldBe expected
    }

    @Test
    fun testFilterHandlesStringWithNoInterpunctionCharacters() {
        val input = "NoInterpunction123"
        val expected = "NoInterpunction123"
        filter.filter(input) shouldBe expected
    }

    @Test
    fun testFilterHandlesStringWithOnlyInterpunctionCharacters() {
        val input = "!@#$%^&*()"
        val expected = ""
        filter.filter(input) shouldBe expected
    }

    @Test
    fun testFilterHandlesStringWithNumbersAndInterpunctionCharacters() {
        val input = "123!@#456"
        val expected = "123   456"
        filter.filter(input) shouldBe expected
    }
}