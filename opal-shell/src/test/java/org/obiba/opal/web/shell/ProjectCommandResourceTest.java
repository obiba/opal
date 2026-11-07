/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.shell;

import jakarta.ws.rs.core.Response;
import org.junit.Test;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.service.CommandJobService;

import java.lang.reflect.Field;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class ProjectCommandResourceTest {

  @Test
  public void testDeleteCommand_NotFoundWhenTheCommandIsOfAnotherProject() throws Exception {
    CommandJob job = createMock(CommandJob.class);
    expect(job.hasProject()).andStubReturn(true);
    expect(job.getProject()).andStubReturn("other");
    CommandJobService service = createMock(CommandJobService.class);
    expect(service.getCommand(1)).andReturn(job);
    replay(job, service);

    Response response = newResource(service).deleteCommand();

    assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    verify(service);
  }

  @Test
  public void testDeleteCommand_DeletesTheCommandOfTheProject() throws Exception {
    CommandJob job = createMock(CommandJob.class);
    expect(job.hasProject()).andStubReturn(true);
    expect(job.getProject()).andStubReturn("project");
    CommandJobService service = createMock(CommandJobService.class);
    expect(service.getCommand(1)).andReturn(job);
    service.deleteCommand(1);
    expectLastCall().once();
    replay(job, service);

    Response response = newResource(service).deleteCommand();

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    verify(service);
  }

  private ProjectCommandResource newResource(CommandJobService service) throws Exception {
    ProjectCommandResource resource = new ProjectCommandResource();
    resource.commandJobService = service;
    set(resource, "name", "project");
    set(resource, "id", 1);
    return resource;
  }

  private void set(ProjectCommandResource resource, String field, Object value) throws Exception {
    Field f = ProjectCommandResource.class.getDeclaredField(field);
    f.setAccessible(true);
    f.set(resource, value);
  }
}
