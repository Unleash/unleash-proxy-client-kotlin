package ai.getunleash

/**
 * @property name Name of the variant
 * @property payload Payload of the variant
 */
data class Variant(val name: String, val payload: Payload)

/**
 * @property type - Type of the payload. This can be any type 'string' | 'number' | 'json' ...
 * @property value - The actual payload
 */
data class Payload(val type: String, val value: String)