package io.getunleash

import kotlinx.serialization.Serializable

@Serializable
data class ProxyResponse(val toggles: List<Toggle>) {
}