/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.system.subject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.obiba.opal.web.model.Opal.BookmarkDto;

@Component
@Scope("request")
@Path("/system/subject-profile/_current")
public class SubjectProfileCurrentResource {

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @GET
  @NoAuthorization
  public Response get() {
    return Response.ok().entity(Dtos.asDto(subjectProfileService.getProfile(getPrincipal()))).build();
  }

  @Path("/bookmarks")
  @GET
  @NoAuthorization
  public List<BookmarkDto> getBookmarks() {
    BookmarksResource resource = applicationContext.getBean(BookmarksResource.class);
    resource.setPrincipal(getPrincipal());
    return resource.getBookmarks();
  }

  @Path("/bookmark/{path:.*}")
  @GET
  @NoAuthorization
  public Response getBookmark(@PathParam("path") String path) throws UnsupportedEncodingException {
    BookmarkResource resource = applicationContext.getBean(BookmarkResource.class);
    resource.setPrincipal(getPrincipal());
    resource.setPath(URLDecoder.decode(path, opalGeneralConfigService.getConfig().getDefaultCharacterSet()));
    return resource.get();
  }

  @Path("/bookmarks")
  @POST
  @NoAuthorization
  public Response addBookmarks(@QueryParam("resource") List<String> resources) throws UnsupportedEncodingException {
    BookmarksResource resource = applicationContext.getBean(BookmarksResource.class);
    resource.setPrincipal(getPrincipal());
    return resource.addBookmarks(decodeResources(resources));
  }

  @Path("/bookmark/{path:.*}")
  @DELETE
  @NoAuthorization
  public Response deleteBookmark(@PathParam("path") String path) throws UnsupportedEncodingException {
    BookmarkResource resource = applicationContext.getBean(BookmarkResource.class);
    resource.setPrincipal(getPrincipal());
    resource.setPath(URLDecoder.decode(path, opalGeneralConfigService.getConfig().getDefaultCharacterSet()));
    return resource.delete();
  }

  private String getPrincipal() {
    return (String)SecurityUtils.getSubject().getPrincipal();
  }

  private List<String> decodeResources(Iterable<String> resourceIterator) throws UnsupportedEncodingException {
    OpalGeneralConfig config = opalGeneralConfigService.getConfig();

    List<String> decodedResources = new ArrayList<>();
    for (String resource : resourceIterator) {
      decodedResources.add(URLDecoder.decode(resource, config.getDefaultCharacterSet()));
    }

    return decodedResources;
  }

}