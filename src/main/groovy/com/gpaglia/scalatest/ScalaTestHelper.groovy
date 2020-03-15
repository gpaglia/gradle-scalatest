package com.gpaglia.scalatest

import groovy.transform.PackageScope
import groovy.transform.ToString
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.util.PatternSet
import java.util.regex.Pattern

/*
    Accepts include patters in Gradle standard format, where '*' is a wildcard character
    Exclusions only possible for tags (so gradle exclude patters will report a warning)

    Rules for parsing:
    - assume class names start with uppercase or with a '*' wildcard character
    - wildcard within package segments only allowed at end of last segment (like alpha.beta*.MyClass)
    - if pattern starts with uppercase or '*', then it is assumed to be a simple class name and managed as suffix
    - to specify methods only, use *.method1.method2 where the leading '*' is interpreted as any class
 */

@ToString
@PackageScope
class ScalaTestHelper {
    static String TAGS = 'tags'
    static String SUITES = '_suites'
    static String CONFIG = '_config'

    // patterns for inclusion
    // TODO: Check scala std for class and method names
    private static final char WILDCARD_CHAR = '*'
    private static final String WILDCARD = Character.toString(WILDCARD_CHAR)
    private static final Pattern CLASS_NAME_PAT = Pattern.compile("[A-Z][^*]*")
    private static final Pattern CLASS_SUFFIX_PAT = Pattern.compile("[*][^*]*")
    private static final Pattern PKG_SEGMENT_PAT = Pattern.compile("[a-z][A-Za-z0-9_]*")
    private static final Pattern PKG_LAST_SEGMENT_PAT = Pattern.compile(PKG_SEGMENT_PAT.toString() + "[*]")
    private static final Pattern METHOD_NAME_PAT = Pattern.compile("[a-z*][A-Za-z0-9_ @*-]*")

    Map<String, ?> configs = new HashMap<String, ?>()
    Set<String> suffixes = new HashSet<>()
    Set<String> suites = new HashSet<>()
    Set<String> testsFull = new HashSet<>()
    Set<String> testsSubstring = new HashSet<>()
    Set<String> packagesMember = new HashSet<>()
    Set<String> packagesWildcard = new HashSet<>()
    Set<String> includeTags = new HashSet<>()
    Set<String> excludeTags = new HashSet<>()


    @Override
    String toString() {
        return "ScalaTestHelper{" +
                "configs=" + configs +
                ", suffixes=" + suffixes +
                ", suites=" + suites +
                ", testsFull=" + testsFull +
                ", testSubstrings=" + testsSubstring +
                ", packagesMember=" + packagesMember +
                ", packagesWildcard=" + packagesWildcard +
                ", includeTags=" + includeTags +
                ", excludeTags=" + excludeTags +
                '}'
    }

