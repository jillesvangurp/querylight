package search

import kotlinx.serialization.Serializable

interface FieldIndex {
    val indexState: IndexState
    fun loadState(fieldIndexState: IndexState): FieldIndex

}

@Serializable
sealed interface IndexState
