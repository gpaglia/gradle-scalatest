package com.gpaglia.gradle.scalatest


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

/**
 * Applies the Java & Scala Plugins
 * Replaces Java Test actions with a <code>ScalaTestAction</code>
 */
class ScalaTestPlugin implements Plugin<Project> {

    public static String MODE = 'com.gpaglia.scalatest.mode'
    public static final String CONFIG = "_config"

    static enum Mode {
        prototype, replaceAll, replaceOne, append
    }

    @Override
    void apply(Project t) {
        if (!t.plugins.hasPlugin(ScalaTestPlugin)) {
            t.plugins.apply(JavaPlugin)
            t.plugins.apply(ScalaPlugin)
            /*
            switch (getMode(t)) {
                case Mode.replaceAll:
                    t.tasks.withType(Test) { configure(it) }
                    break
                case Mode.replaceOne:
                    t.tasks.withType(Test) {
                        if (it.name == JavaPlugin.TEST_TASK_NAME) {
                            configure(it)
                        }
                    }
                    break
                case Mode.append:
                    configure(
                        t.tasks.create(
                            name: 'scalatest', type: Test, group: 'verification',
                            description: 'Run scalatest unit tests',
                            dependsOn: t.tasks.testClasses
                        ) as Test)
                    break
                case Mode.prototype:
                    configure(
                        t.tasks.create(
                            name: 'scalatestproto', type: ScalatestTask, group: 'verification',
                                description: 'Run scalatest unit tests [new]',
                                dependsOn: t.tasks.testClasses
                        )
                    )
                    break
            }
            */
            configure(
                    t.tasks.create(
                            name: 'scalatest', type: Scalatest, group: 'verification',
                            description: 'Run scalatest unit tests [new]',
                            dependsOn: t.tasks.testClasses
                    ).asType(Scalatest.class)
            )
        }
    }

    private static Mode getMode(Project t) {
        if (!t.hasProperty(MODE)) {
            return Mode.replaceAll
        } else {
            return Mode.valueOf(t.properties[MODE].toString())
        }
    }

    static void configure(Scalatest test) {
        test.maxParallelForks = Runtime.runtime.availableProcessors()
        test.testLogging.exceptionFormat = TestExceptionFormat.SHORT
        /*
        test.extensions.add(ScalaTestAction.TAGS, new PatternSet())
        List<String> suites = []
        test.extensions.add(ScalaTestAction.SUITES, suites)
        test.extensions.add("suite", { String name -> suites.add(name) })
        test.extensions.add("suites", { String... name -> suites.addAll(name) })
        */
        Map<String, ?> config = [:]
        test.extensions.add(CONFIG, config)
        test.extensions.add("config", { String name, value -> config.put(name, value) })
        test.extensions.add("configMap", { Map<String, ?> c -> config.putAll(c) })
        test.testLogging.events = TestLogEvent.values() as Set
    }

}