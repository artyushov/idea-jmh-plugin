package ru.artyushov.jmhPlugin.configuration.producer;

import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import ru.artyushov.jmhPlugin.configuration.JmhConfiguration;
import ru.artyushov.jmhPlugin.configuration.JmhConfigurationType;

/**
 * User: nikart
 * Date: 09/04/14
 * Time: 18:45
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class JmhConfigurationProducer extends JavaRunConfigurationProducerBase<JmhConfiguration> implements Cloneable {

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return JmhConfigurationType.getInstance().getConfigurationFactories()[0];
    }

    @Override
    protected abstract boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement);

    @Override
    public abstract boolean isConfigurationFromContext(JmhConfiguration jmhConfiguration, ConfigurationContext configurationContext);

    protected boolean isConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context, String configurationName) {
        if (!configuration.getName().equals(configurationName)) {
            return false;
        }
        Location locationFromContext = context.getLocation();
        if (locationFromContext == null) {
            return false;
        }
        Location location = JavaExecutionUtil.stepIntoSingleClass(locationFromContext);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        if (location.getModule() == null || !location.getModule().equals(originalModule)) {
            return false;
        }
        setupConfigurationModule(context, configuration);
        configuration.restoreOriginalModule(originalModule);

        return true;
    }

    String createProgramParameters(String generatedParams, String defaultParams) {
        return defaultParams != null ? generatedParams + " " + defaultParams : generatedParams;
    }
}
