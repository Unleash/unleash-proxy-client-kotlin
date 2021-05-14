package io.getunleash

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.nio.file.Files
import java.time.Duration
import kotlin.concurrent.fixedRateTimer

/**
 * The UnleashClient is a client connected to the unleash-proxy.
 * The client polls the proxy on a background thread for updates to toggles.
 * This is done to avoid blocking the main io thread.
 * To avoid too many background threads and be able to share feature toggle cache, this should be used as a singleton.
 * @property config - Necessary configuration to instantiate the client
 * @property client - An OkHttpClient - Useful if your setup requires allowing less secure TLS protocols than default in OkHttp
 * @constructor Creates a new client, sets up the background polling
 */
class UnleashClient(
    private val config: UnleashConfig,
    private val client: OkHttpClient = OkHttpClient.Builder().readTimeout(Duration.ofSeconds(2)).cache(
        Cache(
            directory = Files.createTempDirectory("unleash_toggles").toFile(),
            maxSize = 10L * 1024L * 1024L // Use 10 MB as max
        )
    ).build(),
) {
    private val json: Json = Json

    var info: ((String) -> Unit)? = null
    var error: ((String) -> Unit)? = null

    private var unleashContext: UnleashContext =
        UnleashContext(appName = config.appName, environment = config.environment)

    private val proxyUrl = config.url.toHttpUrl()

    private var toggles: Map<String, Toggle> = emptyMap()

    private var sync: java.util.Timer? = null

    init {
        fetchToggles()
        setTimer()
    }

    private fun setTimer() {
        sync = fixedRateTimer("unleash_sync_timer", initialDelay = config.refreshInterval, daemon = true, period = config.refreshInterval * 1000) {
            fetchToggles()
        }
    }

    private fun buildContextUrl(ctx: UnleashContext): HttpUrl {
        var contextUrl = proxyUrl.newBuilder().addQueryParameter("appName", ctx.appName)
            .addQueryParameter("env", ctx.environment)
            .addQueryParameter("userId", ctx.userId)
            .addQueryParameter("remoteAddress", ctx.remoteAddress)
            .addQueryParameter("sessionId", ctx.sessionId)
        ctx.properties.entries.forEach {
            contextUrl = contextUrl.addQueryParameter(it.key, it.value)
        }
        return contextUrl.build()
    }

    fun fetchToggles() {
        val contextUrl = buildContextUrl(unleashContext)
        val request = Request.Builder().url(contextUrl).header("Authorization", config.clientKey).build()
        client.newCall(request).enqueue(object: Callback {

            override fun onFailure(call: Call, e: IOException) {
                error?.let { it(e.message ?: "Failed to load") }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (res.isSuccessful) {
                        res.body?.let { body ->
                            try {
                                val proxyResponse = json.decodeFromString<ProxyResponse>(body.string())
                                info?.let { it("Refreshing toggles") }
                                toggles = proxyResponse.toggles.groupBy { it.name }.mapValues { it.value.first() }
                            } catch (e: Exception) {
                                // If we fail to parse, just keep data
                            }
                        }
                    } else {
                        error?.let { it("Failed to fetch toggles, cowardly not doing anything") }
                    }
                }
            }
        })
    }

    /**
     * Used to check whether a feature toggle is enabled or not.
     * Uses the in-memory cache of “toggles” to look up the first toggle with the specified name and return the value of the “enabled” field.
     * Returns false if the toggle does not exist.
     * @param name name of the feature toggle to check
     * @return true if toggle is enabled, false if toggle is disabled or does not exist
     */
    fun isEnabled(name: String): Boolean {
        return toggles[name]?.enabled ?: false
    }

    /**
     * used to return the variant definition.
     * If no variant is found it will return the default variant, with the name “disabled”
     * example: { "name": "disabled" }.
     * @param name name of the variant to get the definition for
     * @return variant definition or default variant with name disabled if not found
     */
    fun getVariant(name: String): Variant {
        return toggles[name]?.variant ?: Variant("disabled")
    }

    /**
     * Used to update the context which is sent as part of the HTTP get request to the unleash proxy in the background.
     * Should initiate a fetch if background poller has started.
     * @param context the context to update to
     */
    fun updateContext(context: UnleashContext): Unit {
        if (this.unleashContext != context) {
            this.unleashContext = context
            fetchToggles()
        }
    }

    /**
     * - Will immediately do an HTTP get to the Unleash Proxy to fetch the latest toggle state
     *   though we will obey cache headers
     */
    fun start(): Unit {
        sync?.cancel()
        setTimer()
    }

    /**
     * Stops the background polling and cleans up resources used.
     */
    fun stop(): Unit {
        sync?.cancel()
    }

    /**
     * Gets current context to simplify [updateContext]
     * Allows
     * ```
     * val currentContext = unleash.getContext()
     * val newCtx = currentContext.copy(userId = newUserId)
     * unleash.updateContext(newCtx)
     * ```
     * @return current context
     */
    fun getContext(): UnleashContext {
        return unleashContext
    }
}
