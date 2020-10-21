package ru.artyushov.jmhPlugin.inspection;

import com.intellij.codeInsight.intention.AddAnnotationFix;
import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UMethod;

import java.util.Objects;

import static com.intellij.codeInspection.ProblemHighlightType.ERROR;
import static com.intellij.psi.PsiModifier.ABSTRACT;
import static com.intellij.psi.PsiModifier.PUBLIC;
import static com.intellij.psi.PsiType.VOID;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.JMH_ANNOTATION_STATE;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasBenchmarkAnnotation;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasSetupOrTearDownAnnotation;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasStateAnnotation;

public class JmhBenchMethodInvalidSignatureInspection extends AbstractBaseUastLocalInspectionTool {
    private static final LocalQuickFix BENCH_METHOD_QUICK_FIX = new BenchMethodSignatureFix();

    @Override
    public @Nullable ProblemDescriptor[] checkClass(@NotNull UClass klass, @NotNull InspectionManager manager, boolean isOnTheFly) {
        boolean isBenchmarkClass = false;
        UMethod[] methods = klass.getMethods();
        for (UMethod method : methods) {
            boolean hasBenchmarkAnnotation = hasBenchmarkAnnotation(method);
            boolean hasSetupOrTearDownAnnotation = false;
            if (!hasBenchmarkAnnotation) {
                hasSetupOrTearDownAnnotation = hasSetupOrTearDownAnnotation(method);
                if (hasSetupOrTearDownAnnotation) {
                    // Check that Setup or TearDown is void
                    if ((method.getReturnType() == null || !method.getReturnType().equals(VOID))) {
                        ProblemDescriptor problem = manager.createProblemDescriptor(method, "@Setup or @TearDown method should not return anything", BENCH_METHOD_QUICK_FIX, ERROR, isOnTheFly);
                        return new ProblemDescriptor[]{problem};
                    }
                }
            }
            if (hasBenchmarkAnnotation || hasSetupOrTearDownAnnotation) {
                isBenchmarkClass = true;
                if (!method.hasModifierProperty(PUBLIC)) {
                    ProblemDescriptor problem = manager.createProblemDescriptor(method, "@Benchmark method should be public", BENCH_METHOD_QUICK_FIX, ERROR, isOnTheFly);
                    return new ProblemDescriptor[]{problem};
                }
            }
        }

        if (!isBenchmarkClass) {
            return null;
        }
        boolean explicitState = hasStateAnnotation(klass);
        // validate against rogue fields
        if (!explicitState || klass.hasModifierProperty(ABSTRACT)) {
            for (UField field : klass.getFields()) {
                // allow static fields
                if (field.isStatic()) continue;
                AddAnnotationFix addAnnotationFix = new AddAnnotationFix(JMH_ANNOTATION_STATE, klass);
                ProblemDescriptor problem = manager.createProblemDescriptor(field, "Field is declared within the class not having @State annotation", addAnnotationFix, ERROR, isOnTheFly);
                return new ProblemDescriptor[]{problem};
            }
        }

        // if this is a default package
        if (Objects.equals(klass.getName(), klass.getQualifiedName())) {
//            QuickFixFactory.getInstance().createCreateClassOrPackageFix(aClass, "@State missing", )
            ProblemDescriptor problem = manager.createProblemDescriptor(klass, "Benchmark class should have package other than default", (LocalQuickFix) null, ERROR, isOnTheFly);
            return new ProblemDescriptor[]{problem};
        }

        if (klass.isFinal()) {
            ProblemDescriptor problem = manager.createProblemDescriptor(klass, "Benchmark classes should not be final", (LocalQuickFix) null, ERROR, isOnTheFly);
            return new ProblemDescriptor[]{problem};
        }

        return null;
    }

}
