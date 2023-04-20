/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.cluster;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.r.service.*;
import org.obiba.opal.r.service.event.RPackageInstalledEvent;
import org.obiba.opal.r.service.event.RPackageRemovedEvent;
import org.obiba.opal.r.service.event.RServerServiceStartedEvent;
import org.obiba.opal.r.service.event.RServerServiceStoppedEvent;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.RServerException;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RServerCluster implements RServerClusterService {

  private static final Logger log = LoggerFactory.getLogger(RServerCluster.class);

  private final String name;

  private final List<RServerService> rServerServices = Collections.synchronizedList(Lists.newArrayList());

  private final EventBus eventBus;

  public RServerCluster(String name, EventBus eventBus) {
    this.eventBus = eventBus;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void addRServerService(RServerService service) {
    rServerServices.add(service);
  }

  public void removeRServerService(App app) {
    try {
      Optional<RServerService> service = rServerServices.stream()
          .filter(s -> s.isFor(app)).findFirst();
      service.ifPresent(rServerServices::remove);
    } catch (Exception e) {
      // ignored
    }
  }

  public List<RServerService> getRServerServices() {
    return rServerServices;
  }

  public RServerService getRServerService(String sname) {
    Optional<RServerService> service = rServerServices.stream().filter(s -> s.getName().equals(sname)).findFirst();
    if (service.isPresent()) return service.get();
    throw new NoSuchElementException("No R server with name: " + sname + " in cluster " + name);
  }

  //
  // R servers proxy
  //

  /**
   * Dispatch R server start.
   */
  @Override
  public void start() {
    rServerServices.forEach(RServerService::start);
    ensureCRANPackage("resourcer");
    ensureCRANPackage("sqldf");
    eventBus.post(new RServerServiceStartedEvent(getName()));
  }

  /**
   * Dispatch R server stop.
   */
  @Override
  public void stop() {
    rServerServices.forEach(RServerService::stop);
    eventBus.post(new RServerServiceStoppedEvent(getName()));
  }

  /**
   * Check if any R server is running.
   *
   * @return
   */
  @Override
  public boolean isRunning() {
    return rServerServices.stream().anyMatch(RServerService::isRunning);
  }

  /**
   * Merge R server states.
   *
   * @return
   */
  @Override
  public RServerState getState() {
    RServerClusterState state = new RServerClusterState(getName());
    rServerServices.stream()
        .map(s -> {
          try {
            return s.getState();
          } catch (RServerException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .forEach(s -> {
          state.setVersion(s.getVersion());
          if (!state.isRunning())
            state.setRunning(state.isRunning());
          state.addTags(s.getTags());
          state.addRSessionsCount(s.getRSessionsCount());
          state.addBusyRSessionsCount(s.getBusyRSessionsCount());
          state.addSystemCores(s.getSystemCores());
          state.addSystemFreeMemory(s.getSystemFreeMemory());
        });
    return state;
  }

  @Override
  public RServerSession newRServerSession(String user) throws RServerException {
    RServerSession session = getNextRServerService().newRServerSession(user);
    session.setProfile(asProfile());
    return session;
  }

  @Override
  public void execute(ROperation rop) throws RServerException {
    Object principal = SecurityUtils.getSubject().getPrincipal();
    RServerSession rSession = getNextRServerService().newRServerSession(principal == null ? "opal/system" : principal.toString());
    rSession.setProfile(asProfile());
    try {
      rSession.execute(rop);
    } finally {
      rSession.close();
    }
  }

  @Override
  public App getApp() {
    return null;
  }

  @Override
  public boolean isFor(App app) {
    return false;
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    List<OpalR.RPackageDto> allPackages = Lists.newArrayList();
    ExecutorService executor = Executors.newFixedThreadPool(rServerServices.size());
    try {
      List<Future<List<OpalR.RPackageDto>>> futurePkgs = executor.invokeAll(rServerServices.stream()
          .map(service -> (Callable<List<OpalR.RPackageDto>>) service::getInstalledPackagesDtos)
          .collect(Collectors.toList()));
      for (Future<List<OpalR.RPackageDto>> fPkgs : futurePkgs) {
        try {
          allPackages.addAll(fPkgs.get());
        } catch (ExecutionException e) {
          // ignore
        }
      }
      allPackages.sort(Comparator.comparing(OpalR.RPackageDto::getName));
    } catch (InterruptedException e) {
      log.error("Cannot retrieve all R packages", e);
    }
    return allPackages;
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackageDto(String name) {
    List<OpalR.RPackageDto> allPackages = Lists.newArrayList();
    ExecutorService executor = Executors.newFixedThreadPool(rServerServices.size());
    try {
      List<Future<List<OpalR.RPackageDto>>> futurePkgs = executor.invokeAll(rServerServices.stream()
          .map(service -> (Callable<List<OpalR.RPackageDto>>) service.getInstalledPackageDto(name))
          .collect(Collectors.toList()));
      for (Future<List<OpalR.RPackageDto>> fPkgs : futurePkgs) {
        try {
          allPackages.addAll(fPkgs.get());
        } catch (ExecutionException e) {
          // ignore
        }
      }
      allPackages.sort(Comparator.comparing(OpalR.RPackageDto::getName));
    } catch (InterruptedException e) {
      log.error("Cannot retrieve all R packages", e);
    }
    return allPackages;
  }

  @Override
  public Map<String, List<Opal.EntryDto>> getDataShieldPackagesProperties() {
    Map<String, List<Opal.EntryDto>> allProperties = Maps.newHashMap();
    ExecutorService executor = Executors.newFixedThreadPool(rServerServices.size());
    try {
      List<Future<Map<String, List<Opal.EntryDto>>>> futureProps = executor.invokeAll(rServerServices.stream()
          .map(service -> (Callable<Map<String, List<Opal.EntryDto>>>) service::getDataShieldPackagesProperties)
          .collect(Collectors.toList()));
      for (Future<Map<String, List<Opal.EntryDto>>> fProps : futureProps) {
        try {
          allProperties.putAll(fProps.get());
        } catch (ExecutionException e) {
          // ignore
        }
      }
    } catch (InterruptedException e) {
      log.error("Cannot retrieve all Datashield packages properties", e);
    }
    return allProperties;
  }

  @Override
  public void removePackage(String name) {
    invokeAll(rServerServices.stream().map(service -> (Callable<Void>) () -> {
      try {
        service.removePackage(name);
      } catch (RServerException e) {
        log.warn("Failed to remove R package on {}: {}", service.getName(), name, e);
      }
      return null;
    }).collect(Collectors.toList()));
    eventBus.post(new RPackageRemovedEvent(getName(), name));
  }

  @Override
  public void ensureCRANPackage(String name) {
    invokeAll(rServerServices.stream().map(service -> (Callable<Void>) () -> {
      try {
        service.ensureCRANPackage(name);
      } catch (RServerException e) {
        log.warn("Failed to ensure R package is installed on {}: {}", service.getName(), name, e);
      }
      return null;
    }).collect(Collectors.toList()));
  }

  @Override
  public void installCRANPackage(String name) {
    invokeAll(rServerServices.stream().map(service -> (Callable<Void>) () -> {
      try {
        service.installCRANPackage(name);
      } catch (RServerException e) {
        log.warn("Failed to install R package from CRAN on {}: {}", service.getName(), name, e);
      }
      return null;
    }).collect(Collectors.toList()));
    eventBus.post(new RPackageInstalledEvent(getName(), name));
  }

  @Override
  public void installGitHubPackage(String name, String ref) {
    invokeAll(rServerServices.stream().map(service -> (Callable<Void>) () -> {
      try {
        service.installGitHubPackage(name, ref);
      } catch (RServerException e) {
        log.warn("Failed to install R package from GitHub on {}: {}", service.getName(), name, e);
      }
      return null;
    }).collect(Collectors.toList()));
    eventBus.post(new RPackageInstalledEvent(getName(), name));
  }

  @Override
  public void installBioconductorPackage(String name) {
    invokeAll(rServerServices.stream().map(service -> (Callable<Void>) () -> {
      try {
        service.installBioconductorPackage(name);
      } catch (RServerException e) {
        log.warn("Failed to install R package from Bioconductor on {}: {}", service.getName(), name, e);
      }
      return null;
    }).collect(Collectors.toList()));
    eventBus.post(new RPackageInstalledEvent(getName(), name));
  }

  @Override
  public void installLocalPackage(String path) {
    // syntax is /some/path/pkgName_xxx.tar.gz
    String[] segments = path.split("/");
    String[] tokens = segments[segments.length - 1].split("_");
    String pkgName = tokens[0];
    invokeAll(rServerServices.stream().map(service -> (Callable<Void>) () -> {
      try {
        service.installLocalPackage(path);
      } catch (RServerException e) {
        log.warn("Failed to install R package from local R package archive on {}: {}", service.getName(), path, e);
      }
      return null;
    }).collect(Collectors.toList()));
    eventBus.post(new RPackageInstalledEvent(getName(), pkgName));
  }

  @Override
  public void updateAllCRANPackages() {
    invokeAll(rServerServices.stream().map(service -> (Callable<Void>) () -> {
      try {
        service.updateAllCRANPackages();
      } catch (RServerException e) {
        log.warn("Failed to update all CRAN R packages on {}", service.getName(), e);
      }
      return null;
    }).collect(Collectors.toList()));
    eventBus.post(new RPackageInstalledEvent(getName()));
  }

  @Override
  public String[] getLog(Integer nbLines) {
    ExecutorService executor = Executors.newFixedThreadPool(rServerServices.size());
    try {
      List<Future<List<String>>> futureLogs = executor.invokeAll(rServerServices.stream().map(service -> (Callable<List<String>>) () -> {
        List<String> lines = Lists.newArrayList(String.format("[Info] %s R log start", service.getName()));
        try {
          lines.addAll(Arrays.asList(service.getLog(nbLines)));
        } catch (Exception e) {
          log.warn("Failed to retrieve R server log on {}", service.getName(), e);
          lines.add("[Error] Failed to retrieve R server log on " + service.getName());
        }
        lines.add(String.format("[Info] %s R log end", service.getName()));
        return lines;
      }).collect(Collectors.toList()));
      List<String> allLogs = Lists.newArrayList();
      futureLogs.forEach(listFuture -> {
        try {
          allLogs.addAll(listFuture.get());
        } catch (Exception e) {
          // ignore
        }
      });
      return allLogs.toArray(new String[0]);
    } catch (InterruptedException e) {
      log.error("Error while invoking all R servers", e);
      return new String[]{"[Error] Failed to retrieve R server logs"};
    }
  }

  //
  // Cluster methods
  //

  @Override
  public List<App> getApps() {
    return rServerServices.stream().map(RServerService::getApp).collect(Collectors.toList());
  }

  @Override
  public void start(App app) {
    rServerServices.stream().filter(s -> s.isFor(app)).findFirst().ifPresent(RServerService::start);
  }

  @Override
  public void stop(App app) {
    rServerServices.stream().filter(s -> s.isFor(app)).findFirst().ifPresent(RServerService::stop);
  }

  @Override
  public boolean isRunning(App app) {
    return rServerServices.stream().filter(s -> s.isFor(app)).findFirst()
        .map(RServerService::isRunning).orElse(false);
  }

  @Override
  public RServerState getState(App app) {
    return rServerServices.stream()
        .filter(s -> s.isFor(app))
        .map(s -> {
          try {
            return s.getState();
          } catch (RServerException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  //
  // Private methods
  //

  /**
   * Get the next R server service that is running and not too busy (count of R sessions and their business)
   *
   * @return
   */
  private RServerService getNextRServerService() {
    Optional<RServerState> state = rServerServices.stream().map(s -> {
      try {
        return s.getState();
      } catch (RServerException e) {
        return null;
      }
    })
        .filter(Objects::nonNull)
        .filter(RServerState::isRunning)
        .min(Comparator.comparingInt(RServerState::getRSessionsCount));
    if (state.isPresent())
      return getRServerService(state.get().getName());
    throw new NoSuchElementException("No R server is available in cluster: " + name);
  }

  private void invokeAll(List<Callable<Void>> callables) {
    ExecutorService executor = Executors.newFixedThreadPool(rServerServices.size());
    try {
      executor.invokeAll(callables);
    } catch (InterruptedException e) {
      log.error("Error while invoking all R servers", e);
    }
  }

  private RServerProfile asProfile() {
    return new RServerProfile() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public String getCluster() {
        return name;
      }
    };
  }

}
