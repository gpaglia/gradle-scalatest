package com.gpaglia.gradle.scalatest

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
import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class CommandLineIntegrationTest {
    private static final String RESOURCE = 'cmdline'

    @TempDir
    protected Path projectDir

    private Path testReport

    @BeforeEach
    void setup() throws Exception {
        testReport = projectDir.resolve('build/test-results/test/TEST-MySpec.xml')
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
    void testSpecIsRunAndFails() throws Exception {
        BuildResult br = setupBuild('clean', 'test', '--tests', 'MySpec').buildAndFail()

        assertThat(br.task(':test').outcome, is(FAILED))
        assertThat(testReport, both(ran('bob')) & ran('rita') & ran('shouldFail'))
    }

    @Test
    void testSpecIsRun() throws Exception {
        BuildResult br = setupBuild('clean', 'test', '--tests', 'bob', '--tests', 'rita').build()

        assertThat(br.task(':test').outcome, is(SUCCESS))
        assertThat(testReport, allOf(ran('bob'), ran('rita')))
    }


    @Test
    void testOnlyOneTestIsRun() throws Exception {
        BuildResult br = setupBuild('clean', 'test', '--tests', 'bob').build()
        assertThat(br.task(':test').outcome, is(SUCCESS))
        assertThat(testReport, both(ran('bob')) & not(ran('rita')))
    }

    private GradleRunner setupBuild(String... args) {
        return GradleRunner
            .create()
            .withProjectDir(projectDir.toFile())
            .withArguments(args)
            .withPluginClasspath()
            .forwardOutput()
    }

    private static boolean fileContains(Path file, String string) {
        return Files.exists(file) &&
                Files.isReadable(file) &&
                Files.isRegularFile(file) &&
                Files.readAllLines(file).grep { it.contains(string) }
    }

    private static TypeSafeMatcher<Path> ran(String testName) {
        return new TypeSafeMatcher<Path>() {
            @Override
            protected boolean matchesSafely(Path file) {
                return fileContains(file, "testcase name=\"$testName\" classname=\"MySpec\"")
            }

            @Override
            void describeTo(Description description) {
                description.appendText("a file containing a test result for $testName")
            }
        }

    }
}
