package com.gpaglia.gradle.scalatest.framework;

import com.gpaglia.scalatest.framework.api.Fingerprint;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;

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

  public Class<?> getTestClass() { return clazz; }

  public Fingerprint getFingerprint() { return fingerprint; }

  @Override
  public String toString() {
    return "ScalatestSuiteRunInfo{" +
        "clazz=" + clazz +
        ", fingerprint=" + fingerprint +
        '}';
  }
}
