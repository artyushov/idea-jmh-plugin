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
 * Supports creating run configurations from context (by right-clicking a code element in the source editor or the project view).
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class JmhConfigurationProducer extends JavaRunConfigurationProducerBase<JmhConfiguration> implements Cloneable {

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return JmhConfigurationType.getInstance().getConfigurationFactories()[0];
    }

    /**
     * Sets up a configuration based on the specified context.
     *
     * @param configuration a clone of the template run configuration of the specified type
     * @param context       contains the information about a location in the source code.
     * @param sourceElement a reference to the source element for the run configuration (by default contains the element at caret,
     *                      can be updated by the producer to point to a higher-level element in the tree).
     * @return true if the context is applicable to this run configuration producer, false if the context is not applicable and the
     * configuration should be discarded.
     */
    @Override
    protected abstract boolean setupConfigurationFromContext(@NotNull JmhConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement);

    /**
     * Checks if the specified configuration was created from the specified context.
     *
     * @param configuration a configuration instance.
     * @param context       contains the information about a location in the source code.
     * @return true if this configuration was created from the specified context, false otherwise.
     */
    @Override
    public abstract boolean isConfigurationFromContext(@NotNull JmhConfiguration configuration, @NotNull ConfigurationContext context);

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
