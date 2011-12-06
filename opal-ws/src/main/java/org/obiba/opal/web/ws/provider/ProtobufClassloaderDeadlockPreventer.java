/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.ws.provider;

import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Math;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * http://groups.google.com/group/protobuf/browse_thread/thread/c6b8280d4b9de976
 */
@Component
@Scope("singleton")
public class ProtobufClassloaderDeadlockPreventer {

  public ProtobufClassloaderDeadlockPreventer() {
    // Forces the class load to load these classes. Hopefully without deadlocking!
    Commands.getDescriptor();
    DataShield.getDescriptor();
    Magma.getDescriptor();
    Math.getDescriptor();
    Opal.getDescriptor();
    OpalR.getDescriptor();
  }
}
