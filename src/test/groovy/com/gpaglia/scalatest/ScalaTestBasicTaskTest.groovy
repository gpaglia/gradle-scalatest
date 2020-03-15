package com.gpaglia.scalatest

import com.gpaglia.scalatest.framework.ScalatestOptions
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.testing.TestFrameworkOptions
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.*
import org.junit.jupiter.api.Test

import static org.hamcrest.CoreMatchers.instanceOf
import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder

class ScalaTestBasicTaskTest {


    private static Project testProject() {
        Project project = ProjectBuilder.builder().build()
        project.extensions.add(ScalaTestPlugin.MODE, ScalaTestPlugin.Mode.prototype.toString())
        project.plugins.apply(ScalaTestPlugin)
        project
    }

    private static ScalatestTask testTask(Project project) {
        project.tasks.scalatestproto as ScalatestTask
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
    void taskExists() {
        final Project project = testProject()
        Task test = testTask(project)

        assertThat(test, notNullValue())
        assertThat(test, instanceOf(ScalatestTask.class))

        // configure test
        ScalatestTask stTask = (ScalatestTask) test;
        Action<? super ScalatestOptions> action = new Action<ScalatestOptions>() {

            @Override
            void execute(ScalatestOptions scalatestOptions) {
                scalatestOptions
                    .includeTags("good_1", "good_2")
                    .excludeTags("bad_1", "bad_2")
            }
        }

        stTask.useScalatest(action)
        final TestFrameworkOptions gotOptions = test.getOptions()
        assertThat(gotOptions, notNullValue())
        assertThat(gotOptions, instanceOf(ScalatestOptions.class))
        ScalatestOptions stOptions = (ScalatestOptions) test.getOptions()
        assertThat(stOptions.getIncludeTags(), containsInAnyOrder("good_1", "good_2"))
        assertThat(stOptions.getExcludeTags(), containsInAnyOrder("bad_1", "bad_2"))

    }


}