/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import jakarta.ws.rs.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.utils.OIDCHelper;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.security.AuthConfigurationProvider;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.security.SystemKeyStoreService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.KeyStoreResource;
import org.obiba.opal.web.system.news.ObibaNews;
import org.obiba.opal.web.system.news.ObibaNewsDatum;
import org.obiba.opal.web.system.news.ObibaNewsYaml;
import org.obiba.opal.web.taxonomy.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.obiba.runtime.upgrade.VersionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.lang.management.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.obiba.opal.web.model.Database.DatabasesStatusDto;

@Component
@Path("/system")
public class SystemResource {

  private static final Logger log = LoggerFactory.getLogger(SystemResource.class);

  @Autowired
  private VersionProvider opalVersionProvider;

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @Autowired
  private TaxonomyService taxonomyService;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private SystemKeyStoreService systemKeyStoreService;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private AuthConfigurationProvider authConfigurationProvider;

  @Value("${org.obiba.opal.public.url}")
  private String defaultOpalPublicUrl;

  @GET
  @NoAuthorization
  @Path("/version")
  public String getVersion() {
    return opalVersionProvider.getVersion().toString();
  }

  @GET
  @NoAuthorization
  @Path("/news")
  public Opal.NewsDto getNews() {
    Opal.NewsDto.Builder builder = Opal.NewsDto.newBuilder();
    try {
      ObibaNews news = ObibaNewsYaml.loadNews();
      for (ObibaNewsDatum datum : news.getData()) {
        if (datum.getTitle().toLowerCase().contains("opal") || (datum.hasSummary() && datum.getSummary().toLowerCase().contains("opal"))) {
          Opal.NewsDto.NoteDto.Builder note = builder.addNotesBuilder()
              .setTitle(datum.getTitle())
              .setLink(datum.getLink())
              .setDate(datum.getDate());
          if (datum.hasSummary())
            note.setSummary(datum.getSummary());
        }
      }
    } catch (Exception e) {
      // ignore
    }
    return builder.build();
  }


  @GET
  @Path("/env")
  public Opal.OpalEnv getEnvironment() {

    Collection<Opal.EntryDto> systemProperties = new ArrayList<>();
    Collection<String> keys = ManagementFactory.getRuntimeMXBean().getSystemProperties().keySet();
    Map<String, String> properties = ManagementFactory.getRuntimeMXBean().getSystemProperties();
    for (String k : keys) {
      Opal.EntryDto entry = Opal.EntryDto.newBuilder().setKey(k).setValue(properties.get(k)).build();
      systemProperties.add(entry);
    }

    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    return Opal.OpalEnv.newBuilder() //
        .setVersion(opalVersionProvider.getVersion().toString()) //
        .setVmName(runtimeMXBean.getVmName()) //
        .setVmVendor(runtimeMXBean.getVmVendor()) //
        .setVmVersion(runtimeMXBean.getVmVersion()) //
        .setJavaVersion(System.getProperty("java.version")) //
        .addAllSystemProperties(systemProperties).build();
  }

  @GET
  @Path("/status")
  public Opal.OpalStatus getStatus() {
    List<GarbageCollectorMXBean> garbageCollectorMXBeanList = ManagementFactory.getGarbageCollectorMXBeans();
    Collection<Opal.OpalStatus.GarbageCollectorUsage> garbageCollectorUsagesValues = new ArrayList<>();
    for (GarbageCollectorMXBean GC : garbageCollectorMXBeanList) {
      garbageCollectorUsagesValues.add(getGarbageCollector(GC));
    }

    return Opal.OpalStatus.newBuilder()//
        .setTimestamp(System.currentTimeMillis())//
        .setUptime(ManagementFactory.getRuntimeMXBean().getUptime())
        .setHeapMemory(getMemory(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()))
        .setNonHeapMemory(getMemory(ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage()))
        .setThreads(getThread(ManagementFactory.getThreadMXBean()))//
        .addAllGcs(garbageCollectorUsagesValues)//
        .build();
  }

