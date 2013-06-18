package com.rallydev.intellij.config;

import com.intellij.ide.passwordSafe.MasterPasswordUnavailableException;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.ide.passwordSafe.impl.PasswordSafeImpl;
import com.intellij.ide.passwordSafe.impl.providers.memory.MemoryPasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/** Several methods based on org.jetbrains.plugins.github.GithubSettings */
@State(
        name = "RallyConfig",
        storages = @Storage(file = "$APP_CONFIG$/rally.xml")
)
public class RallyConfig implements PersistentStateComponent<Element> {
    private static final String RALLY_CONFIG_TAG = "RallyConfig";
    private static final String RALLY_CONFIG_PASSWORD_KEY = "RALLY_CONFIG_PASSWORD_KEY";
    private static final Logger log = Logger.getInstance(RallyConfig.class);

    private static final String URL = "url";
    private static final String USER_NAME = "userName";
    private static final String REMEMBER_PASSWORD = "rememberPassword";

    public String url;
    public String userName;
    public boolean rememberPassword;

    public List<String> workspaces;

    private boolean passwordChanged = false;
    private boolean masterPasswordRefused = false;

    //Used when no XML file on disk yet
    public RallyConfig() {
        url = "https://rally1.rallydev.com";
        rememberPassword = true;
        workspaces = new LinkedList<String>();
    }

    public static RallyConfig getInstance() {
        return ServiceManager.getService(RallyConfig.class);
    }

    @Nullable
    @Override
    public Element getState() {
        storePassword();

        Element element = new Element(RALLY_CONFIG_TAG);
        element.setAttribute(URL, url);
        element.setAttribute(USER_NAME, userName);
        element.setAttribute(REMEMBER_PASSWORD, String.valueOf(rememberPassword));
        return element;
    }

    public void loadState(@NotNull final Element element) {
        url = element.getAttributeValue(URL);
        userName = element.getAttributeValue(USER_NAME);
        rememberPassword = Boolean.valueOf(element.getAttributeValue(REMEMBER_PASSWORD));
    }

    @NotNull
    public String getPassword() {
        String password;
        final Project project = getProject();
        final PasswordSafeImpl passwordSafe = (PasswordSafeImpl) PasswordSafe.getInstance();
        try {
            password = passwordSafe.getMemoryProvider().getPassword(project, RallyConfig.class, RALLY_CONFIG_PASSWORD_KEY);
            if (password != null && !"".equals(password)) {
                return password;
            }
            String storedPassword = getStoredPassword();
            if (storedPassword != null) {
                password = storedPassword;
            }
        } catch (PasswordSafeException e) {
            log.info("Couldn't get password for key [" + RALLY_CONFIG_PASSWORD_KEY + "]", e);
            masterPasswordRefused = true;
            password = "";
        }
        // Store password in memory
        try {
            passwordSafe.getMemoryProvider().storePassword(getProject(),
                    RallyConfig.class, RALLY_CONFIG_PASSWORD_KEY, password != null ? password : "");
        } catch (PasswordSafeException e) {
            log.info("Couldn't store password for key [" + RALLY_CONFIG_PASSWORD_KEY + "]", e);
        }
        passwordChanged = false;
        return password != null ? password : "";
    }

    private Project getProject() {
        return ProjectManager.getInstance().getDefaultProject();
    }

    public void setPassword(final String password) {
        passwordChanged = !getPassword().equals(password);
        cachePassword(password);
    }

    public void clearPassword() {
        setPassword(null);
    }

    public void clearCachedPassword() {
        cachePassword(null);
    }

    public void storePassword() {
        try {
            if (rememberPassword && passwordChanged && !masterPasswordRefused) {
                final Project project = getProject();
                PasswordSafe.getInstance().storePassword(
                        project, RallyConfig.class, RALLY_CONFIG_PASSWORD_KEY, getPassword()
                );
            }
        } catch (MasterPasswordUnavailableException e) {
            log.info("Couldn't store password for key [" + RALLY_CONFIG_PASSWORD_KEY + "]", e);
            masterPasswordRefused = true;
        } catch (Exception e) {
            Messages.showErrorDialog("Error happened while storing password for Rally", "Error");
            log.info("Couldn't get password for key [" + RALLY_CONFIG_PASSWORD_KEY + "]", e);
        }
    }

    public String getStoredPassword() throws PasswordSafeException {
        PasswordSafeImpl passwordSafe = (PasswordSafeImpl) PasswordSafe.getInstance();
        if (!masterPasswordRefused && rememberPassword) {
            return passwordSafe.getPassword(getProject(), RallyConfig.class, RALLY_CONFIG_PASSWORD_KEY);
        }
        return null;
    }

    protected void cachePassword(final String password) {
        try {
            final MemoryPasswordSafe memoryProvider = ((PasswordSafeImpl) PasswordSafe.getInstance()).getMemoryProvider();
            memoryProvider.storePassword(getProject(),
                    RallyConfig.class, RALLY_CONFIG_PASSWORD_KEY,
                    password != null ? password : "");
        } catch (PasswordSafeException e) {
            log.info("Couldn't get password for key [" + RALLY_CONFIG_PASSWORD_KEY + "]", e);
        }
    }

}
