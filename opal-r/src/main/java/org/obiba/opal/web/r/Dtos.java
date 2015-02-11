/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.r;

import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.web.model.OpalR;

/**
 * Utility class for building R related Dtos.
 */
public class Dtos {

  private Dtos() {}

  public static OpalR.RSessionDto asDto(OpalRSession rSession) {
    UriBuilder ub = UriBuilder.fromPath("/").path(OpalRSessionParentResource.class)
        .path(OpalRSessionParentResource.class, "getOpalRSessionResource");
    return OpalR.RSessionDto.newBuilder().setId(rSession.getId()) //
        .setUser(rSession.getUser()) //
        .setCreationDate(DateTimeType.get().valueOf(rSession.getCreated()).toString()) //
        .setLastAccessDate(DateTimeType.get().valueOf(rSession.getTimestamp()).toString()) //
        .setStatus(rSession.isBusy() ? OpalR.RSessionStatus.BUSY : OpalR.RSessionStatus.WAITING) //
        .setLink(ub.build(rSession.getId()).toString())
        .build();
  }

}
