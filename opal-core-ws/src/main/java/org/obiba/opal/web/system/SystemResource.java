package org.obiba.opal.web.system;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.obiba.opal.web.model.Opal;
import org.obiba.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/system")
public class SystemResource {

  private final Version opalVersion;

  @Autowired
  public SystemResource(Version opalVersion) {
    this.opalVersion = opalVersion;
  }

  @GET
  @Path("/version")
  public String getVersion() {
    return opalVersion.toString();
  }

  @GET
  @Path("/env")
  public Opal.OpalEnv getEnvironment() {

    Collection<Opal.EntryDto> systemProperties = new ArrayList<Opal.EntryDto>();
    Collection<String> keys = ManagementFactory.getRuntimeMXBean().getSystemProperties().keySet();
    Map<String, String> properties = ManagementFactory.getRuntimeMXBean().getSystemProperties();
    for(String k : keys) {
      Opal.EntryDto entry = Opal.EntryDto.newBuilder().setKey(k).setValue(properties.get(k)).build();
      systemProperties.add(entry);
    }

    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    return Opal.OpalEnv.newBuilder() //
        .setVersion(opalVersion.toString()) //
        .setVmName(runtimeMXBean.getVmName()) //
        .setVmVendor(runtimeMXBean.getVmVendor()) //
        .setVmVersion(runtimeMXBean.getVmVersion())//
        .setJavaVersion(System.getProperty("java.version")) //
        .addAllSystemProperties(systemProperties).build();
  }

  @GET
  @Path("/status")
  public Opal.OpalStatus getStatus() {

    List<GarbageCollectorMXBean> garbageCollectorMXBeanList = ManagementFactory.getGarbageCollectorMXBeans();
    Collection<Opal.OpalStatus.GarbageCollectorUsage> garbageCollectorUsagesValues
        = new ArrayList<Opal.OpalStatus.GarbageCollectorUsage>();
    for(GarbageCollectorMXBean GC : garbageCollectorMXBeanList) {
      garbageCollectorUsagesValues.add(getGarbageCollector(GC));
    }

    return Opal.OpalStatus.newBuilder()//
        .setTimestamp(System.currentTimeMillis())//
        .setUptime(ManagementFactory.getRuntimeMXBean().getUptime())
        .setHeapMemory(getMemory(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()))
        .setNonHeapMemory(getMemory(ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage()))
        .setThreads(getThread(ManagementFactory.getThreadMXBean()))//
        .addAllGcs(garbageCollectorUsagesValues).build();
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

}