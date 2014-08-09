### 1. [JMHSample_01_HelloWorld](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_01_HelloWorld.java)

Running with the following parameters: `-f 10 -wi 10 -i 20 -tu us`<br/>
IDEA results:<br/>
`average = 3061.918, stdev: 74.80403`<br/>
Command line results:<br/>
`average: 3111.455, stdev: 77.44337`<br/>
As we see, the difference in means is just 1.5%. Let's check that these distributions are not completely different.
After subtracting mean from each sample and running [Kolmogorov-Smirnov](http://en.wikipedia.org/wiki/Kolmogorov–Smirnov_test) 
test against them we do not reject the distribution equality hypothesis on significance level **0.05**.

Other benchmarks were not analysed as thoroughly, so I'll just present the means.

### 2. [JMHSample_02_BenchmarkModes](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_02_BenchmarkModes.java)

```
Benchmark             Mode   Samples   Score-Idea   Score-Term  (MAX - MIN)/MAX     Units
measureAll           thrpt         5        0.000        0.000                -    ops/us
measureMultiple      thrpt         5        0.000        0.000                -    ops/us
measureThroughput    thrpt         5        9.969        9.926            0.004     ops/s
measureAll            avgt         5   100201.473   100864.473            0.006     us/op
measureAvgTime        avgt         5   100228.545   100779.818            0.005     us/op
measureMultiple       avgt         5   100257.764   100841.600            0.005     us/op
measureAll          sample        55   100086.579   100622.783            0.005     us/op
measureMultiple     sample        55   100117.560   100722.874            0.006     us/op
measureSamples      sample        55   100057.982   100684.744            0.006     us/op
measureAll              ss         5   100190.600   101086.000            0.008        us
measureMultiple         ss         5   100101.800   101094.600            0.009        us
measureSingleShot       ss         5   100184.000   100536.200            0.003        us
```

### 3. [JMHSample_03_States](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_03_States.java)

```
Benchmark           Mode   Samples       Score-Idea      Score-Term  (MAX - MIN)/MAX  Units
measureShared      thrpt        25    644535828.753   647525817.305            0.004  ops/s
measureUnshared    thrpt        25   1276415578.342  1296907288.469            0.015  ops/s
```

### 4. [JMHSample_04_DefaultState](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_04_DefaultState.java)

```
Benchmark   Mode   Samples      Score-Idea      Score-Term   (MAX - MIN)/MAX   Units
measure    thrpt        25   341919594.645   344548681.629             0.007   ops/s
```

### 5. [JMHSample_05_StateFixtures](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_05_StateFixtures.java)

```
Benchmark        Mode   Samples        Score-Idea       Score-Term   (MAX - MIN)/MAX   Units
measureRight    thrpt        25     341193314.735    343132773.161             0.005   ops/s
measureWrong    thrpt        25    3008081495.542   3077264376.682             0.022   ops/s
```
### If you want, you may continue this list :)