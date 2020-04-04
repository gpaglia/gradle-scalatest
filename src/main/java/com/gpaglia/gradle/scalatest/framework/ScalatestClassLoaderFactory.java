package com.gpaglia.gradle.scalatest.framework;

import org.gradle.api.tasks.testing.AbstractScalatest;
import org.gradle.api.internal.initialization.ClassLoaderIds;
import org.gradle.api.internal.initialization.loadercache.ClassLoaderCache;
import org.gradle.internal.Factory;
import org.gradle.internal.classpath.DefaultClassPath;

public class ScalatestClassLoaderFactory implements Factory<ClassLoader> {
  private final ClassLoaderCache classLoaderCache;
  private final AbstractScalatest testTask;
  private ClassLoader testClassLoader;

  public ScalatestClassLoaderFactory(ClassLoaderCache classLoaderCache, AbstractScalatest testTask) {
    this.classLoaderCache = classLoaderCache;
    this.testTask = testTask;
  }

  @Override
  public ClassLoader create() {
    if (testClassLoader == null) {
      testClassLoader = classLoaderCache.get(ClassLoaderIds.testTaskClasspath(testTask.getPath()), DefaultClassPath.of(testTask.getClasspath()), null, null);
    }
    return testClassLoader;
  }
}
