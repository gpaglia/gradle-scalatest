package org.gradle.api.tasks.testing;

import com.gpaglia.scalatest.framework.ScalatestOptions;
import com.gpaglia.scalatest.framework.ScalatestTestFramework;
import org.gradle.api.Action;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.internal.Actions;

public abstract class AbstractScalatestTask extends Test {

  public AbstractScalatestTask() {
    super();
  }

  public void useScalatest() {
    useScalatest(Actions.<TestFrameworkOptions>doNothing());
  }

  public void useScalatest(Action<? super ScalatestOptions> testFrameworkConfigure) {
    useTestFramework(
        new ScalatestTestFramework(this, (DefaultTestFilter) getFilter(), getInstantiator(), getClassLoaderCache())
    );
    final TestFrameworkOptions opts = getOptions();
    if (opts instanceof ScalatestOptions) {
      testFrameworkConfigure.execute((ScalatestOptions) opts);
    } else {
      throw new IllegalStateException("Unexpected option type for ScalatestTask: " + opts.getClass());
    }
  }

}
