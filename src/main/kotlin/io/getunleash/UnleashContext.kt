package io.getunleash

/**
 *
 * To standardise a few activation strategies, we also needed to standardise the Unleash context,
 * which contains fields that vary per request, required to implement the activation strategies.
 * [See the documentation](https://docs.getunleash.io/docs/user_guide/unleash_context)
 * @property userId Whatever implementation of userId you use,
 * if you use the userWithId strategy you'll need to set this
 * @property sessionId However you roll out your session id, if you'd like to use the flexibleRollout with stickiness
 * bound to sessionId you'll need to set this
 * @property remoteAddress the Ip address of the client. If your feature uses the remoteAddress strategy
 * you'll need to set this
 * @property properties - Other properties for custom strategies.
 * @property appName - The name of your app - mostly used for metrics server side, but someone might use this to
 * evaluate a strategy as well
 * @property environment - Which environment are you running in? Not currently supported server side
 * (per Unleash-server v4.0.0), but support is coming, and can be used for custom strategies
 */
data class UnleashContext(
    val userId: String? = null,
    val sessionId: String? = null,
    val remoteAddress: String? = null,
    val properties: Map<String, String> = emptyMap(),
    val appName: String? = null,
    val environment: String? = null
)
