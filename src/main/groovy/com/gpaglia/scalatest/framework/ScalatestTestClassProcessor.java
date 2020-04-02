package com.gpaglia.scalatest.framework;

import org.gradle.api.Action;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;
import org.gradle.api.internal.tasks.testing.TestResultProcessor;
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestExecutionListener;
import org.gradle.internal.actor.Actor;
import org.gradle.internal.actor.ActorFactory;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.impldep.org.junit.platform.launcher.Launcher;
import org.gradle.internal.impldep.org.junit.platform.launcher.core.LauncherFactory;
import org.gradle.internal.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ScalatestTestClassProcessor implements TestClassProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ScalatestTestClassProcessor.class);

  private final ScalatestSpec spec;
  private final IdGenerator<?> idGenerator;
  private final Clock clock;
  private final ActorFactory actorFactory;
  private Actor resultProcessorActor;
  private Action<ScalatestSuiteRunInfo> executor;
  private ScalatestSuiteExecutor testClassExecutor;

  public ScalatestTestClassProcessor(
      final ScalatestSpec spec,
      final IdGenerator idGenerator,
      final ActorFactory actorFactory,
      final Clock clock)  {

    this.spec = spec;
    this.idGenerator = idGenerator;
    this.actorFactory = actorFactory;
    this.clock = clock;
  }

  @Override
  public void startProcessing(TestResultProcessor resultProcessor) {
    LOGGER.debug("Starting processing ...");
    TestResultProcessor resultProcessorChain = createResultProcessorChain(resultProcessor);
    resultProcessorActor = actorFactory.createBlockingActor(resultProcessorChain);
    executor = createTestExecutor(resultProcessorActor);
  }

  @Override
  public void processTestClass(TestClassRunInfo testClass) {
    LOGGER.debug("Processing test class {}", testClass.getTestClassName());

  }

  @Override
  public void stop() {
    LOGGER.debug("Stop requested...");
  }

  @Override
  public void stopNow() {
    LOGGER.debug("Stop-now requested...");
  }

  // for now, no-op
  private TestResultProcessor createResultProcessorChain(TestResultProcessor resultProcessor) {
    return resultProcessor;
  }

  private Action<ScalatestSuiteRunInfo> createTestExecutor(Actor resultProcessorActor) {
    TestResultProcessor threadSafeResultProcessor = resultProcessorActor.getProxy(TestResultProcessor.class);
    testClassExecutor = new ScalatestSuiteExecutor(threadSafeResultProcessor);
    return testClassExecutor;
  }

  private class ScalatestSuiteExecutor implements Action<ScalatestSuiteRunInfo> {
    private final List<Class<?>> testClasses = new ArrayList<>();
    private final TestResultProcessor resultProcessor;

    ScalatestSuiteExecutor(TestResultProcessor resultProcessor) {
      this.resultProcessor = resultProcessor;
    }

    @Override
    public void execute(@Nonnull ScalatestSuiteRunInfo suiteRunInfo) {

      // TODO: Implement this
      LOGGER.debug("Executing suite {}", suiteRunInfo);
    }
  }
}
