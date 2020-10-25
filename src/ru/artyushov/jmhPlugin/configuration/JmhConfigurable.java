package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.ui.JrePathEditor;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ui.DefaultJreSelector;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * User: nikart
 * Date: 01/05/14
 * Time: 12:55
 */
public class JmhConfigurable extends SettingsEditor<JmhConfiguration> implements PanelWithAnchor {
    private final ModulesComboBox myModules = new ModulesComboBox();
    private final JBLabel myModuleLabel = new JBLabel(ExecutionBundle.message("application.configuration.use.classpath.and.jdk.of.module.label"));
    private final LabeledComponent<RawCommandLineEditor> myVMParameters = new LabeledComponent<>();
    private final LabeledComponent<RawCommandLineEditor> myProgramParameters = new LabeledComponent<>();
    private JComponent anchor;
    private final JrePathEditor myJrePathEditor;

    private final JmhConfiguration myConfiguration;

    public JmhConfigurable(final JmhConfiguration configuration) {
        myConfiguration = configuration;
        setAnchor(myModuleLabel);
        myJrePathEditor = new JrePathEditor(DefaultJreSelector.fromModuleDependencies(myModules, true));
        myJrePathEditor.setAnchor(myModuleLabel);
    }

    @Override
    public JComponent getAnchor() {
        return anchor;
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
        this.anchor = anchor;
        myModuleLabel.setAnchor(anchor);
        myVMParameters.setAnchor(anchor);
        myProgramParameters.setAnchor(anchor);
    }

    @Override
    public void resetEditorFrom(@NotNull JmhConfiguration configuration) {
        myModules.setSelectedModule(configuration.getDefaultModule());
        getVMParameters().setText(configuration.getVMParameters());
        getProgramParameters().setText(configuration.getProgramParameters());
        myJrePathEditor.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
    }


    @Override
    public void applyEditorTo(@NotNull JmhConfiguration configuration) {
        configuration.setModule(myModules.getSelectedModule());
        configuration.setVMParameters(getVMParameters().getText());
        configuration.setProgramParameters(getProgramParameters().getText());
        configuration.setAlternativeJrePath(myJrePathEditor.getJrePathOrName());
        configuration.setAlternativeJrePathEnabled(myJrePathEditor.isAlternativeJreSelected());
    }

    @Override
    @NotNull
    public JComponent createEditor() {
        myModules.fillModules(myConfiguration.getProject(), JavaModuleType.getModuleType());
        JPanel wholePanel = new JPanel(new GridBagLayout());
        myVMParameters.setText(ExecutionBundle.message("run.configuration.java.vm.parameters.label"));
        myVMParameters.setComponent(new RawCommandLineEditor());
        myVMParameters.getComponent().setDialogCaption(myVMParameters.getRawText());
        myVMParameters.setLabelLocation(BorderLayout.WEST);
        myVMParameters.setAnchor(myModuleLabel);

        myProgramParameters.setText(ExecutionBundle.message("run.configuration.program.parameters"));
        myProgramParameters.setComponent(new RawCommandLineEditor());
        myProgramParameters.getComponent().setDialogCaption(myProgramParameters.getRawText());
        myProgramParameters.setLabelLocation(BorderLayout.WEST);
        myProgramParameters.setAnchor(myModuleLabel);

        GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                JBUI.insets(2, 0, 0, 0), UIUtil.DEFAULT_HGAP, 0);
        wholePanel.add(myVMParameters, gc);
        wholePanel.add(myProgramParameters, gc);
        gc.gridwidth = 1;
        gc.gridy = 3;
        gc.weightx = 0;
        wholePanel.add(myModuleLabel, gc);
        gc.gridx = 1;
        gc.weightx = 1;
        wholePanel.add(myModules, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        gc.gridwidth = 2;

        wholePanel.add(myJrePathEditor, gc);
        gc.weighty = 1;
        gc.gridy = 5;
        wholePanel.add(Box.createVerticalBox(), gc);
        return wholePanel;
    }

    public RawCommandLineEditor getVMParameters() {
        return myVMParameters.getComponent();
    }

    public RawCommandLineEditor getProgramParameters() {
        return myProgramParameters.getComponent();
    }
}
