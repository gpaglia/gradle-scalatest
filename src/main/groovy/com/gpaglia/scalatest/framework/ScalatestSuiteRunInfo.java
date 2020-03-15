package com.gpaglia.scalatest.framework;

import com.gpaglia.scalatest.framework.api.Fingerprint;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;
import org.gradle.internal.impldep.org.junit.runners.Suite;

public class ScalatestSuiteRunInfo implements TestClassRunInfo {

  private final Class<?> clazz;
  private final Fingerprint fingerprint;

  public ScalatestSuiteRunInfo(final Class<?> clazz, Fingerprint fingerprint) {
    this.clazz = clazz;
    this.fingerprint = fingerprint;
  }

  @Override
  public String getTestClassName() {
    return clazz.getName();
  }

  public Class<?> getTEstClass() { return clazz; }

  public Fingerprint getFIngerprint() { return fingerprint; }
}
