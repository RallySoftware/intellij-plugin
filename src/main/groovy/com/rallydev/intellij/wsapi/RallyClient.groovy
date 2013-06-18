package com.rallydev.intellij.wsapi

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.rallydev.intellij.config.PasswordNotConfiguredException
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.config.RallyPasswordDialog
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.auth.InvalidCredentialsException
import org.apache.commons.httpclient.methods.GetMethod

class RallyClient extends HttpClient {
    private static final Logger log = Logger.getInstance(RallyClient)

    public static RallyClient getInstance() {
        return ServiceManager.getService(RallyClient.class)
    }

    ApiResponse makeRequest(GetRequest request) {
        ensurePasswordLoaded()
        state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(getUsername(), getPassword()))

        GetMethod method = buildMethod(request)
        log.debug "Rally Client requesting [${method.URI}]"
        int code = executeMethod(method)

        switch (code) {
            case HttpStatus.SC_OK:
                return new ApiResponse(method.responseBodyAsString)
                break
            case HttpStatus.SC_UNAUTHORIZED:
                onAuthError()
                throw new InvalidCredentialsException('The provided user name and password are not valid')
            default:
                throw new RuntimeException('Unhandled response code')
        }
    }

    protected GetMethod buildMethod(GetRequest request) {
        GetMethod method = new GetMethod(request.getEncodedUrl(getServer()))
        method.addRequestHeader('X-RallyIntegrationName', 'IntelliJ Plugin')
        method.addRequestHeader('X-RallyIntegrationVendor', 'Rally Software')
        method.addRequestHeader('X-RallyIntegrationPlatform', "${ApplicationInfo.instance?.build}")

        method
    }

    protected String promptForPassword() {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            return RallyPasswordDialog.askPassword()
        }
        null
    }

    protected void ensurePasswordLoaded() {
        String password = getPassword()
        if (!password) {
            password = promptForPassword()
            if(password) {
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
