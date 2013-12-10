package org.obiba.opal.core.magma.js;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.js.methods.AbstractGlobalMethodProvider;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class OpalGlobalMethodProvider extends AbstractGlobalMethodProvider {

  private static final Logger log = LoggerFactory.getLogger(OpalGlobalMethodProvider.class);

  /**
   * Set of methods to be exposed as top-level methods (ones that can be invoked anywhere)
   */
  private static final Set<String> GLOBAL_METHODS = ImmutableSet.of("load");

  @Override
  protected Set<String> getExposedMethods() {
    return GLOBAL_METHODS;
  }

  public static void load(Context cx, Scriptable thisObj, Object[] args, Function funObj) {

    for(Object arg : args) {
      String fileName = Context.toString(arg);
      log.debug("Loading file {}", fileName);
      try {
        cx.evaluateReader(thisObj, new FileReader(OpalRuntime.MAGMA_JS_EXTENSION + "/" + fileName), fileName, 1, null);
      } catch(FileNotFoundException e) {
        throw new RuntimeException("Magma javascript extension not found: " + fileName, e);
      } catch(IOException e) {
        throw new RuntimeException("Magma javascript extension cannot be read: " + fileName, e);
      }
    }
  }
}
