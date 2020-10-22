package ru.artyushov.jmhPlugin.configuration;

import com.intellij.codeInspection.reference.EntryPoint;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.hasSetupOrTearDownAnnotation;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.isBenchmarkEntryElement;

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
      return isBenchmarkEntryElement(psiElement)
              || ((psiElement instanceof PsiMethod) && hasSetupOrTearDownAnnotation((PsiMethod) psiElement));
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
