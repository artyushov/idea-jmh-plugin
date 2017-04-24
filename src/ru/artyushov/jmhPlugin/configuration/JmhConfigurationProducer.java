package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;

/**
 * User: nikart
 * Date: 09/04/14
 * Time: 18:45
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class JmhConfigurationProducer extends JavaRunConfigurationProducerBase<JmhConfiguration> implements Cloneable {

    public JmhConfigurationProducer(ConfigurationType configurationType) {
        super(configurationType);
    }

    public JmhConfigurationProducer() {
        super(ContainerUtil.findInstance(
                Extensions.getExtensions(ConfigurationType.CONFIGURATION_TYPE_EP), JmhConfigurationType.class));
    }

    @Override
    protected abstract boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement);

    @Override
    public abstract boolean isConfigurationFromContext(JmhConfiguration jmhConfiguration, ConfigurationContext configurationContext);

    String createProgramParameters(String generatedParams, String defaultParams) {
        return defaultParams != null ? generatedParams + " " + defaultParams : generatedParams;
    }
}
