package com.gpaglia.scalatest

import groovy.transform.PackageScope
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.DefaultLogger
import org.apache.tools.ant.Project
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.logging.configuration.ConsoleOutput
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.internal.UncheckedException

class ScalaTestAntAction implements Action<Test> {

    private static final ANT_TASK_CLASSNAME = 'org.scalatest.tools.ScalaTestAntTask'

    /*
    @ToString
    @PackageScope
    static class Helper {
        List<String> suffixes = new ArrayList<>()
        List<String> suites = new ArrayList<>()
        List<String> tests = new ArrayList<>()
        List<String> includeTags = new ArrayList<>()
        List<String> excludeTags = new ArrayList<>()


        @Override
        String toString() {
            return "Helper{" +
                    "suffixes=" + suffixes +
                    ", suites=" + suites +
                    ", tests=" + tests +
                    ", includeTags=" + includeTags +
                    ", excludeTags=" + excludeTags +
                    '}'
        }
    }
    */

    @Override
    void execute(Test t) {
        try {
            executeAntActionLoggingOutput(t, false)
        } catch (BuildException ex) {
            handleTestFailures(t, ex)
        }

    }

    private static void handleTestFailures(Test t, Exception ex) {
        String message = 'There were failing tests (' + ex.getMessage() + ')'
        def htmlReport = t.reports.html
        if (htmlReport.isEnabled()) {
            message = message.concat(". See the report at: ").concat(url(htmlReport))
        } else {
            def junitXmlReport = t.reports.junitXml
            if (junitXmlReport.isEnabled()) {
                message = message.concat(". See the results at: ").concat(url(junitXmlReport))
            }
        }
        if (t.ignoreFailures) {
            t.logger.warn(message)
        }
        else {
            throw new GradleException(message)
        }
    }

    private static String url(DirectoryReport report) {
        try {
            return new URI("file", "", report.getEntryPoint().toURI().getPath(), null, null).toString()
        } catch (URISyntaxException e) {
            throw UncheckedException.throwAsUncheckedException(e)
        }
    }

    @PackageScope
    static void executeAntActionLoggingOutput(Test t, boolean doLog, String antTaskClassName = ANT_TASK_CLASSNAME) {
        final ant = t.getAnt()
        final buffer = new ByteArrayOutputStream()
        new PrintStream(buffer, true, "UTF-8").withCloseable { captureStream ->
            def listener = new DefaultLogger(
                    errorPrintStream: captureStream,
                    outputPrintStream: captureStream,
                    messageOutputLevel: Project.MSG_INFO
            )
            ant.project.addBuildListener(listener)
            try {
                executeAntAction(ant, t, antTaskClassName)
            } finally {
                ant.project.removeBuildListener(listener)
            }
            if (doLog) {
                buffer.toString().eachLine { line -> t.logger.lifecycle('[GP] ' + line) }
            }

            void
        }
    }

