package com.gpaglia.gradle.scalatest.framework;

import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.internal.file.RelativeFile;
import org.gradle.api.internal.tasks.testing.DefaultTestClassRunInfo;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;
import org.gradle.api.internal.tasks.testing.detection.TestFrameworkDetector;

public class ScalatestTestClassScanner implements Runnable {
  private final FileTree candidateClassFiles;
  private final TestFrameworkDetector testFrameworkDetector;
  private final TestClassProcessor testClassProcessor;

  public ScalatestTestClassScanner(
      FileTree candidateClassFiles,
      TestFrameworkDetector testFrameworkDetector,
      TestClassProcessor testClassProcessor
  ) {
    this.candidateClassFiles = candidateClassFiles;
    this.testFrameworkDetector = testFrameworkDetector;
    this.testClassProcessor = testClassProcessor;
  }

  @Override
  public void run() {
    if (testFrameworkDetector == null) {
      throw new IllegalStateException("Unexpected null detector [filename scan not supported with scalatest]");
    } else {
      detectionScan();
    }
  }

  private void detectionScan() {
    testFrameworkDetector.startDetection(testClassProcessor);
    candidateClassFiles.visit(new ClassFileVisitor() {
      @Override
      public void visitClassFile(FileVisitDetails fileDetails) {
        testFrameworkDetector.processTestClass(new RelativeFile(fileDetails.getFile(), fileDetails.getRelativePath()));
      }
    });
  }

  /*
  private void filenameScan() {
    candidateClassFiles.visit(new ClassFileVisitor() {
      @Override
      public void visitClassFile(FileVisitDetails fileDetails) {
        TestClassRunInfo testClass = new DefaultTestClassRunInfo(getClassName(fileDetails));
        testClassProcessor.processTestClass(testClass);
      }
    });
  }
  */

  private abstract static class ClassFileVisitor extends EmptyFileVisitor {
    @Override
    public void visitFile(FileVisitDetails fileDetails) {
      if (isClass(fileDetails)) {
        visitClassFile(fileDetails);
      }
    }

    abstract void visitClassFile(FileVisitDetails fileDetails);

    private boolean isClass(FileVisitDetails fileVisitDetails) {
      String fileName = fileVisitDetails.getFile().getName();
      return fileName.endsWith(".class") && !"module-info.class".equals(fileName);
    }
  }

  private String getClassName(FileVisitDetails fileDetails) {
    return fileDetails.getRelativePath().getPathString().replaceAll("\\.class", "").replace('/', '.');
  }

}
