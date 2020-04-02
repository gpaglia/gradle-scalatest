package com.gpaglia.scalatest

import com.gpaglia.scalatest.framework.ScalatestOptions
import com.gpaglia.scalatest.framework.fixtures.SampleSuitesFixtures
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.testing.AbstractScalatest
import org.gradle.api.tasks.testing.TestFrameworkOptions
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import java.util.stream.Stream

import static org.hamcrest.CoreMatchers.instanceOf
import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.hasSize

class ScalaTestBasicTaskTest {


    private static Project testProject() {
        Project project = ProjectBuilder.builder().build()
        project.extensions.add(ScalaTestPlugin.MODE, ScalaTestPlugin.Mode.prototype.toString())
        project.plugins.apply(ScalaTestPlugin)
        project
    }

    private static Scalatest testTask(Project project) {
        project.tasks.scalatest as Scalatest
    }

    /*
    private static List<String> commandLine(org.gradle.api.tasks.testing.Test task) {
        JavaExecAction action = ScalaTestAction.makeAction(task)
        action.getCommandLine()
    }
    */

    private static Matcher<String> hasOutput(String required) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String string) {
                return string.contains(required)
            }

            @Override
            void describeTo(Description description) {
                description.appendText("a string containing [...$required...]")
            }
        }
    }
/*
    private static Map<String, Object> environment(org.gradle.api.tasks.testing.Test task) {
        JavaExecAction action = ScalaTestAction.makeAction(task)
        action.getEnvironment()
    }
*/
    /*
    @Test
    void workingDirectoryIsHonoured() throws Exception {
        Task test = testTask()
        test.workingDir = '/tmp'

JavaExecAction action = ScalaTestAction.makeAction(test)
        assertThat(action.workingDir, equalTo(new File('/tmp')))
    }

    @Test
    void environmentVariableIsCopied() {
        Task test = testTask()
        test.environment.put('a', 'b')
        assertThat(environment(test).get('a') as String, equalTo('b'))
    }
    */

    @Test
    void taskExistsAndIsConfigured() {
        final Project project = testProject()
        final Scalatest stTask = testTask(project)

        assertThat(stTask, notNullValue())
        assertThat(stTask, instanceOf(Scalatest.class))

        // configure test
        Action<? super ScalatestOptions> action = new Action<ScalatestOptions>() {

            @Override
            void execute(ScalatestOptions scalatestOptions) {
                scalatestOptions
                    .includeTags("good_1", "good_2")
                    .excludeTags("bad_1", "bad_2")
            }
        }

        stTask.useScalatest(action)
        final TestFrameworkOptions gotOptions = stTask.getOptions()
        assertThat(gotOptions, notNullValue())
        assertThat(gotOptions, instanceOf(ScalatestOptions.class))
        ScalatestOptions stOptions = stTask.getOptions()
        assertThat(stOptions.getIncludeTags(), containsInAnyOrder("good_1", "good_2"))
        assertThat(stOptions.getExcludeTags(), containsInAnyOrder("bad_1", "bad_2"))

    }

    @Test
    void testSetupWithSamples(@TempDir Path tempDir) {
        final Path samplesDir = Files.createDirectories(tempDir.resolve("samples"))
        final SampleSuitesFixtures fixtures = new SampleSuitesFixtures()
        fixtures.setupCustom(this.getClass().getClassLoader())
        fixtures.extractSamples(samplesDir)

        Files.walk(samplesDir).withCloseable { Stream<Path> pathStream ->
            final List<String> classes = pathStream
                .map {path -> samplesDir.relativize(path) }
                .map {path -> path.toString() }
                .filter {name -> name.endsWith(".class") }
                .map {name -> name.substring(0, name.length() - ".class".length()).replace(File.separator, ".") }
                .collect( Collectors.toList())

            assertThat(classes, containsInAnyOrder(SampleSuitesFixtures.ALL_DEFINED_SUITES))
        }
    }

    @Test
    void testIncludes(@TempDir Path tempDir) {
        final Path samplesDir = Files.createDirectories(tempDir.resolve("samples"))
        final SampleSuitesFixtures fixtures = new SampleSuitesFixtures()
        fixtures.setupCustom(this.getClass().getClassLoader())
        fixtures.extractSamples(samplesDir)

        final Project project = testProject()
        final Scalatest stTask = testTask(project)

        assertThat(stTask, notNullValue())
        assertThat(stTask, instanceOf(Scalatest.class))

        // configure classpath and classesDirs
        stTask.classpath = project.files(samplesDir)
        stTask.testClassesDirs = project.files(samplesDir)

        // configure include
        stTask.include('**/FunSuiteSample.class')

        FileTree candidates = stTask.getCandidateClassFiles()

        Set<File> files = candidates.files

        assertThat(files, hasSize(1))
        File found = files.iterator().next()
        assertThat(found.toPath().getFileName().toString(), is("FunSuiteSample.class"))
    }
}