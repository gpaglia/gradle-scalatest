package com.gpaglia.scalatest.framework;


import com.gpaglia.scalatest.framework.matcher.ScalatestDefaultMatcher;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScalatestSpec implements Serializable {
  private static final long serialVersionUID = 1;

  // input patterns
  private final Set<String> includePatterns;
  private final Set<String> excludePatterns;
  private final Set<String> commandLineIncludePatterns;
  // input params on tags
  private final Set<String> includeTags;
  private final Set<String> excludeTags;

  private final ScalatestMatcher includeMatcher;
  private final ScalatestMatcher excludeMatcher;


  public ScalatestSpec(DefaultTestFilter filter, ScalatestOptions options) {
    this.includePatterns = filter.getIncludePatterns();
    this.excludePatterns = filter.getExcludePatterns();
    this.commandLineIncludePatterns = filter.getCommandLineIncludePatterns();
    this.includeTags = options.getIncludeTags();
    this.excludeTags = options.getExcludeTags();
    this.includeMatcher = generateIncludeMatcher(this.includePatterns, this.commandLineIncludePatterns);
    this.excludeMatcher = generateExcludeMatcher(this.excludePatterns);
  }

  public Set<String> getIncludePatterns() {
    return new HashSet<>(includePatterns);
  }

  public Set<String> getExcludeTests() {
    return new HashSet<>(excludePatterns);
  }

  public Set<String> getCommandLineIncludePatterns() {
    return new HashSet<>(commandLineIncludePatterns);
  }

  public Set<String> getIncludeTags() {
    return new HashSet<>(includeTags);
  }

  public Set<String> getExcludeTags() {
    return new HashSet<String>(excludeTags);
  }

  public boolean test(Class<?> clazz) {
    return includeMatcher.test(clazz) && ! excludeMatcher.test(clazz);
  }

  public boolean test(Class<?> clazz, String testName) {
    return includeMatcher.test(clazz, testName) && ! excludeMatcher.test(clazz, testName);
  }

  // private helper methods

  private ScalatestMatcher generateIncludeMatcher(Set<String> incPatterns, Set<String> incCmdPatterns) {
    final List<ScalatestMatcher> matchers = new ArrayList<>();
    for (String incPattern : incPatterns) {
      matchers.add(new ScalatestDefaultMatcher(incPattern));
    }
    for (String incCmdPattern : incCmdPatterns) {
      matchers.add(new ScalatestDefaultMatcher(incCmdPattern));
    }
    return ScalatestMatcher.or(matchers);
  }

  private ScalatestMatcher generateExcludeMatcher(Set<String> excPatterns) {
    final List<ScalatestMatcher> matchers = new ArrayList<>();
    for (String excPattern : excPatterns) {
      matchers.add(new ScalatestDefaultMatcher(excPattern));
    }
    return ScalatestMatcher.or(matchers);
  }



}
