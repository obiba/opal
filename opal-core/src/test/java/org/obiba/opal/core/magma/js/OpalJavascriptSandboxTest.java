/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.magma.js;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.MagmaContextFactory;
import org.obiba.magma.js.MagmaJsExtension;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Magma JavaScript runs in Opal's JVM, reachable by anyone who can read a variable. With the engine wired the way
 * {@code DefaultOpalConfigurationService} wires it, a script must not reach the Java runtime.
 */
public class OpalJavascriptSandboxTest {

  /**
   * The route of the security review's payload, the Rhino Java bridge, with a harmless probe in place of the shell
   * command: should the sandbox regress, the test must not run anything on the build machine.
   */
  private static final String PAYLOAD = "Packages.java.lang.System.getProperty('java.version')";

  @Before
  public void setUp() {
    MagmaEngine.get().shutdown();
    MagmaContextFactory factory = new MagmaContextFactory();
    factory.setGlobalMethodProviders(List.of(new OpalGlobalMethodProvider()));
    MagmaJsExtension extension = new MagmaJsExtension();
    extension.setMagmaContextFactory(factory);
    new MagmaEngine().extend(extension);
  }

  @After
  public void tearDown() {
    MagmaEngine.get().shutdown();
  }

  @Test(expected = EcmaError.class)
  public void reviewPayloadIsRefused() {
    evaluate(PAYLOAD);
  }

  @Test
  public void javaBridgeIsAbsentButOpalGlobalsArePresent() {
    assertThat(evaluate("typeof Packages")).isEqualTo("undefined");
    assertThat(evaluate("typeof java")).isEqualTo("undefined");
    assertThat(evaluate("typeof getClass")).isEqualTo("undefined");
    assertThat(evaluate("typeof source")).isEqualTo("function");
    assertThat(evaluate("typeof newValue")).isEqualTo("function");
  }

  private Object evaluate(String script) {
    return ContextFactory.getGlobal().call(cx -> cx.evaluateString(MagmaContext.asMagmaContext(cx).newLocalScope(), script, "", 1, null));
  }
}
