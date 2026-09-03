/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Forwards events to nested appenders after rewriting their MDC map: keys can be renamed, dropped or
 * truncated. Nothing is modified in place, so appenders attached to the same logger but not nested
 * here - the datashield.log file appender in particular, whose output is parsed by external tools -
 * keep seeing the original keys and values.
 * <p/>
 * This exists because the OpenTelemetry logback appender reads MDC through
 * {@code ILoggingEvent#getMDCPropertyMap()} and offers no way to map keys onto the names OTel
 * semantic conventions expect.
 * <pre>
 * &lt;appender name="otelds" class="org.obiba.opal.core.logging.MdcRenamingAppender"&gt;
 *   &lt;rename&gt;ds_action=datashield.action&lt;/rename&gt;
 *   &lt;rename&gt;username=enduser.id&lt;/rename&gt;
 *   &lt;truncate&gt;ds_eval=512&lt;/truncate&gt;
 *   &lt;drop&gt;ds_eval&lt;/drop&gt;
 *   &lt;appender-ref ref="otelraw"/&gt;
 * &lt;/appender&gt;
 * </pre>
 */
public class MdcRenamingAppender extends UnsynchronizedAppenderBase<ILoggingEvent>
    implements AppenderAttachable<ILoggingEvent> {

  private final AppenderAttachableImpl<ILoggingEvent> aai = new AppenderAttachableImpl<>();

  private final Map<String, String> renames = new LinkedHashMap<>();

  private final Map<String, Integer> truncations = new LinkedHashMap<>();

  private final Set<String> drops = new LinkedHashSet<>();

  /**
   * {@code <rename>ds_action=datashield.action</rename>}
   */
  public void addRename(String spec) {
    int idx = spec.indexOf('=');
    if(idx < 1 || idx == spec.length() - 1) {
      addError("Invalid <rename>, expected 'mdcKey=attributeName' but got: " + spec);
      return;
    }
    renames.put(spec.substring(0, idx).trim(), spec.substring(idx + 1).trim());
  }

  /**
   * {@code <drop>ds_eval</drop>}: the key never reaches the nested appenders.
   */
  public void addDrop(String key) {
    drops.add(key.trim());
  }

  /**
   * {@code <truncate>ds_eval=512</truncate>}: cap the value length, applied before the rename.
   */
  public void addTruncate(String spec) {
    int idx = spec.indexOf('=');
    if(idx < 1) {
      addError("Invalid <truncate>, expected 'mdcKey=maxLength' but got: " + spec);
      return;
    }
    String key = spec.substring(0, idx).trim();
    try {
      int max = Integer.parseInt(spec.substring(idx + 1).trim());
      if(max < 1) throw new NumberFormatException();
      truncations.put(key, max);
    } catch(NumberFormatException e) {
      addError("Invalid <truncate> length for '" + key + "', expected a positive integer: " + spec);
    }
  }

  @Override
  public void start() {
    if(!aai.iteratorForAppenders().hasNext()) {
      addError("No appender-ref nested in <appender name=\"" + getName() + "\">, nothing to forward to.");
      return;
    }
    super.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    aai.appendLoopOnAppenders(new RewrittenMdcEvent(event, rewrite(event.getMDCPropertyMap())));
  }

  private Map<String, String> rewrite(Map<String, String> mdc) {
    Map<String, String> rewritten = new LinkedHashMap<>(mdc.size());
    for(Map.Entry<String, String> entry : mdc.entrySet()) {
      String key = entry.getKey();
      if(drops.contains(key)) continue;
      String value = entry.getValue();
      Integer max = truncations.get(key);
      if(max != null && value != null && value.length() > max) {
        value = value.substring(0, max) + "...";
      }
      rewritten.put(renames.getOrDefault(key, key), value);
    }
    return rewritten;
  }

  @Override
  public void stop() {
    if(!isStarted()) return;
    super.stop();
    aai.detachAndStopAllAppenders();
  }

  @Override
  public void addAppender(Appender<ILoggingEvent> newAppender) {
    aai.addAppender(newAppender);
  }

  @Override
  public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
    return aai.iteratorForAppenders();
  }

  @Override
  public Appender<ILoggingEvent> getAppender(String name) {
    return aai.getAppender(name);
  }

  @Override
  public boolean isAttached(Appender<ILoggingEvent> appender) {
    return aai.isAttached(appender);
  }

  @Override
  public void detachAndStopAllAppenders() {
    aai.detachAndStopAllAppenders();
  }

  @Override
  public boolean detachAppender(Appender<ILoggingEvent> appender) {
    return aai.detachAppender(appender);
  }

  @Override
  public boolean detachAppender(String name) {
    return aai.detachAppender(name);
  }
}
