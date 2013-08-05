package com.rallydev.intellij.config

import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import com.intellij.openapi.ui.Messages
import com.rallydev.intellij.wsapi.ConnectionTest
import org.apache.commons.httpclient.auth.InvalidCredentialsException
import org.codehaus.groovy.runtime.StackTraceUtils

import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class TestConnectionButtonListener implements ActionListener {

    RallyConfigForm form

    TestConnectionButtonListener(RallyConfigForm form) {
        this.form = form
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String password
        if (form.rememberPassword.isSelected() && !form.passwordChanged) {
            password = RallyConfig.getInstance().getPassword()
        } else {
            password = form.password.password as String
        }

        String error = null
        try {
            new ConnectionTest(
                    form.url.getText().toURL(), form.userName.text,
                    password, !form.rememberPassword.selected
            ).doTest()
        } catch (Exception e) {
            error = messageFromException(e)
        }
        if (error) {
            Messages.showErrorDialog(error, 'Error')
        } else {
            Messages.showMessageDialog('Connection is successful', 'Connection', Messages.informationIcon)
        }
    }

    private static String messageFromException(Exception e) {
        Throwable rootCause = StackTraceUtils.extractRootCause(e)
        switch (rootCause.class) {
            case InvalidCredentialsException:
                "Invalid credentials: ${rootCause.getMessage()}"
                break
            case MalformedURLException:
                "Invalid URL: ${rootCause.message}"
                break
            case UnknownHostException:
                "Unknown host: ${rootCause.message}"
                break
            case MalformedJsonException:
            case JsonSyntaxException:
                "The server responded incorrectly (check the URL)"
                break
            default:
                "Unknown error: ${rootCause.message}"
        }
    }

}
