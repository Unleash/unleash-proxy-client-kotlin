package ai.getunleash

import ai.getunleash.UnleashConfig

/**
 * The UnleashClient is a client connected to the unleash-proxy.
 * The client polls the proxy on a background thread for updates to toggles.
 * This is done to avoid blocking the main io thread.
 * To avoid too many background threads and be able to share feature toggle cache, this should be used as a singleton.
 * @property config - Necessary configuration to instantiate the client
 * @constructor Creates a new client, sets up the background polling
 */
class UnleashClient(val config: UnleashConfig) {


    /**
     * Used to check whether a feature toggle is enabled or not.
     * Uses the in-memory cache of “toggles” to look up the first toggle with the specified name and return the value of the “enabled” field.
     * Returns false if the toggle does not exist.
     * @param name name of the feature toggle to check
     * @return true if toggle is enabled, false if toggle is disabled or does not exist
     */
    fun isEnabled(name: String): Boolean = TODO()

    /**
     * used to return the variant definition.
     * If no variant is found it will return the default variant, with the name “disabled”
     * example: { "name": "disabled" }.
     * @param name name of the variant to get the definition for
     * @return variant definition or default variant with name disabled if not found
     */
    fun getVariant(name: String): Variant = TODO()

    /**
     * Used to update the context which is sent as part of the HTTP get request to the unleash proxy in the background.
     * Should initiate a fetch if background poller has started.
     * @param context the context to update to
     */
    fun updateContext(context: UnleashContext): Unit {
        TODO()
    }

    /**
     * - Will immediately do an HTTP get to the Unleash Proxy to fetch the latest toggle state
     * - Will set up a background process to do new http fetches to the Unleash Proxy at the interval defined in refreshInterval.
     * - If a background process is already running, this is a no-op
     */
    fun start(): Unit {

    }

    /**
     * Stops the background polling and cleans up resources used.
     */
    fun stop(): Unit {

    }
}
