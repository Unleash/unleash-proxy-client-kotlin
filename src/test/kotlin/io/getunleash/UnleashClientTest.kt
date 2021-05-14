package io.getunleash

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UnleashClientTest {

    val threeToggles = """
        {
            	"toggles": [
                    {
                        "name": "variantToggle",
                        "enabled": true,
                        "variant": {
                            "name": "green",
                            "payload": {
                                "type": "string",
                                "value": "some-text"
                            }
                        }
                    }, {
                        "name": "featureToggle",
                        "enabled": true,
                        "variant": {
                            "name": "disabled"
                        }
                    }, {
                        "name": "simpleToggle",
                        "enabled": true
                    }
                ]
            }
    """.trimIndent()


    val complicatedVariants = """{
            	"toggles": [
                    {
                        "name": "variantToggle",
                        "enabled": true,
                        "variant": {
                            "name": "green",
                            "payload": {
                                "type": "number",
                                "value": 54
                            }
                        }
                    }, {
                        "name": "featureToggle",
                        "enabled": true,
                        "variant": {
                            "name": "disabled"
                        }
                    }, {
                        "name": "simpleToggle",
                        "enabled": true
                        "variant": {
                            "name": "red",
                            "payload": {
                                "type": "json",
                                "value": { "key": "value" }
                            }
                        }
                    }
                ]
            }""".trimIndent()

    @Test
    fun `Multiple requests within a short time period does not hit server`() {
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody(threeToggles))
        val client = UnleashClient(
            config = UnleashConfig(
                url = webserver.url("/api/proxy").toString(),
                clientKey = "abc123",
                appName = "tests"
            )
        )
        client.isEnabled("my.feature")
        client.isEnabled("my.feature")
        client.isEnabled("my.feature")
        client.isEnabled("my.feature")
        assertThat(webserver.requestCount).isEqualTo(1)
    }

    @Test
    fun `Client should authenticate using api key`() {
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody(threeToggles))
        val client = UnleashClient(
            config = UnleashConfig(
                url = webserver.url("").toString(),
                clientKey = "abc123",
                appName = "tests"
            )
        )
        client.isEnabled("my.feature")
        val request = webserver.takeRequest()
        assertThat(request.path).contains("appName=tests")
        assertThat(request.headers["Authorization"]).isEqualTo("abc123")
    }

    @Test
    fun `Client should use cache headers`() {
        val etag = """W/"2289-ZF15XK+mp4mxVT56ijQZRgZ551A"""
        val webserver = MockWebServer()
        webserver.enqueue(
            MockResponse().setBody(threeToggles).addHeader("Cache-Control", "private, must-revalidate")
                .addHeader("ETag", etag)
        )
        val client = UnleashClient(
            config = UnleashConfig(
                url = webserver.url("").toString(),
                clientKey = "abc123",
                appName = "tests"
            ),
        )
        client.isEnabled("my.feature")
        webserver.takeRequest()
        val cache = webserver.takeRequest()
        assertThat(cache.getHeader("If-None-Match")).isEqualTo(etag)
    }

    @Test
    fun `Updating context should fire off a new request`() {
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody(threeToggles))
        val toggle = Toggle("variantToggle", enabled = false)
        webserver.enqueue(MockResponse().setBody(Json.encodeToString(ProxyResponse.serializer(), ProxyResponse(toggles = listOf(toggle)))))
        val client = UnleashClient(
            config = UnleashConfig(
                url = webserver.url("").toString(),
                clientKey = "abc123",
                appName = "tests",
                refreshInterval = 3000
            )
        )
        client.info = System.out::println
        assertThat(client.isEnabled("variantToggle")).isTrue
        assertThat(webserver.requestCount).isEqualTo(1)
        client.updateContext(UnleashContext("some_new_user"))
        assertThat(webserver.requestCount).isEqualTo(2)
        assertThat(client.isEnabled("variantToggle")).isFalse
    }

    @Test
    fun `Handles various payloads for variants`() {
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody(complicatedVariants))
        val client = UnleashClient(
            config = UnleashConfig(
                url = webserver.url("").toString(),
                clientKey = "abc123",
                appName = "tests"
            )
        )
        assertThat(client.getVariant("variantToggle").payload!!.value).isEqualTo(JsonPrimitive(54))
        assertThat(client.getVariant("featureToggle").payload).isNull()
        assertThat(client.getVariant("simpleToggle").payload!!.value).isInstanceOf(JsonObject::class.java)
        client.stop()

    }

    @Test
    fun `Handles empty toggles list`() {
        val webserver = MockWebServer()
        webserver.enqueue(
            MockResponse().setBody(
                """
            {
                "toggles": []
            }
        """.trimIndent()
            )
        )
        val client = UnleashClient(
            config = UnleashConfig(
                url = webserver.url("").toString(),
                clientKey = "abc123",
                appName = "tests"
            )
        )
        assertThat(client.getVariant("variantToggle").name).isEqualToIgnoringCase("disabled")
        assertThat(client.isEnabled("any.toggle")).isFalse
    }

    @Test
    fun canGetCurrentConfig() {
        val webserver = MockWebServer()
        webserver.enqueue(
            MockResponse().setBody(
                """
            {
                "toggles": []
            }
        """.trimIndent()
            )
        )
        webserver.enqueue(
            MockResponse().setBody(
                """
            {
                "toggles": []
            }
        """.trimIndent()
            )
        )
        val client = UnleashClient(
            config = UnleashConfig(
                url = webserver.url("").toString(),
                clientKey = "abc123",
                appName = "tests"
            )
        )
        client.isEnabled("some.toggle")
        val ctx = client.getContext()
        val updated = ctx.copy(userId = "somenewuserid")
        assertThat(ctx).isNotEqualTo(updated)
        client.updateContext(updated)
        assertThat(webserver.requestCount).isEqualTo(2)
    }

    @Test
    fun `keeps data if remote server fails`() {
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody(threeToggles))
        webserver.enqueue(
            MockResponse().setBody(
                """
            {
                "unexpectedKey": {}
            }
        """.trimIndent()
            )
        )
        val client = UnleashClient(
            config = UnleashConfig(
                url = webserver.url("").toString(),
                clientKey = "abc123",
                appName = "tests"
            ),
        )
        val firstVariant = client.getVariant("featureToggle")
        client.fetchToggles()
        val variant = client.getVariant("featureToggle")
        assertThat(variant).isEqualTo(firstVariant)
    }
}
