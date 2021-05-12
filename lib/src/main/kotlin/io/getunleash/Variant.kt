package io.getunleash

import kotlinx.serialization.Serializable

/**
 * @property name
 * @property payload
 */
@Serializable
data class Variant(val name: String, val payload: Payload? = null)
