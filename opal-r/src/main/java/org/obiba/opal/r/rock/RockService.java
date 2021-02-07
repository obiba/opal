/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.shiro.SecurityUtils;
import org.json.JSONObject;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.r.service.RServerState;
import org.obiba.opal.spi.r.*;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.NoSuchRPackageException;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * R server service built over a Rock application that got registered.
 */
@Component("rockRService")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RockService implements RServerService {

  private static final Logger log = LoggerFactory.getLogger(RockService.class);

  private String clusterName;

  private App app;

  private final TransactionalThreadFactory transactionalThreadFactory;

  private Credentials managerCredentials;

  private Credentials userCredentials;

  @Value("${rock.defaults.manager.username}")
  private String managerUsername;

  @Value("${rock.defaults.manager.password}")
  private String managerPassword;

  @Value("${rock.defaults.user.username}")
  private String userUsername;

  @Value("${rock.defaults.user.password}")
  private String userPassword;

  @Autowired
  public RockService(TransactionalThreadFactory transactionalThreadFactory) {
    this.transactionalThreadFactory = transactionalThreadFactory;
  }

  public void setRServerClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public void setApp(App app) {
    this.app = app;
  }

  @Override
  public String getName() {
    return app.getName();
  }

  @Override
  public void start() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.exchange(getRServerResourceUrl("/rserver"), HttpMethod.PUT, new HttpEntity<>(createHeaders()), Void.class);
    } catch (RestClientException e) {
      log.warn("Error when starting R server: " + e.getMessage());
    }
  }

  @Override
  public void stop() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.exchange(getRServerResourceUrl("/rserver"), HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Void.class);
    } catch (RestClientException e) {
      log.warn("Error when stopping R server: " + e.getMessage());
    }
  }

  @Override
  public boolean isRunning() {
    try {
      RServerState state = getState();
      return state.isRunning();
    } catch (RestClientException e) {
      log.warn("Error when checking R server: " + e.getMessage());
    }
    return false;
  }

  @Override
  public RServerState getState() {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<RockServerInfo> response =
        restTemplate.exchange(getRServerResourceUrl("/rserver"), HttpMethod.GET, new HttpEntity<>(createHeaders()), RockServerInfo.class);
    return new RockState(response.getBody());
  }

  @Override
  public App getApp() {
    return app;
  }

  @Override
  public boolean isFor(App app) {
    return this.app.equals(app);
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    List<OpalR.RPackageDto> pkgs = Lists.newArrayList();
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<RockStringMatrix> response =
          restTemplate.exchange(getRServerResourceUrl("/rserver/packages"), HttpMethod.GET, new HttpEntity<>(createHeaders()), RockStringMatrix.class);
      RockStringMatrix matrix = response.getBody();
      pkgs = matrix.iterateRows().stream()
          .map(new RPackageResourceHelper.StringsToRPackageDto(matrix))
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error when reading installed packages", e);
    }
    return pkgs;
  }

  @Override
  public OpalR.RPackageDto getInstalledPackageDto(String name) {
    try {
      HttpHeaders headers = createHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response =
          restTemplate.exchange(getRServerResourceUrl("/rserver/package/" + name), HttpMethod.GET, new HttpEntity<>(headers), String.class);
      String jsonSource = response.getBody();
      if (response.getStatusCode().is2xxSuccessful()) {
        RockResult result = new RockResult(new JSONObject(jsonSource));
        if (result.isNamedList()) {
          RNamedList<RServerResult> namedList = result.asNamedList();
          OpalR.RPackageDto.Builder builder = OpalR.RPackageDto.newBuilder();
          for (String key : namedList.getNames()) {
            if ("name".equals(key))
              builder.setName(namedList.get(key).asStrings()[0]);
            else
              builder.addDescription(Opal.EntryDto.newBuilder().setKey(key).setValue(namedList.get(key).asStrings()[0]));
          }
          return builder.build();
        }
      }
    } catch (Exception e) {
      log.error("Error when reading installed package: " + name, e);
    }
    throw new NoSuchRPackageException(name);
  }

  @Override
  public List<String> getInstalledDataSHIELDPackageNames() {
    try {
      HttpHeaders headers = createHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response =
          restTemplate.exchange(getRServerResourceUrl("/rserver/packages/_datashield"), HttpMethod.GET, new HttpEntity<>(headers), String.class);
      String jsonSource = response.getBody();
      if (response.getStatusCode().is2xxSuccessful()) {
        RockResult result = new RockResult(new JSONObject(jsonSource));
        return Lists.newArrayList(result.getNames());
      }
    } catch (Exception e) {
      log.error("Error when reading installed DataSHIELD packages", e);
    }
    return Lists.newArrayList();
  }

  @Override
  public void removePackage(String name) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.exchange(getRServerResourceUrl("/rserver/package/" + name), HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Void.class);
    } catch (Exception e) {
      log.error("Error when removing a package", e);
    }
  }

  @Override
  public void ensureCRANPackage(String name) throws RServerException {
    execute(String.format("if (!require(%s)) { install.packages('%s') }", name, name));
  }

  @Override
  public void installCRANPackage(String name) {
    Map<String, String> params = Maps.newHashMap();
    params.put("name", name);
    params.put("manager", "cran");
    installPackage(params);
  }

  @Override
  public void installGitHubPackage(String name, String ref) {
    Map<String, String> params = Maps.newHashMap();
    params.put("name", name);
    if (!Strings.isNullOrEmpty(ref))
      params.put("ref", ref);
    params.put("manager", "github");
    installPackage(params);
  }

  @Override
  public void installBioconductorPackage(String name) {
    Map<String, String> params = Maps.newHashMap();
    params.put("name", name);
    params.put("manager", "bioconductor");
    installPackage(params);
  }

  @Override
  public void updateAllCRANPackages() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.exchange(getRServerResourceUrl("/rserver/packages"), HttpMethod.PUT, new HttpEntity<>(createHeaders()), Void.class);
    } catch (Exception e) {
      log.error("Error when updating all packages", e);
    }
  }

  @Override
  public String[] getLog(Integer nbLines) {
    try {
      HttpHeaders headers = createHeaders();
      headers.setAccept(Lists.newArrayList(MediaType.TEXT_PLAIN));
      RestTemplate restTemplate = new RestTemplate();
      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getRServerResourceUrl("/rserver/_log"))
          .queryParam("limit", nbLines);
      ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
      return response.getBody().split("\n");
    } catch (RestClientException e) {
      log.warn("Error while getting R server log", e);
      return new String[0];
    }
  }

  @Override
  public RServerSession newRServerSession(String user) throws RServerException {
    return new RockSession(clusterName, app, getUserCredentials(), user, transactionalThreadFactory);
  }

  @Override
  public void execute(ROperation rop) throws RServerException {
    Object principal = SecurityUtils.getSubject().getPrincipal();
    RServerSession rSession = newRServerSession(principal == null ? "opal" : principal.toString());
    try {
      rSession.execute(rop);
    } finally {
      rSession.close();
    }
  }

  //
  // Private methods
  //

  private void installPackage(Map<String, String> params) {
    try {
      HttpHeaders headers = createHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getRServerResourceUrl("/rserver/packages"));
      params.forEach(builder::queryParam);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(headers), Void.class, params);
    } catch (Exception e) {
      log.error("Error when installing a package", e);
    }
  }

  private String getRServerResourceUrl(String path) {
    return app.getServer() + path;
  }

  private HttpHeaders createHeaders() {
    return new HttpHeaders() {{
      String auth = getManagerCredentials().getUserPrincipal().getName() + ":" + getManagerCredentials().getPassword();
      byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
      String authHeader = "Basic " + new String(encodedAuth);
      add("Authorization", authHeader);
    }};
  }

  private void execute(String cmd) throws RServerException {
    RScriptROperation rop = new RScriptROperation(cmd, false);
    execute(rop);
  }

  public Credentials getManagerCredentials() {
    if (managerCredentials == null)
      managerCredentials = new UsernamePasswordCredentials(managerUsername, managerPassword);
    return managerCredentials;
  }

  public Credentials getUserCredentials() {
    if (userCredentials == null)
      userCredentials = new UsernamePasswordCredentials(userUsername, userPassword);
    return userCredentials;
  }
}
