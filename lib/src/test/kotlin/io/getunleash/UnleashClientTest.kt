package io.getunleash

import com.google.common.testing.FakeTicker
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class UnleashClientTest {



    @Test
    fun `Multiple requests within a short time period does not hit server`() {
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody("""
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
        """.trimIndent()))
        val client = UnleashClient(config = UnleashConfig(url = webserver.url("/api/proxy").toString(), clientKey = "abc123", appName = "tests"))
        client.isEnabled("my.feature")
        assertThat(webserver.requestCount).isEqualTo(1)
        client.isEnabled("my.feature")
        client.isEnabled("my.feature")
        client.isEnabled("my.feature")
        assertThat(webserver.requestCount).isEqualTo(1)
    }

    @Test
    fun `Client should authenticate using api key`() {
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody("""
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
        """.trimIndent()))
        val client = UnleashClient(config = UnleashConfig(url = webserver.url("").toString(), clientKey = "abc123", appName = "tests"))
        client.isEnabled("my.feature")
        val request = webserver.takeRequest()
        assertThat(request.path).startsWith("/api/proxy")
        assertThat(request.headers["Authorization"]).isEqualTo("abc123")
    }

    @Test
    fun `Client should use cache headers`() {
        val etag = """W/"2289-ZF15XK+mp4mxVT56ijQZRgZ551A"""
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody("""
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
        """.trimIndent()).addHeader("Cache-Control", "private, must-revalidate").addHeader("ETag", etag))
        val fakeTicker = FakeTicker()
        val client = UnleashClient(config = UnleashConfig(url = webserver.url("").toString(), clientKey = "abc123", appName = "tests"), ticker = fakeTicker::read)
        client.isEnabled("my.feature")
        webserver.takeRequest()
        fakeTicker.advance(31, TimeUnit.SECONDS)
        client.isEnabled("my.feature")
        val cache = webserver.takeRequest()
        assertThat(cache.getHeader("If-None-Match")).isEqualTo(etag)
    }

    @Test
    fun `Updating context should fire off a new request`() {
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody("""
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
        """.trimIndent()))
        webserver.enqueue(MockResponse().setBody("""
            {
            	"toggles": [
                    {
                        "name": "variantToggle",
                        "enabled": false,
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
        """.trimIndent()))
        val client = UnleashClient(config = UnleashConfig(url = webserver.url("").toString(), clientKey = "abc123", appName = "tests"))
        assertThat(client.isEnabled("variantToggle")).isTrue
        assertThat(webserver.requestCount).isEqualTo(1)
        client.updateContext(UnleashContext("some_new_user"))
        assertThat(webserver.requestCount).isEqualTo(2)
        assertThat(client.isEnabled("variantToggle")).isFalse
    }

    @Test
    fun `Handles various payloads for variants`() {
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse().setBody("""
            {
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
            }
        """.trimIndent()))
        val client = UnleashClient(config = UnleashConfig(url = webserver.url("").toString(), clientKey = "abc123", appName = "tests"))
        assertThat(client.getVariant("variantToggle").payload!!.value).isEqualTo(JsonPrimitive(54))
        assertThat(client.getVariant("featureToggle").payload).isNull()
        assertThat(client.getVariant("simpleToggle").payload!!.value).isInstanceOf(JsonObject::class.java)
    }
}