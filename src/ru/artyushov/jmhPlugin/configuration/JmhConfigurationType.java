package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;

/**
 * User: nikart
 * Date: 01/05/14
 * Time: 13:46
 */
public class JmhConfigurationType extends ConfigurationTypeBase {

    private final ConfigurationFactory myFactory;

    public JmhConfigurationType() {
        super("jmh-id", "jmh-display-name", "jmh-description", AllIcons.RunConfigurations.Application);
        myFactory = new ConfigurationFactory(this) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new JmhConfiguration("jmh-configuration-name", project, this);
            }
        };
        addFactory(myFactory);
    }
}
