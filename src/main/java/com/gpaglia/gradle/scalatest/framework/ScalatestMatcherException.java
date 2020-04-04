package com.gpaglia.gradle.scalatest.framework;

public class ScalatestMatcherException extends RuntimeException  {

  private static final long serialVersionUID = 3283294364256915644L;

  public ScalatestMatcherException() {
    super();
  }

  public ScalatestMatcherException(String message) {
    super(message);
  }

  public ScalatestMatcherException(String message, Throwable cause) {
    super(message, cause);
  }

  public ScalatestMatcherException(Throwable cause) {
    super(cause);
  }
}
