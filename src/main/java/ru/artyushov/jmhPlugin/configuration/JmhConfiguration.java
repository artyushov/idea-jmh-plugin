package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configuration.CompatibilityAwareRunProfile;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.execution.util.ProgramParametersUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementAdapter;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.isBenchmarkEntryElement;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.toRunParams;

/**
 * User: nikart
 * Date: 09/04/14
 * Time: 18:46
 */
public class JmhConfiguration extends JavaRunConfigurationBase
        implements CommonJavaRunConfigurationParameters, CompatibilityAwareRunProfile, RefactoringListenerProvider {

    public static final String ATTR_VM_PARAMETERS = "vm-parameters";
    public static final String ATTR_PROGRAM_PARAMETERS = "program-parameters";
    public static final String ATTR_WORKING_DIR = "working-dir";
    public static final String ATTR_BENCHMARK_TYPE = "benchmark-type";
    public static final String ATTR_BENCHMARK_CLASS = "benchmark-class";
    public static final String USE_CLASS_PATH_ONLY = "useClassPathOnly";
    public static final String ATTR_ALTERNATIVE_JRE_PATH = "alternativeJrePath";
    public static final String ATTR_IS_ALTERNATIVE_JRE_PATH_ENABLED = "isAlternativeJrePathEnabled";

    public enum Type {
        METHOD, CLASS
    }

    public static final String JMH_START_CLASS = "org.openjdk.jmh.Main";

    private String vmParameters;
    private boolean isAlternativeJrePathEnabled = false;
    private String alternativeJrePath;
    private String programParameters;
    private String workingDirectory;
    private Map<String, String> envs = new HashMap<>(0, 1.0f);
    private boolean passParentEnvs;
    private boolean myUseModulePath = true;
    private ShortenCommandLine myShortenCommandLine;
    private String benchmarkClass;

    private Type type;

    public JmhConfiguration(String name, @NotNull JavaRunConfigurationModule configurationModule, @NotNull ConfigurationFactory factory) {
        super(name, configurationModule, factory);
    }

    public JmhConfiguration(@NotNull JavaRunConfigurationModule configurationModule, @NotNull ConfigurationFactory factory) {
        super(configurationModule, factory);
    }

    @Override
    public void setVMParameters(String s) {
        this.vmParameters = s;
    }

    @Override
    public String getVMParameters() {
        return vmParameters;
    }

    @Override
    public boolean isAlternativeJrePathEnabled() {
        return isAlternativeJrePathEnabled;
    }

    @Override
    public void setAlternativeJrePathEnabled(boolean enabled) {
        boolean changed = isAlternativeJrePathEnabled != enabled;
        isAlternativeJrePathEnabled = enabled;
        ApplicationConfiguration.onAlternativeJreChanged(changed, getProject());
    }

    @Override
    public String getAlternativeJrePath() {
        return alternativeJrePath != null ? new AlternativeJrePathConverter().fromString(alternativeJrePath) : null;
    }

    @Override
    public void setAlternativeJrePath(String path) {
        String collapsedPath = path != null ? new AlternativeJrePathConverter().toString(path) : null;
        boolean changed = !Objects.equals(alternativeJrePath, collapsedPath);
        alternativeJrePath = collapsedPath;
        ApplicationConfiguration.onAlternativeJreChanged(changed, getProject());
    }

    @Nullable
    @Override
    public String getRunClass() {
        return JMH_START_CLASS;
    }

    @Nullable
    @Override
    public String getPackage() {
        return null;
    }

    @Override
    public void setProgramParameters(@Nullable String s) {
        this.programParameters = s;
    }

    @Nullable
    @Override
    public String getProgramParameters() {
        return programParameters;
    }

    @Override
    public void setWorkingDirectory(@Nullable String s) {
        this.workingDirectory = s;
    }

    @Nullable
    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public void setEnvs(@NotNull Map<String, String> map) {
        envs = new HashMap<>(map);
    }

    @NotNull
    @Override
    public Map<String, String> getEnvs() {
        return new HashMap<>(envs);
    }

    @Override
    public void setPassParentEnvs(boolean b) {
        this.passParentEnvs = b;
    }

    @Override
    public boolean isPassParentEnvs() {
        return passParentEnvs;
    }

    public boolean isUseModulePath() {
        return myUseModulePath;
    }

    public void setUseModulePath(boolean useModulePath) {
        myUseModulePath = useModulePath;
    }

    @Nullable
    @Override
    public ShortenCommandLine getShortenCommandLine() {
        return myShortenCommandLine;
    }

    @Override
    public void setShortenCommandLine(@Nullable ShortenCommandLine shortenCommandLine) {
        myShortenCommandLine = shortenCommandLine;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getBenchmarkType() {
        return type;
    }

    public void setBenchmarkClass(String benchmarkClass) {
        this.benchmarkClass = benchmarkClass;
    }

    public String getBenchmarkClass() {
        return benchmarkClass;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException{
        JavaParametersUtil.checkAlternativeJRE(this);
        ProgramParametersUtil.checkWorkingDirectoryExist(this, getProject(), getConfigurationModule().getModule());
    }

    @Override
    public Collection<Module> getValidModules() {
        try {
            checkConfiguration();
        } catch (RuntimeConfigurationError e) {
            return Arrays.asList(ModuleManager.getInstance(getProject()).getModules());
        } catch (RuntimeConfigurationException ignored) {
        }
        return JavaRunConfigurationModule.getModulesForClass(getProject(), getRunClass());
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        if (Registry.is("ide.new.run.config", true)) {
            return new JmhSettingsEditor(this);
        }
        SettingsEditorGroup<JmhConfiguration> group = new SettingsEditorGroup<JmhConfiguration>();
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), new JmhConfigurable());
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new JmhBenchmarkCommandLineState(executionEnvironment, this);
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        if (vmParameters != null) {
            element.setAttribute(ATTR_VM_PARAMETERS, vmParameters);
        }
        if (programParameters != null) {
            element.setAttribute(ATTR_PROGRAM_PARAMETERS, programParameters);
        }
        if (workingDirectory != null) {
            element.setAttribute(ATTR_WORKING_DIR, workingDirectory);
        }
        if (type != null) {
            element.setAttribute(ATTR_BENCHMARK_TYPE, type.name());
        }
        if (benchmarkClass != null) {
            element.setAttribute(ATTR_BENCHMARK_CLASS, benchmarkClass);
        }
        element.setAttribute(ATTR_IS_ALTERNATIVE_JRE_PATH_ENABLED, String.valueOf(isAlternativeJrePathEnabled));
        if (alternativeJrePath != null) {
            element.setAttribute(ATTR_ALTERNATIVE_JRE_PATH, alternativeJrePath);
        }
        if (!myUseModulePath) {
            element.addContent(new Element(USE_CLASS_PATH_ONLY));
        }
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        setVMParameters(element.getAttributeValue(ATTR_VM_PARAMETERS));
        setProgramParameters(element.getAttributeValue(ATTR_PROGRAM_PARAMETERS));
        setWorkingDirectory(element.getAttributeValue(ATTR_WORKING_DIR));
        setBenchmarkClass(element.getAttributeValue(ATTR_BENCHMARK_CLASS));
        String typeString = element.getAttributeValue(ATTR_BENCHMARK_TYPE);
        if (typeString != null) {
            setType(Type.valueOf(typeString));
        }
        setAlternativeJrePathEnabled(Boolean.parseBoolean(element.getAttributeValue(ATTR_IS_ALTERNATIVE_JRE_PATH_ENABLED)));
        setAlternativeJrePath(element.getAttributeValue(ATTR_ALTERNATIVE_JRE_PATH));
        readModule(element);
        myUseModulePath = element.getChild(USE_CLASS_PATH_ONLY) == null;
//        EnvironmentVariablesComponent.readExternal(element, getPersistentData().getEnvs());

    }

    @Override
    public boolean mustBeStoppedToRun(@NotNull RunConfiguration configuration) {
        return JmhConfigurationType.TYPE_ID.equals(configuration.getType().getId());
    }


    @Nullable
    @Override
    public RefactoringElementListener getRefactoringElementListener(PsiElement element) {
        if (StringUtil.isEmpty(getProgramParameters())) {
            return null;
        }
        if (!isBenchmarkEntryElement(element)) {
            return null;
        }
        if (!getProgramParameters().startsWith(toRunParams(element, true))) {
            return null;
        }
        return new RefactoringElementAdapter() {
            @Override
            protected void elementRenamedOrMoved(@NotNull PsiElement newElement) {
                // replace benchmark class name at beginning of program params
                String newRunParams = toRunParams(newElement, true);
                int firstSpace = getProgramParameters().indexOf(' ');
                if (firstSpace == -1) {
                    setProgramParameters(newRunParams);
                } else {
                    setProgramParameters(newRunParams + getProgramParameters().substring(firstSpace));
                }
                //TODO smart update name to change only bench name. But we can't do this because we don't know old class name
                setName(toRunParams(newElement, false));
                if (newElement instanceof PsiClass) {
                    setBenchmarkClass(((PsiClass)newElement).getQualifiedName());
                }
            }

            @Override
            public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
                elementRenamedOrMoved(newElement);
            }
        };
    }

}
