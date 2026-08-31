/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.storage;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import jakarta.annotation.Nullable;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.event.DiskLevelChangedEvent;
import org.obiba.opal.core.service.storage.DiskLevel;
import org.obiba.opal.core.service.storage.DiskSpaceService;
import org.obiba.opal.core.service.storage.DiskStatus;
import org.obiba.opal.core.service.storage.InsufficientStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * Samples the free space of the volumes Opal writes to, on a timer, and answers from the last sample.
 * <p>
 * Reading is cheap but not free, and the callers that ask are on request threads: the {@code stat} calls happen on the
 * sampler thread and everything else reads a volatile field.
 * <p>
 * The folders are resolved on every sample rather than once at startup. Several of them do not exist on a fresh
 * installation, the Opal file system root is only known once the configuration has been read, and a volume can be
 * mounted under a running server. When a folder is missing, the nearest existing ancestor is measured instead: that is
 * the volume the folder would be created on.
 */
@Component
public class DefaultDiskSpaceService implements DiskSpaceService {

  private static final Logger log = LoggerFactory.getLogger(DefaultDiskSpaceService.class);

  @Value("${OPAL_HOME}/data")
  private File dataFolder;

  @Value("${OPAL_HOME}/data/config")
  private File configFolder;

  @Value("${OPAL_HOME}/data/h2")
  private File h2Root;

  @Value("${OPAL_HOME}/logs")
  private File logsFolder;

  @Value("${org.obiba.opal.storage.disk.interval}")
  private long interval;

  @Value("${org.obiba.opal.storage.disk.enforce}")
  private boolean enforce;

  @Value("${org.obiba.opal.storage.disk.warn.bytes}")
  private long warnBytes;

  @Value("${org.obiba.opal.storage.disk.degraded.bytes}")
  private long degradedBytes;

  @Value("${org.obiba.opal.storage.disk.critical.bytes}")
  private long criticalBytes;

  /**
   * How much more room than the announced size a write is asked to leave. An upload is written through a temporary
   * file and a multipart envelope is bigger than its payload, so the announced length is a lower bound.
   */
  @Value("${org.obiba.opal.storage.disk.upload.safetyFactor}")
  private double safetyFactor;

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  @Autowired
  private StorageTaskScheduler scheduler;

  @Autowired
  private EventBus eventBus;

  private volatile List<Volume> volumes = ImmutableList.of();

  private volatile DiskLevel level = DiskLevel.UNKNOWN;

  private ScheduledFuture<?> sampler;

  /**
   * Paths that could not be measured, so that a volume Opal cannot see is reported once and not once a minute.
   */
  private final Set<String> unreadable = Collections.synchronizedSet(new HashSet<>());

  /**
   * Volumes too small to ever satisfy the thresholds, reported once each.
   */
  private final Set<String> oversizedThreshold = Collections.synchronizedSet(new HashSet<>());

  @Override
  public void start() {
    sample();
    if(interval <= 0) {
      log.info("Disk space monitoring is disabled (org.obiba.opal.storage.disk.interval={})", interval);
      return;
    }
    sampler = scheduler.scheduleWithFixedDelay(this::sample, Duration.ofMillis(interval));
    log.info("Disk space monitoring every {} ms, enforcement is {}", interval, enforce ? "on" : "off");
  }

  @Override
  public void stop() {
    if(sampler != null) {
      sampler.cancel(false);
      sampler = null;
    }
  }

  @Override
  public DiskLevel getLevel() {
    return level;
  }

  @Override
  public List<DiskStatus> getStatuses() {
    ImmutableList.Builder<DiskStatus> builder = ImmutableList.builder();
    for(Volume volume : volumes) {
      builder.add(volume.status);
    }
    return builder.build();
  }

  @Nullable
  @Override
  public DiskStatus getStatus(File path) {
    Volume volume = findVolume(path);
    return volume == null ? null : volume.status;
  }

  @Override
  public boolean isEnforced() {
    return enforce;
  }

  @Override
  public void checkWritable() throws InsufficientStorageException {
    DiskLevel current = level;
    if(!enforce || !current.isAtLeast(DiskLevel.DEGRADED)) return;
    throw new InsufficientStorageException(
        "Not enough free disk space to start this operation: " + describeWorstVolume() +
            ". Free some space, or remove data that is no longer needed, and try again.");
  }

