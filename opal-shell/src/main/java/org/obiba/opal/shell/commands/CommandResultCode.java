/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

/**
 *
 */
public interface CommandResultCode {

  /**
   * Success
   */
  int SUCCESS = 0;

  /**
   * Critical error (or interruption)
   */
  int CRITICAL_ERROR = 1;

  /**
   * Non critical error
   */
  int NON_CRITICAL_ERROR = 2;

}
