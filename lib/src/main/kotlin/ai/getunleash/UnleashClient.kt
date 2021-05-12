package ai.getunleash

/**
 * The UnleashClient starts a background poller which checks for updates from the unleash-proxy url (configured via . It should do this preferably as a non-blocking IO call and on a separate dedicated thread. This to limit the impact of the native app in general.
 */
class UnleashClient(val config: UnleashConfig) {


    fun isEnabled(name: String): Boolean
}
