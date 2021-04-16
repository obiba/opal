/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime;

/**
 * Thrown when a service refers to a non-running {@link Service}.
 */
public class ServiceNotRunningException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ServiceNotRunningException(String serviceName, String message) {
    super("Service " + serviceName + " is not running: " + message);
  }

}
