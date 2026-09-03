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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An {@link ILoggingEvent} that delegates everything to the event it wraps, except for the MDC map
 * which is replaced by an already rewritten one. Downstream appenders see the new keys; appenders
 * attached upstream of {@link MdcRenamingAppender} keep seeing the original event.
 */
class RewrittenMdcEvent implements ILoggingEvent {

  private final ILoggingEvent delegate;

  private final Map<String, String> mdc;

  RewrittenMdcEvent(ILoggingEvent delegate, Map<String, String> mdc) {
    this.delegate = delegate;
    this.mdc = Collections.unmodifiableMap(mdc);
  }

  @Override
  public Map<String, String> getMDCPropertyMap() {
    return mdc;
  }

  /**
   * @deprecated kept because {@link ILoggingEvent} still declares it; same map as
   * {@link #getMDCPropertyMap()}.
   */
  @Deprecated
  @Override
  public Map<String, String> getMdc() {
    return mdc;
  }

  @Override
  public String getThreadName() {
    return delegate.getThreadName();
  }

  @Override
  public Level getLevel() {
    return delegate.getLevel();
  }

  @Override
  public String getMessage() {
    return delegate.getMessage();
  }

  @Override
  public Object[] getArgumentArray() {
    return delegate.getArgumentArray();
  }

  @Override
  public String getFormattedMessage() {
    return delegate.getFormattedMessage();
  }

  @Override
  public String getLoggerName() {
    return delegate.getLoggerName();
  }

  @Override
  public LoggerContextVO getLoggerContextVO() {
    return delegate.getLoggerContextVO();
  }

  @Override
  public IThrowableProxy getThrowableProxy() {
    return delegate.getThrowableProxy();
  }

  @Override
  public StackTraceElement[] getCallerData() {
    return delegate.getCallerData();
  }

  @Override
  public boolean hasCallerData() {
    return delegate.hasCallerData();
  }

  @Override
  public Marker getMarker() {
    return delegate.getMarker();
  }

  @Override
  public List<Marker> getMarkerList() {
    return delegate.getMarkerList();
  }

  @Override
  public long getTimeStamp() {
    return delegate.getTimeStamp();
  }

  @Override
  public int getNanoseconds() {
    return delegate.getNanoseconds();
  }

  @Override
  public Instant getInstant() {
    return delegate.getInstant();
  }

  @Override
  public long getSequenceNumber() {
    return delegate.getSequenceNumber();
  }

  @Override
  public List<KeyValuePair> getKeyValuePairs() {
    return delegate.getKeyValuePairs();
  }

  @Override
  public void prepareForDeferredProcessing() {
    delegate.prepareForDeferredProcessing();
  }
}
