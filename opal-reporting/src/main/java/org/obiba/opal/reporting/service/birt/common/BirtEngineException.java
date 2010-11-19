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

import java.util.Collection;

public class BirtEngineException extends Exception {

  private static final long serialVersionUID = 1L;

  public BirtEngineException(String message) {
    super(message);
  }

  public BirtEngineException(Collection<String> messages) {
    super(messages.toString());
  }

}
