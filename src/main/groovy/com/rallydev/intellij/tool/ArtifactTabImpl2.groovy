package com.rallydev.intellij.tool

import com.google.common.util.concurrent.FutureCallback
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
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

import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static com.rallydev.intellij.wsapi.ApiEndpoint.HIERARCHICAL_REQUIREMENT
import static com.rallydev.intellij.wsapi.ApiEndpoint.PROJECT

class ArtifactTabImpl2 extends ArtifactTab2 {

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

    public ArtifactTabImpl2(@NotNull Artifact artifact, @NotNull Project project) {
        this.project = project
        this.artifact = artifact
    }

    void setup() {
        dynamicFieldsPanel.setLayout(new GridBagLayout())

        setupDefaultFields()
        setupLabels()

        setupDynamicFields()
        populateFields()

        setupViewButton()
        openTaskContextButton.addActionListener(new OpenTaskListener(project, artifact))
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

    private void setupDynamicFields() {
        //todo: can I check class now with the refactoring?
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
                        dynamicFieldsPanel.add(
                                new JLabel(scheduleStateAttributeDefinition.name),
                                getLabelConstraints(100)
                        )
                        dynamicFieldsPanel.add(
                                scheduleStateComboBox,
                                //todo: left off here - combo box is filling horizontal width, but I that's not the desired behavior
                                getFieldSimpleConstraints(100, [weightx: 0, fill: GridBagConstraints.NONE])
                        )

                        dynamicFieldsPanel.revalidate()
                        dynamicFieldsPanel.repaint()
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
        lastUpdated.text = artifact.formattedLastUpdateDate

        description.text = "<html>${artifact.description ?: ''}</html>"
        description.border = new EmptyBorder(5, 5, 5, 5)

        notes.text = "<html>${artifact.notes ?: ''}</html>"
        notes.border = new EmptyBorder(5, 5, 5, 5)
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

    private GridBagConstraints getLabelConstraints(int row) {
        return new GridBagConstraints(
                gridx: 0, gridy: row, weightx: 0, insets: defaultInsets,
                anchor: GridBagConstraints.FIRST_LINE_START
        )
    }

    private GridBagConstraints getFieldSimpleConstraints(int row, Map<String, Integer> overrides = [:]) {
        return new GridBagConstraints(
                gridx: overrides['gridx'] ?: 1, gridy: row,
                weightx: overrides['weightx'] ?: 1, weighty: overrides['weighty'] ?: 0,
                insets: defaultInsets,
                fill: overrides['fill'] ?: GridBagConstraints.HORIZONTAL
        )
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
