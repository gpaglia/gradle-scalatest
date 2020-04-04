package com.gpaglia.gradle.scalatest.testutils

import com.gpaglia.gradle.scalatest.testutils.ResourceUtils
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

class ResourceUtilsTest {

    private static final String FILE_RESOURCES = 'testutils'
    private static final String JAR_RESOURCES = 'testutils_jar'


    @TempDir
    protected Path dest

    @BeforeEach
    public void setup() {
        System.out.println("Created temp dir: ${dest.toString()}")
    }

    @Test
    public void testResourcesInFiles() throws Exception {
        ResourceUtils.copyResources(FILE_RESOURCES, dest)
        List<Path> paths = Files.walk(dest).withCloseable {
            paths -> paths
                    .filter { Files.isRegularFile(it) }
                    .map { dest.relativize(it) }
                    .collect(Collectors.toList())
        }

        org.hamcrest.MatcherAssert.assertThat(paths, org.hamcrest.Matchers.hasSize(2))
        org.hamcrest.MatcherAssert.assertThat(paths, org.hamcrest.Matchers.containsInAnyOrder([org.hamcrest.Matchers.is(Paths.get('file1.txt')), org.hamcrest.Matchers.is(Paths.get('dir1/file1_1.txt'))]))
    }

    @Test
    public void testResourcesInJar() throws Exception {
        ResourceUtils.copyResources(JAR_RESOURCES, dest)
        List<Path> paths = Files.walk(dest).withCloseable {
            paths -> paths
                    .filter { Files.isRegularFile(it) }
                    .map { dest.relativize(it) }
                    .collect(Collectors.toList())
        }

        org.hamcrest.MatcherAssert.assertThat(paths, org.hamcrest.Matchers.hasSize(2))
        org.hamcrest.MatcherAssert.assertThat(paths, org.hamcrest.Matchers.containsInAnyOrder([org.hamcrest.Matchers.is(Paths.get('file1.txt')), org.hamcrest.Matchers.is(Paths.get('dir1/file1_1.txt'))]))
    }
}
