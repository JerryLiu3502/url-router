# UrlRouter

A lightweight, modern URL router for Android applications.

## Features

- **URI Routing**: Map custom URL schemes to Activities
- **Interceptor Chain**: Pre-process and post-process navigation
- **Fallback Handling**: Handle cases when no target is found
- **Parameter Passing**: Pass query parameters and extras seamlessly
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
        
        UrlRouter.apply(
            mapOf(
                "myapp://home" to Target(HomeActivity::class.java.name),
                "myapp://profile" to Target(ProfileActivity::class.java.name)
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

## Architecture

- `UrlRouter` - Main entry point
- `Configuration` - Router configuration
- `Target` - Target Activity mapping
- `TargetMap` - URL to Target resolution
- `Navigation` - Navigation builder
- `RequestInterceptor` - Pre-process URLs
- `TargetInterceptor` - Post-process resolved targets
- `TargetNotFoundHandler` - Handle unmatched URLs

## Sample App

See the `sample` module for a complete working example.

## License

Apache License 2.0
