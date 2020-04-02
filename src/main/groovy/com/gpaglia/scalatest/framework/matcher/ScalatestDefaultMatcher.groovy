package com.gpaglia.scalatest.framework.matcher

import com.gpaglia.scalatest.framework.ScalatestMatcher
import com.gpaglia.scalatest.framework.ScalatestMatcherException
import groovy.transform.PackageScope
import groovy.transform.ToString
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import java.util.regex.Pattern

/**
 * Accepts an include or exclude pattern in a format that extends the <a href="https://docs.gradle.org/current/userguide/java_testing.html#test_filtering">Gradle standard format</a>
 *
 * <pattern> := <composite> | <simple>
 * <composite> <pkg+cls> [ <nest> <pkg+cls> ] <nest> { <pkg+cls> | <pkg+cls+mth> }
 * <simple> := <clssegment> | <eclssegment> | <emthsegment> | <pkg+> | <pkg+class> | <cls+mth> | <pkg+cls+mth>
 * <pkg+> := pkgsegment { <dot> <pkgsegment> } ...
 * <pkg+cls> := pkgsegment [ { <dot> <pkgsegment> } ... ] <dot> <clssegment>
 * <cls+mth> := { <clssegment> | <eclssegment> } <dot> { <mthsegment> | <emthsegment> }
 * <pkg+cls+mth> := <pkg+cls> <dot> { <mthsegment> | <emthsegment> }
 * <pkgsegment> := { lcase | under | wildcard } [ letter | digit | under | wildcard ] ...
 * <clssegment> := { upcase | dollar | under | wildcard } [ letter | digit | under | dollar | wildcard ] ...
 * <eclssegment> := clsmeta { upcase | dollar | under | wildcard } [ letter | digit | under | dollar | wildcard ] ...
 * <mthsegment> := { letter | digit | dollar | under | wildcard } [ letter | digit | under | dollar | space | wildcard ] ...
 * <emthsegment> := mthmeta { letter | digit | dollar | under | wildcard } [ letter | digit | under | dollar | space | wildcard ] ...
 * <upcase> := A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R | S | T | U | V | W | X | Y | Z
 * <lcase> := a | b | c | d | e | f | g | h | i | j | k | l | m | n | o | p | q | r | s | t | u | v | w | x | y | z
 * <letter> := <ucase> | <lcase>
 * <digit> := 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
 * <space> := \u0020 \u0009
 * <dollar> := $
 * <under> := _
 * <specials> := <dollar> | <under>
 * <dot> := .
 * <wildcard> := *
 * <nest> := / /
 * <meta> := <clsmeta> | <mthmeta>
 * <clsmeta> := % c
 * <mthmeta> := % m
 *
 * Naming convention:
 * - the selector is made up of multiple suiteSpec(s) separated by //
 * - each suiteSpec is made up of segment(s) separated by the . character
 * - each segment can be a pkg segment, a cls segment or a mth segment
 * - a segment will be split in items or elements [e.g.  around the WILDCARD character to create the Pattern]
 *
 * Two formats:
 * - flex: the same as gradle, with the only improvements that
 *      - the %c meta-char is recognized at the start of a segment as a class segment marker to disambiguate cases
 *          when the class would not be clearly identified (as, f.e., when the class name pattern starts with a * wildcard)
 *      - the ** glob can be used as the last package segment with the meaning "any package at any depth below"
 * - extended: <suiteSpec> { [ / <suiteSpec> ] ... } [ # testSpec ]
 *      where suiteSpec is a package or class or package + class pattern (with no testSpec)
 *
 */
@ToString
class ScalatestDefaultMatcher implements ScalatestMatcher {

    // TODO: make wildcard translation more specific than ^[.]*, adding character ranges for cls, pkg and mth

    // patterns for inclusion
    @PackageScope static final char WILDCARD_CHAR = '*'
    @PackageScope static final String WILDCARD = Character.toString(WILDCARD_CHAR)
    @PackageScope static final String GLOB = WILDCARD + WILDCARD
    @PackageScope static final String META_PKG = "%p"
    @PackageScope static final String META_CLS = "%c"
    @PackageScope static final String META_MTH = "%m"
    @PackageScope static final String META_NEST = "//"

    @PackageScope static final Pattern PKG_GLOB = Pattern.compile("[a-z][a-z0-9_]*([.][a-z][a-z0-9_]*)*")
    @PackageScope static final Pattern PKG_PATTERN = Pattern.compile("([%]p)?[a-z*][a-z0-9_*]*")
    @PackageScope static final Pattern CLS_PATTERN = Pattern.compile("([%]c)?[A-Z_\$*][a-zA-Z0-9_\$*]*")
    // TODO: Add tab inside method pattern
    @PackageScope static final Pattern MTH_PATTERN = Pattern.compile("([%]m)?[a-zA-Z0-9_*\\s]*")

