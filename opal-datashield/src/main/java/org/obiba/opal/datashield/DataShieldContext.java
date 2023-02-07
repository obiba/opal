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

import org.obiba.datashield.core.DSEnvironment;

public class DataShieldContext {

  private final DSEnvironment environment;

  private final String profile;
  private final String rParserVersion;
  private final String clientIP;

  public DataShieldContext(DSEnvironment environment, String profile, String rParserVersion, String clientIP) {
    this.environment = environment;
    this.profile = profile;
    this.rParserVersion = rParserVersion;
    this.clientIP = clientIP;
  }

  public DSEnvironment getEnvironment() {
    return environment;
  }

  public String getProfile() {
    return profile;
  }

  public String getRParserVersion() {
    return rParserVersion;
  }

  public String getClientIP() {
    return clientIP;
  }
}