  @PUT
  @Path("/status/gc")
  public Response launchGC() {
    System.gc();
    return Response.noContent().build();
  }

  @GET
  @Path("/status/databases")
  @NoAuthorization
  public DatabasesStatusDto getDatabasesStatus() {
    DatabasesStatusDto.Builder db = DatabasesStatusDto.newBuilder();
    db.setHasIdentifiers(databaseRegistry.hasIdentifiersDatabase());
    db.setHasStorage(databaseRegistry.hasDatabases(Database.Usage.STORAGE));
    return db.build();
  }

  private Opal.OpalStatus.MemoryUsage getMemory(MemoryUsage memoryUsage) {
    return Opal.OpalStatus.MemoryUsage.newBuilder()//
        .setInit(memoryUsage.getInit())//
        .setUsed(memoryUsage.getUsed())//
        .setCommitted(memoryUsage.getCommitted())//
        .setMax(memoryUsage.getMax())//
        .build();
  }

  private Opal.OpalStatus.ThreadsUsage getThread(ThreadMXBean threadMXBean) {
    return Opal.OpalStatus.ThreadsUsage.newBuilder()//
        .setCount(threadMXBean.getThreadCount())//
        .setPeak(threadMXBean.getPeakThreadCount())//
        .build();
  }

  private Opal.OpalStatus.GarbageCollectorUsage getGarbageCollector(GarbageCollectorMXBean garbageCollectorMXBean) {
    return Opal.OpalStatus.GarbageCollectorUsage.newBuilder()//
        .setName(garbageCollectorMXBean.getName())//
        .setCollectionCount(garbageCollectorMXBean.getCollectionCount())//
        .setCollectionTime(garbageCollectorMXBean.getCollectionTime())//
        .build();
  }

  @GET
  @Path("/conf")
  public Opal.OpalConf getOpalConfiguration() {
    Collection<Opal.TaxonomyDto> taxonomies = new ArrayList<>();
    for (Taxonomy taxonomy : taxonomyService.getTaxonomies()) {
      taxonomies.add(Dtos.asDto(taxonomy));
    }
    return Opal.OpalConf.newBuilder()//
        .setGeneral(getOpalGeneralConfiguration()).addAllTaxonomies(taxonomies)//
        .build();
  }

  @GET
  @Path("/conf/general")
  @NotAuthenticated // allow anonymous to be able to retrieve Opal instance name
  public Opal.GeneralConf getOpalGeneralConfiguration() {
    OpalGeneralConfig conf = opalGeneralConfigService.getConfig();
    Opal.GeneralConf.Builder builder = Opal.GeneralConf.newBuilder()
        .setName(conf.getName())
        .addAllLanguages(conf.getLocalesAsString())
        .setDefaultCharSet(conf.getDefaultCharacterSet())
        .setEnforced2FA(conf.isEnforced2FA())
        .setAllowRPackageManagement(conf.isAllowRPackageManagement());

    if (!Strings.isNullOrEmpty(conf.getPublicUrl())) {
      builder.setPublicURL(conf.getPublicUrl());
    }

    String logoutUrl = getLogoutUrl();
    if (!Strings.isNullOrEmpty(logoutUrl)) {
      builder.setLogoutURL(logoutUrl);
    }

    return builder.build();
  }

  @PUT
  @Path("/conf/general")
  public Response updateGeneralConfigurations(Opal.GeneralConf dto) {
    OpalGeneralConfig conf = opalGeneralConfigService.getConfig();
    conf.setName(dto.getName().isEmpty() ? OpalGeneralConfig.DEFAULT_NAME : dto.getName());
    conf.setPublicUrl(dto.getPublicURL());
    conf.setLogoutUrl(dto.getLogoutURL());
    conf.setEnforced2FA(dto.getEnforced2FA());
    conf.setAllowRPackageManagement(dto.getAllowRPackageManagement());

    if (dto.getLanguagesList().isEmpty()) {
      conf.setLocales(Lists.newArrayList(OpalGeneralConfig.DEFAULT_LOCALE));
    } else {
      conf.setLocales(Lists.newArrayList(dto.getLanguagesList().stream().map(Locale::new).collect(Collectors.toList())));
    }

    conf.setDefaultCharacterSet(
        dto.getDefaultCharSet().isEmpty() ? OpalGeneralConfig.DEFAULT_CHARSET : dto.getDefaultCharSet());

    opalGeneralConfigService.save(conf);

    return Response.ok().build();
  }

