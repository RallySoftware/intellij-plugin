package com.rallydev.intellij.tool

import com.google.common.util.concurrent.FutureCallback
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.util.AsyncService
import com.rallydev.intellij.util.ErrorMessageFutureCallback
import com.rallydev.intellij.util.SwingService
import com.rallydev.intellij.wsapi.cache.TypeDefinitionCacheService
import com.rallydev.intellij.wsapi.client.PostRequest
import com.rallydev.intellij.wsapi.client.RallyClient
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.AttributeDefinition
import com.rallydev.intellij.wsapi.domain.Requirement
import com.rallydev.intellij.wsapi.domain.TypeDefinition
import org.jetbrains.annotations.NotNull

import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextPane
import javax.swing.border.EmptyBorder
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static com.rallydev.intellij.wsapi.ApiEndpoint.HIERARCHICAL_REQUIREMENT
import static com.rallydev.intellij.wsapi.ApiEndpoint.PROJECT

class ArtifactTabImpl extends ArtifactTab {

    protected JLabel projectLabel = new JLabel()
    protected JLabel projectValue = new JLabel()

    protected JLabel lastUpdatedLabel = new JLabel('Last Updated')
    protected JLabel lastUpdated = new JLabel()

    protected JLabel descriptionLabel = new JLabel(text: 'Description')
    protected JTextPane description = new JTextPane(
            preferredSize: new Dimension(0, 0), //force line-wraps rather than horizontal scrolling
            contentType: 'text/html',
            editable: false,
    )

    protected JLabel notesLabel = new JLabel('Notes')
    protected JTextPane notes = new JTextPane(
            preferredSize: new Dimension(0, 0), //force line-wraps rather than horizontal scrolling
            contentType: 'text/html',
            editable: false,
    )

    Project project
    Artifact artifact
    Map<String, JComponent> dynamicFields = new HashMap<String, JComponent>()

    public ArtifactTabImpl(@NotNull Artifact artifact, @NotNull Project project) {
        this.project = project
        this.artifact = artifact
    }

    void setup() {
        dynamicFieldsPanel.setLayout(new GridBagLayout())

        setupDefaultFields()
        populateDefaultFields()
        loadLabels()

        setupAndPopulateDynamicFields()

        setupViewButton()
        setupSaveButton()
        openTaskContextButton.addActionListener(new OpenTaskListener(project, artifact))
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

    private void setupDefaultFields() {
        dynamicFieldsPanel.add(projectLabel, getLabelConstraints(0))
        dynamicFieldsPanel.add(projectValue, getFieldSimpleConstraints(0))

        dynamicFieldsPanel.add(lastUpdatedLabel, getLabelConstraints(1))
        dynamicFieldsPanel.add(lastUpdated, getFieldSimpleConstraints(1))

        dynamicFieldsPanel.add(descriptionLabel, getLabelConstraints(2))
        dynamicFieldsPanel.add(description, getFieldTextConstraints(2, 2))

        dynamicFieldsPanel.add(notesLabel, getLabelConstraints(200))
        dynamicFieldsPanel.add(notes, getFieldTextConstraints(200))

        dynamicFieldsPanel.revalidate()
        dynamicFieldsPanel.repaint()
    }

    private void populateDefaultFields() {
        header.text = "${artifact.formattedID} - ${artifact.name}"
        projectValue.text = artifact.projectName
        lastUpdated.text = artifact.formattedLastUpdateDate

        description.text = "<html>${artifact.description ?: ''}</html>"
        description.border = new EmptyBorder(5, 5, 5, 5)

        notes.text = "<html>${artifact.notes ?: ''}</html>"
        notes.border = new EmptyBorder(5, 5, 5, 5)
    }



    private void loadLabels() {
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

    private void setupAndPopulateDynamicFields() {
        if (artifact instanceof Requirement) {
            Closure<TypeDefinition> call = {
                TypeDefinitionCacheService.instance.getTypeDefinition(HIERARCHICAL_REQUIREMENT.typeDefinitionElementName, artifact.workspaceRef)
            }

            FutureCallback<TypeDefinition> callback = new ErrorMessageFutureCallback<TypeDefinition>() {
                void onSuccess(TypeDefinition typeDefinition) {
                    populateRequirementFields(typeDefinition, (Requirement) artifact)
                }

                void onFailure(Throwable error) {
                    super.onFailure(error)
                }
            }

            AsyncService.instance.schedule(call, callback)
        }
    }

    private void populateRequirementFields(TypeDefinition typeDefinition, Requirement requirement) {
        //setup schedule state
        AttributeDefinition scheduleStateAttributeDefinition = typeDefinition.findAttributeDefinition("ScheduleState")
        JComboBox scheduleStateComboBox = new JComboBox()
        dynamicFields.put("scheduleState", scheduleStateComboBox)
        scheduleStateAttributeDefinition.allowedValues.each { allowedValue ->
            scheduleStateComboBox.addItem(allowedValue)
        }

        SwingService.instance.queueForUiThread {
            scheduleStateComboBox.selectedItem = requirement.scheduleState

            dynamicFieldsPanel.add(
                    new JLabel(scheduleStateAttributeDefinition.name),
                    getLabelConstraints(100)
            )
            dynamicFieldsPanel.add(
                    scheduleStateComboBox,
                    getFieldSimpleConstraints(100, [weightx: 0, fill: GridBagConstraints.NONE])
            )

            dynamicFieldsPanel.revalidate()
            dynamicFieldsPanel.repaint()
        }
    }

    private void setupViewButton() {
        viewInRallyButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent actionEvent) {
                viewInRally()
            }
        })
    }

    private void setupSaveButton() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent actionEvent) {
                RallyClient.instance.makeRequest(new PostRequest(domainObject: artifact))
            }
        })
    }

    private void viewInRally() {
        String url = "${RallyConfig.instance.url}/#/search?keywords=${artifact.formattedID}"
        launchBrowser(url)
    }

    private void launchBrowser(String url) {
        BrowserUtil.launchBrowser(url)
    }

    private GridBagConstraints getLabelConstraints(int row) {
        return new GridBagConstraints(
                gridx: 0, gridy: row, weightx: 0, insets: defaultInsets,
                anchor: GridBagConstraints.FIRST_LINE_START
        )
    }

    private GridBagConstraints getFieldSimpleConstraints(int row, Map<String, Object> overrides = [:]) {
        Map<String, Object> defaultConstraints = [
                gridx: 1,
                weightx: 1,
                gridy: row,
                weighty: 0,
                insets: defaultInsets,
                anchor: GridBagConstraints.FIRST_LINE_START,
                fill: GridBagConstraints.HORIZONTAL
        ]
        return new GridBagConstraints(defaultConstraints + overrides)
    }

    private GridBagConstraints getFieldTextConstraints(int row, int weighty = 1) {
        return new GridBagConstraints(
                gridx: 1, gridy: row,
                weightx: 1, weighty: weighty,
                insets: defaultInsets,
                fill: GridBagConstraints.BOTH
        )
    }

    private Insets getDefaultInsets() {
        Integer marginVertical = 3
        Integer marginHorizontal = 5
        return new Insets(marginVertical, marginHorizontal, marginVertical, marginHorizontal)
    }

}
