package ru.artyushov.jmhPlugin.inspection;

import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UMethod;

import static com.intellij.codeInspection.ProblemHighlightType.ERROR;
import static com.intellij.psi.PsiModifier.PUBLIC;
import static com.intellij.psi.PsiType.VOID;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasBenchmarkAnnotation;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasSetupOrTearDownAnnotation;

public class JmhBenchMethodInvalidSignatureInspection extends AbstractBaseUastLocalInspectionTool {
    private static final LocalQuickFix BENCH_METHOD_QUICK_FIX = new BenchMethodSignatureFix();

    @Override
    public @Nullable
    ProblemDescriptor[] checkMethod(@NotNull UMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (!method.isPhysical()) {
            return null;
        }
        boolean isSetupOrTearDown = false;
        if (!(hasBenchmarkAnnotation(method) || (isSetupOrTearDown = hasSetupOrTearDownAnnotation(method)))) {
            return null;
        }
        if (!method.hasModifierProperty(PUBLIC) || (isSetupOrTearDown && (method.getReturnType() == null || !method.getReturnType().equals(VOID)))) {
            String message = isSetupOrTearDown ? "@Setup and @TearDown method should be public and void" : "@Benchmark method should be public";
            ProblemDescriptor problem = manager.createProblemDescriptor(method, message, BENCH_METHOD_QUICK_FIX, ERROR, isOnTheFly);
            return new ProblemDescriptor[]{problem};
        }
        return null;
    }
}
