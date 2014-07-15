package ru.artyushov.jmhPlugin.configuration;

import com.intellij.psi.PsiMethod;

/**
 * User: nikart
 * Date: 16/07/14
 * Time: 00:09
 */
public class ConfigurationUtils {

    public static boolean hasBenchmarkAnnotation(PsiMethod method) {
        return method.getModifierList().findAnnotation(JmhConfiguration.JMH_ANNOTATION_NAME) != null;
    }
}
