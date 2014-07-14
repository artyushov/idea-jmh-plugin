package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import java.util.Iterator;

/**
 * User: nikart
 * Date: 14/07/14
 * Time: 23:06
 */
public class JmhMethodConfigurationProducer extends JmhConfigurationProducer {

    @Override
    protected boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        Location<PsiMethod> methodLocation = getTestMethod(context.getLocation());
        if (methodLocation == null) {
            return false;
        }
        sourceElement.set(methodLocation.getPsiElement());
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);

        configuration.beMethodConfiguration(methodLocation);
        return true;

    }

    private static Location<PsiMethod> getTestMethod(final Location<?> location) {
        Iterator<Location<PsiMethod>> iterator = location.getAncestors(PsiMethod.class, false);
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

}
