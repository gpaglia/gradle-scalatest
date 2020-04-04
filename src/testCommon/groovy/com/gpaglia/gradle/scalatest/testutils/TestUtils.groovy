package com.gpaglia.gradle.scalatest.testutils

import com.gpaglia.gradle.scalatest.ScalaTestPlugin
import com.gpaglia.gradle.scalatest.Scalatest
import com.gpaglia.scalatest.framework.fixtures.SampleSuitesFixtures
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files
import java.nio.file.Path

class TestUtils {

    static Project testProject() {
        Project project = ProjectBuilder.builder().build()
        project.extensions.add(ScalaTestPlugin.MODE, ScalaTestPlugin.Mode.prototype.toString())
        project.plugins.apply(ScalaTestPlugin)
        project
    }

    static Scalatest testTask(Project project) {
        project.tasks.scalatest as Scalatest
    }

    static Path setupSamples(Path destinationDir, ClassLoader loader) {
        final Path samplesDir = Files.createDirectories(destinationDir.resolve("samples"))
        final SampleSuitesFixtures fixtures = new SampleSuitesFixtures()
        fixtures.setupCustom(loader)
        fixtures.extractSamples(samplesDir)
        samplesDir
    }
}