    private final String selector

    private final List<SuiteNameMatcher> nestedSuiteMatchers = new ArrayList<>()
    private SuiteNameMatcher suiteMatcher = null;
    private Pattern testPattern = null

    private static class SuiteNameMatcher {
        private final Pattern pkgPattern
        private final Pattern clsPattern

        private SuiteNameMatcher(final Pattern pkgPattern, final Pattern clsPattern) {
            this.pkgPattern = pkgPattern
            this.clsPattern = clsPattern
        }

        Pattern getPkgPattern() { return pkgPattern }
        Pattern getClsPattern() { return clsPattern }

        boolean match(Class<?> clazz) {
            boolean matchPkg = pkgPattern == null || pkgPattern.asMatchPredicate().test(clazz.getPackageName())
            boolean matchCls = clsPattern == null || clsPattern.asMatchPredicate().test(clazz.simpleName)

            return matchPkg && matchCls
        }
    }

    ScalatestDefaultMatcher(String selector) {
        if (selector == null || selector.isEmpty()) {
            throw new IllegalArgumentException("Selector cannot be empty")
        }
        this.selector = selector
        doParse()
    }

    static boolean isExplicitPkg(String segment) {
        return segment != null && segment.startsWith(META_PKG)
    }

    static boolean maybePkg(String segment) {
        return segment != null && (GLOB.equals(segment) || segment.matches(PKG_PATTERN))
    }

    static boolean isExplicitCls(String segment) {
        return segment != null && segment.startsWith(META_CLS)
    }

    static boolean maybeCls(String segment) {
        return segment != null && segment.matches(CLS_PATTERN) && ! segment.contains(GLOB)
    }

    boolean isExplicitMethod(String segment) {
        return segment != null && segment.matches(META_MTH)
    }

    static boolean maybeMth(String segment) {
        return segment != null && segment.matches(MTH_PATTERN) && ! segment.contains(GLOB)
    }

    void doParse() throws ScalatestMatcherException {
        String[] suiteSpec = selector.split(Pattern.quote(META_NEST))
        if (suiteSpec.length < 2) {
            final ImmutablePair<SuiteNameMatcher, Pattern> pair = parseFlex(selector)
            suiteMatcher = pair.getLeft()
            testPattern = pair.getRight()
        } else {
            for (int i = 0; i < suiteSpec.length - 2; i++) {
                nestedSuiteMatchers.add(parsePkgAndCls(suiteSpec[i]))
            }
            Pair<SuiteNameMatcher, Pattern> pair = parsePkgAndClsAndMth(suiteSpec[suiteSpec.length - 1])
            // add the pkg + suite pattern
            suiteMatcher = pair.getleft()
            // add the mth pattern -- this is possibly null === all tests
            testPattern = pair.getRight()
        }

    }

    @PackageScope
    SuiteNameMatcher parsePkgAndCls(String suiteSpec) throws ScalatestMatcherException {
        String[] segments = suiteSpec.split(Pattern.quote("."))

        if (segments.length == 0) {
            // in this case, suiteSpec must have been just a ".", throw an exception
            throw new ScalatestMatcherException("Invalid suiteSpec " + suiteSpec)
        } else if (segments.length == 1) {
            // if there is only one segment, assume it is a class simple name
            if (! maybeCls(segments[0])) {
                throw new ScalatestMatcherException("Invalid [expected] class pattern: " + segments[0])
            }
            return new SuiteNameMatcher(null, toPattern(stripMeta(segments[0])))
        } else {
            // we have >1 suiteSpecs  -- check last element - is it a class?
            final String lastSegment = segments[segments.length -1]

            if (maybeCls(lastElement)) {
                final clsPattern = toPattern(lastSegment)
                // parse all but last segments as packages
                final pkgPattern = toPkgPattern(Arrays.copyOf(segments, segments.length - 1))
                // combine the patterns
                return new SuiteNameMatcher(pkgPattern, clsPattern)
            } else {
                // parse all segments as packages
                return  new SuiteNameMatcher(toPkgPattern(segments), null)
            }
        }
    }

    @PackageScope
    static Pair<SuiteNameMatcher, Pattern> parsePkgAndClsAndMth(String suiteSpec) throws ScalatestMatcherException {
        // TODO: Complete
        return null
    }

