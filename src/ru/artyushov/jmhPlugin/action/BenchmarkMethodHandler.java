package ru.artyushov.jmhPlugin.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.codeInsight.daemon.impl.quickfix.CreateFromUsageUtils;
import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.OverrideImplementUtil;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.DocCommandGroupId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testIntegration.TestIntegrationUtils;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Author: artyushov
 * Date: 2016-04-16 00:16
 */
class BenchmarkMethodHandler implements CodeInsightActionHandler {

    @Override
    public void invoke(@NotNull Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
        PsiClass targetClass = findTargetClass(editor, file);
        if (targetClass == null) {
            return;
        }
        generate(editor, file, targetClass);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    private void generate(final Editor editor, final PsiFile file, final PsiClass targetClass) {

        WriteCommandAction.runWriteCommandAction(file.getProject(), new Runnable() {
            @Override
            public void run() {
                PsiDocumentManager.getInstance(file.getProject()).commitAllDocuments();
                PsiMethod method = generateDummyMethod(editor, file);
                if (method == null) {
                    return;
                }

                Template template = BenchmarkMethodTemplateFactory.create(targetClass);
                createSpaceForNewMethod(file.getProject(), editor, file);
                TemplateEditingAdapter adapter = createTemplateAdapter(new Runnable() {
                    public void run() {

                        PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
                        PsiFile psi = PsiDocumentManager.getInstance(file.getProject()).getPsiFile(editor.getDocument());
                        if (psi == null) {
                            return;
                        }
                        PsiElement el = PsiTreeUtil.findElementOfClassAtOffset(psi, editor.getCaretModel().getOffset() - 1, PsiMethod.class, false);

                        if (el == null) {
                            return;
                        }

                        PsiMethod method = PsiTreeUtil.getParentOfType(el, PsiMethod.class, false);
                        if (method == null) {
                            return;
                        }

                        if (method.findDeepestSuperMethods().length > 0) {
                            GenerateMembersUtil.setupGeneratedMethod(method);
                        }
                        CreateFromUsageUtils.setupEditor(method, editor);
                    }
                });
                TemplateManager.getInstance(file.getProject()).startTemplate(editor, template, adapter);
            }
        });
    }

    private static PsiClass findTargetClass(@NotNull Editor editor, @NotNull PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(element, PsiClass.class, false) == null ? null : TestIntegrationUtils.findOuterClass(element);
    }

    private static PsiMethod generateDummyMethod(Editor editor, PsiFile file) throws IncorrectOperationException {
        PsiMethod method = TestIntegrationUtils.createDummyMethod(file);
        PsiGenerationInfo<PsiMethod> info = OverrideImplementUtil.createGenerationInfo(method);

        int offset = findOffsetToInsertMethodTo(editor, file);
        GenerateMembersUtil.insertMembersAtOffset(file, offset, Collections.singletonList(info));

        PsiMethod member = info.getPsiMember();
        return member != null ? CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(member) : null;
    }

    private static void createSpaceForNewMethod(Project project, final Editor editor, final PsiFile psiFile) {
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            @Override
            public void run() {

                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        PsiMethod method = generateDummyMethod(editor, psiFile);
                        if (method == null) {
                            return;
                        }

                        TextRange range = method.getTextRange();
                        editor.getDocument().replaceString(range.getStartOffset(), range.getEndOffset(), "");
                        editor.getCaretModel().moveToOffset(range.getStartOffset());
                    }
                });

            }
        }, "", DocCommandGroupId.noneGroupId(editor.getDocument()));
    }

    private static TemplateEditingAdapter createTemplateAdapter(final Runnable runnable) {
        return new TemplateEditingAdapter() {
            @Override
            public void templateFinished(@NotNull Template template, boolean brokenOff) {
                ApplicationManager.getApplication().runWriteAction(runnable);
            }
        };
    }

    private static int findOffsetToInsertMethodTo(Editor editor, PsiFile file) {
        int result = editor.getCaretModel().getOffset();

        PsiClass classAtCursor = PsiTreeUtil.getParentOfType(file.findElementAt(result), PsiClass.class, false);

        while (classAtCursor != null && !(classAtCursor.getParent() instanceof PsiFile)) {
            result = classAtCursor.getTextRange().getEndOffset();
            classAtCursor = PsiTreeUtil.getParentOfType(classAtCursor, PsiClass.class);
        }

        return result;
    }
}
