package ru.artyushov.jmhPlugin.configuration.producer;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.artyushov.jmhPlugin.configuration.ConfigurationUtils;
import ru.artyushov.jmhPlugin.configuration.JmhConfiguration;

import java.util.Iterator;

import static ru.artyushov.jmhPlugin.configuration.JmhConfiguration.Type.CLASS;
import static ru.artyushov.jmhPlugin.configuration.JmhConfiguration.Type.METHOD;

/**
 * User: nikart
 * Date: 15/07/14
 * Time: 23:30
 */
public class JmhClassConfigurationProducer extends JmhConfigurationProducer {

    @Override
    protected boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        JmhConfiguration.Type runType;
        PsiClass benchmarkClass;
        PsiMethod method = ConfigurationUtils.getAnnotatedMethod(context);
        if (method == null) {
            benchmarkClass = getBenchmarkClass(context);
            runType = CLASS;
        } else {
            benchmarkClass = method.getContainingClass();
            runType = METHOD;
        }
        if (benchmarkClass == null) {
            return false;
        }
        configuration.setBenchmarkClass(benchmarkClass.getQualifiedName());

        sourceElement.set(benchmarkClass);
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);
        configuration.setProgramParameters(
                createProgramParameters(toRunParams(benchmarkClass, method), configuration.getProgramParameters()));
        configuration.setWorkingDirectory(PathUtil.getLocalPath(context.getProject().getBaseDir()));
        configuration.setName(getNameForConfiguration(benchmarkClass, method));
        configuration.setType(runType);
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context) {
        PsiClass benchmarkClass;
        PsiMethod method;
        if (configuration.getBenchmarkType() == METHOD) {
            method = ConfigurationUtils.getAnnotatedMethod(context);
            if (method == null) {
                return false;
            }
            benchmarkClass = method.getContainingClass();
        } else if (configuration.getBenchmarkType() == CLASS) {
            benchmarkClass = getBenchmarkClass(context);
            method = null;
        } else {
            return false;
        }
        if (benchmarkClass == null) {
            return false;
        }
        if (benchmarkClass.getQualifiedName() == null
                || !benchmarkClass.getQualifiedName().equals(configuration.getBenchmarkClass())) {
            return false;
        }
        String configurationName = getNameForConfiguration(benchmarkClass, method);
        return isConfigurationFromContext(configuration, context, configurationName);
    }

    private PsiClass getBenchmarkClass(ConfigurationContext context) {
        Location<?> location = context.getLocation();
        if (location == null) {
            return null;
        }
        for (Iterator<Location<PsiClass>> iterator = location.getAncestors(PsiClass.class, false); iterator.hasNext(); ) {
            final Location<PsiClass> classLocation = iterator.next();
            if (ConfigurationUtils.hasBenchmarks(classLocation.getPsiElement())) {
                return classLocation.getPsiElement();
            }
        }
        return null;
    }

    private String getNameForConfiguration(@NotNull PsiClass benchmarkClass, @Nullable PsiMethod method) {
        if (method == null) {
            return benchmarkClass.getName();
        }
        return benchmarkClass.getName() + '.' + method.getName();
    }

    private String toRunParams(@NotNull PsiClass benchmarkClass, @Nullable PsiMethod method) {
        if (method == null) {
            return benchmarkClass.getQualifiedName() + ".*";
        }
        return benchmarkClass.getQualifiedName() + '.' + method.getName();
    }
}
