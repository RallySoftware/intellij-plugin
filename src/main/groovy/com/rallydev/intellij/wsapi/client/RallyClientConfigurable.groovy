package com.rallydev.intellij.wsapi.client

import com.rallydev.intellij.util.AsyncService

import java.awt.*

class RallyClientConfigurable extends RallyClient {

    URL server
    String username
    String password

    Boolean promptForPassword

    RallyClientConfigurable(URL server, String username, String password, Boolean promptForPassword) {
        super(AsyncService.getInstance())

        this.server = server
        this.username = username
        this.password = password

        this.promptForPassword = promptForPassword
    }

    @Override
    void ensurePasswordLoaded() {
        if (promptForPassword && EventQueue.isDispatchThread()) {
            password = promptForPassword()
        }
    }

    protected void onAuthError() {
        password = null
    }

}
