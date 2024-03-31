/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/service/r/workspaces")
public class RServiceWorkspacesResource {

  private final static Authorizer authorizer = new ShiroAuthorizer();

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @GET
  public List<OpalR.RWorkspaceDto> getRWorkspaces(@QueryParam("context") String context, @QueryParam("user") String user) {
    List<OpalR.RWorkspaceDto> ws = Lists.newArrayList();
    opalRSessionManager.getWorkspaces().stream()
        .filter(File::isDirectory)
        .filter(file -> Strings.isNullOrEmpty(context) || file.getName().equals(context))
        .forEach(contextFile -> {
          File[] userFolders = contextFile.listFiles();
          if (userFolders != null) {
            Lists.newArrayList(userFolders).stream()
                .filter(File::isDirectory)
                .filter(file -> Strings.isNullOrEmpty(user) || file.getName().equals(user))
                .filter(file -> isUserHomeFolderReadable(file.getName()))
                .forEach(userFolder -> {
                  File[] workspaces = userFolder.listFiles();
                  if (workspaces != null) {
                    ws.addAll(Lists.newArrayList(workspaces).stream().filter(File::isDirectory)
                        .map(folder -> Dtos.asDto(contextFile.getName(), userFolder.getName(), folder))
                        .collect(Collectors.toList()));
                  }
                });
          }
        });
    return ws;
  }

  @DELETE
  public Response removeRWorkspaces(@QueryParam("context") String context, @QueryParam("user") String user, @QueryParam("name") String name) {
    opalRSessionManager.getWorkspaces().stream()
        .filter(File::isDirectory)
        .filter(file -> Strings.isNullOrEmpty(context) || file.getName().equals(context))
        .forEach(contextFile -> {
          File[] userFolders = contextFile.listFiles();
          if (userFolders != null) {
            Lists.newArrayList(userFolders).stream()
                .filter(File::isDirectory)
                .filter(file -> Strings.isNullOrEmpty(user) || file.getName().equals(user))
                .filter(file -> isUserHomeFolderReadable(file.getName()))
                .forEach(userFolder -> {
                  File[] workspaces = userFolder.listFiles();
                  if (workspaces != null) {
                    Lists.newArrayList(workspaces).stream()
                        .filter(File::isDirectory)
                        .filter(file -> Strings.isNullOrEmpty(name) || file.getName().equals(name))
                        .forEach(file -> {
                          try {
                            FileUtil.delete(file);
                          } catch (IOException e) {
                            // ignore
                          }
                        });
                  }
                });
          }
        });
    return Response.ok().build();
  }

  private boolean isUserHomeFolderReadable(String user) {
    return authorizer
        .isPermitted("rest:/files/home/" + user + ":GET");
  }

}
