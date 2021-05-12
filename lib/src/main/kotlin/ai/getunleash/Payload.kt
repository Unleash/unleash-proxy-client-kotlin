package ai.getunleash

import kotlinx.serialization.Serializable


/**
 * @property type - Type of the payload. This can be any type 'string' | 'number' | 'json' ...
 * @property value - The actual payload
 */
@Serializable
data class Payload(val type: String, val value: String)