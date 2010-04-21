/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime;

/**
 * A high-level abstraction of a component offering some type of service which has its own lifecycle. Service instances
 * are managed by the {@code OpalRuntime}
 */
public interface Service {

  public boolean isRunning();

  public void start();

  public void stop();

}
