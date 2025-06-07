package search

import com.jillesvangurp.geo.GeoGeometry
import com.jillesvangurp.geo.GeoHashUtils
import com.jillesvangurp.geojson.Geometry
import com.jillesvangurp.geojson.PolygonCoordinates
import com.jillesvangurp.serializationext.DEFAULT_JSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("GeoFieldIndexState")
data class GeoFieldIndexState(
    val precision: Int,
    val geohashMap: Map<String, List<String>>,
    val documents: Map<String, String>
) : IndexState

class GeoFieldIndex(
    private val precision: Int = 5,
    private val geohashMap: MutableMap<String, MutableList<String>> = mutableMapOf(),
    private val documents: MutableMap<String, String> = mutableMapOf()
) : FieldIndex {
    override val indexState: IndexState
        get() = GeoFieldIndexState(precision, geohashMap, documents)

    override fun loadState(fieldIndexState: IndexState): FieldIndex {
        if (fieldIndexState is GeoFieldIndexState) {
            val loaded = GeoFieldIndex(
                precision = fieldIndexState.precision,
                geohashMap = fieldIndexState.geohashMap.mapValues { it.value.toMutableList() }.toMutableMap(),
                documents = fieldIndexState.documents.toMutableMap()
            )
            return loaded
        }
        error("wrong index type; expecting GeoFieldIndexState but was ${fieldIndexState::class.simpleName}")
    }

    fun add(docId: String, geoJson: String) {
        documents[docId] = geoJson
        val geometry = DEFAULT_JSON.decodeFromString(Geometry.serializer(), geoJson)
        geohashesForGeometry(geometry).flatMap { normalizeHash(it) }.forEach { hash ->
            geohashMap.getOrPut(hash) { mutableListOf() }.add(docId)
        }
    }

    private fun geohashesForGeometry(geometry: Geometry): Set<String> = when (geometry) {
        is Geometry.Point -> geometry.coordinates?.let { setOf(GeoHashUtils.encode(it[1], it[0], precision)) } ?: emptySet()
        is Geometry.Polygon -> geometry.coordinates?.let { GeoHashUtils.geoHashesForPolygon(it, maxLength = precision, includePartial = true) } ?: emptySet()
        is Geometry.MultiPolygon -> geometry.coordinates?.let { GeoHashUtils.geoHashesForMultiPolygon(it, maxLength = precision, includePartial = true) } ?: emptySet()
        else -> emptySet()
    }

    private fun normalizeHash(hash: String): Set<String> {
        var hashes = setOf(hash)
        while (hashes.first().length < precision) {
            hashes = hashes.flatMap { GeoHashUtils.subHashes(it).toList() }.toSet()
        }
        return hashes.map { if (it.length > precision) it.substring(0, precision) else it }.toSet()
    }

    fun queryPoint(lat: Double, lon: Double): List<String> {
        val hash = GeoHashUtils.encode(lat, lon, precision)
        val ids = geohashMap[hash] ?: return emptyList()
        return ids.filter { verifyPoint(it, lat, lon) }
    }

    private fun verifyPoint(docId: String, lat: Double, lon: Double): Boolean {
        val geoJson = documents[docId] ?: return false
        val geometry = DEFAULT_JSON.decodeFromString(Geometry.serializer(), geoJson)
        return when (geometry) {
            is Geometry.Point -> geometry.coordinates?.let { it[0] == lon && it[1] == lat } ?: false
            is Geometry.Polygon -> geometry.coordinates?.let { GeoGeometry.polygonContains(lat, lon, it) } ?: false
            is Geometry.MultiPolygon -> geometry.coordinates?.any { GeoGeometry.polygonContains(lat, lon, it) } ?: false
            else -> false
        }
    }

    fun queryPolygon(polygon: PolygonCoordinates): List<String> {
        val hashes = GeoHashUtils.geoHashesForPolygon(polygon, maxLength = precision, includePartial = true)
            .flatMap { normalizeHash(it) }
        val candidates = hashes.flatMap { geohashMap[it].orEmpty() }.distinct()
        return candidates.filter { verifyPolygon(it, polygon) }
    }

    private fun verifyPolygon(docId: String, polygon: PolygonCoordinates): Boolean {
        val geoJson = documents[docId] ?: return false
        val geometry = DEFAULT_JSON.decodeFromString(Geometry.serializer(), geoJson)
        return when (geometry) {
            is Geometry.Point -> geometry.coordinates?.let { GeoGeometry.polygonContains(it[1], it[0], polygon) } ?: false
            is Geometry.Polygon -> geometry.coordinates?.let { GeoGeometry.overlap(it, polygon) || GeoGeometry.contains(polygon[0], it[0]) || GeoGeometry.contains(it[0], polygon[0]) } ?: false
            is Geometry.MultiPolygon -> geometry.coordinates?.any { GeoGeometry.overlap(it, polygon) } ?: false
            else -> false
        }
    }
}
