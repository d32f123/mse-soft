package com.itmo.mse.soft.performance;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

abstract public class AbstractBenchmark {

  private final static Integer MEASUREMENT_ITERATIONS = 5;
  private final static Integer WARMUP_ITERATIONS = 1;

  @Test
  public void executeJmhRunner() throws RunnerException {
    Options opt = new OptionsBuilder()
        // set the class name regex for benchmarks to search for to the current class
        .include("\\." + this.getClass().getSimpleName() + "\\.")
        .warmupIterations(WARMUP_ITERATIONS)
        .measurementIterations(MEASUREMENT_ITERATIONS)
        // do not use forking or the benchmark methods will not see references stored within its class
        .forks(0)
        // do not use multiple threads
        .threads(1)
        .shouldDoGC(true)
        .shouldFailOnError(true)
        .resultFormat(ResultFormatType.JSON)
        .result("C:\\Users\\Aleksandr_iunusov\\IdeaProjects\\mse-soft\\backend\\src\\main\\resources\\feedPigs.txt")
        .shouldFailOnError(true)
        .jvmArgs("-server")
        .build();

    new Runner(opt).run();
  }
}
