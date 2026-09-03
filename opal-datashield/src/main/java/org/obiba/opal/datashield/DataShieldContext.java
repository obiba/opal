/*
 * Copyright (c) 2023 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.datashield;

import io.opentelemetry.context.Context;
import org.obiba.datashield.core.DSEnvironment;

import java.util.Map;

public class DataShieldContext {

  private final DSEnvironment environment;

  private final String rid;

  private final String profile;

  private final String rParserVersion;

  private final Map<String, String> contextMap;

  private final Context traceContext;

  public DataShieldContext(DSEnvironment environment, String rid, String profile, String rParserVersion, Map<String, String> contextMap) {
    this.environment = environment;
    this.rid = rid;
    this.profile = profile;
    this.rParserVersion = rParserVersion;
    this.contextMap = contextMap;
    // Captured here, on the request thread, for the same reason contextMap is: an asynchronous R
    // command runs on the session's consumer thread, where the request's trace context is gone.
    this.traceContext = Context.current();
  }

  public DSEnvironment getEnvironment() {
    return environment;
  }

  public String getRId() {
    return rid;
  }

  public String getProfile() {
    return profile;
  }

  public String getRParserVersion() {
    return rParserVersion;
  }

  public Map<String, String> getContextMap() {
    return contextMap;
  }

  /**
   * The trace context of the request that created this one, so that a span opened on the R execution
   * thread still hangs under the HTTP server span when one exists.
   */
  public Context getTraceContext() {
    return traceContext;
  }
}
