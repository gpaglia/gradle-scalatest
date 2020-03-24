package com.gpaglia.scalatest.framework;


import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ScalatestSpec implements Serializable {
  private static final long serialVersionUID = 1;

  // input patterns
  private final Set<String> includePatterns;
  private final Set<String> excludePatterns;
  private final Set<String> commandLineIncludePatterns;
  // input params on tags
  private final Set<String> includeTags;
  private final Set<String> excludeTags;

  private final Set<BiPredicate<String, String>> testIncludePredicates; // class, test
  private final Set<BiPredicate<String, String>> testExcludePredicates; // class, test
  private final Set<Predicate<String>> classIncludePredicates;
  private final Set<Predicate<String>> classExcludePredicates;


  public ScalatestSpec(DefaultTestFilter filter, ScalatestOptions options) {
    this.includePatterns = filter.getIncludePatterns();
    this.excludePatterns = filter.getExcludePatterns();
    this.commandLineIncludePatterns = filter.getCommandLineIncludePatterns();
    this.includeTags = options.getIncludeTags();
    this.excludeTags = options.getExcludeTags();
    this.testIncludePredicates = generateTestIncludePredicates(this.includePatterns, this.commandLineIncludePatterns);
    this.testExcludePredicates = generateTestExcludePredicates(this.excludePatterns);
    this.classIncludePredicates = generateClassIncludePredicates(this.includePatterns, this.commandLineIncludePatterns);
    this.classExcludePredicates = generateClassExcludePredicates(this.excludePatterns);
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

  public Set<String>
  getExcludeTags() {
    return new HashSet<String>(excludeTags);
  }

  private Set<BiPredicate<String, String>> generateTestIncludePredicates(Set<String> incPatterns, Set<String> incCmdPatterns) {
    // TODO: complete
    return null;
  }

  private Set<BiPredicate<String, String>> generateTestExcludePredicates(Set<String> excPatterns) {
    // TODO: complete
    return null;
  }

  private Set<Predicate<String>> generateClassIncludePredicates(Set<String> incPatterns, Set<String> incCmdPatterns) {
    // TODO: complete
    return null;
  }

  private Set<Predicate<String>> generateClassExcludePredicates(Set<String> excPatterns) {
    // TODO: complete
    return null;
  }

}
