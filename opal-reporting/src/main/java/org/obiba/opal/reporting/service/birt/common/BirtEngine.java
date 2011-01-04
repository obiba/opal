/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.reporting.service.birt.common;

import java.util.Map;

/**
 * An internal interface used to share a common interface between BIRT and Opal's classloaders.
 * <p/>
 * We need to isolate BIRT's classloader from Opal's due to conflicts with common dependencies (ie: Rhino).
 */
public interface BirtEngine {

  void render(BirtReportFormat format, Map<String, String> parameters, String reportDesign, String reportOutput) throws BirtEngineException;

  boolean isRunning();

  void start();

  void stop();

}
