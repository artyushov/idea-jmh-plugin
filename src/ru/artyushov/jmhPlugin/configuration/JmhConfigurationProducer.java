package ru.artyushov.jmhPlugin.configuration;

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

import java.util.Objects;

import static com.intellij.psi.util.PsiTreeUtil.findFirstParent;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.containsBenchmarkMethod;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.isBenchmarkMethod;
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
    protected boolean setupConfigurationFromContext(@NotNull JmhConfiguration configuration, ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        Location locationFromContext = context.getLocation();
        if (locationFromContext == null) {
            return false;
        }
        PsiElement benchmarkEntry = findBenchmarkEntry(locationFromContext.getPsiElement());

        final JmhConfiguration.Type runType;
        final PsiClass benchmarkClass;
        final PsiMethod benchmarkMethod;
        if (benchmarkEntry instanceof PsiClass) {
            runType = CLASS;
            benchmarkClass = (PsiClass) benchmarkEntry;
            benchmarkMethod = null;
        } else if (benchmarkEntry instanceof PsiMethod) {
            runType = METHOD;
            benchmarkMethod = (PsiMethod) benchmarkEntry;
            benchmarkClass = benchmarkMethod.getContainingClass();
        } else {
            return false;
        }
        configuration.setBenchmarkClass(benchmarkClass.getQualifiedName());

        sourceElement.set(benchmarkEntry);
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);
        String generatedParams = toRunParams(benchmarkEntry, true);
        configuration.setProgramParameters(createProgramParameters(generatedParams, configuration.getProgramParameters()));
        if (configuration.getWorkingDirectory() == null || configuration.getWorkingDirectory().isEmpty()) { // respect default working directory if set
            configuration.setWorkingDirectory(PathUtil.getLocalPath(context.getProject().getBaseDir()));
        }
        configuration.setName(getNameForConfiguration(benchmarkEntry));
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
    public boolean isConfigurationFromContext(@NotNull JmhConfiguration configuration, @NotNull ConfigurationContext context) {
        Location locationFromContext = context.getLocation();
        if (locationFromContext == null) {
            return false;
        }
        PsiElement benchmarkEntry = findBenchmarkEntry(locationFromContext.getPsiElement());
        PsiClass benchmarkClass;
        PsiMethod benchmarkMethod;
        if (benchmarkEntry instanceof PsiMethod) {
            benchmarkMethod = (PsiMethod) benchmarkEntry;
            benchmarkClass = benchmarkMethod.getContainingClass();
            // if the config is for a whole benchmark class then ignore the method
            if (configuration.getBenchmarkType() == CLASS) {
                benchmarkMethod = null;
            } else if (configuration.getBenchmarkType() != METHOD) {
                // unexpected BenchmarkType, must be METHOD
                return false;
            }
        } else if (benchmarkEntry instanceof PsiClass) {
            // if the config is for a specific method but we are on a class then the config can't be applied
            if (configuration.getBenchmarkType() == METHOD) {
                return false;
            } else if (configuration.getBenchmarkType() != CLASS) {
                // unexpected BenchmarkType, must be CLASS
                return false;
            }
            benchmarkClass = (PsiClass) benchmarkEntry;
            benchmarkMethod = null;
        } else {
            return false;
        }
        //TODO: this check may be skipped because we'll then check ProgramParameters, but it still faster to filter out
        if (!Objects.equals(benchmarkClass.getQualifiedName(), configuration.getBenchmarkClass())) {
            return false;
        }
        String generatedParams = toRunParams(benchmarkEntry, true);
        if (configuration.getProgramParameters() == null || configuration.getProgramParameters().isEmpty()
                || !configuration.getProgramParameters().startsWith(generatedParams)) {
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

    @Nullable
    private PsiElement findBenchmarkEntry(PsiElement locationElement) {
        // find a parent method or class
        PsiElement parent = findFirstParent(locationElement, elem -> elem instanceof PsiMethod || elem instanceof PsiClass);
        if (parent instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) parent;
            if (isBenchmarkMethod(method)) {
                return method;
            }
            // if this is not a benchmark method then check if this is a benchmark class
            parent = method.getContainingClass();
        }
        if (parent instanceof PsiClass) {
            PsiClass klass = (PsiClass) parent;
            if (containsBenchmarkMethod(klass)) {
                return klass;
            }
        }
        return null;
    }

    private String getNameForConfiguration(@NotNull PsiElement benchmarkEntry) {
        return toRunParams(benchmarkEntry, false);
    }

    @NotNull
    private String toRunParams(@NotNull PsiElement benchmarkEntry, boolean fqn) {
        if (benchmarkEntry instanceof PsiMethod) {
            PsiMethod benchmarkMethod = (PsiMethod) benchmarkEntry;
            PsiClass benchmarkClass = benchmarkMethod.getContainingClass();
            assert benchmarkClass != null;
            String benchmarkClassName = fqn ? benchmarkClass.getQualifiedName() : benchmarkClass.getName();
            return benchmarkClassName + '.' + benchmarkMethod.getName();
        } else if (benchmarkEntry instanceof PsiClass) {
            PsiClass benchmarkClass = (PsiClass) benchmarkEntry;
            String benchmarkClassName = fqn ? benchmarkClass.getQualifiedName() : benchmarkClass.getName();
            return benchmarkClassName + ".*";
        } else {
            return "";
        }
    }

    private String createProgramParameters(String generatedParams, String defaultParams) {
        return defaultParams != null && !defaultParams.isEmpty() ? generatedParams + ' ' + defaultParams : generatedParams;
    }
}
