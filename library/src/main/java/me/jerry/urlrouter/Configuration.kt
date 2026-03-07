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
}
