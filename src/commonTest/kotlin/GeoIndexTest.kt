import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test
import search.Document
import search.DocumentIndex
import search.GeoFieldIndex
import search.GeoPointQuery
import search.GeoPolygonQuery
import search.search
import com.jillesvangurp.geojson.Geometry
import com.jillesvangurp.geojson.toGeometry

class GeoIndexTest {
    @Test
    fun testGeoQueries() {
        val geoIndex = GeoFieldIndex()
        val index = DocumentIndex(mutableMapOf("loc" to geoIndex))
        val berlin = Geometry.Point.of(13.4, 52.5)
        val paris = Geometry.Point.of(2.35, 48.85)
        index.index(Document("berlin", mapOf("loc" to listOf(berlin.toString()))))
        index.index(Document("paris", mapOf("loc" to listOf(paris.toString()))))

        val hits = index.search { query = GeoPointQuery("loc", 52.5, 13.4) }
        hits.map { it.first } shouldContain "berlin"

        val polygon = doubleArrayOf(12.0, 52.0, 14.0, 53.0).toGeometry().coordinates!!
        val polyHits = index.search { query = GeoPolygonQuery("loc", polygon) }
        polyHits.map { it.first } shouldContainExactlyInAnyOrder listOf("berlin")
    }
}