    @PackageScope
    static void executeAntAction(Object ant, Test t, String antTaskClassName = ANT_TASK_CLASSNAME) {
        // final ant = t.getAnt()

        final nt = getParallelForks(t)
        // System.out.println('***** Num threads: ' + nt.toString())
        // System.out.println('***** jvm args: ' + t.getAllJvmArgs().toString())

        ant.lifecycleLogLevel = 'INFO'

        ant.taskdef(
            name: 'scalatest',
            classname: antTaskClassName, // 'org.scalatest.tools.ScalaTestAntTask',
            classpath: t.getClasspath().asPath // test.getRuntimeClasspath().asPath
        )

        def params = [
                // runpath: t.getProject().getTasks().getByName('compileTestScala').getOutputs().files.asPath,
                fork: true,
                haltonfailure: true,
                parallel: true,
                sortSuites: true
        ]

        if (nt > 0) {
            [ numthreads: nt ]
        }

        ScalaTestHelper helper = ScalaTestHelper.getHelper(t)

        if (helper.suffixes.size() > 0) {
            params << [ suffixes: helper.suffixes.join('|') ]
        }

        t.logger.debug("params: ${params.toString()}")
        t.logger.debug("helper: ${helper.toString()}")

        ant.scalatest(params) {
            for(String arg: t.getAllJvmArgs()) {
                jvmarg(value: arg)
            }
            runpath {
                for (String rp in getRunPaths(t)) {
                    path(location: rp)
                }
            }
            if (t.testLogging.events?.size() > 0) {
                reporter(
                        type: 'stdout',
                        config: reportingConfig(t)
                )
            }
            if (getJunitXmlPath(t) != null) {
                reporter(
                    type: 'junitxml',
                    directory: getJunitXmlPath(t)
                )
            }

            if (getHtmlPath(t) != null) {
                reporter(
                    type: 'html',
                    directory: getHtmlPath(t)
                )

            }

            if (helper.includeTags.size() > 0) {
                tagsToInclude(helper.includeTags.join(' '))
            }
            if (helper.excludeTags.size() > 0) {
                tagsToExclude(helper.excludeTags.join(' '))
            }
            for (String sut in helper.suites) {
                suite(classname: sut)
            }
            for (String tst in helper.testsFull) {
                test(name: tst)
            }
            for (String tst in helper.testSubstrings) {
                test(substring: tst)
            }
            for (String pkg in helper.packagesMember) {
                membersonly(package: pkg)
            }
            for (String pkg in helper.packagesWildcard) {
                wildcard(package: pkg)
            }
            for (Map.Entry<String, ?> entry in helper.configs.entrySet()) {
                config(name: entry.key, value: entry.value)
            }
        }
    }

    static Set<TestLogEvent> other(Set<TestLogEvent> required) {
        def all = TestLogEvent.values() as Set
        return (required + all) - required.intersect(all)
    }

    static String drop(TestLogEvent event/*, int granularity*/) {
        switch (event) {
            case TestLogEvent.STARTED: return 'NHP' // test and suite and scope
            case TestLogEvent.PASSED: return 'CLQ' // test and suite and scope
            case TestLogEvent.SKIPPED: return 'XER' // ignored and pending and scope
            case TestLogEvent.FAILED: return ''
            case TestLogEvent.STANDARD_OUT:
            case TestLogEvent.STANDARD_ERROR: return 'OM' // info provided, markup provided
            default: return ''
        }
    }

    static String dropped(Test t) {
        return other(t.testLogging.events).collect { drop(it /*, t.testLogging.displayGranularity*/) }.join('')
    }

    static String color(Test t) {
        if (t.getProject().getGradle().getStartParameter().getConsoleOutput() == ConsoleOutput.Plain) {
            return 'W'
        } else {
            return ''
        }
    }

    static String exceptions(Test t) {
        if (t.testLogging.showExceptions) {
            switch (t.testLogging.exceptionFormat) {
                case TestExceptionFormat.FULL:
                    return 'F'
                case TestExceptionFormat.SHORT:
                    return 'S'
            }
        }
        return ''
    }

    static String durations = 'D'

    @PackageScope
    static String reportingConfig(Test t) {
        return ((dropped(t) + color(t) + exceptions(t) + durations) as List).unique().sort().join('')
    }

    @PackageScope
    static List<String> getRunPaths(Test t) {
        List<String> runPaths = new ArrayList<String>()

        t.getTestClassesDirs().each {
            runPaths.add(it.absolutePath)
        }
        t.logger.debug('RunPaths: ' + runPaths.toString())

        return runPaths
    }

    @PackageScope
    static int getParallelForks(Test t) {
        return t.maxParallelForks
    }

