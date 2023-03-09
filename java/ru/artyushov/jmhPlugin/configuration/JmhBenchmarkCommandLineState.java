package ru.artyushov.jmhPlugin.configuration;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.application.BaseJavaApplicationCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.compiler.ProcessorConfigProfile;

import static com.intellij.execution.configurations.JavaParameters.CLASSES_AND_TESTS;
import static com.intellij.execution.configurations.JavaParameters.CLASSES_ONLY;
import static com.intellij.execution.configurations.JavaParameters.JDK_AND_CLASSES;
import static com.intellij.execution.configurations.JavaParameters.JDK_AND_CLASSES_AND_TESTS;
import static com.intellij.openapi.ui.Messages.showOkCancelDialog;

/**
 * User: nikart
 * Date: 14/07/14
 * Time: 21:36
 */
public class JmhBenchmarkCommandLineState extends BaseJavaApplicationCommandLineState<JmhConfiguration> {

    public JmhBenchmarkCommandLineState(final ExecutionEnvironment environment, @NotNull final JmhConfiguration configuration) {
        super(environment, configuration);
        automaticallyEnableAnnotationProcessor(configuration);
    }

    private void automaticallyEnableAnnotationProcessor(JmhConfiguration configuration) {
        Module module = configuration.getConfigurationModule().getModule();
        if (module != null) {
            CompilerConfigurationImpl compilerConfiguration =
                    (CompilerConfigurationImpl) CompilerConfiguration.getInstance(module.getProject());
            ProcessorConfigProfile processorConfigProfile = compilerConfiguration.getAnnotationProcessingConfiguration(module);
            if (!processorConfigProfile.isEnabled()) {
                if (showOkCancelDialog(configuration.getProject(),
                        "Annotation processing is not enabled but JMH needs to preprocess classes bytecode",
                        "JMH benchmark should be processed by its Annotation Processor",
                        "Enable Annotation processing and rebuild", "Skip",
                        Messages.getQuestionIcon()) != Messages.OK) {
                    return;
                }
                processorConfigProfile.setEnabled(true);
                // refresh compilerConfiguration
                compilerConfiguration.getState();
                rebuild(module);
            }
        }
    }

    private void rebuild(Module module) {
        @Nullable CompileStatusNotification notification = (aborted, errors, warnings, compileContext) -> {};
        CompilerManager.getInstance(module.getProject()).rebuild(notification);
    }

    @Override
    protected JavaParameters createJavaParameters() throws ExecutionException {
        JavaParameters parameters = new JavaParameters();
        JavaParametersUtil.configureConfiguration(parameters, myConfiguration);

        parameters.setMainClass(JmhConfiguration.JMH_START_CLASS);

        int classPathType = removeJdkClasspath(JavaParametersUtil.getClasspathType(myConfiguration.getConfigurationModule(),
                myConfiguration.getBenchmarkClass(), true));
        JavaParametersUtil.configureModule(myConfiguration.getConfigurationModule(), parameters, classPathType, null);

        Module module = myConfiguration.getConfigurationModule().getModule();
        if (parameters.getJdk() == null){
            Sdk jdk = module != null
                    ? ModuleRootManager.getInstance(module).getSdk()
                    : ProjectRootManager.getInstance(myConfiguration.getProject()).getProjectSdk();
            parameters.setJdk(jdk);
        }
        return parameters;
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
