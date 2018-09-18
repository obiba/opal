package org.obiba.opal.web.system.log;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Component
public class SystemLogService {

  private static final Logger log = LoggerFactory.getLogger(SystemLogService.class);

  private static final String LOGS_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "logs";

  private static final String OPAL_LOG = "opal.log";

  private static final String DATASHIELD_LOG = "datashield.log";

  private static final String REST_LOG = "rest.log";

  private TailBroadcaster opalLogBroadcaster;

  @PostConstruct
  public void initialize() {
    this.opalLogBroadcaster = new TailBroadcaster();
    Tailer.create(getOpalLogFile(), opalLogBroadcaster);
  }

  public File getOpalLogFile() {
    return new File(LOGS_DIR, OPAL_LOG);
  }

  public File getDatashieldLogFile() {
    return new File(LOGS_DIR, DATASHIELD_LOG);
  }

  public File getRestLogFile() {
    return new File(LOGS_DIR, REST_LOG);
  }

  public void subscribeOpalLog(TailerListener tailer) {
    opalLogBroadcaster.register(tailer);
  }

  public void unSubscribeOpalLog(TailerListener tailer) {
    opalLogBroadcaster.unregister(tailer);
  }

  private class TailBroadcaster extends TailerListenerAdapter {

    private EvictingQueue<String> queue = EvictingQueue.create(1000);

    private List<TailerListener> tailers = Lists.newArrayList();

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