  @PUT
  @Path("/conf/general/_rPackage")
  public Response enableAllowRPackageManagement() {
    OpalGeneralConfig conf = opalGeneralConfigService.getConfig();
    if (!conf.isAllowRPackageManagement()) {
      conf.setAllowRPackageManagement(true);
      opalGeneralConfigService.save(conf);
    }
    return Response.ok().build();
  }

  @DELETE
  @Path("/conf/general/_rPackage")
  public Response disableAllowRPackageManagement() {
    OpalGeneralConfig conf = opalGeneralConfigService.getConfig();
    if (conf.isAllowRPackageManagement()) {
      conf.setAllowRPackageManagement(false);
      opalGeneralConfigService.save(conf);
    }
    return Response.ok().build();
  }

  @GET
  @Path("/name")
  @Produces("text/plain")
  @NotAuthenticated
  public Response getApplicationName() {
    OpalGeneralConfig conf = opalGeneralConfigService.getConfig();
    return Response.ok().entity(conf.getName()).build();
  }

  @GET
  @Path("/charset")
  @Produces("text/plain")
  @NoAuthorization
  public Response getDefaultCharset() {
    return Response.ok().entity(opalGeneralConfigService.getConfig().getDefaultCharacterSet()).build();
  }

  @Path("/keystore")
  public KeyStoreResource getKeyStoreResource() {
    KeyStoreResource resource = applicationContext.getBean(KeyStoreResource.class);
    resource.setKeyStore(systemKeyStoreService.getKeyStore());
    return resource;
  }

  private String getLogoutUrl() {
    OpalGeneralConfig opalConfig = opalGeneralConfigService.getConfig();
    if (SecurityUtils.getSubject().isAuthenticated()) {
      PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();

      if (principals != null) {
        for (String realmName : principals.getRealmNames()) {
          OIDCConfiguration oidcConfig = authConfigurationProvider.getConfiguration(realmName);
          if (oidcConfig != null) {
            boolean useLogout = true;
            if (oidcConfig.getCustomParams().containsKey("useLogout")) {
              try {
                useLogout = Boolean.parseBoolean(oidcConfig.getCustomParam("useLogout"));
              } catch (Exception e) {
                // ignore
              }
            }

            try {
              OIDCProviderMetadata metadata = OIDCHelper.discoverProviderMetaData(oidcConfig);
              URI logoutEndpoint = metadata.getEndSessionEndpointURI();
              if (useLogout && logoutEndpoint != null) {
                log.debug("Using {} logout endpoint: {}", realmName, logoutEndpoint);
                String logoutRedirect = opalConfig.getLogoutUrl();
                if (Strings.isNullOrEmpty(logoutRedirect)) {
                  logoutRedirect = opalConfig.getPublicUrl();
                }
                if (Strings.isNullOrEmpty(logoutRedirect)) {
                  logoutRedirect = defaultOpalPublicUrl;
                }
                UriBuilder logoutURIBuilder = UriBuilder.fromUri(logoutEndpoint);
                if (!Strings.isNullOrEmpty(logoutRedirect)) {
                  logoutURIBuilder.queryParam("post_logout_redirect_uri", logoutRedirect);
                }
                logoutURIBuilder.queryParam("client_id", oidcConfig.getClientId());
                return logoutURIBuilder.build().toString();
              }
            } catch (Exception e) {
              log.error("Error when getting OIDC logout URL {}", realmName, e);
            }
          }
        }
      }
    }

    return opalConfig.getLogoutUrl();
  }

}