package com.gpaglia.scalatest

import com.gpaglia.scalatest.testutils.ResourceUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

import static java.util.stream.Collectors.toList
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.greaterThan
import static org.hamcrest.Matchers.is

class ModeIFunctionalTest {
    private static final String RESOURCE = 'mixed'

    @TempDir
    protected Path projectDir

    private Path testReport
    private Path scalaTestReport
    private Path integrationTestReport


    @BeforeEach
    void setup() throws Exception {
        testReport = projectDir.resolve('build/reports/tests/test/index.html')
        scalaTestReport = projectDir.resolve('build/reports/tests/scalatest/index.html')
        integrationTestReport = projectDir.resolve('build/reports/tests/integrationTest/index.html')
        ResourceUtils.copyResources(RESOURCE, projectDir)
    }

    @Test
    void testSetupIsDone() {
        List<String> files = Files.walk(projectDir).withCloseable {
            paths -> paths
                    .filter { Files.isRegularFile(it) }
                    .map { it.toString() }
                    .collect(toList())
        }

        assertThat(files.size(), greaterThan(0))
    }

    @Test
    void testDefaultIsToReplaceAllTestTasks() throws Exception {
        BuildResult br = setupBuild()
            .withArguments('clean', 'test', 'integrationTest')
            .build()

        assertThat(br.task(':test').outcome, is(SUCCESS))
        assertThat(br.task(':integrationTest').outcome, is(SUCCESS))

        assertThat(testReport, isScalaTestReport)
        assertThat(integrationTestReport, isScalaTestReport)
    }


    @Test
    void testAppendScalaTestTask() throws Exception {
        BuildResult br = setupBuild()
            .withArguments(setMode(ScalaTestPlugin.Mode.append), 'clean', 'test', 'integrationTest', 'scalatest')
            .build()

        assertThat(br.task(':test').outcome, is(SUCCESS))
        assertThat(br.task(':integrationTest').outcome, is(SUCCESS))
        assertThat(br.task(':scalatest').outcome, is(SUCCESS))

        assertThat(scalaTestReport, isScalaTestReport)
        assertThat(testReport, isJUnitReport)
        assertThat(integrationTestReport, isJUnitReport)
    }

    @Test
    void testReplaceTestTask() throws Exception {
        BuildResult br = setupBuild()
            .withArguments(setMode(ScalaTestPlugin.Mode.replaceOne), 'clean', 'test', 'integrationTest')
            .build()

        assertThat(br.task(':test').outcome, is(SUCCESS))
        assertThat(br.task(':integrationTest').outcome, is(SUCCESS))

        assertThat(testReport, isScalaTestReport)
        assertThat(integrationTestReport, isJUnitReport)
    }

    @Test
    void testReplaceAllTestTasks() throws Exception {
        BuildResult br = setupBuild()
            .withArguments(setMode(ScalaTestPlugin.Mode.replaceAll), 'clean', 'test', 'integrationTest')
            .build()

        assertThat(br.task(':test').outcome, is(SUCCESS))
        assertThat(br.task(':integrationTest').outcome, is(SUCCESS))

        assertThat(testReport, isScalaTestReport)
        assertThat(integrationTestReport, isScalaTestReport)
    }


    private GradleRunner setupBuild() {
        return GradleRunner
                .create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .forwardOutput()
    }

    private static String setMode(final ScalaTestPlugin.Mode mode) {
        return "-P${ScalaTestPlugin.MODE}=${mode}"
    }

    private static boolean contains(Path file, String string) {
        return  Files.exists(file) &&
                Files.isRegularFile(file) &&
                Files.isReadable(file) &&
                Files.readAllLines(file).grep { it.contains(string) }
    }

    private TypeSafeMatcher<Path> isJUnitReport = new TypeSafeMatcher<Path>() {
        @Override
        protected boolean matchesSafely(Path file) {
            return contains(file, 'http://www.gradle.org')
        }

        @Override
        void describeTo(Description description) {
            description.appendText('a file containing the gradle runner signature')
        }
    }

    private TypeSafeMatcher<Path> isScalaTestReport = new TypeSafeMatcher<Path>() {
        @Override
        protected boolean matchesSafely(Path file) {
            return contains(file, 'scalatest-report')
        }

        @Override
        void describeTo(Description description) {
            description.appendText('a file containing the scalatest signature')
        }
    }

}
