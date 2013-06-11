package com.rallydev.intellij.tool

import com.rallydev.intellij.wsapi.domain.Artifact

class OpenArtifacts extends Observable {

    List<Artifact> artifacts = new ArrayList<>()

    void add(Artifact artifact) {
        setChanged()
        notifyObservers(artifact)
        artifacts.add(artifact)
    }

    void leftShift(Artifact artifact) {
        add(artifact)
    }

}
