package org.gradle.api.tasks.testing;

import com.gpaglia.scalatest.framework.ScalatestOptions;
import org.gradle.api.Action;
import org.gradle.internal.Actions;

public abstract class AbstractScalatestTask__OLD extends Test {

  public AbstractScalatestTask__OLD() {
    super();
  }

  public void useScalatest() {
    useScalatest(Actions.<TestFrameworkOptions>doNothing());
  }

  public void useScalatest(Action<? super ScalatestOptions> testFrameworkConfigure) {
    /*
    useTestFramework(
        new ScalatestTestFramework(this, (DefaultTestFilter) getFilter(), getInstantiator(), getClassLoaderCache())
    );
    final TestFrameworkOptions opts = getOptions();
    if (opts instanceof ScalatestOptions) {
      testFrameworkConfigure.execute((ScalatestOptions) opts);
    } else {
      throw new IllegalStateException("Unexpected option type for ScalatestTask: " + opts.getClass());
    }

    */
  }

}
