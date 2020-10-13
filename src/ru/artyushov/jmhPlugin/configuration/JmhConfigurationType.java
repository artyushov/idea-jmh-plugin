package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import org.jetbrains.annotations.NotNull;

import static com.intellij.icons.AllIcons.Actions.ProfileYellow;

/**
 * User: nikart
 * Date: 01/05/14
 * Time: 13:46
 */
public class JmhConfigurationType extends SimpleConfigurationType {

    public static final String TYPE_ID = "jmh-id";

    public JmhConfigurationType() {
        super(TYPE_ID, "Jmh", "Configuration to run a JMH benchmark", NotNullLazyValue.createValue(() -> ProfileYellow));
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        JmhConfiguration configuration = new JmhConfiguration("jmh-configuration-name", project, this);
        configuration.setPassParentEnvs(true);
        return configuration;
    }

    @NotNull
    public static JmhConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(JmhConfigurationType.class);
    }
}
