package com.gpaglia.scalatest;


import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.testing.AbstractScalatestTask;

@CacheableTask
public class ScalatestTask extends AbstractScalatestTask {
  public ScalatestTask() { super(); }
}
