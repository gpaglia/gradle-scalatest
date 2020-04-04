package com.gpaglia.gradle.scalatest.support;

import com.gpaglia.gradle.scalatest.framework.ScalatestTestFramework;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.tasks.testing.TestExecutionSpec;
import org.gradle.process.JavaForkOptions;
import org.gradle.util.Path;

import java.io.File;
import java.util.Set;

public class ScalatestTestExecutionSpec implements TestExecutionSpec {
  private final ScalatestTestFramework testFramework;
  private final Iterable<? extends File> classpath;
  private final FileTree candidateClassFiles;
  private final boolean scanForTestClasses;
  private final FileCollection testClassesDirs;
  private final String path;
  private final Path identityPath;
  private final long forkEvery;
  private final JavaForkOptions javaForkOptions;
  private final int maxParallelForks;
  private final Set<String> previousFailedTestClasses;


  public ScalatestTestExecutionSpec(
      ScalatestTestFramework testFramework,
      Iterable<? extends File> classpath,
      FileTree candidateClassFiles,
      boolean scanForTestClasses,
      FileCollection testClassesDirs,
      String path,
      Path identityPath,
      long forkEvery,
      JavaForkOptions javaForkOptions,
      int maxParallelForks,
      Set<String> previousFailedTestClasses) {

    this.testFramework = testFramework;
    this.classpath = classpath;
    this.candidateClassFiles = candidateClassFiles;
    this.scanForTestClasses = scanForTestClasses;
    this.testClassesDirs = testClassesDirs;
    this.path = path;
    this.identityPath = identityPath;
    this.forkEvery = forkEvery;
    this.javaForkOptions = javaForkOptions;
    this.maxParallelForks = maxParallelForks;
    this.previousFailedTestClasses = previousFailedTestClasses;
  }

  public ScalatestTestFramework getTestFramework() {
    return testFramework;
  }

  public Iterable<? extends File> getClasspath() {
    return classpath;
  }

  public FileTree getCandidateClassFiles() {
    return candidateClassFiles;
  }

  public boolean isScanForTestClasses() {
    return scanForTestClasses;
  }

  public FileCollection getTestClassesDirs() {
    return testClassesDirs;
  }

  public String getPath() {
    return path;
  }

  public Path getIdentityPath() {
    return identityPath;
  }

  public long getForkEvery() {
    return forkEvery;
  }

  public JavaForkOptions getJavaForkOptions() {
    return javaForkOptions;
  }

  public int getMaxParallelForks() {
    return maxParallelForks;
  }

  public Set<String> getPreviousFailedTestClasses() {
    return previousFailedTestClasses;
  }
}
