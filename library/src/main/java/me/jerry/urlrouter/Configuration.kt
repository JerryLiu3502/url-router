package me.jerry.urlrouter

/**
 * Configuration for UrlRouter.
 */
class Configuration {

    var debugEnabled: Boolean = false
    var intentHandler: IntentHandler = DefaultIntentHandler()

    private val requestInterceptors = mutableListOf<RequestInterceptor>()
    private val targetInterceptors = mutableListOf<TargetInterceptor>()
    private val targetNotFoundHandlers = mutableListOf<TargetNotFoundHandler>()
    private val routeAspects = mutableListOf<RouteAspect>()

    /**
     * Add a request interceptor
     */
    fun addRequestInterceptor(interceptor: RequestInterceptor): Configuration {
        requestInterceptors.add(interceptor)
        return this
    }

    /**
     * Add a target interceptor
     */
    fun addTargetInterceptor(interceptor: TargetInterceptor): Configuration {
        targetInterceptors.add(interceptor)
        return this
    }

    /**
     * Add a target not found handler
     */
    fun addTargetNotFoundHandler(handler: TargetNotFoundHandler): Configuration {
        targetNotFoundHandlers.add(handler)
        return this
    }

    /**
     * Add a route aspect observer.
     */
    fun addRouteAspect(aspect: RouteAspect): Configuration {
        routeAspects.add(aspect)
        return this
    }

    /**
     * Get all request interceptors
     */
    fun getRequestInterceptors(): List<RequestInterceptor> = requestInterceptors.toList()

    /**
     * Get all target interceptors
     */
    fun getTargetInterceptors(): List<TargetInterceptor> = targetInterceptors.toList()

    /**
     * Get all target not found handlers
     */
    fun getTargetNotFoundHandlers(): List<TargetNotFoundHandler> = targetNotFoundHandlers.toList()

    /**
     * Get all route aspects.
     */
    fun getRouteAspects(): List<RouteAspect> = routeAspects.toList()

    /**
     * Enable/disable debug mode
     */
    fun setDebugEnabled(enabled: Boolean): Configuration {
        debugEnabled = enabled
        return this
    }

    /**
     * Set custom intent handler
     */
    fun setIntentHandler(handler: IntentHandler): Configuration {
        intentHandler = handler
        return this
    }

    /**
     * Reset configuration to defaults.
     */
    fun reset(): Configuration {
        debugEnabled = false
        intentHandler = DefaultIntentHandler()
        requestInterceptors.clear()
        targetInterceptors.clear()
        targetNotFoundHandlers.clear()
        routeAspects.clear()
        return this
    }
}
