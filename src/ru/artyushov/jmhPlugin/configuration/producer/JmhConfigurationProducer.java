package ru.artyushov.jmhPlugin.configuration.producer;

import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.artyushov.jmhPlugin.configuration.JmhConfiguration;
import ru.artyushov.jmhPlugin.configuration.JmhConfigurationType;

import java.util.Iterator;

import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasBenchmarkAnnotation;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasBenchmarks;
import static ru.artyushov.jmhPlugin.configuration.JmhConfiguration.Type.CLASS;
import static ru.artyushov.jmhPlugin.configuration.JmhConfiguration.Type.METHOD;

/**
 * Supports creating run configurations from context (by right-clicking a code element in the source editor or the project view).
 */
public class JmhConfigurationProducer extends JavaRunConfigurationProducerBase<JmhConfiguration> implements Cloneable {

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
    protected boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context, Ref<PsiElement> sourceElement) {
        Location locationFromContext = context.getLocation();
        if (locationFromContext == null) {
            return false;
        }
        JmhConfiguration.Type runType;
        PsiClass benchmarkClass;
        PsiMethod method = getAnnotatedMethod(locationFromContext);
        if (method == null) {
            benchmarkClass = getBenchmarkClass(locationFromContext);
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
        String generatedParams = toRunParams(benchmarkClass, method);
        configuration.setProgramParameters(createProgramParameters(generatedParams, configuration.getProgramParameters()));
        if (configuration.getWorkingDirectory() == null) { // respect default working directory if set
            configuration.setWorkingDirectory(PathUtil.getLocalPath(context.getProject().getBaseDir()));
        }
        configuration.setName(getNameForConfiguration(benchmarkClass, method));
        configuration.setType(runType);
        return true;
    }

    /**
     * Checks if the specified configuration was created from the specified context.
     *
     * @param configuration a configuration instance.
     * @param context       contains the information about a location in the source code.
     * @return true if this configuration was created from the specified context, false otherwise.
     */
    @Override
    public boolean isConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context) {
        Location locationFromContext = context.getLocation();
        if (locationFromContext == null) {
            return false;
        }
        PsiClass benchmarkClass;
        PsiMethod method;
        if (configuration.getBenchmarkType() == METHOD) {
            method = getAnnotatedMethod(locationFromContext);
            if (method == null) {
                return false;
            }
            benchmarkClass = method.getContainingClass();
        } else if (configuration.getBenchmarkType() == CLASS) {
            benchmarkClass = getBenchmarkClass(locationFromContext);
            method = null;
        } else {
            return false;
        }
        if (benchmarkClass == null || benchmarkClass.getQualifiedName() == null
                || !benchmarkClass.getQualifiedName().equals(configuration.getBenchmarkClass())) {
            return false;
        }
        String configurationName = getNameForConfiguration(benchmarkClass, method);
        if (!configuration.getName().equals(configurationName)) {
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

    public static PsiMethod getAnnotatedMethod(Location<?> location) {
        Iterator<Location<PsiMethod>> iterator = location.getAncestors(PsiMethod.class, false);
        Location<PsiMethod> methodLocation = null;
        if (iterator.hasNext()) {
            methodLocation = iterator.next();
        }
        if (methodLocation == null) {
            return null;
        }
        PsiMethod method = methodLocation.getPsiElement();
        if (hasBenchmarkAnnotation(method)) {
            return method;
        }
        return null;
    }

    private PsiClass getBenchmarkClass(Location<?> location) {
        for (Iterator<Location<PsiClass>> iterator = location.getAncestors(PsiClass.class, false); iterator.hasNext(); ) {
            final Location<PsiClass> classLocation = iterator.next();
            if (hasBenchmarks(classLocation.getPsiElement())) {
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

    String createProgramParameters(String generatedParams, String defaultParams) {
        return defaultParams != null ? generatedParams + ' ' + defaultParams : generatedParams;
    }
}
