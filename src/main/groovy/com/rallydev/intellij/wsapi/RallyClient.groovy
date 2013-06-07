package com.rallydev.intellij.wsapi

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.rallydev.intellij.config.RallyConfig
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.auth.InvalidCredentialsException
import org.apache.commons.httpclient.methods.GetMethod

class RallyClient extends HttpClient {
    private static final Logger log = Logger.getInstance(RallyClient)

    URL server
    String username
    String password

    RallyClient() {}

    RallyClient(URL server, String username, String password) {
        this.server = server
        this.username = username
        this.password = password
    }

    RallyClient(RallyConfig rallyConfig) {
        this.server = new URL(rallyConfig.url)
        this.username = rallyConfig.userName
        this.password = rallyConfig.password
    }

    public static RallyClient getInstance() {
        return ServiceManager.getService(RallyClient.class);
    }

    ApiResponse makeRequest(GetRequest request) {
        state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password))

        def method = new GetMethod(request.getEncodedUrl(server))
        log.debug "Rally Client requesting [${method.URI}]"
        int code = executeMethod(method)

        switch (code) {
            case HttpStatus.SC_OK:
                return new ApiResponse(method.responseBodyAsString)
                break
            case HttpStatus.SC_UNAUTHORIZED:
                throw new InvalidCredentialsException('The provided user name and password are not valid')
            default:
                throw new RuntimeException('Unhandled response code')
        }
    }

}
