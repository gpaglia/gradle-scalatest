package com.gpaglia.scalatest.framework

import com.gpaglia.scalatest.framework.matcher.ScalatestDefaultMatcher
import org.junit.jupiter.api.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.jupiter.api.Assertions.assertThrows

class ScalatestMatcherFlexTest {

    @Test
    public void testClassifiers() {
        // class
        assertThat(ScalatestDefaultMatcher.maybeCls("*"), is(true))
        assertThat(ScalatestDefaultMatcher.maybeCls("MyClass123"), is(true))
        assertThat(ScalatestDefaultMatcher.maybeCls("MyClass_\$"), is(true))
        assertThat(ScalatestDefaultMatcher.maybeCls("%cMyClass"), is(true))
        assertThat(ScalatestDefaultMatcher.maybeCls("12_MyClass"), is(false))
        assertThat(ScalatestDefaultMatcher.maybeCls("My Class"), is(false))
        assertThat(ScalatestDefaultMatcher.maybeCls("aMyClass"), is(false))

    }

    @Test
    public void testSimpleTestNameWithWildcardBefore() {
        final ScalatestDefaultMatcher matcher = new ScalatestDefaultMatcher("*.*method")

        assertThat(matcher.getTestPattern().pattern(), is("[^.]*\\Qmethod\\E"))
        assertThat(matcher.getClsPattern().pattern(), is("[^.]*"))
        assertThat(matcher.getPkgPattern(), nullValue())
    }


    @Test
    void testSimpleTestNameWithWildcardAfter() {
        final ScalatestDefaultMatcher matcher = new ScalatestDefaultMatcher("method*")

        assertThat(matcher.getTestPattern().pattern(), is("\\Qmethod\\E[^.]*"))
        assertThat(matcher.getClsPattern(), nullValue())
        assertThat(matcher.getPkgPattern(), nullValue())

    }

    @Test
    void testFullyQualifiedClassName() {
        final ScalatestDefaultMatcher matcher = new ScalatestDefaultMatcher("alpha.beta.gamma.MyClass.method")

        assertThat(matcher.getTestPattern().pattern(), is("\\Qmethod\\E"))
        assertThat(matcher.getClsPattern().pattern(), is("\\QMyClass\\E"))
        assertThat(matcher.getPkgPattern().pattern(), is("\\Qalpha\\E\\Q.\\E\\Qbeta\\E\\Q.\\E\\Qgamma\\E"))

    }

    @Test
    void testClassNameWithoutWildcards() {

        Throwable ex = assertThrows(ScalatestMatcherException.class)  {
            new ScalatestDefaultMatcher("alpha.beta.gamma.MyClass.MySubclass.method")
        }

        assertThat(ex.getMessage(), is("Invalid element[3] in expected package: [alpha, beta, gamma, MyClass]"))
    }


    @Test
    void testSimpleClassName() {
        final ScalatestDefaultMatcher matcher = new ScalatestDefaultMatcher("MyClass*suffix")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern().pattern(), is("\\QMyClass\\E[^.]*\\Qsuffix\\E"))
        assertThat(matcher.getPkgPattern(), nullValue())

    }

    @Test
    void testSimpleClassNameWithMethodsWithWildcards() {
        final ScalatestDefaultMatcher matcher = new ScalatestDefaultMatcher("MyClass*one.meth*two")

        assertThat(matcher.getTestPattern().pattern(), is("\\Qmeth\\E[^.]*\\Qtwo\\E"))
        assertThat(matcher.getClsPattern().pattern(), is("\\QMyClass\\E[^.]*\\Qone\\E"))
        assertThat(matcher.getPkgPattern(), nullValue())

    }


    @Test
    void testPackageOnly() {
        final ScalatestDefaultMatcher matcher = new ScalatestDefaultMatcher("alpha.beta.gamma")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern(), nullValue())
        assertThat(matcher.getPkgPattern().pattern(), is("\\Qalpha\\E\\Q.\\E\\Qbeta\\E\\Q.\\E\\Qgamma\\E"))
    }

    @Test
    void testPackageOnlyWithWildcards() {
        final ScalatestDefaultMatcher matcher = new ScalatestDefaultMatcher("alp*ha.be*ta.gam*ma")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern(), nullValue())
        assertThat(matcher.getPkgPattern().pattern(), is("\\Qalp\\E[^.]*\\Qha\\E\\Q.\\E\\Qbe\\E[^.]*\\Qta\\E\\Q.\\E\\Qgam\\E[^.]*\\Qma\\E"))
    }

    @Test
    void testPackageOnlyWitGlob() {
        final ScalatestDefaultMatcher matcher = new ScalatestDefaultMatcher("alp*ha.beta.gamma.**")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern(), nullValue())
        assertThat(
                matcher.getPkgPattern().pattern(),
                is("\\Qalp\\E[^.]*\\Qha\\E\\Q.\\E\\Qbeta\\E\\Q.\\E\\Qgamma\\E\\Q.\\E" + ScalatestDefaultMatcher.PKG_GLOB.pattern()))
    }


    @Test
    void testSingleStar() {
        final ScalatestDefaultMatcher matcher = new ScalatestDefaultMatcher("*")

        assertThat(matcher.getTestPattern(), nullValue())
        assertThat(matcher.getClsPattern().pattern(), is("[^.]*"))
        assertThat(matcher.getPkgPattern(), nullValue())

    }

}
