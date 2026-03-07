# UrlRouter

A lightweight, modern URL router for Android applications.

## Features

- **URI Routing**: Map custom URL schemes to Activities
- **Path Parameters**: Register routes like `sample://user/{id}`
- **Interceptor Chain**: Pre-process and post-process navigation
- **Fallback Handling**: Handle cases when no target is found
- **Parameter Passing**: Auto-copy query params and path params into `Intent` extras
- **Router Utilities**: `canOpen()` pre-check and `remove()` unregister APIs
- **Simple API**: Clean, builder-based API

## Quick Start

### 1. Add Dependency

```groovy
dependencies {
    implementation 'me.jerry.urlrouter:urlrouter:1.0.0'
}
```

### 2. Configure in Application

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        UrlRouter.configuration()
            .setDebugEnabled(BuildConfig.DEBUG)
            .addRequestInterceptor(LogInterceptor())
            .addTargetNotFoundHandler(object : TargetNotFoundHandler {
                override fun handle(uri: Uri): Boolean {
                    return false
                }
            })

        UrlRouter.apply(
            mapOf(
                "myapp://home" to Target(HomeActivity::class.java.name),
                "myapp://profile" to Target(ProfileActivity::class.java.name),
                "myapp://user/{id}" to Target(
                    UserActivity::class.java.name,
                    pathTemplate = "/user/{id}"
                )
            )
        )
    }
}
```

### 3. Navigate

```kotlin
UrlRouter.navigation(this, "myapp://home")
    .putExtra("userId", 123)
    .start()
```

Or with query parameters:

```kotlin
UrlRouter.navigation(this, "myapp://profile?userId=123")
    .start()
```

Or with path parameters:

```kotlin
UrlRouter.navigation(this, "myapp://user/42?from=feed")
    .start()

val canOpen = UrlRouter.canOpen("myapp://user/42")
UrlRouter.remove("myapp://legacy")
```

## Sample Demo

The `sample` app demonstrates:
- normal routing to `TargetActivity`
- path-template routing via `sample://user/42`
- request interception for `sample://blocked`
- target interception for `sample://web`
- fallback handling for unknown routes

## Architecture

Routing flow:
- incoming `Uri`
- request interceptors
- target lookup in `TargetMap`
- target interceptors
- intent creation and launch
- optional not-found handlers


- `UrlRouter` - Main entry point
- `Configuration` - Router configuration
- `Target` - Target Activity mapping
- `TargetMap` - URL to Target resolution
- `Navigation` - Navigation builder
- `RequestInterceptor` - Pre-process URLs
- `TargetInterceptor` - Post-process resolved targets
- `TargetNotFoundHandler` - Handle unmatched URLs
- `IntentHandler` - Customize how Intents are created

## Sample App

See the `sample` module for a complete working example.

## License

Apache License 2.0
