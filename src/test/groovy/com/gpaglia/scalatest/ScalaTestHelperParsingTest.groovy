package com.gpaglia.scalatest

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.jupiter.api.Assertions.assertThrows

class ScalaTestHelperParsingTest {

    private ScalaTestHelper helper

    @BeforeEach
    void setup() {
        helper = new ScalaTestHelper()
    }

    @AfterEach
    void teardown() {
        helper = null
    }

    @Test
    void testSimpleTestName() {
        final tuple = helper.parsePattern("method")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, empty())
        assertThat(classes, empty())
        assertThat(methods, contains("method"))

        helper.decodeTuple(tuple)

        assertThat(helper.suffixes, empty())
        assertThat(helper.suites, empty())
        assertThat(helper.testsFull, containsInAnyOrder("method"))
        assertThat(helper.testSubstrings, empty())
        assertThat(helper.packagesMember, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())

    }

    @Test
    void testSimpleTestNameWithWildcardBefore() {
        final tuple = helper.parsePattern("*.*method")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, empty())
        assertThat(classes, contains("*"))
        assertThat(methods, contains("*method"))

        helper.decodeTuple(tuple)

        assertThat(helper.suffixes, empty())
        assertThat(helper.suites, empty())
        assertThat(helper.testsFull, empty())
        assertThat(helper.testSubstrings, containsInAnyOrder("method"))
        assertThat(helper.packagesMember, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())

    }

    @Test
    void testSimpleTestNameWithWildcardAfter() {
        final tuple = helper.parsePattern("method*")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, empty())
        assertThat(classes, empty())
        assertThat(methods, contains("method*"))

        helper.decodeTuple(tuple)

        assertThat(helper.suffixes, empty())
        assertThat(helper.suites, empty())
        assertThat(helper.testsFull, empty())
        assertThat(helper.testSubstrings, containsInAnyOrder("method"))
        assertThat(helper.packagesMember, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())

    }

    @Test
    void testFullyQualifiedClassName() {
        final tuple = helper.parsePattern("alpha.beta.gamma.MyClass.MySubclass.method1.method2")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, contains("alpha", "beta", "gamma"))
        assertThat(classes, contains("MyClass", "MySubclass"))
        assertThat(methods, contains("method1", "method2"))

        helper.decodeTuple(tuple)

        assertThat(helper.suffixes, empty())
        assertThat(helper.suites, containsInAnyOrder("alpha.beta.gamma.MyClass.MySubclass"))
        assertThat(helper.testsFull, containsInAnyOrder("method1.method2"))
        assertThat(helper.testSubstrings, empty())
        assertThat(helper.packagesMember, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())

    }

    @Test
    void testClassNameWithoutWildcards() {

        Throwable ex = assertThrows(IllegalArgumentException.class)  {
            helper.parsePattern("alpha.beta.gamma.MyClass*.MySubclass.method1.method2")
        }

        assertThat(ex.getMessage(), is("Invalid pattern: unmanageable segment MyClass* in state 0"))
        assertThat(helper.suffixes, empty())
        assertThat(helper.suites, empty())
        assertThat(helper.testsFull, empty())
        assertThat(helper.testSubstrings, empty())
        assertThat(helper.packagesMember, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())

    }

    @Test
    void testSimpleClassName() {
        final tuple = helper.parsePattern("MyClass.MySubclass")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, empty())
        assertThat(classes, contains("MyClass", "MySubclass"))
        assertThat(methods, empty())

        helper.decodeTuple(tuple)

        assertThat(helper.suffixes, containsInAnyOrder("MyClass"))
        assertThat("suites", helper.suites, empty())
        assertThat("testsFull", helper.testsFull, empty())
        assertThat("testSubstring", helper.testSubstrings, empty())
        assertThat("packagesMember", helper.packagesMember, empty())
        assertThat("packagesWildcard", helper.packagesWildcard, empty())
        assertThat("includeTags", helper.includeTags, empty())
        assertThat("excludeTags", helper.excludeTags, empty())
    }

    @Test
    void testSimpleClassNameWithMethods() {
        final tuple = helper.parsePattern("MyClass.MySubclass.method1.method2")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, empty())
        assertThat(classes, contains("MyClass", "MySubclass"))
        assertThat(methods, contains("method1", "method2"))

        helper.decodeTuple(tuple)

        assertThat(helper.suffixes, containsInAnyOrder("MyClass"))
        assertThat(helper.testsFull, containsInAnyOrder("method1.method2"))
        assertThat(helper.suites, empty())
        assertThat(helper.testSubstrings, empty())
        assertThat(helper.packagesMember, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())

    }

    @Test
    void testPackageOnly() {
        final tuple = helper.parsePattern("alpha.beta.gamma")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, contains("alpha", "beta", "gamma"))
        assertThat(classes, empty())
        assertThat(methods, empty())

        helper.decodeTuple(tuple)
        assertThat(helper.packagesMember, containsInAnyOrder("alpha.beta.gamma"))
        assertThat(helper.suffixes, empty())
        assertThat(helper.suites, empty())
        assertThat(helper.testsFull, empty())
        assertThat(helper.testSubstrings, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())
    }

    @Test
    void testPackageOnlyWithWildcard() {
        final tuple = helper.parsePattern("alpha.beta.gamma*")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, contains("alpha", "beta", "gamma", "*"))
        assertThat(classes, empty())
        assertThat(methods, empty())

        helper.decodeTuple(tuple)
        assertThat(helper.packagesWildcard, containsInAnyOrder("alpha.beta.gamma"))
        assertThat(helper.suffixes, empty())
        assertThat(helper.suites, empty())
        assertThat(helper.testsFull, empty())
        assertThat(helper.testSubstrings, empty())
        assertThat(helper.packagesMember, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())
    }


    @Test
    void testOnlyMethod() {
        final tuple = helper.parsePattern("*.method1")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, empty())
        assertThat(classes, contains("*"))
        assertThat(methods, contains("method1"))

        helper.decodeTuple(tuple)
        assertThat(helper.testsFull, containsInAnyOrder("method1"))
        assertThat(helper.suffixes, empty())
        assertThat(helper.suites, empty())
        assertThat(helper.testSubstrings, empty())
        assertThat(helper.packagesMember, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())

    }


    @Test
    void testSingleStar() {
        final tuple = helper.parsePattern("*")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, empty())
        assertThat(classes, contains("*"))
        assertThat(methods, empty())

        assertThat(helper.suffixes, empty())
        assertThat(helper.suites, empty())
        assertThat(helper.testsFull, empty())
        assertThat(helper.testSubstrings, empty())
        assertThat(helper.packagesMember, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())

    }

    @Test
    void testSingleStarAndSuffix() {
        final tuple = helper.parsePattern("*abc.*def.ghi*")

        checkTuple(tuple)

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, empty())
        assertThat(classes, contains("*abc"))
        assertThat(methods, contains("*def", "ghi*"))

        helper.decodeTuple(tuple)

        assertThat(helper.suffixes, containsInAnyOrder("abc"))
        assertThat(helper.suites, empty())
        assertThat(helper.testsFull, empty())
        assertThat(helper.testSubstrings, containsInAnyOrder("def", "ghi"))
        assertThat(helper.packagesMember, empty())
        assertThat(helper.packagesWildcard, empty())
        assertThat(helper.includeTags, empty())
        assertThat(helper.excludeTags, empty())

    }


    private static void checkTuple(Tuple3<List<String>, List<String>, List<String>> tuple) {
        assertThat(tuple, notNullValue())

        final packages = tuple.first
        final classes = tuple.second
        final methods = tuple.third

        assertThat(packages, notNullValue())
        assertThat(classes, notNullValue())
        assertThat(methods, notNullValue())

    }
}
