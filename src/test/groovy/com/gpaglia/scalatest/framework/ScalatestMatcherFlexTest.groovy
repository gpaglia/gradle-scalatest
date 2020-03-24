package com.gpaglia.scalatest.framework


import org.junit.jupiter.api.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.jupiter.api.Assertions.assertThrows

class ScalatestMatcherFlexTest {

    @Test
    public void testClassifiers() {
        // class
        assertThat(ScalatestMatcher.maybeCls("*"), is(true))
        assertThat(ScalatestMatcher.maybeCls("MyClass123"), is(true))
        assertThat(ScalatestMatcher.maybeCls("MyClass_\$"), is(true))
        assertThat(ScalatestMatcher.maybeCls("%cMyClass"), is(true))
        assertThat(ScalatestMatcher.maybeCls("12_MyClass"), is(false))
        assertThat(ScalatestMatcher.maybeCls("My Class"), is(false))
        assertThat(ScalatestMatcher.maybeCls("aMyClass"), is(false))

    }

    @Test
    public void testSimpleTestNameWithWildcardBefore() {
        final ScalatestMatcher matcher = new ScalatestMatcher("*.*method")

        assertThat(matcher.getTestPattern().pattern(), is("[^.]*\\Qmethod\\E"))
        assertThat(matcher.getClsPattern().pattern(), is("[^.]*"))
        assertThat(matcher.getPkgPattern(), nullValue())
    }


    @Test
    void testSimpleTestNameWithWildcardAfter() {
        final ScalatestMatcher matcher = new ScalatestMatcher("method*")

        assertThat(matcher.getTestPattern().pattern(), is("\\Qmethod\\E[^.]*"))
        assertThat(matcher.getClsPattern(), nullValue())
        assertThat(matcher.getPkgPattern(), nullValue())

    }

    @Test
    void testFullyQualifiedClassName() {
        final ScalatestMatcher matcher = new ScalatestMatcher("alpha.beta.gamma.MyClass.method")

        assertThat(matcher.getTestPattern().pattern(), is("\\Qmethod\\E"))
        assertThat(matcher.getClsPattern().pattern(), is("\\QMyClass\\E"))
        assertThat(matcher.getPkgPattern().pattern(), is("\\Qalpha\\E\\Q.\\E\\Qbeta\\E\\Q.\\E\\Qgamma\\E"))

    }

    @Test
    void testClassNameWithoutWildcards() {

        Throwable ex = assertThrows(ScalatestMatcherException.class)  {
            new ScalatestMatcher("alpha.beta.gamma.MyClass.MySubclass.method")
        }

        assertThat(ex.getMessage(), is("Invalid element[3] in expected package: [alpha, beta, gamma, MyClass]"))
    }


    @Test
    void testSimpleClassName() {
        final ScalatestMatcher matcher = new ScalatestMatcher("MyClass*suffix")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern().pattern(), is("\\QMyClass\\E[^.]*\\Qsuffix\\E"))
        assertThat(matcher.getPkgPattern(), nullValue())

    }

    @Test
    void testSimpleClassNameWithMethodsWithWildcards() {
        final ScalatestMatcher matcher = new ScalatestMatcher("MyClass*one.meth*two")

        assertThat(matcher.getTestPattern().pattern(), is("\\Qmeth\\E[^.]*\\Qtwo\\E"))
        assertThat(matcher.getClsPattern().pattern(), is("\\QMyClass\\E[^.]*\\Qone\\E"))
        assertThat(matcher.getPkgPattern(), nullValue())

    }


    @Test
    void testPackageOnly() {
        final ScalatestMatcher matcher = new ScalatestMatcher("alpha.beta.gamma")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern(), nullValue())
        assertThat(matcher.getPkgPattern().pattern(), is("\\Qalpha\\E\\Q.\\E\\Qbeta\\E\\Q.\\E\\Qgamma\\E"))
    }

    @Test
    void testPackageOnlyWithWildcards() {
        final ScalatestMatcher matcher = new ScalatestMatcher("alp*ha.be*ta.gam*ma")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern(), nullValue())
        assertThat(matcher.getPkgPattern().pattern(), is("\\Qalp\\E[^.]*\\Qha\\E\\Q.\\E\\Qbe\\E[^.]*\\Qta\\E\\Q.\\E\\Qgam\\E[^.]*\\Qma\\E"))
    }

    @Test
    void testPackageOnlyWitGlob() {
        final ScalatestMatcher matcher = new ScalatestMatcher("alp*ha.beta.gamma.**")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern(), nullValue())
        assertThat(
                matcher.getPkgPattern().pattern(),
                is("\\Qalp\\E[^.]*\\Qha\\E\\Q.\\E\\Qbeta\\E\\Q.\\E\\Qgamma\\E\\Q.\\E" + ScalatestMatcher.PKG_GLOB.pattern()))
    }


    @Test
    void testSingleStar() {
        final ScalatestMatcher matcher = new ScalatestMatcher("*")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern().pattern(), is("[^.]*"))
        assertThat(matcher.getPkgPattern(), nullValue())

    }

}
