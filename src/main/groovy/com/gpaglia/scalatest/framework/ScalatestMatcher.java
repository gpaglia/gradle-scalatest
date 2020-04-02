package com.gpaglia.scalatest.framework;

import com.gpaglia.scalatest.framework.matcher.BooleanBinaryOperator;
import com.gpaglia.scalatest.framework.matcher.ScalatestDefaultMatcher;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.tasks.testing.TestFilter;

import java.util.Arrays;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

public interface ScalatestMatcher {
  static BooleanBinaryOperator AND = (b1, b2) -> b1 && b2;
  static BooleanBinaryOperator OR = (b1, b2) -> b1 || b2;

  boolean test(Class<?> clazz);
  boolean test(Class<?> clazz, String testName);
  default Predicate<Class<?>> toClassPredicate() {
    return this::test;
  }
  default BiPredicate<Class<?>, String> toClassAndTestNamePredicate() {
    return this::test;
  }

  static ScalatestMatcher combine(BooleanBinaryOperator op, final ScalatestMatcher... matchers ) {
    return combine(op, Arrays.asList(matchers));
  }

  static ScalatestMatcher combine(BooleanBinaryOperator op, final Iterable<ScalatestMatcher> matchers) {

    return new ScalatestMatcher() {

      @Override
      public boolean test(Class<?> clazz) {
        boolean result = true;
        for (ScalatestMatcher m: matchers) {
          result = op.applyAsBoolean(result, m.test(clazz));
        }
        return result;
      }

      @Override
      public boolean test(Class<?> clazz, String testName) {
        boolean result = true;
        for (ScalatestMatcher m: matchers) {
          result = op.applyAsBoolean(result, m.test(clazz, testName));
        }
        return result;
      }
    };

  }

  static ScalatestMatcher and(final Iterable<ScalatestMatcher> matchers) {
    return combine(AND, matchers);
  }

  static ScalatestMatcher and(final ScalatestMatcher... matchers) {
    return combine(AND, matchers);
  }

  static ScalatestMatcher or(final Iterable<ScalatestMatcher> matchers) {
    return combine(OR, matchers);
  }

  static ScalatestMatcher or(final ScalatestMatcher... matchers) {
    return combine(OR, matchers);
  }

  static ScalatestMatcher not(final ScalatestMatcher matcher) {
    return new ScalatestMatcher() {

      @Override
      public boolean test(Class<?> clazz) {
        return ! matcher.test(clazz) ;
      }

      @Override
      public boolean test(Class<?> clazz, String testName) {
        return ! matcher.test(clazz, testName);
      }
    };
  }

  static ScalatestMatcher toMatcher(DefaultTestFilter filter) {
    final Set<ScalatestMatcher> inclMatchers = filter
        .getIncludePatterns()
        .stream()
        .map(p -> new ScalatestDefaultMatcher(p))
        .collect(toSet());

    final Set<ScalatestMatcher> inclCmdMatchers = filter
        .getCommandLineIncludePatterns()
        .stream()
        .map(p -> new ScalatestDefaultMatcher(p))
        .collect(toSet());

    final Set<ScalatestMatcher> exclMatchers = filter
        .getExcludePatterns()
        .stream()
        .map(p -> new ScalatestDefaultMatcher(p))
        .collect(toSet());

    return
        and(
            or(
                or(inclMatchers),
                or(inclCmdMatchers)
            ),
            not(
                or(exclMatchers)
            )
        );
  }

}
