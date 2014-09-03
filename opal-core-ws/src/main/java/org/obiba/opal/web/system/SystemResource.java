package org.obiba.opal.web.system;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.security.SystemKeyStoreService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.KeyStoreResource;
import org.obiba.opal.web.taxonomy.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.obiba.runtime.upgrade.VersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.obiba.opal.web.model.Database.DatabasesStatusDto;

@Component
@Path("/system")
public class SystemResource {

  @Autowired
  private VersionProvider opalVersionProvider;

  @Autowired
  private OpalGeneralConfigService serverService;

  @Autowired
  private TaxonomyService taxonomyService;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private SystemKeyStoreService systemKeyStoreService;

  @Autowired
  private ApplicationContext applicationContext;

  @GET
  @Path("/version")
  public String getVersion() {
    return opalVersionProvider.getVersion().toString();
  }

  @GET
  @Path("/env")
  public Opal.OpalEnv getEnvironment() {

    Collection<Opal.EntryDto> systemProperties = new ArrayList<>();
    Collection<String> keys = ManagementFactory.getRuntimeMXBean().getSystemProperties().keySet();
    Map<String, String> properties = ManagementFactory.getRuntimeMXBean().getSystemProperties();
    for(String k : keys) {
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
    for(GarbageCollectorMXBean GC : garbageCollectorMXBeanList) {
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
    for(Taxonomy taxonomy : taxonomyService.getTaxonomies()) {
      taxonomies.add(Dtos.asDto(taxonomy));
    }
    return Opal.OpalConf.newBuilder()//
        .setGeneral(getOpalGeneralConfiguration()).addAllTaxonomies(taxonomies)//
        .build();
  }

  @GET
  @Path("/conf/general")
  @NotAuthenticated // allow anonymous to be able to retrive Opal instance name
  public Opal.GeneralConf getOpalGeneralConfiguration() {
    OpalGeneralConfig conf = serverService.getConfig();
    Opal.GeneralConf.Builder builder = Opal.GeneralConf.newBuilder()//
        .setName(conf.getName())//
        .addAllLanguages(conf.getLocalesAsString())//
        .setDefaultCharSet(conf.getDefaultCharacterSet());

    if(conf.getPublicUrl() != null) {
      builder.setPublicURL(conf.getPublicUrl());
    }

    return builder.build();
  }

  @PUT
  @Path("/conf/general")
  public Response updateGeneralConfigurations(Opal.GeneralConf dto) {
    OpalGeneralConfig conf = serverService.getConfig();
    conf.setName(dto.getName().isEmpty() ? OpalGeneralConfig.DEFAULT_NAME : dto.getName());
    conf.setPublicUrl(dto.getPublicURL());

    if(dto.getLanguagesList().isEmpty()) {
      conf.setLocales(Lists.newArrayList(OpalGeneralConfig.DEFAULT_LOCALE));
    } else {
      conf.setLocales(Lists.newArrayList(Iterables.transform(dto.getLanguagesList(), new Function<String, Locale>() {
        @Override
        public Locale apply(String locale) {
          return new Locale(locale);
        }
      })));
    }

    conf.setDefaultCharacterSet(
        dto.getDefaultCharSet().isEmpty() ? OpalGeneralConfig.DEFAULT_CHARSET : dto.getDefaultCharSet());

    serverService.save(conf);

    return Response.ok().build();
  }

  @GET
  @Path("/name")
  @NotAuthenticated
  public Response getApplicationName() {
    OpalGeneralConfig conf = serverService.getConfig();
    return Response.ok().entity(conf.getName()).build();
  }

  @GET
  @Path("/charset")
  @NoAuthorization
  public Response getDefaultCharset() {
    return Response.ok().entity(serverService.getConfig().getDefaultCharacterSet()).build();
  }

  @Path("/keystore")
  public KeyStoreResource getKeyStoreResource() {
    KeyStoreResource resource = applicationContext.getBean(KeyStoreResource.class);
    resource.setKeyStore(systemKeyStoreService.getKeyStore());
    return resource;
  }
}