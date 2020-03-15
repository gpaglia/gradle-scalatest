package com.gpaglia.scalatest.framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScalatestSpec implements Serializable {
  private static final long serialVersionUID = 1;

  private final Set<String> includedTests;
  private final Set<String> excludedTests;
  private final Set<String> includedTestsCommandLine;
  private final Set<String> includeTags;
  private final Set<String> excludeTags;

  public ScalatestSpec(
      Set<String> includedTests,
      Set<String> excludedTests,
      Set<String> includedTestsCommandLine,
      Set<String> includeTags,
      Set<String> excludeTags
  ) {
    this.includedTests = includedTests;
    this.excludedTests = excludedTests;
    this.includedTestsCommandLine = includedTestsCommandLine;
    this.includeTags = includeTags;
    this.excludeTags = excludeTags;
  }

  public Set<String> getIncludedTests() {
    return includedTests;
  }

  public Set<String> getExcludedTests() {
    return excludedTests;
  }

  public Set<String> getIncludedTestsCommandLine() {
    return includedTestsCommandLine;
  }

  public Set<String> getIncludeTags() {
    return new HashSet<>(includeTags);
  }


  public Set<String> getExcludeTags() {
    return new HashSet<String>(excludeTags);
  }
}
