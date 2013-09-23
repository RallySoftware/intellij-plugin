package com.rallydev.intellij.wsapi.cache;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.rallydev.intellij.wsapi.domain.Workspace;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@State(
        name = "Rally Workspace Cache",
        storages = @Storage(file = "$APP_CONFIG$/rally_workspace_cache.xml")
)
public class WorkspaceCache implements PersistentStateComponent<WorkspaceCache> {

    public List<Workspace> workspaces = new LinkedList<Workspace>();
    public Date loadedOn;

    @Nullable
    @Override
    public WorkspaceCache getState() {
        return this;
    }

    @Override
    public void loadState(WorkspaceCache state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
