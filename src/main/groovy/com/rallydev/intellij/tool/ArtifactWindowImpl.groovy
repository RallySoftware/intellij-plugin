package com.rallydev.intellij.tool

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.impl.ContentImpl
import com.rallydev.intellij.wsapi.domain.Artifact

class ArtifactWindowImpl implements ToolWindowFactory, Observer {

    ToolWindow myToolWindow

    List<Artifact> artifacts = new LinkedList<>()

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        myToolWindow = toolWindow

        List<Artifact> testArtifacts = [
                new Artifact(formattedID: 'S1', name: 'Add panels',
                        projectName: "Don't Panic", lastUpdateDate: new Date(),
                        description: """
As a <em>developer</em>
I want <strong>write</strong> this really really long sentence to see if it wraps and
<ul>
<li>Do thing</li>
<li>Do thing well</li>
<li>Undo thing</li>
</ul>
This<br/>
content<br/>
needs<br/>
to<br/>
scroll<br/>
because<br/>
it<br/>
is<br/>
too<br/>
high<br/>
and<br/>
should<br/>
not<br/>
all<br/>
be<br/>
displayed<br/>
at<br/>
once<br/>
and<br/>
it<br/>
really<br/>
is<br/>
too<br/>
long<br/>
""",
                        notes: """
This only happens in IE
"""
                )
        ]

        ServiceManager.getService(OpenArtifacts.class).artifacts.addAll(testArtifacts)

        ServiceManager.getService(OpenArtifacts.class).addObserver(this)
        ServiceManager.getService(OpenArtifacts.class).artifacts.each {
            addTab(it)
        }
//        toolWindow.getContentManager().addContent(new ContentImpl(new ArtifactTab(), "test", true))
    }

    //Cache by id & refresh if already there
    void addTab(Artifact artifact) {
        ContentImpl content = new ContentImpl(new ArtifactTab(artifact), artifact.formattedID, true)
        myToolWindow.getContentManager().addContent(content)
        myToolWindow.getContentManager().setSelectedContent(content, true)
    }

    @Override
    void update(Observable observable, Object artifact) {
        addTab((Artifact) artifact)
    }

}
