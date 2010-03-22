package org.obiba.magma.test;

import org.junit.After;
import org.junit.Before;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.xstream.MagmaXStreamExtension;

public abstract class AbstractMagmaTest {

  @Before
  public void startYourEngine() {
    new MagmaEngine();
    MagmaEngine.get().extend(new MagmaXStreamExtension());
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }
}
