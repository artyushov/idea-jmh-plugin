package ru.artyushov.jmhPlugin.configuration.producer;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.PathUtil;
import ru.artyushov.jmhPlugin.configuration.ConfigurationUtils;
import ru.artyushov.jmhPlugin.configuration.JmhConfiguration;

import java.util.Iterator;

import static ru.artyushov.jmhPlugin.configuration.JmhConfiguration.Type.CLASS;

/**
 * User: nikart
 * Date: 15/07/14
 * Time: 23:30
 */
public class JmhClassConfigurationProducer extends JmhConfigurationProducer {

    @Override
    protected boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        PsiClass benchmarkClass = getBenchmarkClass(context);
        if (benchmarkClass == null) {
            return false;
        }
        configuration.setBenchmarkClass(benchmarkClass.getQualifiedName());

        sourceElement.set(benchmarkClass);
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);
        configuration.setProgramParameters(
                createProgramParameters(toRunParams(benchmarkClass), configuration.getProgramParameters()));
        configuration.setWorkingDirectory(PathUtil.getLocalPath(context.getProject().getBaseDir()));
        configuration.setName(getNameForConfiguration(benchmarkClass));
        configuration.setType(CLASS);
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context) {
        if (configuration.getBenchmarkType() != CLASS) {
            return false;
        }
        if (ConfigurationUtils.getAnnotatedMethod(context) != null) {
            return false;
        }
        PsiClass benchmarkClass = getBenchmarkClass(context);
        if (benchmarkClass == null) {
            return false;
        }
        if (benchmarkClass.getQualifiedName() == null
                || !benchmarkClass.getQualifiedName().equals(configuration.getBenchmarkClass())) {
            return false;
        }
        String configurationName = getNameForConfiguration(benchmarkClass);
        return isConfigurationFromContext(configuration, context, configurationName);
    }

    private PsiClass getBenchmarkClass(ConfigurationContext context) {
        Location<?> location = context.getLocation();
        if (location == null) {
            return null;
        }
        for (Iterator<Location<PsiClass>> iterator = location.getAncestors(PsiClass.class, false); iterator.hasNext(); ) {
            final Location<PsiClass> classLocation = iterator.next();
            if (hasBenchmarks(classLocation.getPsiElement())) {
                return classLocation.getPsiElement();
            }
        }
        return null;
    }

    private boolean hasBenchmarks(PsiClass psiClass) {
        for (PsiMethod method : psiClass.getMethods()) {
            if (ConfigurationUtils.hasBenchmarkAnnotation(method)) {
                return true;
            }
        }
        return false;
    }

    private String getNameForConfiguration(PsiClass benchmarkClass) {
        return benchmarkClass.getName();
    }

    private String toRunParams(PsiClass benchmarkClass) {
        return benchmarkClass.getQualifiedName() + ".*";
    }
}
