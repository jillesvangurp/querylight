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

    @Test
    fun testPolygonContainmentAndIntersection() {
        val geoIndex = GeoFieldIndex()
        val index = DocumentIndex(mutableMapOf("loc" to geoIndex))

        val berlinPoint = Geometry.Point.of(13.4, 52.5)
        val berlinBox = doubleArrayOf(13.3, 52.4, 13.5, 52.6).toGeometry()

        index.index(Document("berlin_point", mapOf("loc" to listOf(berlinPoint.toString()))))
        index.index(Document("berlin_box", mapOf("loc" to listOf(berlinBox.toString()))))

        val containingPoly = doubleArrayOf(13.0, 52.0, 14.0, 53.0).toGeometry().coordinates!!
        val containHits = index.search { query = GeoPolygonQuery("loc", containingPoly) }
        containHits.map { it.first } shouldContainExactlyInAnyOrder listOf("berlin_point", "berlin_box")

        val overlappingPoly = doubleArrayOf(13.4, 52.5, 13.6, 52.7).toGeometry().coordinates!!
        val overlapHits = index.search { query = GeoPolygonQuery("loc", overlappingPoly) }
        overlapHits.map { it.first } shouldContain "berlin_box"
    }
}
