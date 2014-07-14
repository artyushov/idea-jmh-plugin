package ru.artyushov.jmhPlugin.configuration;

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

//    private JComponent anchor;
    private JPanel editor = new JPanel();

//    @Override
//    public JComponent getAnchor() {
//        return anchor;
//    }
//
//    @Override
//    public void setAnchor(@Nullable JComponent anchor) {
//        this.anchor = anchor;
//    }
//
    @Override
    protected void resetEditorFrom(JmhConfiguration jmhConfiguration) {

    }

    @Override
    protected void applyEditorTo(JmhConfiguration jmhConfiguration) throws ConfigurationException {

    }

    @NotNull
    @Override
    protected JComponent createEditor() {

        return editor;
    }
}
