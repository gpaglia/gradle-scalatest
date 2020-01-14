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
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.greaterThan

class JacocoTestActionIntegrationTest {
    private static final String RESOURCE = 'jacoco'

    @TempDir
    protected Path projectDir

    @BeforeEach
    void setup() throws Exception {
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
    void testReportsAreProduced() throws Exception {
        BuildResult br = setupBuild(projectDir.toFile())
            .withArguments('clean', 'test', 'jacocoTestReport')
            .build()
        assertThat(projectDir.resolve('build/reports/jacoco/test/html'), isReport)
        assertThat(projectDir.resolve('build/reports/tests/test'), isReport)
        assertThat(projectDir.resolve('build/test-results/test/TEST-HelloSpec.xml'), isReadableFile)
    }

    private static GradleRunner setupBuild(File projectRoot) {
        return GradleRunner
                .create()
                .withProjectDir(projectRoot)
                .withPluginClasspath()
                .forwardOutput()
    }

    private TypeSafeMatcher<Path> isReport = new TypeSafeMatcher<Path>() {
        @Override
        protected boolean matchesSafely(Path file) {
            return Files.isDirectory(file) && Files.isRegularFile(file.resolve('index.html'))
        }

        @Override
        void describeTo(Description description) {
            description.appendText('a directory containing index.html')
        }
    }

    private TypeSafeMatcher<Path> isReadableFile = new TypeSafeMatcher<Path>() {
        @Override
        protected boolean matchesSafely(Path file) {
            return Files.isRegularFile(file) && Files.exists(file) && Files.isReadable(file)
        }

        @Override
        void describeTo(Description description) {
            description.appendText('a readable file')
        }
    }


}
