package ru.artyushov.jmhPlugin.configuration.producer;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.PathUtil;
import ru.artyushov.jmhPlugin.configuration.ConfigurationUtils;
import ru.artyushov.jmhPlugin.configuration.JmhConfiguration;

/**
 * User: nikart
 * Date: 14/07/14
 * Time: 23:06
 */
public class JmhMethodConfigurationProducer extends JmhConfigurationProducer {

    @Override
    protected boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        PsiMethod method = ConfigurationUtils.getAnnotatedMethod(context);
        if (method == null) {
            return false;
        }
        sourceElement.set(method);
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);

        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return false;
        }
        configuration.setBenchmarkClass(containingClass.getQualifiedName());
        configuration.setType(JmhConfiguration.Type.METHOD);
        configuration.setProgramParameters(
                createProgramParameters(containingClass.getQualifiedName() + "." + method.getName(), configuration.getProgramParameters()));
        configuration.setName(containingClass.getName() + "." + method.getName());
        configuration.setWorkingDirectory(PathUtil.getLocalPath(context.getProject().getBaseDir()));
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context) {
        if (configuration.getBenchmarkType() != JmhConfiguration.Type.METHOD) {
            return false;
        }
        PsiMethod method = ConfigurationUtils.getAnnotatedMethod(context);
        if (method == null) {
            return false;
        }
        if (method.getContainingClass() == null
                || method.getContainingClass().getQualifiedName() == null
                || !method.getContainingClass().getQualifiedName().equals(configuration.getBenchmarkClass())) {
            return false;
        }
        String configurationName = getNameForConfiguration(method);
        return isConfigurationFromContext(configuration, context, configurationName);
    }

    private String getNameForConfiguration(PsiMethod method) {
        PsiClass clazz = method.getContainingClass();
        if (clazz == null) {
            return null;
        }
        return clazz.getName() + "." + method.getName();
    }
}
