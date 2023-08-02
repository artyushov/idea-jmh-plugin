package ru.artyushov.jmhPlugin.inspection;

import com.intellij.codeInsight.intention.AddAnnotationFix;
import com.intellij.codeInsight.intention.QuickFixFactory;
import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UMethod;

import java.util.Objects;

import static com.intellij.codeInspection.ProblemHighlightType.ERROR;
import static com.intellij.psi.PsiModifier.ABSTRACT;
import static com.intellij.psi.PsiModifier.FINAL;
import static com.intellij.psi.PsiModifier.PUBLIC;
import static com.intellij.psi.PsiModifier.STATIC;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.JMH_ANNOTATION_STATE;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasBenchmarkAnnotation;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasSetupOrTearDownAnnotation;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasStateAnnotation;

public class JmhInspections extends AbstractBaseUastLocalInspectionTool {
    private static com.intellij.psi.PsiType VOID = PsiTypes.voidType();

    /**
     * For performance reasons all checks are executed in one method
     */
    @Override
    public @Nullable
    ProblemDescriptor[] checkClass(@NotNull UClass klass, @NotNull InspectionManager manager, boolean isOnTheFly) {
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
                        LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createMethodReturnFix(method, VOID, false);
                        ProblemDescriptor problem = manager.createProblemDescriptor(method.getIdentifyingElement(), "@Setup or @TearDown method should not return anything", fix, ERROR, isOnTheFly);
                        return new ProblemDescriptor[]{problem};
                    }
                }
            }
            if (hasBenchmarkAnnotation || hasSetupOrTearDownAnnotation) {
                isBenchmarkClass = true;
                if (!method.hasModifierProperty(PUBLIC)) {
                    LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createModifierListFix(method, PUBLIC, true, false);
                    ProblemDescriptor problem = manager.createProblemDescriptor(method.getIdentifyingElement(), "@Benchmark method should be public", fix, ERROR, isOnTheFly);
                    return new ProblemDescriptor[]{problem};
                }
                if (method.hasModifierProperty(ABSTRACT)) {
                    LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createModifierListFix(method, ABSTRACT, false, false);
                    ProblemDescriptor problem = manager.createProblemDescriptor(method.getIdentifyingElement(), "@Benchmark method can not be abstract", fix, ERROR, isOnTheFly);
                    return new ProblemDescriptor[]{problem};
                }
            }
        }

        boolean explicitState = hasStateAnnotation(klass);
        // validate if enclosing class is implicit @State
        if (explicitState) {
            ProblemDescriptor problem = validateState(klass, manager, isOnTheFly);
            if (problem != null) {
                return new ProblemDescriptor[]{problem};
            }
        }

        if (!isBenchmarkClass) {
            return null;
        }
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
            //TODO QuickFixFactory.getInstance().createCreateClassOrPackageFix(aClass, "Create package", )
            ProblemDescriptor problem = manager.createProblemDescriptor(klass, "Benchmark class should have package other than default", (LocalQuickFix) null, ERROR, isOnTheFly);
            return new ProblemDescriptor[]{problem};
        }

        if (klass.isFinal()) {
            LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createModifierListFix(klass, FINAL, false, true);
            ProblemDescriptor problem = manager.createProblemDescriptor(klass, "Benchmark classes should not be final", fix, ERROR, isOnTheFly);
            return new ProblemDescriptor[]{problem};
        }

        return null;
    }

    private ProblemDescriptor validateState(UClass stateClass, InspectionManager manager, boolean isOnTheFly) {
        if (!stateClass.hasModifierProperty(PUBLIC)) {
            LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createModifierListFix(stateClass, PUBLIC, true, true);
            return manager.createProblemDescriptor(stateClass, "The instantiated @State annotation only supports public classes", fix, ERROR, isOnTheFly);
        }
        if (stateClass.isFinal()) {
            LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createModifierListFix(stateClass, FINAL, false, true);
            return manager.createProblemDescriptor(stateClass, "The instantiated @State annotation does not support final classes", fix, ERROR, isOnTheFly);
        }
        // is inner class
        if (stateClass.getContainingClass() != null && !stateClass.isStatic()) {
            LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createModifierListFix(stateClass, STATIC, true, true);
            return manager.createProblemDescriptor(stateClass, "The instantiated @State annotation does not support inner classes, make sure your class is static", fix, ERROR, isOnTheFly);
        }
        if (stateClass.hasModifierProperty(ABSTRACT)) {
            LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createModifierListFix(stateClass, ABSTRACT, false, true);
            return manager.createProblemDescriptor(stateClass, "The instantiated @State class cannot be abstract", fix, ERROR, isOnTheFly);
        }
        PsiMethod[] constructors = stateClass.getConstructors();
        // if no any constructors then implicit default constructor exists
        boolean hasDefaultConstructor = constructors.length == 0;
        if (!hasDefaultConstructor) {
            for (PsiMethod constructor : constructors) {
                if (constructor.getParameterList().isEmpty()) {
                    hasDefaultConstructor = true;
                    if (!constructor.hasModifierProperty(PUBLIC)) {
                        LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createModifierListFix(constructor, PUBLIC, true, true);
                        return manager.createProblemDescriptor(constructor, "For @State class the default constructor must be public", fix, ERROR, isOnTheFly);
                    }
                    break;
                }
            }
        }
        if (!hasDefaultConstructor) {
            LocalQuickFixAndIntentionActionOnPsiElement fix = QuickFixFactory.getInstance().createAddDefaultConstructorFix(stateClass);
            return manager.createProblemDescriptor(stateClass, "The @State annotation can only be applied to the classes having the default public constructor", fix, ERROR, isOnTheFly);
        }
        return null;
    }

}
