package com.gpaglia.gradle.scalatest.framework;

import com.gpaglia.scalatest.framework.api.Framework;
import com.gpaglia.scalatest.framework.api.FrameworkBuilder;
import com.gpaglia.scalatest.framework.impl.FrameworkFactoryImpl;
import org.gradle.api.Action;
import org.gradle.api.internal.initialization.loadercache.ClassLoaderCache;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestFramework;
import org.gradle.api.internal.tasks.testing.WorkerTestClassProcessorFactory;
import org.gradle.api.internal.tasks.testing.detection.TestFrameworkDetector;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.tasks.testing.AbstractScalatest;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.actor.ActorFactory;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.time.Clock;
import org.gradle.process.internal.worker.WorkerProcessBuilder;

import java.io.Serializable;

/**
 * Scalatest specific test framework.
 * Partially copied from gradle NG test framework implementation
 *
 * @see <a href="https://github.com/gradle/gradle/blob/master/subprojects/testing-jvm/src/main/java/org/gradle/api/internal/tasks/testing/testng/TestNGTestFramework.java">TtestNGTestFramework</a>
 */
public class ScalatestTestFramework implements TestFramework {
  private final AbstractScalatest testTask;
  private final DefaultTestFilter filter;
  private final ScalatestOptions options;
  private final ScalatestDetector detector;
  private final ScalatestClassLoaderFactory classLoaderFactory;
  private final Framework framework;


  public ScalatestTestFramework(final AbstractScalatest testTask, DefaultTestFilter filter, Instantiator instantiator, ClassLoaderCache classLoaderCache) {
    this.testTask = testTask;
    this.filter = filter;
    options = instantiator.newInstance(ScalatestOptions.class, testTask.getProject().getProjectDir());
    classLoaderFactory = new ScalatestClassLoaderFactory(classLoaderCache, testTask);
    framework = FrameworkBuilder
      .builder()
      .withClassLoader(classLoaderFactory.create())
      .build();
    detector = new ScalatestDetector(framework.newSelector());
  }
  @Override
  public TestFrameworkDetector getDetector() {
    return detector;
  }

  @Override
  public ScalatestOptions getOptions() {
    return options;
  }

  @Override
  public WorkerTestClassProcessorFactory getProcessorFactory() {
    //TODO: TBC
    final ScalatestSpec spec = new ScalatestSpec(filter, options);
    return new TestClassProcessorFactoryImpl(spec);
  }

  @Override
  public Action<WorkerProcessBuilder> getWorkerConfigurationAction() {
    return null;
  }

  private static class TestClassProcessorFactoryImpl implements WorkerTestClassProcessorFactory, Serializable {
    private final ScalatestSpec spec;

    public TestClassProcessorFactoryImpl(ScalatestSpec spec) {
      this.spec = spec;
    }

    @Override
    public TestClassProcessor create(ServiceRegistry serviceRegistry) {
      try {
        IdGenerator idGenerator = serviceRegistry.get(IdGenerator.class);
        Clock clock = serviceRegistry.get(Clock.class);
        ActorFactory actorFactory = serviceRegistry.get(ActorFactory.class);
        return new ScalatestTestClassProcessor(spec, idGenerator, actorFactory, clock);
      } catch (Exception e) {
        throw UncheckedException.throwAsUncheckedException(e);
      }
    }
  }
}
