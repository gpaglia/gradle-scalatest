package com.gpaglia.scalatest.framework;

import com.gpaglia.scalatest.framework.api.Detector;
import com.gpaglia.scalatest.framework.api.Fingerprint;
import com.gpaglia.scalatest.framework.api.Framework;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.internal.file.RelativeFile;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.detection.TestFrameworkDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

public class ScalatestDetector implements TestFrameworkDetector {
  private static final Logger LOGGER = LoggerFactory.getLogger(ScalatestDetector.class);

  private TestClassProcessor testClassProcessor;
  private Set<File> testClasspath;
  private Set<File> testClassesDirectories;
  private final Detector detector;

  public ScalatestDetector(final Detector detector) {
    this.detector = detector;
  }

  @Override
  public void startDetection(TestClassProcessor testClassProcessor) {
    this.testClassProcessor = testClassProcessor;
  }

  @Override
  public boolean processTestClass(RelativeFile testClassFile) {
    final String relativeClassFile = testClassFile.getRelativePath().getPathString();
    LOGGER.debug("Testing class file {}, relative path {}", testClassFile, relativeClassFile);
    final Set<Pair<Class<?>, Fingerprint>> result = detector.processSuite(relativeClassFile);
    if (! result.isEmpty()) {
      for (Pair<Class<?>, Fingerprint> pair: result) {
        publishTestClass(pair.getLeft(), pair.getRight());
      }
    }
    return false;
  }

  @Override
  public void setTestClasses(Set<File> testClassesDirectories) {
    this.testClassesDirectories = testClassesDirectories;
  }

  @Override
  public void setTestClasspath(Set<File> testClasspath) {
    this.testClasspath = testClasspath;
  }

  // private methods

  private void publishTestClass(final Class<?> clazz, final Fingerprint fingerprint) {
    testClassProcessor.processTestClass(new ScalatestSuiteRunInfo(clazz, fingerprint));
  }
}
