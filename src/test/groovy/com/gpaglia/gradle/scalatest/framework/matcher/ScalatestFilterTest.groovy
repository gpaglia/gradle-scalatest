package com.gpaglia.gradle.scalatest.framework.matcher

import com.gpaglia.gradle.scalatest.framework.ScalatestMatcher
import com.gpaglia.scalatest.framework.fixtures.SampleSuitesFixtures
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter
import org.junit.jupiter.api.Test
import java.util.stream.Collectors

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder

class ScalatestFilterTest {
    @Test
    void nullFilterTest() {
        final ClassLoader loader = this.getClass().getClassLoader()
        final DefaultTestFilter filter = new DefaultTestFilter()
        final ScalatestMatcher matcher = ScalatestMatcher.fromFilter(filter)
        final Set<String> matched = Arrays
                .stream(SampleSuitesFixtures.ALL_DEFINED_SUITES)
                .map { loadClassSafely(it, loader) }
                .filter { matcher.test(it) }
                .map { it.getName() }
                .collect(Collectors.toSet())

        assertThat(matched, containsInAnyOrder(SampleSuitesFixtures.ALL_DEFINED_SUITES))
    }

    @Test
    void includePackageFilterTest() {
        final ClassLoader loader = this.getClass().getClassLoader()
        final DefaultTestFilter filter = new DefaultTestFilter()
        filter.setIncludePatterns(SampleSuitesFixtures.SAMPLES_SUITE_PACKAGE + ".*")
        final ScalatestMatcher matcher = ScalatestMatcher.fromFilter(filter)
        final Set<String> matched = Arrays
                .stream(SampleSuitesFixtures.ALL_DEFINED_SUITES)
                .map{ loadClassSafely(it, loader) }
                .filter { matcher.test(it) }
                .map { it.getName() }
                .collect(Collectors.toSet())

        final String[] expected = Arrays
                .stream(SampleSuitesFixtures.ALL_DEFINED_SUITES)
                .map{ loadClassSafely(it, loader) }
                .filter { it.getPackage().getName().equals(SampleSuitesFixtures.SAMPLES_SUITE_PACKAGE) }
                .map { it.getName() }
                .toArray { new String[it] }

        System.out.printf("Expected: %d, found: %d\n", expected.length, matched.size())

        assertThat(matched, containsInAnyOrder(expected))
    }

    @Test
    void excludePackageFilterTest() {
        final ClassLoader loader = this.getClass().getClassLoader()
        final DefaultTestFilter filter = new DefaultTestFilter()
        filter.setExcludePatterns(SampleSuitesFixtures.SAMPLES_SUITE_PACKAGE + ".*")
        final ScalatestMatcher matcher = ScalatestMatcher.fromFilter(filter)
        final Set<String> matched = Arrays
                .stream(SampleSuitesFixtures.ALL_DEFINED_SUITES)
                .map{ loadClassSafely(it, loader) }
                .filter { matcher.test(it) }
                .map {it.getName() }
                .collect(Collectors.toSet())

        final String[] expected = Arrays
                .stream(SampleSuitesFixtures.ALL_DEFINED_SUITES)
                .map{ loadClassSafely(it, loader) }
                .filter { ! it.getPackage().getName().equals(SampleSuitesFixtures.SAMPLES_SUITE_PACKAGE) }
                .map { it.getName() }
                .toArray { new String[it] }

        System.out.printf("Expected: %d, found: %d\n", expected.length, matched.size())

        assertThat(matched, containsInAnyOrder(expected))
    }

    private static Class<?> loadClassSafely(String className, ClassLoader loader) {
        try {
            return loader.loadClass(className)
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load class " + className, e)
        }
    }
}
