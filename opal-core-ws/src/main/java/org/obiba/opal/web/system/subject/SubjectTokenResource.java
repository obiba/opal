/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.system.subject;

import org.obiba.opal.core.service.SubjectTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Component
@Scope("request")
@Path("/system/subject-token/{principal}/token/{name}")
public class SubjectTokenResource {

  @PathParam("principal")
  private String principal;

  @PathParam("name")
  private String name;

  @Autowired
  private SubjectTokenService subjectTokenService;

  @DELETE
  public Response delete() {
    subjectTokenService.deleteToken(principal, name);
    return Response.ok().build();
  }

}