package com.gpaglia.gradle.scalatest.framework.processors;

import com.gpaglia.gradle.scalatest.framework.ScalatestMatcher;
import com.gpaglia.gradle.scalatest.framework.ScalatestSuiteRunInfo;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;
import org.gradle.api.internal.tasks.testing.TestResultProcessor;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;

public class ScalatestPatternMatchTestClassProcessor implements TestClassProcessor {
  private final ScalatestMatcher matcher;
  private final TestClassProcessor delegate;

  public ScalatestPatternMatchTestClassProcessor(DefaultTestFilter testFilter, TestClassProcessor delegate) {
    this.matcher = ScalatestMatcher.fromFilter(testFilter);
    this.delegate = delegate;
  }

  @Override
  public void startProcessing(TestResultProcessor resultProcessor) {
    delegate.startProcessing(resultProcessor);
  }

  @Override
  public void processTestClass(TestClassRunInfo testClass) {
    if (testClass instanceof ScalatestSuiteRunInfo) {
      ScalatestSuiteRunInfo runInfo = (ScalatestSuiteRunInfo) testClass;
      if (matcher.test(runInfo.getTestClass())) {
        delegate.processTestClass(testClass);
      }
    } else {
      throw new IllegalStateException(
          "Unexpected TestClassRunInfo type [should be ScalatestSuiteRunInfo]: "
              + testClass.getClass().getName()
      );
    }
  }

  @Override
  public void stop() {
    delegate.stop();
  }

  @Override
  public void stopNow() {
    delegate.stopNow();
  }
}