/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

/**
 *
 */
public final class ROperations {

  private static class NoOpROperation implements ROperation {

    private final static ROperation INSTANCE = new NoOpROperation();

    @Override
    public void doWithConnection(RServerConnection connection) {
      // no-op
    }
  }

  public static ROperation noOp() {
    return NoOpROperation.INSTANCE;
  }

  public static ROperationWithResult eval(final String script, String env) {
    return new AbstractROperationWithResult() {

      @Override
      protected void doWithConnection() {
        eval(script);
      }

      @Override
      public String toString() {
        return script;
      }
    };
  }

  public static ROperation assign(String name, String script) {
    return assign(name, script, null, false);
  }

  public static ROperation assign(final String name, final String script, final String env, final boolean lock) {
    return new AbstractROperation() {

      @Override
      protected void doWithConnection() {
        String format = env == null ? "is.null(base::assign('%s', value={%s}))" : "is.null(base::assign('%s', value={%s}, envir=%s))";
        eval(String.format(format, name, script, env));
        if (lock) {
          eval(String.format("base::lockBinding('%s', %s)", name, env));
        }
      }
    };
  }

}
