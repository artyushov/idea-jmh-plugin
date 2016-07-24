package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Sitnikov
 */
public class JmhRunLineMarkerContributor extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(PsiElement psiElement) {
        if (!ConfigurationUtils.isBenchmarkMethod(psiElement))
          return null;

        final AnAction[] actions = ExecutorAction.getActions(0);
        return new Info(AllIcons.RunConfigurations.TestState.Run, new Function<PsiElement, String>() {
            @Override
            public String fun(final PsiElement element) {
                return StringUtil.join(ContainerUtil.mapNotNull(actions, new Function<AnAction, String>() {
                  @Override
                  public String fun(AnAction action) {
                      return getText(action, element);
                  }
                }), "\n");
            }
        }, actions);
    }

}
