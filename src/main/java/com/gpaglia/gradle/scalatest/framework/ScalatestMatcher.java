package com.gpaglia.gradle.scalatest.framework;

import com.gpaglia.gradle.scalatest.framework.matcher.BinaryOp;
import com.gpaglia.gradle.scalatest.framework.matcher.BooleanBinaryOperator;
import com.gpaglia.gradle.scalatest.framework.matcher.ScalatestDefaultMatcher;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.gpaglia.gradle.scalatest.framework.matcher.BinaryOp.AND;
import static com.gpaglia.gradle.scalatest.framework.matcher.BinaryOp.OR;
import static java.util.stream.Collectors.toSet;

public interface ScalatestMatcher {
  ScalatestMatcher MATCH_ALL = new ScalatestMatcher() {
    @Override
    public boolean test(Class<?> clazz) { return true; }

    @Override
    public boolean test(Class<?> clazz, String testName) { return true; }
  };
  ScalatestMatcher MATCH_NONE = new ScalatestMatcher() {
    @Override
    public boolean test(Class<?> clazz) { return false; }

    @Override
    public boolean test(Class<?> clazz, String testName) { return false; }
  };

  boolean test(Class<?> clazz);
  boolean test(Class<?> clazz, String testName);
  default Predicate<Class<?>> toClassPredicate() {
    return this::test;
  }
  default BiPredicate<Class<?>, String> toClassAndTestNamePredicate() {
    return this::test;
  }

  static ScalatestMatcher combine(BinaryOp op, final boolean matchIfEmpty, final ScalatestMatcher... matchers) {
    return combine(op, matchIfEmpty, Arrays.asList(matchers));
  }

  static ScalatestMatcher combine(BinaryOp op, final boolean matchIfEmpty, final Collection<ScalatestMatcher> matchers) {

    return new ScalatestMatcher() {

      final List<ScalatestMatcher> ourMatchers = new ArrayList<>(matchers);

      @Override
      public boolean test(Class<?> clazz) {
        if (ourMatchers.isEmpty()) {
          return matchIfEmpty;
        } else {
          boolean result = ourMatchers.get(0).test(clazz);
          for (ScalatestMatcher m: ourMatchers.subList(1, ourMatchers.size())) {
            result = op.applyAsBoolean(result, m.test(clazz));
          }
          return result;
        }
      }

      @Override
      public boolean test(Class<?> clazz, String testName) {
        if (ourMatchers.isEmpty()) {
          return matchIfEmpty;
        } else {
          boolean result = ourMatchers.get(0).test(clazz, testName);
          for (ScalatestMatcher m : ourMatchers.subList(1, ourMatchers.size())) {
            result = op.applyAsBoolean(result, m.test(clazz, testName));
          }
          return result;
        }
      }

      @Override
      public String toString() { return op.toString() + "--" + ourMatchers.toString(); }
    };

  }

  static ScalatestMatcher and(final boolean matchIfEmpty, final Collection<ScalatestMatcher> matchers) {
    return combine(AND, matchIfEmpty, matchers);
  }

  static ScalatestMatcher and(final boolean matchIfEmpty, final ScalatestMatcher... matchers) {
    return combine(AND, matchIfEmpty, matchers);
  }

  static ScalatestMatcher or(final boolean matchIfEmpty, final Collection<ScalatestMatcher> matchers) {
    return combine(OR, matchIfEmpty, matchers);
  }

  static ScalatestMatcher or(final boolean matchIfEmpty, final ScalatestMatcher... matchers) {
    return combine(OR, matchIfEmpty, matchers);
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

      @Override
      public String toString() { return "NOT--" + matcher.toString(); }
    };
  }

  static ScalatestMatcher fromFilter(DefaultTestFilter filter) {
    final Set<ScalatestMatcher> inclMatchers = Stream.concat(
        filter
          .getIncludePatterns()
          .stream()
          .map(ScalatestDefaultMatcher::new),
        filter
          .getCommandLineIncludePatterns()
          .stream()
          .map(ScalatestDefaultMatcher::new)
    ).collect(toSet());

    final Set<ScalatestMatcher> exclMatchers = filter
        .getExcludePatterns()
        .stream()
        .map(ScalatestDefaultMatcher::new)
        .collect(toSet());

    return
        and(
            true,
            or(true, inclMatchers),
            not( or(false, exclMatchers) )
        );
  }

}
