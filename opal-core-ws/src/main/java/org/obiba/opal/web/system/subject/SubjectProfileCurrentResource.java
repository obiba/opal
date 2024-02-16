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

import com.google.common.base.Strings;
import org.apache.shiro.SecurityUtils;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.IDProvidersService;
import org.obiba.opal.core.service.security.TotpService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.SQL;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.obiba.opal.web.model.Opal.BookmarkDto;

@Component
@Scope("request")
@Path("/system/subject-profile/_current")
public class SubjectProfileCurrentResource {
  private static final Logger log = LoggerFactory.getLogger(SubjectProfileCurrentResource.class);

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @Autowired
  private IDProvidersService idProvidersService;

  @Autowired
  private TotpService totpService;

  @Value("${org.obiba.realm.publicUrl}")
  private String obibaRealmPublicUrl;

  @GET
  @NoAuthorization
  public Response get() {
    SubjectProfile profile = subjectProfileService.getProfile(getPrincipal());
    String accountUrl = null;
    for (String realm : profile.getRealms()) {
      if ("obiba-realm".equals(realm) && !Strings.isNullOrEmpty(obibaRealmPublicUrl))
        accountUrl = String.format("%s/profile", obibaRealmPublicUrl);
      else
        try {
          OIDCConfiguration config = idProvidersService.getConfiguration(realm);
          accountUrl = config.getCustomParam("providerUrl");
        } catch (Exception e) {
          // ignored, does not apply
        }
      if (!Strings.isNullOrEmpty(accountUrl)) break;
    }
    Opal.SubjectProfileDto dto = Dtos.asDto(profile, accountUrl)
        .toBuilder()
        .setOtpRequired(isSubjectProfileSecretRequired(profile.getPrincipal()))
        .build();
    return Response.ok().entity(dto).build();
  }

  @PUT
  @Path("/otp")
  @Produces("text/plain")
  @NoAuthorization
  public Response enableOtp() {
    subjectProfileService.updateProfileSecret(getPrincipal(), true);
    SubjectProfile profile = subjectProfileService.getProfile(getPrincipal());
    return Response.ok(totpService.getQrImageDataUri(getPrincipal(), profile.getSecret()), "text/plain").build();
  }

  @DELETE
  @Path("/otp")
  @NoAuthorization
  public Response disableOtp() {
    subjectProfileService.updateProfileSecret(getPrincipal(), false);
    return Response.ok().build();
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
    log.debug("Getting configuration defaultCharSet");
    String defaultCharacterSet = opalGeneralConfigService.getConfig().getDefaultCharacterSet();
    log.debug("Retrieving current user's bookmark with path: {} and default Charset: {}", path, defaultCharacterSet);

    BookmarkResource resource = applicationContext.getBean(BookmarkResource.class);
    resource.setPrincipal(getPrincipal());
    resource.setPath(URLDecoder.decode(path, defaultCharacterSet));
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

  @Path("/sql-history")
  @GET
  @NoAuthorization
  public List<SQL.SQLExecutionDto> getSQLHistory(@QueryParam("datasource") String datasource,
                                                 @QueryParam("offset") @DefaultValue("0") int offset,
                                                 @QueryParam("limit") @DefaultValue("100") int limit) {
    SQLHistoryResource resource = applicationContext.getBean(SQLHistoryResource.class);
    resource.setSubject(getPrincipal());
    return resource.getSQLHistory(datasource, offset, limit);
  }

  private String getPrincipal() {
    return (String) SecurityUtils.getSubject().getPrincipal();
  }

  private List<String> decodeResources(Iterable<String> resourceIterator) throws UnsupportedEncodingException {
    OpalGeneralConfig config = opalGeneralConfigService.getConfig();

    List<String> decodedResources = new ArrayList<>();
    for (String resource : resourceIterator) {
      decodedResources.add(URLDecoder.decode(resource, config.getDefaultCharacterSet()));
    }

    return decodedResources;
  }

  private boolean isSubjectProfileSecretRequired(String principal) {
    boolean secretRequired = false;
    if (opalGeneralConfigService.getConfig().isEnforced2FA()) {
      SubjectProfile profile = subjectProfileService.getProfile(principal);
      boolean otpRealm = StreamSupport.stream(profile.getRealms().spliterator(), false)
          .anyMatch(realm -> realm.equals("opal-user-realm") || realm.equals("opal-ini-realm"));
      secretRequired = otpRealm && !profile.hasSecret();
    }
    return secretRequired;
  }

}