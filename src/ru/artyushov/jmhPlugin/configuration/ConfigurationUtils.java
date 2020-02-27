package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;

import java.util.Iterator;

/**
 * User: nikart
 * Date: 16/07/14
 * Time: 00:09
 */
public class ConfigurationUtils {

    public static final String SETUP_ANNOTATION = "org.openjdk.jmh.annotations.Setup";
    public static final String TEAR_DOWN_ANNOTATION = "org.openjdk.jmh.annotations.TearDown";

    public static boolean hasBenchmarkAnnotation(PsiMethod method) {
        return method.getModifierList().findAnnotation(JmhConfiguration.JMH_ANNOTATION_NAME) != null;
    }

    public static boolean hasSetupOrTearDownAnnotation(PsiMethod method) {
        return method.getModifierList().findAnnotation(SETUP_ANNOTATION) != null ||
                method.getModifierList().findAnnotation(TEAR_DOWN_ANNOTATION) != null;
    }

    public static PsiMethod getAnnotatedMethod(ConfigurationContext context) {
        Location<?> location = context.getLocation();
        if (location == null) {
            return null;
        }
        Iterator<Location<PsiMethod>> iterator = location.getAncestors(PsiMethod.class, false);
        Location<PsiMethod> methodLocation = null;
        if (iterator.hasNext()) {
            methodLocation = iterator.next();
        }
        if (methodLocation == null) {
            return null;
        }
        PsiMethod method = methodLocation.getPsiElement();
        if (hasBenchmarkAnnotation(method)) {
            return method;
        }
        return null;
    }

    public static boolean isBenchmarkMethod(PsiElement element) {
        if (!(element instanceof PsiIdentifier))
            return false;

        element = element.getParent();
        if (!(element instanceof PsiMethod))
            return false;

        return isBenchmarkMethod((PsiMethod) element);
    }

    private static boolean isBenchmarkMethod(PsiMethod method) {
        return method.getContainingClass() != null && method.hasModifierProperty("public") && hasBenchmarkAnnotation(method);
    }

    public static boolean isBenchmarkClass(final PsiElement psiElement) {
        if (!(psiElement instanceof PsiIdentifier))
            return false;

        final PsiElement element = psiElement.getParent();

        return element instanceof PsiClass && containsBenchmarkMethod((PsiClass) element);
    }

    private static boolean containsBenchmarkMethod(final PsiClass aClass) {
        final PsiMethod[] methods = aClass.getMethods();
        for (final PsiMethod method : methods) {
            if (isBenchmarkMethod(method)) return true;
        }
        return false;
    }
}
