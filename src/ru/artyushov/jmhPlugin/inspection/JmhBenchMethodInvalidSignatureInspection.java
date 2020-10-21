package ru.artyushov.jmhPlugin.inspection;

import com.intellij.codeInsight.intention.AddAnnotationFix;
import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UMethod;

import static com.intellij.codeInspection.ProblemHighlightType.ERROR;
import static com.intellij.psi.PsiModifier.PUBLIC;
import static com.intellij.psi.PsiType.VOID;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.JMH_ANNOTATION_STATE;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasBenchmarkAnnotation;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasSetupOrTearDownAnnotation;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasStateAnnotation;

public class JmhBenchMethodInvalidSignatureInspection extends AbstractBaseUastLocalInspectionTool {
    private static final LocalQuickFix BENCH_METHOD_QUICK_FIX = new BenchMethodSignatureFix();

    @Override
    public @Nullable ProblemDescriptor[] checkClass(@NotNull UClass aClass, @NotNull InspectionManager manager, boolean isOnTheFly) {
        boolean isBenchmarkClass = false;
        final UMethod[] methods = aClass.getMethods();
        for (final UMethod method : methods) {
            if (hasBenchmarkAnnotation(method)) {
                isBenchmarkClass = true;
                ProblemDescriptor[] problem = checkPublicModifier(method, manager, isOnTheFly);
                if (problem != null) return problem;
            } else if (hasSetupOrTearDownAnnotation(method)) {
                isBenchmarkClass = true;
                ProblemDescriptor[] problem = checkPublicModifier(method, manager, isOnTheFly);
                if (problem != null) return problem;
                problem = checkSetupTearDownIsNotVoid(method, manager, isOnTheFly);
                if (problem != null) return problem;
            }
        }

        if (isBenchmarkClass && !hasStateAnnotation(aClass)) {
            if (aClass.getFields().length > 0) {
                AddAnnotationFix addAnnotationFix = new AddAnnotationFix(JMH_ANNOTATION_STATE, aClass);
                ProblemDescriptor problem = manager.createProblemDescriptor(aClass, "@State missing", addAnnotationFix, ERROR, isOnTheFly);
                return new ProblemDescriptor[]{problem};
            }
        }
        return null;
    }

    @Nullable
    private ProblemDescriptor[] checkPublicModifier(@NotNull UMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (!method.hasModifierProperty(PUBLIC)) {
            ProblemDescriptor problem = manager.createProblemDescriptor(method, "@Benchmark method should be public", BENCH_METHOD_QUICK_FIX, ERROR, isOnTheFly);
            return new ProblemDescriptor[]{problem};
        }
        return null;
    }

    @Nullable
    private ProblemDescriptor[] checkSetupTearDownIsNotVoid(@NotNull UMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if ((method.getReturnType() == null || !method.getReturnType().equals(VOID))) {
            ProblemDescriptor problem = manager.createProblemDescriptor(method, "@Setup and @TearDown method should return void", BENCH_METHOD_QUICK_FIX, ERROR, isOnTheFly);
            return new ProblemDescriptor[]{problem};
        }
        return null;
    }
}
