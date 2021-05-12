package ai.getunleash

data class UnleashContext(
    val userId: String,
    val sessionId: String,
    val remoteAddress: String,
    val properties: Map<String, String>,
    val appName: String,
    val environment: String
)