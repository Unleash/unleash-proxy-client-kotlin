# Unleash Proxy Client SDK for Kotlin
[![Coverage Status](https://coveralls.io/repos/github/Unleash/unleash-proxy-client-kotlin/badge.svg?branch=main)](https://coveralls.io/github/Unleash/unleash-proxy-client-kotlin?branch=main)
[![main](https://github.com/Unleash/unleash-proxy-client-kotlin/actions/workflows/main.yml/badge.svg)](https://github.com/Unleash/unleash-proxy-client-kotlin/actions/workflows/main.yml)

This is the Unleash Proxy Client SDK for Kotlin. It is compatible with the unleash-proxy included in our enterprise offering. Though it is written in kotlin, it works seamlessly in Java projects as well

## Getting started

You will require the client sdk on your class path. 

#### Maven

```xml
<dependency>
    <groupId>io.getunleash</groupId>
    <artifactId>unleash-proxy-client-kotlin</artifactId>
    <version>${CURRENT_VERSION}</version>
</dependency>
```

#### Gradle
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.getunleash:unleash-proxy-client-kotlin:$CURRENT_VERSION")
}
```

### Now configure your client instance
You should use this as a singleton to avoid file contention on cache director

```kotlin
val config = UnleashConfig(url = "URL to your proxy", clientKey="API key with access to proxy", appName="The name of your app")
val unleash = UnleashClient(config)
```


### See if a toggle is enabled

```kotlin
if (unleash.isEnabled("my-toggle-name")) {
    println("Our toggle was enabled")
} else {
    println("Our toggle was disabled")
}
```

### If you're using variants you can use

```kotlin
val variant = unleash.getVariant("my-toggle-with-variant")
variant.name.match {
    "orange" -> somethingForOrange()
    else -> somethingelseForEveryoneElse()
}
```

### If you're using variants with payload, the payload has been serialized as a kotlinx.serialization.JsonElement
```kotlin
val variant = unleash.getVariant("my-toggle-variant-with-payload")
val el: JsonElement = variant.payload.value
```

### If you've received new information that you'd like to add your context you can use the `updateContext` method
```kotlin
val ctx = unleash.getContext().copy(userId = newUserId)
unleash.updateContext(ctx)
```