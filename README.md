idea-jmh-plugin
===============

The first step in this project is to make somthing similar to JUnit plugin for idea.
1. Benchmark methods generation.
2. Running all benchmarks in class, package, project with convenient output.

Hot to open project and run plugin.
1. Import project from .iml file.
2. Create new run configuration with type "plugin". An instance of IDEA will be started with plugin already installed. However, some problems might arise. For example, plugin might not be updated after code is edited. This was solved by manually copying META-INF directory to the configation derectory of idea instance. On Mac OS it is ~/Library/Caches/IdeaIC13/plugins-sandbox/plugins/idea-jmh-plugin. Looks like IDEA bug.
