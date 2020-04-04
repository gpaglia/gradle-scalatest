package com.gpaglia.gradle.scalatest.framework;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.testing.TestFrameworkOptions;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Scalatest specific test options.
 * Partially copied from gradle NG test framework implementation
 *
 * @see <a href="https://github.com/gradle/gradle/blob/master/subprojects/testing-jvm/src/main/java/org/gradle/api/tasks/testing/testng/TestNGOptions.java">TestNGOptions </a>
 */
public class ScalatestOptions extends TestFrameworkOptions {

  private final File projectDir;
  private File outputDirectory;
  private Set<String> includeTags = new LinkedHashSet<String>();
  private Set<String> excludeTags = new LinkedHashSet<String>();

  public ScalatestOptions(File projectDir) {
    this.projectDir = projectDir;
  }

  /**
   * The set of tags to run with.
   * @param includeTags the tags to include
   * @return the updated options
   * @see <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-tagging-and-filtering">Tagging and Filtering</a>
   */
  public ScalatestOptions includeTags(String... includeTags) {
    this.includeTags.addAll(Arrays.asList(includeTags));
    return this;
  }

  /**
   * The set of tags to exclude.
   * @param excludeTags the tags to exclude
   * @return the updated options
   * @see <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-tagging-and-filtering">Tagging and Filtering</a>
   */
  public ScalatestOptions excludeTags(String... excludeTags) {
    this.excludeTags.addAll(Arrays.asList(excludeTags));
    return this;
  }

  @Input
  public Set<String> getIncludeTags() {
    return includeTags;
  }

  public void setIncludeTags(Set<String> includeTags) {
    this.includeTags = includeTags;
  }

  @Input
  public Set<String> getExcludeTags() {
    return excludeTags;
  }

  public void setExcludeTags(Set<String> excludeTags) {
    this.excludeTags = excludeTags;
  }

  @OutputDirectory
  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

}
