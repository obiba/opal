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
import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.r.service.RServerClusterService;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.r.service.RServerState;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.RServerException;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
  }

  /**
   * Dispatch R server stop.
   */
  @Override
  public void stop() {
    rServerServices.forEach(RServerService::stop);
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
    rServerServices.stream().map(RServerService::getState).forEach(s -> {
      state.setVersion(s.getVersion());
      if (!state.isRunning())
        state.setRunning(state.isRunning());
      state.addTags(s.getTags());
      state.addRSessionsCount(s.getRSessionsCount());
      state.addBusyRSessionsCount(s.getBusyRSessionsCount());
    });
    return state;
  }

  @Override
  public RServerSession newRServerSession(String user) throws RServerException {
    return getNextRServerService().newRServerSession(user);
  }

  @Override
  public void execute(ROperation rop) throws RServerException {
    getNextRServerService().execute(rop);
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
    return getNextRServerService().getInstalledPackagesDtos();
  }

  @Override
  public OpalR.RPackageDto getInstalledPackageDto(String name) {
    return getNextRServerService().getInstalledPackageDto(name);
  }

  @Override
  public List<String> getInstalledDataSHIELDPackageNames() {
    return getNextRServerService().getInstalledDataSHIELDPackageNames();
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
    return rServerServices.stream().filter(s -> s.isFor(app)).findFirst()
        .map(RServerService::getState).orElse(null);
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
    Optional<RServerState> state = rServerServices.stream().map(RServerService::getState)
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

}
