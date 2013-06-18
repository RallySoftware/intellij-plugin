package com.rallydev.intellij.wsapi.cache;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.rallydev.intellij.wsapi.domain.Project;
import com.rallydev.intellij.wsapi.domain.Workspace;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@State(
        name = "Rally Project Cache",
        storages = @Storage(file = "$APP_CONFIG$/rally_project_cache.xml")
)
public class ProjectCache implements PersistentStateComponent<ProjectCache> {

    public Workspace workspace;
    public List<Project> projects = new LinkedList<Project>();
    public Date loaded;

    @Nullable
    @Override
    public ProjectCache getState() {
        return this;
    }

    @Override
    public void loadState(ProjectCache state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
