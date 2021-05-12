package ai.getunleash

import kotlinx.serialization.Serializable

/**
 * @property name Name of the variant
 * @property payload Payload of the variant
 */
@Serializable
data class Variant(val name: String, val payload: Payload? = null)
