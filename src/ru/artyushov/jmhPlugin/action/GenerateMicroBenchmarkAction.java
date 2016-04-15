package ru.artyushov.jmhPlugin.action;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;

/**
 * User: nikart
 * Date: 10/03/14
 * Time: 16:43
 */
public class GenerateMicroBenchmarkAction extends BaseGenerateAction {

    public GenerateMicroBenchmarkAction() {
        super(new BenchmarkMethodHandler());
    }
}
