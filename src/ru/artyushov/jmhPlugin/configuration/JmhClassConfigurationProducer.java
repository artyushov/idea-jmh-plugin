package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.PathUtil;

import java.util.Iterator;

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
        sourceElement.set(benchmarkClass);
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);
        configuration.setProgramParameters(benchmarkClass.getQualifiedName() + ".*");
        configuration.setWorkingDirectory(PathUtil.getLocalPath(context.getProject().getBaseDir()));
        configuration.setName(benchmarkClass.getName());

        configuration.setType(JmhConfiguration.Type.CLASS);
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context) {
        if (getAnnotatedMethod(context) != null) {
            return false;
        }
        if (configuration.getBenchmarkType() != JmhConfiguration.Type.CLASS) {
            return false;
        }
        PsiClass benchmarkClass = getBenchmarkClass(context);
        if (benchmarkClass == null) {
            return false;
        }
        String nameFromContext = benchmarkClass.getName();
        if (configuration.getName() == null || !configuration.getName().equals(nameFromContext)) {
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

    private PsiMethod getAnnotatedMethod(ConfigurationContext context) {
        Location<?> location = context.getLocation();
        if (location == null) {
            return null;
        }
        Iterator<Location<PsiMethod>> iterator = location.getAncestors(PsiMethod.class, false);
        Location<PsiMethod> methodLocation = null;
        if (iterator.hasNext()) {
            methodLocation = iterator.next();
        }
        if (methodLocation == null) {
            return null;
        }
        PsiMethod method = methodLocation.getPsiElement();
        if (ConfigurationUtils.hasBenchmarkAnnotation(method)) {
            return method;
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
}
