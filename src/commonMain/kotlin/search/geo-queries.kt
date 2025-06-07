package search

import com.jillesvangurp.geojson.PolygonCoordinates

class GeoPointQuery(
    private val field: String,
    private val latitude: Double,
    private val longitude: Double,
    override val boost: Double? = null,
) : Query {
    override fun hits(documentIndex: DocumentIndex, context: QueryContext): Hits {
        val index = documentIndex.getFieldIndex(field)
        return if (index is GeoFieldIndex) {
            index.queryPoint(latitude, longitude).map { it to 1.0 }.boost(normalizedBoost)
        } else emptyList()
    }
}

class GeoPolygonQuery(
    private val field: String,
    private val polygon: PolygonCoordinates,
    override val boost: Double? = null,
) : Query {
    override fun hits(documentIndex: DocumentIndex, context: QueryContext): Hits {
        val index = documentIndex.getFieldIndex(field)
        return if (index is GeoFieldIndex) {
            index.queryPolygon(polygon).map { it to 1.0 }.boost(normalizedBoost)
        } else emptyList()
    }
}
