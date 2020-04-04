package com.gpaglia.gradle.scalatest.framework.processor


import com.gpaglia.gradle.scalatest.Scalatest
import com.gpaglia.gradle.scalatest.framework.ScalatestDetector
import com.gpaglia.gradle.scalatest.framework.ScalatestSuiteRunInfo
import com.gpaglia.gradle.scalatest.framework.ScalatestTestFramework
import com.gpaglia.gradle.scalatest.framework.processors.ScalatestPatternMatchTestClassProcessor
import com.gpaglia.gradle.scalatest.testutils.TestUtils
import com.gpaglia.scalatest.framework.api.Framework
import com.gpaglia.scalatest.framework.fixtures.SampleSuitesFixtures
import com.gpaglia.scalatest.framework.impl.FrameworkImpl
import org.gradle.api.Project
import org.gradle.api.internal.tasks.testing.TestClassProcessor
import org.gradle.api.internal.tasks.testing.TestClassRunInfo
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.nio.file.Path
import java.util.stream.Collectors

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.mockito.Mockito.*

class ScalatestPatternMatchTestClassProcessorTest {

    @Test
    void nullPatternTest(@TempDir Path tempDir) {
        final Path samplesDir = TestUtils.setupSamples(tempDir, this.getClass().getClassLoader())
        final Framework fw = new FrameworkImpl(this.getClass().getClassLoader())

        // mocks
        final ArgumentCaptor<TestClassRunInfo> infoCaptor = ArgumentCaptor.forClass(TestClassRunInfo.class)
        final TestClassProcessor mockClassProcessor = mock(TestClassProcessor.class)
        final TestResultProcessor mockResultProcessor = mock(TestResultProcessor.class)

        final DefaultTestFilter filter = new DefaultTestFilter()

        final ScalatestPatternMatchTestClassProcessor proc =
                new ScalatestPatternMatchTestClassProcessor(filter, mockClassProcessor)

        final Set<ScalatestSuiteRunInfo> infos = fw
                .newSelector()
                .discoverCandidateSuites(samplesDir.toUri(), {it -> true}, {it -> true})
                .stream()
                .map {pair -> new ScalatestSuiteRunInfo(pair.getLeft(), pair.getRight()) }
                .collect(Collectors.toSet())

        System.out.printf("Discovered %d suites\n", infos.size())

        proc.startProcessing(mockResultProcessor)

        infos.each { proc.processTestClass(it) }

        verify(mockClassProcessor, times(1)).startProcessing(Mockito.eq(mockResultProcessor))
        verify(mockClassProcessor, times(SampleSuitesFixtures.EXPECTED_ALL_SUITES.length)).processTestClass(infoCaptor.capture())
        final List<TestClassRunInfo> results = infoCaptor.getAllValues()

        results.forEach { ri ->
            assertThat(ri, instanceOf(ScalatestSuiteRunInfo.class))
            final ScalatestSuiteRunInfo stri = (ScalatestSuiteRunInfo) ri
            assertThat(stri.getFingerprint(), notNullValue())
        }

        Set<String> names = results
                .stream()
                .map { it.testClassName }
                .collect(Collectors.toSet())

        assertThat(names, containsInAnyOrder(SampleSuitesFixtures.EXPECTED_ALL_SUITES))
    }

    @Test
    @Disabled
    void testDetector(@TempDir Path tempDir) {
        final Path samplesDir = TestUtils.setupSamples(tempDir. this.getClass().getClassLoader())

        final Project project = TestUtils.testProject()
        final Scalatest stTask = TestUtils.testTask(project)

        assertThat(stTask, notNullValue())
        assertThat(stTask, instanceOf(Scalatest.class))

        // configure classpath and classesDirs
        stTask.classpath = project.files(samplesDir)
        stTask.testClassesDirs = project.files(samplesDir)

        stTask.useScalatest()

        final ScalatestTestFramework fw = stTask.getTestFramework()
        final ScalatestDetector detector = fw.detector

    }
}
