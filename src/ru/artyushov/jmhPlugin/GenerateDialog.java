package ru.artyushov.jmhPlugin;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: nikart
 * Date: 10/03/14
 * Time: 16:47
 */
public class GenerateDialog extends DialogWrapper {

//    private final JComponent centerPanel;

    protected GenerateDialog(PsiClass psiClass) {
        super(psiClass.getProject());
        setTitle("Generate Benchmark");


    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }
}
