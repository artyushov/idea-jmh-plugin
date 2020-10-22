package ru.artyushov.jmhPlugin.configuration;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UMethod;

import static com.intellij.psi.PsiModifier.PUBLIC;
import static org.jetbrains.uast.UastUtils.findContaining;

/**
 * User: nikart
 * Date: 16/07/14
 * Time: 00:09
 */
public class ConfigurationUtils {

    public static final String SETUP_ANNOTATION = "org.openjdk.jmh.annotations.Setup";
    public static final String TEAR_DOWN_ANNOTATION = "org.openjdk.jmh.annotations.TearDown";
    public static final String JMH_ANNOTATION_NAME = "org.openjdk.jmh.annotations.Benchmark";
    public static final String JMH_ANNOTATION_STATE = "org.openjdk.jmh.annotations.State";

    public static boolean hasStateAnnotation(@NotNull UClass aClass) {
        return aClass.hasAnnotation(JMH_ANNOTATION_STATE);
    }

    public static boolean hasBenchmarkAnnotation(@NotNull PsiMethod method) {
        return method.hasAnnotation(JMH_ANNOTATION_NAME);
    }

    public static boolean hasBenchmarkAnnotation(@NotNull UMethod method) {
        return method.hasAnnotation(JMH_ANNOTATION_NAME);
    }

    public static boolean hasSetupOrTearDownAnnotation(@NotNull PsiMethod method) {
        return method.hasAnnotation(SETUP_ANNOTATION) ||
                method.hasAnnotation(TEAR_DOWN_ANNOTATION);
    }

    public static boolean hasSetupOrTearDownAnnotation(@NotNull UMethod method) {
        return method.hasAnnotation(SETUP_ANNOTATION) ||
                method.hasAnnotation(TEAR_DOWN_ANNOTATION);
    }

    public static boolean isBenchmarkEntryElement(PsiElement element) {
        return element instanceof PsiMethod && hasBenchmarkAnnotation((PsiMethod) element)
                || (element instanceof PsiClass && isBenchmarkClass((PsiClass) element));
    }

    public static boolean isBenchmarkMethod(@NotNull PsiMethod method) {
        return method.hasModifierProperty(PUBLIC) && hasBenchmarkAnnotation(method);
    }

    public static boolean isBenchmarkMethod(@NotNull UMethod method) {
        return method.hasModifierProperty(PUBLIC) && hasBenchmarkAnnotation(method);
    }

    public static boolean isBenchmarkClass(@NotNull PsiClass aClass) {
        final PsiMethod[] methods = aClass.getMethods();
        for (final PsiMethod method : methods) {
            if (isBenchmarkMethod(method)) return true;
        }
        return false;
    }

    public static boolean isBenchmarkClass(@NotNull UClass aClass) {
        final UMethod[] methods = aClass.getMethods();
        for (final UMethod method : methods) {
            if (isBenchmarkMethod(method)) return true;
        }
        return false;
    }

    @Nullable
    static PsiElement findBenchmarkEntry(PsiElement locationElement) {
        UMethod method = findContaining(locationElement, UMethod.class);
        if (method != null && isBenchmarkMethod(method)) {
            return method;
        }
        UClass klass = findContaining(locationElement, UClass.class);
        if (klass != null && isBenchmarkClass(klass)) {
            return klass;
        }
        return null;
    }

    @NotNull
    public static String toRunParams(@NotNull PsiElement benchmarkEntry, boolean fqn) {
        if (benchmarkEntry instanceof PsiMethod) {
            PsiMethod benchmarkMethod = (PsiMethod) benchmarkEntry;
            PsiClass benchmarkClass = benchmarkMethod.getContainingClass();
            assert benchmarkClass != null;
            String benchmarkClassName = fqn ? benchmarkClass.getQualifiedName() : benchmarkClass.getName();
            return benchmarkClassName + '.' + benchmarkMethod.getName();
        } else if (benchmarkEntry instanceof PsiClass) {
            PsiClass benchmarkClass = (PsiClass) benchmarkEntry;
            String benchmarkClassName = fqn ? benchmarkClass.getQualifiedName() : benchmarkClass.getName();
            return benchmarkClassName + ".*";
        } else {
            return "";
        }
    }

}
