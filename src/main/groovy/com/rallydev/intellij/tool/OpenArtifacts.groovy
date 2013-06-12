package com.rallydev.intellij.tool

import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.wsapi.domain.Artifact

class OpenArtifacts extends Observable {

    List<Artifact> artifacts = new ArrayList<>()

    public static OpenArtifacts getInstance() {
        return ServiceManager.getService(OpenArtifacts.class);
    }

    void add(Artifact artifact) {
        setChanged()
        notifyObservers(artifact)
        artifacts.add(artifact)
    }

    void leftShift(Artifact artifact) {
        add(artifact)
    }

}
