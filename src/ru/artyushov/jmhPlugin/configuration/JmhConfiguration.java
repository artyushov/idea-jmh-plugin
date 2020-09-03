package ru.artyushov.jmhPlugin.configuration;

import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.*;
import com.intellij.execution.configuration.CompatibilityAwareRunProfile;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: nikart
 * Date: 09/04/14
 * Time: 18:46
 */
public class JmhConfiguration extends ModuleBasedConfiguration<JavaRunConfigurationModule, Object>
        implements CommonJavaRunConfigurationParameters, CompatibilityAwareRunProfile {

    public static final String ATTR_VM_PARAMETERS = "vm-parameters";
    public static final String ATTR_PROGRAM_PARAMETERS = "program-parameters";
    public static final String ATTR_WORKING_DIR = "working-dir";
    public static final String ATTR_BENCHMARK_TYPE = "benchmark-type";
    public static final String ATTR_BENCHMARK_CLASS = "benchmark-class";

    public enum Type {
        METHOD, CLASS
    }

    public static final String JMH_START_CLASS = "org.openjdk.jmh.Main";
    public static final String JMH_ANNOTATION_NAME = "org.openjdk.jmh.annotations.Benchmark";

    private String vmParameters;
    private boolean isAlternativeJrePathEnabled = false;
    private String alternativeJrePath;
    private String programParameters;
    private String workingDirectory;
    private Map<String, String> envs = new HashMap<String, String>();
    private boolean passParentEnvs;
    private String benchmarkClass;

    private Type type;

    public JmhConfiguration(final String name, final Project project, ConfigurationFactory configurationFactory) {
        this(name, new JavaRunConfigurationModule(project, false), configurationFactory);
    }

    public JmhConfiguration(String name, JavaRunConfigurationModule configurationModule, ConfigurationFactory factory) {
        super(name, configurationModule, factory);
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
    public void setAlternativeJrePathEnabled(boolean b) {
        this.isAlternativeJrePathEnabled = b;
    }

    @Override
    public String getAlternativeJrePath() {
        return alternativeJrePath;
    }

    @Override
    public void setAlternativeJrePath(String s) {
        this.alternativeJrePath = s;
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
        envs = new HashMap<String, String>(map);
    }

    @NotNull
    @Override
    public Map<String, String> getEnvs() {
        return new HashMap<String, String>(envs);
    }

    @Override
    public void setPassParentEnvs(boolean b) {
        this.passParentEnvs = b;
    }

    @Override
    public boolean isPassParentEnvs() {
        return passParentEnvs;
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
    public Collection<Module> getValidModules() {
        return JavaRunConfigurationModule.getModulesForClass(getProject(), getRunClass());
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<JmhConfiguration> group = new SettingsEditorGroup<JmhConfiguration>();
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), new JmhConfigurable());
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<JmhConfiguration>());
        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new BenchmarkState(getProject(), this, executionEnvironment);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
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
        writeModule(element);
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        setVMParameters(element.getAttributeValue(ATTR_VM_PARAMETERS));
        setProgramParameters(element.getAttributeValue(ATTR_PROGRAM_PARAMETERS));
        setWorkingDirectory(element.getAttributeValue(ATTR_WORKING_DIR));
        setBenchmarkClass(element.getAttributeValue(ATTR_BENCHMARK_CLASS));
        String typeString = element.getAttributeValue(ATTR_BENCHMARK_TYPE);
        if (typeString != null) {
            setType(Type.valueOf(typeString));
        }
        readModule(element);
    }

    @Override
    public boolean mustBeStoppedToRun(@NotNull RunConfiguration configuration) {
        return JmhConfigurationType.TYPE_ID.equals(configuration.getType().getId());
    }
}
