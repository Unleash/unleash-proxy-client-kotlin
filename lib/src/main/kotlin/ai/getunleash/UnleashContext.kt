package ai.getunleash

data class UnleashContext(
    val userId: String? = null,
    val sessionId: String? = null,
    val remoteAddress: String? = null,
    val properties: Map<String, String> = emptyMap(),
    val appName: String? = null,
    val environment: String? = null
)