package com.gpaglia.scalatest

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.configuration.ConsoleOutput
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.*
import org.junit.jupiter.api.Test

import static ScalaTestAntAction.color
import static com.gpaglia.scalatest.ScalaTestAntAction.*
import static com.gpaglia.scalatest.ScalaTestHelper.getHelper
import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class ScalaTestAntActionTest {


    private static Project testProject() {
        Project project = ProjectBuilder.builder().build()
        project.plugins.apply(ScalaTestPlugin)
        project
    }

    private static org.gradle.api.tasks.testing.Test testTask() {
        testProject().tasks.test as org.gradle.api.tasks.testing.Test
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
    void plainOutputIsWithoutColour() {
        Task test = testTask()
        test.getProject().getGradle().startParameter.setConsoleOutput(ConsoleOutput.Plain)
        assertThat(color(test), is('W'.toString()))
    }

    @Test
    void richOutput() {
        Task test = testTask()
        test.getProject().getGradle().startParameter.setConsoleOutput(ConsoleOutput.Rich)
        assertThat(color(test), is(emptyString()))
    }

    @Test
    void autoOutputRetainsColour() {
        Task test = testTask()
        test.getProject().getGradle().startParameter.setConsoleOutput(ConsoleOutput.Auto)
        assertThat(color(test), is(emptyString()))
    }

    @Test
    void testDefaultLogging() throws Exception {
        Task test = testTask()
        assertThat(test.testLogging.events, is(TestLogEvent.values() as Set))
        assertThat(reportingConfig(test), hasOutput('D'))
    }

    @Test
    void fullStackTraces() throws Exception {
        Task test = testTask()
        test.testLogging.exceptionFormat = TestExceptionFormat.FULL
        assertThat(reportingConfig(test), hasOutput('DF'))
    }

    @Test
    void shortStackTraces() throws Exception {
        Task test = testTask()
        test.testLogging.exceptionFormat = TestExceptionFormat.SHORT
        assertThat(reportingConfig(test), hasOutput('DS'))
    }

    @Test
    void maxHeapSizeIsAdded() throws Exception {
        Task test = testTask()
        String size = '123m'
        test.maxHeapSize = size
        assertThat(test.getAllJvmArgs(), hasItem("-Xmx$size".toString()))
    }

    @Test
    void minHeapSizeIsAdded() throws Exception {
        Task test = testTask()
        String size = '123m'
        test.minHeapSize = size
        assertThat(test.getAllJvmArgs(), hasItem("-Xms$size".toString()))
    }

    @Test
    void jvmArgIsAdded() throws Exception {
        String permSize = '-XX:MaxPermSize=256m'
        Task test = testTask().jvmArgs(permSize)
        assertThat(test.getAllJvmArgs(), hasItem(permSize))
    }

    @Test
    void sysPropIsAdded() throws Exception {
        Task test = testTask()
        test.systemProperties.put('bob', 'rita')
//        assertThat(getConfigs(test), hasEntry(is('bob'), is('rita')))
        //noinspection SpellCheckingInspection
        assertThat(test.getAllJvmArgs(), hasItem('-Dbob=rita'))
    }

    @Test
    void parallelDefaultsToProcessorCount() throws Exception {
        Task test = testTask()
        int processors = Runtime.runtime.availableProcessors()
        assertThat(getParallelForks(test), is(processors))
    }

    @Test
    void parallelSupportsConfiguration() throws Exception {
        Task test = testTask()
        int forks = Runtime.runtime.availableProcessors() + 1
        test.maxParallelForks = forks
        assertThat(getParallelForks(test), is(forks))
    }

    @Test
    void noIncludeTagsAreSpecifiedByDefault() throws Exception {
        Task test = testTask()
        assertThat(getHelper(test).includeTags, is(empty()))
    }

    @Test
    void noExcludeTagsAreSpecifiedByDefault() throws Exception {
        Task test = testTask()
        assertThat(getHelper(test).excludeTags, is(empty()))
    }
/*
    private static Matcher<List<String>> hasOption(String option, String required) {
        return new TypeSafeMatcher<List<String>>() {
            @Override
            protected boolean matchesSafely(List<String> strings) {
                def optionLocations = strings.findIndexValues { it == option }
                def optionValues = optionLocations.grep { locationOfOption ->
                    def optionValue = strings.get((locationOfOption + 1) as Integer)
                    required.equals(optionValue)
                }
                return optionValues.size() == 1
            }

            @Override
            void describeTo(Description description) {
                description.appendText("a list containing $option followed by $required")
            }
        }
    }
*/
    @Test
    void includesAreAddedAsTags() throws Exception {
        Task test = testTask()
        test.tags.include('bob', 'rita')
        assertThat(getHelper(test).includeTags, containsInAnyOrder('bob', 'rita'))
    }

    @Test
    void excludesAreAddedAsTags() throws Exception {
        Task test = testTask()
        test.tags.exclude('jane', 'sue')
        assertThat(getHelper(test).excludeTags, containsInAnyOrder('jane', 'sue'))
    }

    @Test
    void uppercaseClassesAreTranslatedToSuffixes() throws Exception {
        Task test = testTask()
        String[] tests = ['MyTest', 'MySpec', 'MySuite', 'MySpecificObj']
        test.filter.setIncludePatterns(tests)
        assertThat(getHelper(test).suffixes, containsInAnyOrder(tests))
    }

    @Test
    void filtersAreTranslatedToTestsFull() throws Exception {
        Task test = testTask()
        test.filter.setIncludePatterns('popped', 'weasel')
        assertThat(getHelper(test).testsFull, containsInAnyOrder('popped', 'weasel'))
    }

    @Test
    void filtersWithWildcardAreTranslatedToTestsSubstring() throws Exception {
        Task test = testTask()
        test.filter.setIncludePatterns('*popped', 'weasel*')
        assertThat(getHelper(test).testsSubstring, containsInAnyOrder('popped', 'weasel'))
    }

    @Test
    void filtersWithDotsAreTranslatedToPackagesMember() throws Exception {
        Task test = testTask()
        test.filter.setIncludePatterns('alpha.beta.gamma', 'com.example.test')
        assertThat(getHelper(test).packagesMember, containsInAnyOrder('alpha.beta.gamma', 'com.example.test'))
    }

    @Test
    void filtersWithDotsAndWIldcardsAreTranslatedToPackageWildcard() throws Exception {
        Task test = testTask()
        test.filter.setIncludePatterns('alpha.beta.gamma*', 'com.example.test*')
        assertThat(getHelper(test).packagesWildcard, containsInAnyOrder('alpha.beta.gamma', 'com.example.test'))
    }

    @Test
    void filtersStartingUppercaseAreTranslatedToSuffixesAndTests() throws Exception {
        Task test = testTask()
        test.filter.setIncludePatterns('MySuite.method1.method2', 'AnotherSuite.method3')
        assertThat(getHelper(test).suffixes, containsInAnyOrder('MySuite', 'AnotherSuite'))
        assertThat(getHelper(test).testsFull, containsInAnyOrder("method1.method2", "method3"))
    }

    @Test
    void filtersStartingUppercaseAreTranslatedToSuffixesAndTestsSubstring() throws Exception {
        Task test = testTask()
        test.filter.setIncludePatterns('MySuite.method1.method2*', 'AnotherSuite.method3*')
        assertThat(getHelper(test).suffixes, containsInAnyOrder('MySuite', 'AnotherSuite'))
        assertThat(getHelper(test).testsSubstring, containsInAnyOrder("method2", "method3"))
        assertThat(getHelper(test).testsFull, containsInAnyOrder("method1"))
    }

    private static void checkSuiteTranslation(String message, Closure<Task> task, List<String> suites) {
        Task test = testTask()
        test.configure(task)
        assertThat(message, getHelper(test).suites, containsInAnyOrder(suites.toArray(String [])))
    }


    @Test
    void suiteIsTranslatedToSuite() throws Exception {
        checkSuiteTranslation('simple suite', { it.suite 'hello.World' }, ['hello.World'])
        checkSuiteTranslation('multiple calls', { it.suite 'a'; it.suite 'b' }, ['a', 'b'])
    }

    @Test
    void suitesAreTranslatedToSuite() throws Exception {
        checkSuiteTranslation('list of suites', { it.suites 'a', 'b' }, ['a', 'b'])
    }

    @Test
    void distinctSuitesAreRun() throws Exception {
        Task test = testTask()
        test.suites 'a', 'a'
        assertThat(getHelper(test).suites, hasSize(1))
    }

    @Test
    void testKnockout() throws Exception {
        assertThat(other([TestLogEvent.FAILED] as Set),
                not(hasItem(TestLogEvent.FAILED)))

        assertThat(other([TestLogEvent.PASSED, TestLogEvent.FAILED] as Set),
                both(not(hasItem(TestLogEvent.PASSED))) & not(hasItem(TestLogEvent.PASSED)))
    }

    /*
    @Test
    void testNoEventsRemovesStdOut() throws Exception {
        Task test = testTask()
        test.testLogging.events = []
        def args = commandLine(test)
        def stdoutReporter = args.findAll { it.startsWith('-o') }
        assertThat(stdoutReporter.size(), equalTo(0))
    }
    */

    @Test
    void failedOnlyReporting() throws Exception {
        Task test = testTask()
        test.testLogging.events = [TestLogEvent.FAILED]
        //noinspection SpellCheckingInspection
        assertThat(reportingConfig(test), hasOutput('CDEHLMNOPQRSX'))

    }

    @Test
    void configString() throws Exception {
        Task test = testTask()
        test.config 'a', 'b'
        assertThat(getHelper(test).configs, hasEntry(is('a'), is('b')))
    }

    @Test
    void configNumber() throws Exception {
        Task test = testTask()
        test.config 'a', 1
        assertThat(getHelper(test).configs, hasEntry(is('a'), is(1)))
    }

    @Test
    void testConfigMap() throws Exception {
        Task test = testTask()
        test.configMap([a: 'b', c: 1])

        final matcher = both(
                hasEntry(equalTo('a'), equalTo((Object) 'b'))) & hasEntry(equalTo('c'), equalTo((Object) 1))

        assertThat(
            getHelper(test).configs,
            matcher
        )
    }


}