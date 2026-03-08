# UrlRouter

A lightweight, modern URL router for Android applications.

> Test commit: 2026-03-08 10:17 - Verify Git push from AI agent

## Features

- **URI Routing**: Map custom URL schemes to Activities
- **Path Parameters**: Register routes like `sample://user/{id}`
- **Host+Path Matching**: Resolve routes from incomplete URLs like `user/42`
- **Redirect Targets**: Alias legacy routes to another URI before intent creation
- **Interceptor Chain**: Pre-process and post-process navigation
- **Fallback Handling**: Handle cases when no target is found
- **Parameter Passing**: Auto-copy query params and path params into `Intent` extras
- **Preflight Inspection**: Check `hasTarget()` and inspect `getIntent()` before start
- **Router Utilities**: `canOpen()` pre-check and `remove()` unregister APIs
- **Stack Control**: Pop multiple activities with result delivery and chained navigation
- **Cross-Page Payloads**: Batch extras/results from `Map`, `Bundle`, or `Intent`
- **Route AOP Hooks**: Observe navigation lifecycle with lightweight aspects
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
                "myapp://legacy-home" to Target.redirect("myapp://home"),
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

Or append multiple query parameters at once:

```kotlin
UrlRouter.navigation(this, "myapp://profile")
    .appendQueryParameters(mapOf("userId" to "123", "tab" to "posts"))
    .start()
```

Or send a batch payload to the target page:

```kotlin
UrlRouter.navigation(this, "myapp://profile")
    .putExtras(
        mapOf(
            "userId" to 123,
            "from" to "feed",
            "trace" to "open-profile"
        )
    )
    .start()
```

Or with path parameters:

```kotlin
UrlRouter.navigation(this, "myapp://user/42?from=feed")
    .start()
```

Or from an incomplete host+path key:

```kotlin
UrlRouter.navigation(this, "user/42?from=feed")
    .start()
```

Inspect before launch:

```kotlin
val navigation = UrlRouter.navigation(this, "legacy-home")

if (navigation.hasTarget()) {
    navigation.getIntent()?.let(::startActivity)
}
```

Redirect a legacy route to a newer route:

```kotlin
UrlRouter.apply("myapp://legacy-user/{id}", Target.redirect("myapp://user/{id}"))

val canOpen = UrlRouter.canOpen("myapp://user/42")
UrlRouter.remove("myapp://legacy")
```

### Stack Control (Pop with Result)

Pop the current activity and deliver a result:

```kotlin
UrlRouter.stack(this)
    .result("selected", itemId)
    .start()
```

Pop 2 activities and navigate to a new route:

```kotlin
UrlRouter.stack(this)
    .popCount(2)
    .target("myapp://home")
    .start()
```

Pop with custom result code and payload:

```kotlin
UrlRouter.stack(this)
    .resultCode(Activity.RESULT_CANCELED)
    .result("reason", "user_backed_out")
    .start()
```

Or merge result payloads from multiple sources:

```kotlin
UrlRouter.stack(this)
    .result(bundleOf("status" to "done"))
    .result(intent)
    .results(mapOf("trace" to "checkout-finished"))
    .start()
```

### Route AOP Hooks

Observe the routing lifecycle without changing navigation behavior:

```kotlin
UrlRouter.configuration()
    .addRouteAspect(object : RouteAspect {
        override fun onNavigationStart(originalUri: Uri) {
            Log.d("Router", "start: $originalUri")
        }

        override fun onRouteResolved(originalUri: Uri, resolvedUri: Uri, target: Target) {
            Log.d("Router", "resolved: $resolvedUri -> ${target.className}")
        }

        override fun onTargetNotFound(originalUri: Uri) {
            Log.w("Router", "not found: $originalUri")
        }
    })
```

## Sample Demo

The `sample` app demonstrates:
- normal routing to `TargetActivity`
- path-template routing via `sample://user/42`
- incomplete routing keys like `user/42`
- redirect-style route aliases
- request interception for `sample://blocked`
- target interception for `sample://web`
- fallback handling for unknown routes
- stack pop with result payload delivery
- route lifecycle observation via `RouteAspect`

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
