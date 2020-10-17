package ru.artyushov.jmhPlugin.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiTypeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UMethod;

import static com.intellij.psi.PsiModifier.PUBLIC;
import static com.intellij.psi.PsiType.VOID;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasSetupOrTearDownAnnotation;

/**
 * @Benchmark: make public
 * @Setup @TearDown: make public and void
 */
public class BenchMethodSignatureFix implements LocalQuickFix {

    @NotNull
    @Override
    public String getName() {
        return "Fix method signature";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        UMethod benchMethod = (UMethod) descriptor.getPsiElement();
        if (!benchMethod.getModifierList().hasModifierProperty(PUBLIC)) {
            benchMethod.getModifierList().setModifierProperty(PUBLIC, true);
        }
        if (hasSetupOrTearDownAnnotation(benchMethod)
                && (benchMethod.getReturnType() == null || !benchMethod.getReturnType().equals(VOID))) {
            //FIXME create UAST element
            PsiTypeElement voidReturnEl = JavaPsiFacade.getElementFactory(project).createTypeElement(VOID);
            if (benchMethod.getReturnTypeElement() != null) {
                benchMethod.getReturnTypeElement().replace(voidReturnEl);
            }
        }
    }

}
