package com.gpaglia.scalatest.framework.matcher;

@FunctionalInterface
public interface BooleanBinaryOperator {
  boolean applyAsBoolean(boolean b1, boolean b2);
}
