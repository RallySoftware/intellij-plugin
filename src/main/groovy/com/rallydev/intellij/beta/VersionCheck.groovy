package com.rallydev.intellij.beta

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.concurrency.JobScheduler
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger
import com.rallydev.intellij.util.IdeNotification

import java.util.concurrent.TimeUnit

class VersionCheck implements ApplicationComponent {
    static final Logger log = Logger.getInstance(this)
    static final String PAGES_URL = 'http://rallysoftware.github.io/intellij-plugin'

    @Override
    void initComponent() {
        Closure check = {
            checkVersion()
        }
        JobScheduler.getScheduler().scheduleAtFixedRate(check, 0, 8, TimeUnit.HOURS)
    }

    private void checkVersion() {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginManager.getPluginByClassName(VersionCheck.name))
        Double installedVersion = plugin.version as Double
        try {
            String versionJsonRaw = "${PAGES_URL}/version.json".toURL().text
            JsonElement versionJson = new JsonParser().parse(versionJsonRaw)
            Double currentVersion = versionJson['current-version'].value
            if (installedVersion < currentVersion) {
                IdeNotification.showWarning(
                        "${plugin.name} is out of date", """
Dear beta tester,
The ${plugin.name} plugin is still under active development and your installed version is out of date.
Please run 'IntelliJ IDEA' -> 'Check for Updates' and install the new version.
"""
                )
            }
        } catch (Exception e) {
            log.error(e)
        }
    }

    @Override
    void disposeComponent() {
    }

    @Override
    String getComponentName() {
        'VersionCheck'
    }

}
