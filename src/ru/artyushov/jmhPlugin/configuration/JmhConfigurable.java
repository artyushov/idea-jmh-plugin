package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: nikart
 * Date: 01/05/14
 * Time: 12:55
 */
public class JmhConfigurable extends SettingsEditor<JmhConfiguration> {

    private JPanel editor = new JPanel();

    private final CommonJavaParametersPanel commonProgramParameters;

    public JmhConfigurable() {
        editor.setLayout(new BoxLayout(editor, BoxLayout.X_AXIS));
        commonProgramParameters = new CommonJavaParametersPanel();
        editor.add(commonProgramParameters);
    }

    @Override
    protected void resetEditorFrom(@NotNull JmhConfiguration jmhConfiguration) {
        commonProgramParameters.reset(jmhConfiguration);
    }

    @Override
    protected void applyEditorTo(@NotNull JmhConfiguration jmhConfiguration) throws ConfigurationException {
        commonProgramParameters.applyTo(jmhConfiguration);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {

        return editor;
    }
}
