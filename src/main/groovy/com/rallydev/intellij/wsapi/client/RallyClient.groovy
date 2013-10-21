package com.rallydev.intellij.wsapi.client

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.ListenableFuture
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.net.HttpConfigurable
import com.rallydev.intellij.config.PasswordNotConfiguredException
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.config.RallyPasswordDialog
import com.rallydev.intellij.util.AsyncService
import com.rallydev.intellij.wsapi.ApiResponse
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.auth.InvalidCredentialsException
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class RallyClient {
    private static final Logger log = Logger.getInstance(this)

    static final String WSAPI_VERSION = '1.43'

    AsyncService asyncService
    HttpClient httpClient

    RallyClient(AsyncService asyncService) {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager()
        httpClient = new HttpClient(connectionManager)

        this.asyncService = asyncService
    }

    public static RallyClient getInstance() {
        return ServiceManager.getService(RallyClient.class)
    }

    ListenableFuture<ApiResponse> makeRequest(@NotNull RallyRequest request, @Nullable FutureCallback<ApiResponse> callback) {
        ensurePasswordLoaded()
        Closure<ApiResponse> requestCall = {
            doMakeRequest(request)
        }

        return asyncService.schedule(requestCall, callback)
    }

    ApiResponse makeRequest(@NotNull RallyRequest request) {
        ensurePasswordLoaded()
        doMakeRequest(request)
    }

    private ApiResponse doMakeRequest(@NotNull RallyRequest request) {
        httpClient.state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(getUsername(), getPassword()))
        configureProxy()

        HttpMethod method = buildMethod(request)
        log.info "Rally Client making [$method.name] on:\n\t\t${method.URI}"

        int code = httpClient.executeMethod(method)
        try {
            switch (code) {
                case HttpStatus.SC_OK:
                    return new ApiResponse(method.responseBodyAsString)
                case HttpStatus.SC_UNAUTHORIZED:
                    onAuthError()
                    throw new InvalidCredentialsException('The provided user name and password are not valid')
                case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                    throw new RuntimeException("""
The server returned [${code}]. This code indicates that you have IntelliJ configured to use a proxy server and the proxy
server is requiring authentication, but proper authentication is not being sent to the proxy server. Double check
settings 'HTTP Proxy' -> 'Proxy authentication'
"""
                    )
                default:
                    throw new RuntimeException("Unhandled response code [${code}]")
            }
        } finally {
            method.releaseConnection()
        }
    }

    private void configureProxy() {
        final HttpConfigurable proxySettings = HttpConfigurable.getInstance()
        if (proxySettings && proxySettings.USE_HTTP_PROXY && !StringUtil.isEmptyOrSpaces(proxySettings.PROXY_HOST)) {
            httpClient.hostConfiguration.setProxy(proxySettings.PROXY_HOST, proxySettings.PROXY_PORT)
            if (proxySettings.PROXY_AUTHENTICATION) {
                httpClient.state.setProxyCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(proxySettings.PROXY_LOGIN, proxySettings.getPlainProxyPassword())
                )
            }
        }
    }

    protected HttpMethod buildMethod(RallyRequest request) {
        HttpMethod method = null
        switch (request.class) {
            case GetRequest:
                GetRequest getRequest = (GetRequest) request
                method = new GetMethod(getRequest.getEncodedUrl(getServer()))
                break
            case PostRequest:
                PostRequest postRequest = (PostRequest) request
                method = new PostMethod(postRequest.encodedUrl)
                method.setRequestBody(postRequest.body)
        }

        method.addRequestHeader('X-RallyIntegrationName', 'IntelliJ Plugin')
        method.addRequestHeader('X-RallyIntegrationVendor', 'Rally Software')
        method.addRequestHeader('X-RallyIntegrationPlatform', "${ApplicationInfo.instance?.build}")

        return method
    }

    protected String promptForPassword() {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            return RallyPasswordDialog.askPassword()
        }
        null
    }

    void ensurePasswordLoaded() {
        String password = getPassword()
        if (!password) {
            password = promptForPassword()
            if (password) {
                RallyConfig.instance.password = password
            }
        }

        if (!password) {
            throw new PasswordNotConfiguredException()
        }
    }

    protected void onAuthError() {
        RallyConfig.instance.clearCachedPassword()
    }

    URL getServer() {
        return RallyConfig.instance.url.toURL()
    }

    String getUsername() {
        return RallyConfig.instance.userName
    }

    String getPassword() {
        return RallyConfig.instance.password
    }

}