    @PackageScope
    static Pair<SuiteNameMatcher, Pattern> parseFlex(String selector) throws ScalatestMatcherException {
        // first, we split the segments
        final String[] segments = selector.split(Pattern.quote("."), -1)
        if (segments.length == 1) {
            // this is either a class pattern or a test name pattern; give priority to class
            if (maybeCls(segments[0])) {
                return new ImmutablePair<>(new SuiteNameMatcher(null, toPattern(segments[0])), null)
            } else {
                // assume test name pattern
                return new ImmutablePair<>(null, toPattern(segments[0]))
            }
        } else if (segments.length == 2) {
            if (maybeCls(segments[0])) {
                return new ImmutablePair<>(new SuiteNameMatcher(null, toPattern(segments[0])), toPattern(segments[1]))
            } else if (maybeCls(segments[1])) {
                return new ImmutablePair<>(new SuiteNameMatcher(toPattern(segments[0]), toPattern(segments[1])), null)
            } else {    // Must be just a package pattern
                return new ImmutablePair<>(new SuiteNameMatcher(toPkgPattern(segments), null), null)
            }
        } else {
            // we have 3+ segments
            if (maybeCls(segments[segments.length - 1])) {
                // package and class

                return new ImmutablePair<>(
                    new SuiteNameMatcher(toPkgPattern(Arrays.copyOf(segments, segments.length - 1)), toPattern(segments[segments.length - 1])),
                    null
                )
            } else if (maybeCls(segments[segments.length - 2])) {
                return new ImmutablePair<>(
                        new SuiteNameMatcher(toPkgPattern(Arrays.copyOf(segments, segments.length - 2)), toPattern(segments[segments.length - 2])),
                        toPattern(segments[segments.length - 1])
                )
            } else {
                // assume it is a package pattern
                for (int i = 0; i < segments.length; i++) {
                    if (! maybePkg(segments[i])) {
                        throw new ScalatestMatcherException("Invalid multi-segment selector: " + Arrays.toString(segments))
                    }
                }
                return new ImmutablePair<>(new SuiteNameMatcher(toPkgPattern(segments), null), null)}
        }

    }

    private String stripMeta(String segment) {
        if (segment.startsWith(META_CLS)) {
            return segment.substring(META_CLS.length())
        } else if (segment.startsWith(META_PKG)) {
            return segment.substring(META_PKG.length())
        } else if (segment.startsWith(META_MTH)) {
            return segment.substring(META_MTH.length())
        } else {
            return segment
        }
    }

    @PackageScope
    static Pattern toPkgPattern(String[] segments) throws ScalatestMatcherException {
        final String[] patternStr = new String[segments.length]

        for (int i = 0; i < segments.length; i++) {
            if (!maybePkg(segments[i])) {
                throw new ScalatestMatcherException("Invalid element[" + i + "] in expected package: " + Arrays.toString(segments))
            } else {
                patternStr[i] = (i == segments.length - 1
                        ? toPkgPattern(segments[i])
                        : toPattern(segments[i])
                ).pattern()
            }
        }

        return Pattern.compile(String.join(Pattern.quote("."), patternStr))
    }

    @PackageScope
    static Pattern toPkgPattern(String segment) {
        if (segment.equals(GLOB)) {
            return PKG_GLOB
        } else {
            return toPattern(segment)
        }
    }

    @PackageScope
    static Pattern toPattern(String segment) {
        final String[] items = segment.split(Pattern.quote(WILDCARD), -1)
        final StringBuffer sb = new StringBuffer()
        for (int i = 0; i < items.length - 1; i++) {
            if (! items[i].isEmpty()) {
                sb.append(Pattern.quote(items[i]))
            }
            sb.append("[^.]*")
        }

        if (! items[items.length - 1].isEmpty()) {
            sb.append(Pattern.quote(items[items.length - 1]))
        }

        return Pattern.compile(sb.toString())
    }

    // ScalatestMatcher interface

    @Override
    boolean test(Class<?> clazz) {
        return (matchesClassName(clazz.getName()) && matchesPackageName(clazz.getPackageName()));
    }

    @Override
    boolean test(Class<?> clazz, String testName) {
        return test(clazz) && matchesTestName(testName);
    }

    // getters and std methods
    String getSelector() {
        return selector
    }

    List<SuiteNameMatcher> getNestedSuiteMatchers() {
        return Collections.unmodifiableList(nestedSuiteMatchers)
    }

    SuiteNameMatcher getSuiteMatcher() {
        return suiteMatcher
    }

    Pattern getClsPattern() {
        return suiteMatcher == null ? null : suiteMatcher.getClsPattern()
    }

    boolean matchesClassName(String name) {
        final Pattern clsPattern = getClsPattern()
        return clsPattern == null || name.matches(clsPattern)
    }

    Pattern getPkgPattern() {
        return suiteMatcher == null ? null : suiteMatcher.getPkgPattern()
    }

    boolean matchesPackageName(String name) {
        final Pattern pkgPattern = getPkgPattern()
        return pkgPattern == null || name.matches(pkgPattern)
    }

    Pattern getTestPattern() {
        return testPattern
    }

    boolean matchesTestName(String name) {
        final Pattern testPattern = getTestPattern()
        return testPattern == null || name.matches(testPattern)
    }

}