    /*
    @PackageScope
    static Helper getHelper(Test t) {
        Helper helper = new Helper()
        def isSuffix =  { String s ->
            s.endsWith('Test') || s.endsWith('Spec') || s.endsWith('Suite')
        }

        if (t.filter.hasProperty('commandLineIncludePatterns')) {
            t.filter.commandLineIncludePatterns.each {
                String s = it.toString()
                if (isSuffix(s)) {
                    helper.suffixes.add(s)
                } else {
                    helper.tests.add(s)
                }
            }
        }
        t.filter.includePatterns.each {
            if (isSuffix(it)) {
                helper.suffixes.add(it)
            } else {
                helper.tests.add(it)
            }
        }

        def suites = t.extensions.findByName(SUITES) as List<String>
        suites?.toSet()?.each {
            helper.suites.add(it)
        }

        def tags = t.extensions.findByName(TAGS) as PatternSet
        if (tags) {
            tags.includes.each {
                helper.includeTags.add(it)
            }
            tags.excludes.each {
                helper.excludeTags.add(it)
            }
        }

        return helper
    }
    */

    /*
    @PackageScope
    static Map<String, ?> getConfigs(Test t) {
        Map<String, ?> configs = new HashMap<String, ?>()

        def configMap = t.extensions.findByName(CONFIG) as Map<String, ?>
        configMap?.entrySet()?.each { entry ->
            configs.put(entry.key, entry.value)
        }

        return configs
    }
    */

    @PackageScope
    static String getJunitXmlPath(Test t) {
        if (t.reports.getJunitXml().isEnabled()){
            return t.reports.getJunitXml().getEntryPoint().getAbsolutePath()
        } else {
            return null
        }
    }

    @PackageScope
    static String getHtmlPath(Test t) {
        if (t.reports.getHtml().isEnabled()){
            def dest = t.reports.getHtml().getDestination()
            dest.mkdirs()
            return dest.getAbsolutePath()
        } else {
            return null
        }
    }

    /*
    private static Iterable<String> getArgs(Test t) {
    List<String> args = new ArrayList<String>()

     // this represents similar behaviour to the existing JUnit test action
     if (t.testLogging.events) {
         args.add(reporting(t))
     }
     if (t.maxParallelForks == 0) {
         args.add('-PS')
     } else {
         args.add("-PS${t.maxParallelForks}".toString())
     }
     */
        /*
        if (t.hasProperty("testClassesDirs")) {
            t.getTestClassesDirs().each {
                args.add('-R')
                args.add(it.absolutePath.replace(' ', '\\ '))
            }
        } else {
            args.add('-R')
            args.add(t.getTestClassesDir().absolutePath.replace(' ', '\\ '))
        }
        */
        /*
        def appendTestPattern = { String it ->
            if (it.endsWith("Test") || it.endsWith("Spec") || it.endsWith("Suite")) {
                args.add('-q')
            } else {
                args.add('-z')
            }
            args.add(it)
        }
        if (t.filter.hasProperty('commandLineIncludePatterns')) {
            t.filter.commandLineIncludePatterns.each { appendTestPattern(it) }
        }
        t.filter.includePatterns.each { appendTestPattern(it) }
        */
        /*
        if (t.reports.getJunitXml().isEnabled()){
            args.add('-u')
            args.add(t.reports.getJunitXml().getEntryPoint().getAbsolutePath())
        }
        if (t.reports.getHtml().isEnabled()){
            args.add('-h')
            def dest = t.reports.getHtml().getDestination()
            dest.mkdirs()
            args.add(dest.getAbsolutePath())
        }
        */
        /*
        def tags = t.extensions.findByName(TAGS) as PatternSet
        if (tags) {
            tags.includes.each {
                args.add('-n')
                args.add(it)
            }
            tags.excludes.each {
                args.add('-l')
                args.add(it)
            }
        }
        */
        /*
        def suites = t.extensions.findByName(SUITES) as List<String>
        suites?.toSet()?.each {
            args.add('-s')
            args.add(it)
        }
        def config = t.extensions.findByName(CONFIG) as Map<String, ?>
        config?.entrySet()?.each { entry ->
            args.add("-D${entry.key}=${entry.value}")
        }
        assert args.every { it.length() > 0}
        return args
    }
    */
}
