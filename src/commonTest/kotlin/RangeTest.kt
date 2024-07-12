import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import kotlin.test.Test
import search.Document
import search.DocumentIndex
import search.RangeQuery
import search.TextFieldIndex
import search.search

class RangeTest {

    val testIndex by lazy {
        val documentIndex = DocumentIndex(
            mutableMapOf(
                "value" to TextFieldIndex(),
            )
        )

        (100..200).forEach { num ->
            documentIndex.index(
                Document(
                    id = num.toString(), fields = mapOf(
                        "value" to listOf(num.toString())
                    )
                )
            )
        }

        documentIndex
    }

    @Test
    fun shouldFilterCorrectly() {

        assertSoftly {
            testIndex.search {
                query = RangeQuery(
                    "value",
                    gt = "150",
                    lt = "152",
                )
            }.map { it.first } shouldContainExactlyInAnyOrder listOf("151")

            testIndex.search {
                query = RangeQuery(
                    "value",
                    gte = "150",
                    lt = "152",
                )
            }.map { it.first } shouldContainExactlyInAnyOrder listOf("150", "151")

            testIndex.search {
                query = RangeQuery(
                    "value",
                    gt = "150",
                    lte = "152",
                )
            }.map { it.first } shouldContainExactlyInAnyOrder listOf("151", "152")

            testIndex.search {
                query = RangeQuery(
                    "value",
                    gte = "150",
                    lte = "152",
                )
            }.map { it.first } shouldContainExactlyInAnyOrder listOf("150", "151", "152")

            testIndex.search {
                query = RangeQuery(
                    "value",
                    lte = "150",
                    gte = "152",
                )
            } shouldHaveSize 0

            testIndex.search {
                query = RangeQuery(
                    "value",
                )
            } shouldHaveSize 101
        }
    }
}