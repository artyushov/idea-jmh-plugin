package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastUtils;

import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;
import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run_run;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.isBenchmarkClass;
import static ru.artyushov.jmhPlugin.configuration.ConfigurationUtils.isBenchmarkMethod;

/**
 * @author Sergey Sitnikov
 */
public class JmhRunLineMarkerContributor extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement psiElement) {
        UElement uElement = UastUtils.getUParentForIdentifier(psiElement);
        if (uElement instanceof UMethod) {
            boolean isBenchmarkMethod = isBenchmarkMethod((UMethod) uElement);
            if (isBenchmarkMethod) {
                final AnAction[] actions = ExecutorAction.getActions(0);
                return new Info(Run, new TooltipProvider(actions), actions);
            }
        } else if (uElement instanceof UClass) {
            boolean isBenchmarkClass = isBenchmarkClass((UClass) uElement);
            if (isBenchmarkClass) {
                final AnAction[] actions = ExecutorAction.getActions(0);
                return new Info(Run_run, new TooltipProvider(actions), actions);
            }
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
