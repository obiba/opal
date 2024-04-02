/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class SystemLogService implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(SystemLogService.class);

  private File logsDir;

  private static final String OPAL_LOG_FILE = "opal.log";

  private static final String DATASHIELD_LOG_FILE = "datashield.log";

  private static final String REST_LOG_FILE = "rest.log";

  private static final String SQL_LOG_FILE = "sql.log";

  private TailBroadcaster opalLogBroadcaster;

  @Override
  public void afterPropertiesSet() throws Exception {
    logsDir = new File(System.getenv().get("OPAL_LOG"));
    if (!logsDir.exists())
      logsDir = new File(System.getenv().get("OPAL_HOME") + File.separatorChar + "logs");
    this.opalLogBroadcaster = new TailBroadcaster();
    Tailer.builder().setFile(getOpalLogFile()).setTailerListener(opalLogBroadcaster).get();
  }

  public File getOpalLogFile() {
    return new File(logsDir, OPAL_LOG_FILE);
  }

  public List<File> getOpalLogFiles() {
    return getLogFiles("opal");
  }

  public File getDatashieldLogFile() {
    return new File(logsDir, DATASHIELD_LOG_FILE);
  }

  public List<File> getDatashieldLogFiles() {
    return getLogFiles("datashield");
  }

  public File getRestLogFile() {
    return new File(logsDir, REST_LOG_FILE);
  }

  public List<File> getRestLogFiles() {
    return getLogFiles("rest");
  }

  public File getSQLLogFile() {
    return new File(logsDir, SQL_LOG_FILE);
  }

  public List<File> getSQLLogFiles() {
    return getLogFiles("sql");
  }

  public void subscribeOpalLog(TailerListener tailer) {
    opalLogBroadcaster.register(tailer);
  }

  public void unSubscribeOpalLog(TailerListener tailer) {
    opalLogBroadcaster.unregister(tailer);
  }

  private List<File> getLogFiles(String prefix) {
    List<File> logs = Lists.newArrayList();
    File[] logFiles = logsDir.listFiles(f -> f.isFile() && f.getName().endsWith(".log") && f.getName().startsWith(prefix));
    if (logFiles != null) {
      Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));
      logs = Arrays.asList(logFiles);
    }
    return logs;
  }

  private static class TailBroadcaster extends TailerListenerAdapter {

    private final EvictingQueue<String> queue = EvictingQueue.create(1000);

    private final List<TailerListener> tailers = Lists.newArrayList();

    public void register(TailerListener tailer) {
      if (tailers.contains(tailer)) return;
      // welcome by sending last lines
      for (String line : queue) {
        tailer.handle(line);
      }
    }


    public void unregister(TailerListener tailer) {
      tailers.remove(tailer);
    }

    @Override
    public void handle(String line) {
      //System.out.println("tail: " + line);
      queue.add(line);
      for (TailerListener tailer : tailers) {
        tailer.handle(line);
      }
    }
  }
}
