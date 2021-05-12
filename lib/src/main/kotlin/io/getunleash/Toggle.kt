package io.getunleash

import kotlinx.serialization.Serializable

/**
 * @property name Name of the toggle
 * @property enabled Did this toggle get evaluated to true
 * @property variant used by [io.getunleash.UnleashClient.getVariant] to get the variant data
 */
@Serializable
data class Toggle(val name: String, val enabled: Boolean, val variant: Variant = Variant(name = "disabled"))