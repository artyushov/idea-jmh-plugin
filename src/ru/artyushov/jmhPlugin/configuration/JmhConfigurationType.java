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

    public static final String TYPE_ID = "jmh-id";

    public JmhConfigurationType() {
        super(TYPE_ID, "Jmh", "", AllIcons.RunConfigurations.Application);
        ConfigurationFactory myFactory = new ConfigurationFactory(this) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                JmhConfiguration configuration = new JmhConfiguration("jmh-configuration-name", project, this);
                configuration.setPassParentEnvs(true);
                return configuration;
            }
        };
        addFactory(myFactory);
    }
}
