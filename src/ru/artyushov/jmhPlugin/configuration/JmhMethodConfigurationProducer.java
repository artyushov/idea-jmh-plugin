package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
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
        PsiMethod method = methodLocation.getPsiElement();
        sourceElement.set(method);
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);

        if (!ConfigurationUtils.hasBenchmarkAnnotation(method)) {
            return false;
        }
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return false;
        }
        configuration.setProgramParameters(containingClass.getQualifiedName() + "." + method.getName());
        configuration.setName(containingClass.getName() + "." + method.getName());
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
