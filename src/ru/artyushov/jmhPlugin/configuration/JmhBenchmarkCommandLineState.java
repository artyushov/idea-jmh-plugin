package ru.artyushov.jmhPlugin.configuration;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.compiler.ProcessorConfigProfile;

import static com.intellij.execution.configurations.JavaParameters.CLASSES_AND_TESTS;
import static com.intellij.execution.configurations.JavaParameters.CLASSES_ONLY;
import static com.intellij.execution.configurations.JavaParameters.JDK_AND_CLASSES;
import static com.intellij.execution.configurations.JavaParameters.JDK_AND_CLASSES_AND_TESTS;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;

/**
 * User: nikart
 * Date: 14/07/14
 * Time: 21:36
 */
public class JmhBenchmarkCommandLineState extends CommandLineState {

    private final Project project;
    private final JmhConfiguration configuration;

    public JmhBenchmarkCommandLineState(Project project, JmhConfiguration configuration, ExecutionEnvironment environment) {
        super(environment);
        this.project = project;
        this.configuration = configuration;
        automaticallyEnableAnnotationProcessor(configuration);
    }

    private void automaticallyEnableAnnotationProcessor(JmhConfiguration configuration) {
        Module module = configuration.getConfigurationModule().getModule();
        if (module != null) {
            CompilerConfigurationImpl compilerConfiguration =
                    (CompilerConfigurationImpl) CompilerConfiguration.getInstance(module.getProject());
            ProcessorConfigProfile processorConfigProfile = compilerConfiguration.getAnnotationProcessingConfiguration(module);
            if (!processorConfigProfile.isEnabled()) {
                processorConfigProfile.setEnabled(true);
                // refresh compilerConfiguration
                compilerConfiguration.getState();
            }
        }
    }

    protected JavaParameters createJavaParameters() throws ExecutionException {
        JavaParameters parameters = new JavaParameters();
        JavaParametersUtil.configureConfiguration(parameters, configuration);

        parameters.setMainClass(JmhConfiguration.JMH_START_CLASS);

        int classPathType = removeJdkClasspath(JavaParametersUtil.getClasspathType(configuration.getConfigurationModule(),
                configuration.getBenchmarkClass(), true));
        JavaParametersUtil.configureModule(configuration.getConfigurationModule(), parameters, classPathType, null);

        Module module = configuration.getConfigurationModule().getModule();
        if (parameters.getJdk() == null){
            parameters.setJdk(module != null
                    ? ModuleRootManager.getInstance(module).getSdk()
                    : ProjectRootManager.getInstance(project).getProjectSdk());
        }
        return parameters;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        return JavaCommandLineStateUtil.startProcess(createCommandLine(), false);
    }

    private GeneralCommandLine createCommandLine() throws ExecutionException {
        return CommandLineBuilder.createFromJavaParameters(createJavaParameters(), PROJECT
                .getData(DataManager.getInstance().getDataContext()), true);
    }


    private int removeJdkClasspath(int classpathType) {
        switch (classpathType) {
            case JDK_AND_CLASSES:
                return CLASSES_ONLY;
            case JDK_AND_CLASSES_AND_TESTS:
                return CLASSES_AND_TESTS;
            default:
                return classpathType;
        }
    }
}
