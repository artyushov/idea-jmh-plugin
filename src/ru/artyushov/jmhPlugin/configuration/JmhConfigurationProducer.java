package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.containers.ContainerUtil;

import java.util.Iterator;

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
    protected boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        Location<PsiMethod> methodLocation = getTestMethod(context.getLocation());
        if (methodLocation == null) return false;
        sourceElement.set(methodLocation.getPsiElement());
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);

        final PsiMethod method = methodLocation.getPsiElement();
        configuration.setProgramParameters(method.getContainingClass().getQualifiedName() + "." + method.getName() + " -f 1");



        JavaRunConfigurationExtensionManager.getInstance().extendCreatedConfiguration(configuration, context.getLocation());
        return true;

    }

    @Override
    public boolean isConfigurationFromContext(JmhConfiguration jmhConfiguration, ConfigurationContext configurationContext) {
        return false;
    }

    private static Location<PsiMethod> getTestMethod(final Location<?> location) {
        for (Iterator<Location<PsiMethod>> iterator = location.getAncestors(PsiMethod.class, false); iterator.hasNext();) {
            return iterator.next();
        }
        return null;
    }
}
