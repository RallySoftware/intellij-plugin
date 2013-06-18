package com.rallydev.intellij.config;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RallyPasswordDialog extends DialogWrapper {
    private JPanel myPanel;
    private JPasswordField password;

    public RallyPasswordDialog() {
        super(false);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myPanel;
    }

    public static String askPassword() {
        RallyPasswordDialog dialog = new RallyPasswordDialog();
        dialog.show();
        switch (dialog.getExitCode()) {
            case OK_EXIT_CODE:
                return new String(dialog.password.getPassword());
        }
        return null;
    }

}
