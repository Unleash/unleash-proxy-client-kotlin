package io.getunleash

/**
 * Represents configuration for Unleash.
 * @property url HTTP(s) URL to the Unleash Proxy (Required).
 * @property clientKey the secret added as the Authorization header sent to the unleash-proxy (Required)
 * @property appName: name of the underlying application. Will be part of the unleash context if not overridden in the [io.getunleash.UnleashClient.updateContext] call (Required).
 * @property refreshInterval The number of seconds to wait between each HTTP fetch. (Optional - Defaults to 30 seconds)
 * @property metricsInterval The number of seconds to wait between each HTTP post sending metrics back to the Unleash Proxy. (Optional - Defaults to 30 seconds)
 * @property environment Part of unleash context if not overridden when using [io.getunleash.UnleashClient.updateContext] (Optional - Defaults to 'default')
 */
data class UnleashConfig(
    val url: String,
    val clientKey: String,
    val appName: String,
    val refreshInterval: Long = 30,
    val metricsInterval: Long = 30,
    val environment: String = "default"
)
