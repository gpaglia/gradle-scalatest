package com.gpaglia.scalatest.framework;

import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;
import org.gradle.api.internal.tasks.testing.TestResultProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ScalatestTestClassProcessor implements TestClassProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ScalatestTestClassProcessor.class);

  private final ScalatestSpec spec;
  private final File testReportDir;

  public ScalatestTestClassProcessor(final ScalatestSpec spec, File testreportDir) {
    this.spec = spec;
    this.testReportDir = testreportDir;
  }

  @Override
  public void startProcessing(TestResultProcessor resultProcessor) {
    LOGGER.debug("Starting processing ...");
  }

  @Override
  public void processTestClass(TestClassRunInfo testClass) {
    LOGGER.debug("Processing test class {}", testClass.getTestClassName());
  }

  @Override
  public void stop() {
    LOGGER.debug("Stop requested...");
  }

  @Override
  public void stopNow() {
    LOGGER.debug("Stop-now requested...");
  }
}
