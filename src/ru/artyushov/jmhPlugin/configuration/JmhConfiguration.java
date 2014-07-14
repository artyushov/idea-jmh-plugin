package ru.artyushov.jmhPlugin.configuration;

import com.google.common.collect.Maps;
import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.*;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * User: nikart
 * Date: 09/04/14
 * Time: 18:46
 */
public class JmhConfiguration extends ModuleBasedConfiguration<JavaRunConfigurationModule>
        implements CommonJavaRunConfigurationParameters {

    public static final String JMH_START_CLASS = "org.openjdk.jmh.Main";

    private String vmParameters;
    private boolean isAlternaticeJrePathEnabled = false;
    private String alternativeJrePath;
    private String programParameters;
    private String workingDirectory;
    private Map<String, String> envs = Maps.newHashMap();
    private boolean passParentEnvs;

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
        return isAlternaticeJrePathEnabled;
    }

    @Override
    public void setAlternativeJrePathEnabled(boolean b) {
        this.isAlternaticeJrePathEnabled = b;
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
        return "org.openjdk.jmh.Main";
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
        envs = Maps.newHashMap(map);
    }

    @NotNull
    @Override
    public Map<String, String> getEnvs() {
        return Maps.newHashMap(envs);
    }

    @Override
    public void setPassParentEnvs(boolean b) {
        this.passParentEnvs = b;
    }

    @Override
    public boolean isPassParentEnvs() {
        return passParentEnvs;
    }

    @Override
    public Collection<Module> getValidModules() {
        return JavaRunConfigurationModule.getModulesForClass(getProject(), getRunClass());
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<JmhConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), new JmhConfigurable());
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<JmhConfiguration>());
        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new BenchmarkMethod(getProject(), this, executionEnvironment);
    }
}
