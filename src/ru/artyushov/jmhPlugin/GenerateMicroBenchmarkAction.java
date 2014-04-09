package ru.artyushov.jmhPlugin;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.codeInsight.daemon.impl.quickfix.CreateFromUsageUtils;
import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.OverrideImplementUtil;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.testIntegration.TestIntegrationUtils;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * User: nikart
 * Date: 10/03/14
 * Time: 16:43
 */
public class GenerateMicroBenchmarkAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            return;
        }
        final PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiClass psiClass = getPsiClassFromContext(e);
        Template template = BenchmarkMethodTemplate.create(psiClass);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                final PsiMethod method = generateDummyMethod(editor, psiFile);
                if (method == null) {
                    return;
                }

                TextRange range = method.getTextRange();
                editor.getDocument().replaceString(range.getStartOffset(), range.getEndOffset(), "");
                editor.getCaretModel().moveToOffset(range.getStartOffset());
            }
        });

        TemplateEditingAdapter adapter = new TemplateEditingAdapter() {
            @Override
            public void templateFinished(Template template, boolean brokenOff) {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {

                        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
                        PsiFile psi = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
                        if (psi == null) {
                            return;
                        }
                        PsiElement el = PsiTreeUtil.findElementOfClassAtOffset(psi, editor.getCaretModel().getOffset() - 1, PsiMethod.class, false);

                        if (el != null) {
                            PsiMethod method = PsiTreeUtil.getParentOfType(el, PsiMethod.class, false);
                            if (method != null) {
                                if (method.findDeepestSuperMethods().length > 0) {
                                    GenerateMembersUtil.setupGeneratedMethod(method);
                                }
                                CreateFromUsageUtils.setupEditor(method, editor);
                            }
                        }
                    }
                });

            }
        };

        TemplateManager.getInstance(project).startTemplate(editor, template, adapter);
    }

    @Nullable
    private static PsiMethod generateDummyMethod(Editor editor, PsiFile file) throws IncorrectOperationException {
        final PsiMethod method = TestIntegrationUtils.createDummyMethod(file);
        final PsiGenerationInfo<PsiMethod> info = OverrideImplementUtil.createGenerationInfo(method);

        int offset = findOffsetToInsertMethodTo(editor, file);
        GenerateMembersUtil.insertMembersAtOffset(file, offset, Collections.singletonList(info));

        final PsiMethod member = info.getPsiMember();
        return member != null ? CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(member) : null;
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

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    }
}