    @PackageScope
    static ScalaTestHelper getHelper(Test t) {
        ScalaTestHelper helper = new ScalaTestHelper()

        // parse command line pattern(s)
        if (t.filter.hasProperty('commandLineIncludePatterns')) {
            t.filter.commandLineIncludePatterns.each {
                helper.parseCommandLineInclude(it.toString())
            }
        }

        // parse include patterns from extension
        t.filter.includePatterns.each {
            helper.parseInclude(it.toString())
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

        def suites = t.extensions.findByName(SUITES) as List<String>
        suites?.toSet()?.each {
            helper.suites.add(it)
        }

        def configMap = t.extensions.findByName(CONFIG) as Map<String, ?>
        configMap?.entrySet()?.each { entry ->
            helper.configs.put(entry.key, entry.value)
        }

        helper
    }

    @PackageScope
    void parseCommandLineInclude(String selector) {
        final tuple = parsePattern(selector)
        decodeTuple(tuple)
    }

    @PackageScope
    void parseInclude(String selector) {
        final tuple = parsePattern(selector)
        decodeTuple(tuple)
    }

    @PackageScope
    void decodeTuple(Tuple3<List<String>, List<String>, List<String>> tuple) {
        final List<String> packages = tuple.first
        final List<String> classes = tuple.second
        final List<String> methods = tuple.third

        if (! packages.empty) {
            if (! packages.last().contentEquals(WILDCARD)) {
                if (! classes.empty && ! containWildcards(classes)) {
                    // fully qualified class name
                    suites.add(packages.join(".") + "." + classes.join("."))
                } else {
                    packagesMember.add(packages.join("."))
                    if (! classes.empty) {
                        // here we have just 1 class with wildcard; check for safety
                        if (classes.size() > 1 || ! classes.first().matches(CLASS_SUFFIX_PAT)) {
                            throw new IllegalStateException("Unexpected classes with wildcard: " + classes.toString())
                        }
                        if (! classes.first().contentEquals(WILDCARD)) {
                            suffixes.add(classes.first().substring(1))
                        }
                    }
                }

            } else {
                packagesWildcard.add(packages.subList(0, packages.size() - 1).join("."))
                if (! classes.empty) {
                    // manage as suffix only the main class, ignoring the nested classes
                    if (classes.size() > 1) {
                        Logging.getLogger(this.getClass()).warn("The nested classes will be ignored: {}", classes.subList(1, classes.size()))
                    }
                    if (! classes.first().contentEquals(WILDCARD)) {
                        suffixes.add(classes.first().replace(WILDCARD, ""))
                    }
                }
            }
        } else {
            if (! classes.empty) {
                // manage as suffix only the main class, ignoring the nested classes
                if (classes.size() > 1) {
                    Logging.getLogger(this.getClass()).warn("The nested classes will be ignored: {}", classes.subList(1, classes.size()))
                }
                if (! classes.first().contentEquals(WILDCARD)) {
                    suffixes.add(classes.first().replace(WILDCARD, ""))
                }
            }
        }

        if (! methods.empty) {
            final mthd = methods.join(".")
            if (! mthd.contains(WILDCARD)) {
                testsFull.add(mthd)
            } else {
                final String[] items = mthd.split(Pattern.quote(WILDCARD))
                for (int i = 0; i < items.length; i++) {
                    if (! items[i].isEmpty()) {
                        testsSubstring.add(items[i])
                        break
                    }
                }
            }
        }
    }

    private static boolean containWildcards(List<String> obj) {
        obj.any { it.contains(WILDCARD) }
    }

    @PackageScope
    static Tuple3<List<String>, List<String>, List<String>> parsePattern(String pattern) {
        final int IN_PKG = 0
        final int IN_CLASS = 1
        final int IN_METHOD = 2

        final String[] splits = pattern.split("[.]")
        final List<String> packages = new ArrayList<>()
        final List<String> classes = new ArrayList<>()
        final List<String> methods = new ArrayList<>()

        int state = splits.length < 2 ? IN_CLASS : IN_PKG

        splits.each {
            if (it.size() == 0) {
                throw new IllegalArgumentException("Invalid pattern: consecutive '.' characters not allowed in " + pattern)
            } else if (state == IN_PKG && it.matches(CLASS_SUFFIX_PAT)) {
                state = IN_METHOD
                classes.add(it)
            } else if (state <= IN_CLASS && (WILDCARD.equals(it) || it.matches(CLASS_NAME_PAT))) {
                state = IN_CLASS
                classes.add(it)
            } else if (state == IN_PKG && it.matches(PKG_SEGMENT_PAT)) {
                packages.add(it)
            } else if (state == IN_PKG && it.matches(PKG_LAST_SEGMENT_PAT)) {
                state = IN_CLASS
                packages.add(it.substring(0, it.length() - 1))
                packages.add(WILDCARD)
            } else if (state >= IN_CLASS && it.matches(METHOD_NAME_PAT)) {
                state = IN_METHOD
                methods.add(it)
            } else {
                throw new IllegalArgumentException("Invalid pattern: unmanageable segment " + it +  " in state " + state)
            }
        }

        new Tuple3<List<String>, List<String>, List<String>>(packages, classes, methods)
    }


}