  @Override
  public void checkWritable(File path, long requiredBytes) throws InsufficientStorageException {
    if(!enforce) return;
    checkWritable();

    Volume volume = findVolume(path);
    if(volume == null || volume.status.getLevel() == DiskLevel.UNKNOWN) return;

    long needed = withSafetyMargin(requiredBytes);
    // What the write must leave behind it, and not only what it takes: the DEGRADED floor is what lets the databases
    // compact and close cleanly, so a write is refused when it would eat into the floor rather than when it runs out.
    if(volume.status.getUsableSpace() - needed >= degradedBytes) return;

    throw new InsufficientStorageException(
        "Not enough free disk space on " + volume.status.getPath() + " to write " + needed + " bytes: " +
            volume.status.getUsableSpace() + " bytes free, of which " + degradedBytes + " are reserved.");
  }

  @Override
  public void checkFileSystemWritable(long requiredBytes) throws InsufficientStorageException {
    File root = fileSystemRoot();
    if(root == null) {
      checkWritable();
      return;
    }
    checkWritable(root, requiredBytes);
  }

  //
  // Private methods
  //

  private long withSafetyMargin(long requiredBytes) {
    if(requiredBytes <= 0) return 0;
    double margin = safetyFactor < 1 ? 1 : safetyFactor;
    double needed = requiredBytes * margin;
    return needed >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) needed;
  }

  /**
   * Take one reading of every watched volume and publish it as a whole, so that a caller never sees half of a sample.
   */
  private void sample() {
    try {
      List<Volume> sampled = readVolumes();
      DiskLevel worst = DiskLevel.UNKNOWN;
      for(Volume volume : sampled) {
        worst = worst.worst(volume.status.getLevel());
      }
      DiskLevel previous = level;
      volumes = sampled;
      level = worst;
      checkThresholdsFit(sampled);
      report(previous, worst);
    } catch(RuntimeException e) {
      // The checker is a safety net, and a safety net that throws on the scheduler thread stops being one.
      log.warn("Disk space sampling failed: {}", e.getMessage());
    }
  }

  /**
   * Log a transition, not a state. A full disk stays full, and a level triggered message would say so every minute.
   */
  private void report(DiskLevel previous, DiskLevel current) {
    if(previous == current) return;
    String detail = describeWorstVolume();
    if(current.isAtLeast(DiskLevel.DEGRADED)) {
      log.error("Free disk space is {}: {}. {}", current, detail,
          enforce ? "Imports, copies, backups and uploads are being refused." : "Enforcement is off, nothing is refused.");
    } else if(current == DiskLevel.WARN) {
      log.warn("Free disk space is low: {}", detail);
    } else if(current == DiskLevel.OK && previous.isAtLeast(DiskLevel.WARN)) {
      log.info("Free disk space is back to normal: {}", detail);
    }
    eventBus.post(new DiskLevelChangedEvent(previous, current, detail));
  }

  /**
   * A threshold larger than the volume it applies to holds that level on however empty the volume is. That is a
   * configuration error rather than a reading worth acting upon, and saying so once is more use to an administrator
   * than silently clamping the threshold to something they did not ask for. Only the WARN threshold is checked, being
   * the largest of the three. Done here rather than at start(), since the volumes are resolved on every sample and
   * the Opal file system root only appears once the configuration has been read.
   */
  private void checkThresholdsFit(List<Volume> sampled) {
    for(Volume volume : sampled) {
      DiskStatus status = volume.status;
      if(status.getTotalSpace() <= 0 || status.getTotalSpace() > warnBytes) continue;
      if(oversizedThreshold.add(status.getPath())) {
        log.warn("Volume {} ({}) holds {} bytes in total, less than the {} bytes of the WARN threshold: it can never " +
                "report OK. Lower org.obiba.opal.storage.disk.warn.bytes, or give Opal a larger volume.",
            status.getName(), status.getPath(), status.getTotalSpace(), warnBytes);
      }
    }
  }

  private String describeWorstVolume() {
    Volume worst = null;
    for(Volume volume : volumes) {
      if(worst == null || volume.status.getLevel().isWorseThan(worst.status.getLevel())) {
        worst = volume;
      }
    }
    return worst == null ? "no volume could be measured" : worst.status.toString();
  }

  private List<Volume> readVolumes() {
    // Registration order decides which folder names a volume: the folders that hold data first, since that is the one
    // an administrator needs to see. Folders that share a mount, which is the usual case, are collapsed into one.
    Map<String, File> watched = new LinkedHashMap<>();
    watched.put("data", dataFolder);
    watched.put("config", configFolder);
    watched.put("h2", h2Root);
    File root = fileSystemRoot();
    if(root != null) watched.put("filesystem", root);
    watched.put("logs", logsFolder);
    watched.put("tmp", new File(System.getProperty("java.io.tmpdir")));

    Map<FileStore, Sample> byStore = new LinkedHashMap<>();
    for(Map.Entry<String, File> entry : watched.entrySet()) {
      Path path = nearestExisting(entry.getValue());
      if(path == null) continue;
      try {
        FileStore store = Files.getFileStore(path);
        Sample sample = byStore.get(store);
        if(sample == null) {
          byStore.put(store, new Sample(entry.getKey(), path, store));
        } else {
          sample.names.add(entry.getKey());
        }
        unreadable.remove(entry.getValue().getAbsolutePath());
      } catch(IOException | RuntimeException e) {
        logUnreadable(entry.getValue(), e);
      }
    }

    ImmutableList.Builder<Volume> builder = ImmutableList.builder();
    for(Sample sample : byStore.values()) {
      builder.add(sample.toVolume());
    }
    return builder.build();
  }

  private void logUnreadable(File file, Exception e) {
    if(unreadable.add(file.getAbsolutePath())) {
      log.warn("Cannot read the free disk space of {}: {}", file.getAbsolutePath(), e.getMessage());
    }
  }

  /**
   * The volume a folder is on, whether or not the folder itself is there yet.
   */
  @Nullable
  private Path nearestExisting(@Nullable File file) {
    for(File current = file; current != null; current = current.getParentFile()) {
      if(current.exists()) return current.toPath();
    }
    return null;
  }

  @Nullable
  private File fileSystemRoot() {
    try {
      String root = opalConfigurationService.getOpalConfiguration().getFileSystemRoot();
      return Strings.isNullOrEmpty(root) ? null : new File(root);
    } catch(RuntimeException e) {
      // Not yet readable on a first startup, and it will be on the next sample.
      return null;
    }
  }

  @Nullable
  private Volume findVolume(@Nullable File path) {
    Path existing = nearestExisting(path);
    if(existing == null) return null;
    try {
      FileStore store = Files.getFileStore(existing);
      for(Volume volume : volumes) {
        if(store.equals(volume.store)) return volume;
      }
    } catch(IOException | RuntimeException e) {
      logUnreadable(path, e);
    }
    return null;
  }

  /**
   * The thresholds are absolute sizes and deliberately not a share of the volume. A level answers whether there is
   * room left for Opal to do its work, and what has to fit — an import, a compaction, a clean close — does not need
   * more room on a larger disk. A percentage gets the answer more wrong the bigger the volume gets: 15% of a 468 GB
   * volume asks for 70 GB of headroom that nothing is ever going to use.
   */
  DiskLevel levelOf(long total, long usable) {
    if(total <= 0 || usable < 0) return DiskLevel.UNKNOWN;
    if(usable < criticalBytes) return DiskLevel.CRITICAL;
    if(usable < degradedBytes) return DiskLevel.DEGRADED;
    if(usable < warnBytes) return DiskLevel.WARN;
    return DiskLevel.OK;
  }

  //
  // Inner classes
  //

  private class Sample {

    private final List<String> names = new ArrayList<>();

    private final Path path;

    private final FileStore store;

    private Sample(String name, Path path, FileStore store) {
      this.names.add(name);
      this.path = path;
      this.store = store;
    }

    private Volume toVolume() {
      long total = -1;
      long usable = -1;
      try {
        total = store.getTotalSpace();
        usable = store.getUsableSpace();
      } catch(IOException | RuntimeException e) {
        logUnreadable(path.toFile(), e);
      }
      DiskStatus status = new DiskStatus(Joiner.on(", ").join(names), path.toAbsolutePath().toString(), total, usable,
          levelOf(total, usable));
      return new Volume(store, status);
    }
  }

  private static class Volume {

    private final FileStore store;

    private final DiskStatus status;

    private Volume(FileStore store, DiskStatus status) {
      this.store = store;
      this.status = status;
    }
  }
}
