package com.gpaglia.gradle.scalatest.framework.matcher;

public enum BinaryOp implements BooleanBinaryOperator {
  AND {
    @Override
    public boolean applyAsBoolean(boolean b1, boolean b2) {
      return b1 && b2;
    }
  },
  OR {
    @Override
    public boolean applyAsBoolean(boolean b1, boolean b2) {
      return b1 || b2;
    }
  }
}
