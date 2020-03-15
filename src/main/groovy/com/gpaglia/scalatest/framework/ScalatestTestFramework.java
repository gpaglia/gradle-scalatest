package com.gpaglia.scalatest.framework;

import com.gpaglia.scalatest.framework.api.Framework;
import com.gpaglia.scalatest.framework.impl.FrameworkFactoryImpl;
import org.gradle.api.Action;
import org.gradle.api.internal.initialization.loadercache.ClassLoaderCache;
import org.gradle.api.internal.tasks.testing.TestClassLoaderFactory;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestFramework;
import org.gradle.api.internal.tasks.testing.WorkerTestClassProcessorFactory;
import org.gradle.api.internal.tasks.testing.detection.TestFrameworkDetector;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestFrameworkOptions;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.process.internal.worker.WorkerProcessBuilder;

import java.io.File;
import java.io.Serializable;

/**
 * Scalatest specific test framework.
 * Partially copied from gradle NG test framework implementation
 *
 * @see <a href="https://github.com/gradle/gradle/blob/master/subprojects/testing-jvm/src/main/java/org/gradle/api/internal/tasks/testing/testng/TestNGTestFramework.java">TtestNGTestFramework</a>
 */
public class ScalatestTestFramework implements TestFramework {
  private final Test testTask;
  private final DefaultTestFilter filter;
  private final ScalatestOptions options;
  private final ScalatestDetector detector;
  private final TestClassLoaderFactory classLoaderFactory;
  private final Framework framework;


  public ScalatestTestFramework(final Test testTask, DefaultTestFilter filter, Instantiator instantiator, ClassLoaderCache classLoaderCache) {
    this.testTask = testTask;
    this.filter = filter;
    options = instantiator.newInstance(ScalatestOptions.class, testTask.getProject().getProjectDir());
    classLoaderFactory = new TestClassLoaderFactory(classLoaderCache, testTask);
    framework = new FrameworkFactoryImpl().newFramework(classLoaderFactory.create());
    detector = new ScalatestDetector(framework.newDetector());
  }
  @Override
  public TestFrameworkDetector getDetector() {
    return detector;
  }

  @Override
  public TestFrameworkOptions getOptions() {
    return options;
  }

  @Override
  public WorkerTestClassProcessorFactory getProcessorFactory() {
    //TODO: tbc
    final ScalatestSpec spec = new ScalatestSpec(
        filter.getIncludePatterns(),
        filter.getExcludePatterns(),
        filter.getCommandLineIncludePatterns(),
        options.getIncludeTags(),
        options.getExcludeTags());
    return new TestClassProcessorFactoryImpl(options.getOutputDirectory(), spec);
  }

  @Override
  public Action<WorkerProcessBuilder> getWorkerConfigurationAction() {
    return null;
  }

  private static class TestClassProcessorFactoryImpl implements WorkerTestClassProcessorFactory, Serializable {
    private final File testReportDir;
    private final ScalatestSpec spec;

    public TestClassProcessorFactoryImpl(File testReportDir, ScalatestSpec spec) {
      this.testReportDir = testReportDir;
      this.spec = spec;
    }

    @Override
    public TestClassProcessor create(ServiceRegistry serviceRegistry) {
      return new ScalatestTestClassProcessor(spec, testReportDir);
    }
  }
}
