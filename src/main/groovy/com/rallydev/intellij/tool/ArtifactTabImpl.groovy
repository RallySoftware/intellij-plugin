package com.rallydev.intellij.tool

import com.google.common.util.concurrent.FutureCallback
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.util.AsyncService
import com.rallydev.intellij.util.ErrorMessageFutureCallback
import com.rallydev.intellij.util.SwingService
import com.rallydev.intellij.wsapi.cache.TypeDefinitionCacheService
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.AttributeDefinition
import com.rallydev.intellij.wsapi.domain.Requirement
import com.rallydev.intellij.wsapi.domain.TypeDefinition
import org.jetbrains.annotations.NotNull

import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.border.EmptyBorder
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static com.rallydev.intellij.wsapi.ApiEndpoint.HIERARCHICAL_REQUIREMENT
import static com.rallydev.intellij.wsapi.ApiEndpoint.PROJECT

class ArtifactTabImpl extends ArtifactTab {

    Project project
    Artifact artifact
    Map<String, JComponent> dynamicFields = new HashMap<String, JComponent>()

    public ArtifactTabImpl(@NotNull Artifact artifact, @NotNull Project project) {
        this.project = project
        this.artifact = artifact
    }

    void setup() {
        setupLabels()
        populateFields()
        setupDynamicFields()
        setupViewButton()
        openTaskContextButton.addActionListener(new OpenTaskListener(project, artifact))
    }

    private void setupDynamicFields() {
        if (artifact._type == HIERARCHICAL_REQUIREMENT.typeDefinitionElementName) {
            Requirement requirement = (Requirement) artifact
            Closure<TypeDefinition> call = {
                TypeDefinitionCacheService.instance.getTypeDefinition(HIERARCHICAL_REQUIREMENT.typeDefinitionElementName, artifact.workspaceRef)
            }

            FutureCallback<TypeDefinition> callback = new ErrorMessageFutureCallback<TypeDefinition>() {
                void onSuccess(TypeDefinition typeDefinition) {
                    //setup schedule state
                    AttributeDefinition scheduleStateAttributeDefinition = typeDefinition.findAttributeDefinition("ScheduleState")
                    JComboBox scheduleStateComboBox = new JComboBox()
                    dynamicFields.put("scheduleState", scheduleStateComboBox)
                    scheduleStateAttributeDefinition.allowedValues.each { allowedValue ->
                        scheduleStateComboBox.addItem(allowedValue)
                    }

                    //populate
                    scheduleStateComboBox.selectedItem = requirement.scheduleState

                    SwingService.instance.queueForUiThread {
                        dynamicFieldPanel.setLayout(new GridLayoutManager(1, 2))
//                        dynamicFieldPanel.add(new JLabel(scheduleStateAttributeDefinition.name), new GridBagConstraints(gridx: 0, gridy: 0))
//                        dynamicFieldPanel.add(scheduleStateComboBox, new GridBagConstraints(gridx: 0, gridy: 1))
                        dynamicFieldPanel.add(new JLabel(scheduleStateAttributeDefinition.name), new GridConstraints(row: 0, column: 0))
                        dynamicFieldPanel.add(scheduleStateComboBox, new GridConstraints(row: 0, column: 1))
                    }
                }

                void onFailure(Throwable error) {
                    super.onFailure(error)
                }
            }

            AsyncService.instance.schedule(call, callback)
        }

    }

    private void populateFields() {
        header.text = "${artifact.formattedID} - ${artifact.name}"
        projectValue.text = artifact.projectName
        lastUpdatedLabel.text = artifact.formattedLastUpdateDate

        description.text = "<html>${artifact.description ?: ''}</html>"
        description.border = new EmptyBorder(5, 5, 5, 5)

        notes.text = "<html>${artifact.notes ?: ''}</html>"
        notes.border = new EmptyBorder(5, 5, 5, 5)
    }

    private void setupLabels() {
        Closure<TypeDefinition> call = {
            TypeDefinitionCacheService.instance.getTypeDefinition(PROJECT.typeDefinitionElementName, artifact.workspaceRef)
        }

        FutureCallback<TypeDefinition> callback = new ErrorMessageFutureCallback<TypeDefinition>() {
            void onSuccess(TypeDefinition typeDefinition) {
                SwingService.instance.queueForUiThread {
                    projectLabel.setText(typeDefinition.displayName)
                }
            }

            void onFailure(Throwable error) {
                super.onFailure(error)
            }
        }

        AsyncService.instance.schedule(call, callback)
    }


    private void setupViewButton() {
        viewInRallyButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent actionEvent) {
                viewInRally()
            }
        })
    }

    void viewInRally() {
        String url = "${RallyConfig.instance.url}/#/search?keywords=${artifact.formattedID}"
        launchBrowser(url)
    }

    void launchBrowser(String url) {
        BrowserUtil.launchBrowser(url)
    }

    String getFieldValue(String fieldName) {
        String value = null
        if (dynamicFields.containsKey(fieldName)) {
            switch (dynamicFields[fieldName].class) {
                case JComboBox:
                    value = ((JComboBox) dynamicFields[fieldName]).selectedItem as String
            }
        }

        return value
    }

}
