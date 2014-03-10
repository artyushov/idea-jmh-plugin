import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * User: nikart
 * Date: 10/03/14
 * Time: 16:43
 */
public class GenerateMicroBenchmarkAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        generateMethod(psiClass);

//        GenerateDialog dlg = new GenerateDialog(psiClass);
//        dlg.show();
//        if (dlg.isOK()) {
//
//        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private void generateMethod(final PsiClass psiClass) {
        new WriteCommandAction.Simple(psiClass.getProject(), psiClass.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
                PsiMethod method = elementFactory.createMethodFromText(generateBenchmarkMethodString(), psiClass);
                PsiElement element = psiClass.add(method);
                JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(element);
            }
        }.execute();
    }

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        PsiClass psiClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        return psiClass;
    }

    private String generateBenchmarkMethodString() {
        StringBuilder builder = new StringBuilder();
        builder
                .append("@GenerateMicroBenchmark\n")
                .append("public void benchmarkName() {\n")
                .append("\n")
                .append("}\n");
        return builder.toString();

    }
}
