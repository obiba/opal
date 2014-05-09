package org.obiba.opal.core.magma.js;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.methods.AbstractGlobalMethodProvider;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class OpalGlobalMethodProvider extends AbstractGlobalMethodProvider {

  private static final Logger log = LoggerFactory.getLogger(OpalGlobalMethodProvider.class);

  /**
   * Set of methods to be exposed as top-level methods (ones that can be invoked anywhere)
   */
  private static final Set<String> GLOBAL_METHODS = ImmutableSet.of("source");

  @Override
  protected Set<String> getExposedMethods() {
    return GLOBAL_METHODS;
  }

  public static void source(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {

    for(Object arg : args) {
      String fileName = Context.toString(arg);
      try {
        File file;
        if(fileName.startsWith("/")) {
          file = new File(System.getProperty("OPAL_HOME") + File.separator + "fs", fileName);
        } else {
          file = new File(OpalRuntime.MAGMA_JS_EXTENSION, fileName);
        }
        if(!file.getCanonicalPath().startsWith(System.getProperty("OPAL_HOME")))
          throw new RuntimeException("Unauthorized javascript library path: " + fileName);
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        RequireCache cache;
        if(context.has(RequireCache.class)) {
          cache = context.peek(RequireCache.class);
          if(cache.hasLibrary(file.getAbsolutePath())) return;
        } else {
          cache = new RequireCache();
          context.push(RequireCache.class, cache);
        }
        log.debug("Loading file at path: {}", file.getAbsolutePath());
        context.evaluateReader(thisObj, new FileReader(file), file.getName(), 1, null);
        cache.addLibrary(file.getAbsolutePath());
      } catch(FileNotFoundException e) {
        throw new RuntimeException("Javascript library not found: " + fileName, e);
      } catch(IOException e) {
        throw new RuntimeException("Javascript library cannot be read: " + fileName, e);
      }
    }
  }

  /**
   * Do not load the same library multiple times.
   */
  private static class RequireCache {

    private final List<String> libraries = Lists.newArrayList();

    public boolean hasLibrary(String path) {
      return libraries.contains(path);
    }

    public void addLibrary(String path) {
      if(!hasLibrary(path)) libraries.add(path);
    }

  }
}
