package ru.artyushov.jmhPlugin.configuration;

import com.intellij.codeInspection.reference.EntryPoint;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * User: nikart
 * Date: 07/08/14
 * Time: 23:33
 */
public class JmhEntryPoint extends EntryPoint {

  private boolean isSelected = true;

  @NotNull
  @Override
  public String getDisplayName() {
    return "";
  }

  @Override
  public boolean isEntryPoint(@NotNull RefElement refElement, @NotNull PsiElement psiElement) {
    return isEntryPoint(psiElement);
  }

  @Override
  public boolean isEntryPoint(@NotNull PsiElement psiElement) {
    if (isSelected) {
      if (psiElement instanceof PsiClass) {
        final PsiClass aClass = (PsiClass)psiElement;
        for (PsiMethod psiMethod : aClass.getMethods()) {
          if (ConfigurationUtils.hasBenchmarkAnnotation(psiMethod)) {
            return true;
          }
        }
      }

      if (psiElement instanceof PsiMethod) {
        final PsiMethod method = (PsiMethod)psiElement;
        if (ConfigurationUtils.hasBenchmarkAnnotation(method)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isSelected() {
    return isSelected;
  }

  @Override
  public void setSelected(boolean isSelected) {
    this.isSelected = isSelected;
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    isSelected = Boolean.parseBoolean(element.getAttributeValue("isSelected"));
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    element.setAttribute("isSelected", isSelected + "");
  }
}
