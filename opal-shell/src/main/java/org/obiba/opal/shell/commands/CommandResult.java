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
 * Marker interface for the result produced by a command after execution.
 * Concrete subtypes carry the actual result data (e.g. a file, JSON, …).
 */
public interface CommandResult {

  /**
   * A short, display-friendly label describing the result (e.g. a filename).
   * This value is surfaced in {@code CommandStateDto.result} so the UI can
   * show it without exposing internal paths.
   *
   * @return human-readable result label
   */
  String getLabel();

  /**
   * Clean up any resources held by this result (e.g. delete a temporary file).
   * Called by the framework when the owning command job is deleted or
   * when the service shuts down.
   */
  void cleanup();
}


