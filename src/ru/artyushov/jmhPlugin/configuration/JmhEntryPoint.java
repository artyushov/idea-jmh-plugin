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
import org.jetbrains.annotations.Nullable;

import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.containsBenchmarkMethod;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.isBenchmarkMethod;

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
    return "jmhEntryPoint";
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
        if (containsBenchmarkMethod(aClass)) {
          return true;
        }
      }

      if (psiElement instanceof PsiMethod) {
        final PsiMethod method = (PsiMethod)psiElement;
        if (isBenchmarkMethod(method)
                || ConfigurationUtils.hasSetupOrTearDownAnnotation(method)) {
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

  @Nullable
  @Override
  public String[] getIgnoreAnnotations() {
    return super.getIgnoreAnnotations();
  }
}
