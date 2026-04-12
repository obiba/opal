/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

/**
 * Optional interface for {@link Command} implementations that produce a typed result
 * after execution. Existing commands are unaffected; new commands opt in by
 * implementing this interface alongside {@link Command}.
 *
 * <p>Example usage:
 * <pre>
 *   public class FileBundleCommand
 *       extends AbstractOpalRuntimeDependentCommand&lt;FileBundleCommandOptions&gt;
 *       implements ResultCapable&lt;FileCommandResult&gt; { ... }
 * </pre>
 *
 * @param <R> the concrete {@link CommandResult} type produced by the command
 */
public interface ResultCapable<R extends CommandResult> {

  /**
   * Returns {@code true} if the command has produced a result (i.e. after a
   * successful {@link Command#execute()} call).
   *
   * @return whether a result is available
   */
  boolean hasResult();

  /**
   * Returns the command result.
   *
   * @return result, or {@code null} if {@link #hasResult()} is {@code false}
   */
  R getResult();
}

