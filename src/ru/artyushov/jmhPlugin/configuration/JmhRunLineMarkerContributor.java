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
        boolean isBenchmarkMethod = ConfigurationUtils.isBenchmarkMethod(psiElement);
        if (isBenchmarkMethod) {
            final AnAction[] actions = ExecutorAction.getActions(0);
            return new Info(AllIcons.RunConfigurations.TestState.Run, new TooltipProvider(actions), actions);
        }

        boolean isBenchmarkClass = ConfigurationUtils.isBenchmarkClass(psiElement);
        if (isBenchmarkClass) {
            final AnAction[] actions = ExecutorAction.getActions(0);
            return new Info(AllIcons.RunConfigurations.TestState.Run_run, new TooltipProvider(actions), actions);
        }

        return null;
    }

    private static class TooltipProvider implements com.intellij.util.Function<PsiElement, String> {
        private final AnAction[] actions;

        private TooltipProvider(AnAction[] actions) {
            this.actions = actions;
        }

        @Override
        public String fun(PsiElement element) {
            return StringUtil.join(ContainerUtil.mapNotNull(actions, new Function<AnAction, String>() {
                @Override
                public String fun(AnAction action) {
                    return getText(action, element);
                }
            }), "\n");
        }
    }
}
