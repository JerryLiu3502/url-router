# UrlRouter - Android URL Router Library

## Project Overview

- **Project Name**: UrlRouter
- **Type**: Android Native Library (Kotlin)
- **Core Functionality**: A lightweight, modern URL/URI router for Android that maps custom schemes to Activities, supports interceptors, fallback handlers, and parameter passing.
- **Target Developers**: Android app developers who need deep linking and URL routing

## Functionality Specification

### MVP Features (v0.1)

1. **URI Routing**
   - Support custom URL schemes (e.g., `myapp://home`, `https://myapp.com/home`)
   - Map source URLs to target Activities
   - Parse query parameters automatically

2. **Interceptor Chain**
   - `RequestInterceptor`: Pre-process URLs before routing
   - `TargetInterceptor`: Post-process after target resolution

3. **Fallback Handling**
   - `TargetNotFoundHandler`: Handle cases when no target is registered

4. **Intent Building**
   - Build Android Intents with extras, flags, and data

5. **Configuration**
   - Debug mode toggle
   - Custom IntentHandler support

### Future Features (v0.2+)

- Stack control (pop, result passing)
- Compose support
- Annotation-based registration
- Runtime dynamic routing

## Technical Stack

- **Language**: Kotlin 1.9
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34
- **Compile SDK**: 34
- **Build Tool**: AGP 8.2, Gradle 8.4
- **Dependencies**:
  - AndroidX Core KTX
  - AndroidX AppCompat
  - AndroidX Annotation

## Package Structure

```
me.jerry.urlrouter
├── UrlRouter.kt          # Main entry point
├── Configuration.kt      # Router configuration
├── Target.kt            # Target mapping model
├── TargetMap.kt         # URL to Target resolution
├── Navigation.kt        # Navigation builder
├── Interceptor.kt       # Interceptor interfaces
├── TargetNotFoundHandler.kt
├── IntentHandler.kt
└── extensions/
    └── LogInterceptor.kt
```

## Sample App

A demo app showing:
- Basic navigation
- Parameter passing
- Interceptor usage
- Not-found fallback
