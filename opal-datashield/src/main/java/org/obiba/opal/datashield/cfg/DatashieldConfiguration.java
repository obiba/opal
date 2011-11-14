/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield.cfg;

import java.util.List;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.datashield.DataShieldEnvironment;
import org.obiba.opal.datashield.DataShieldMethod;

import com.google.common.base.Preconditions;

public class DatashieldConfiguration implements OpalConfigurationExtension {

  public enum Level {
    RESTRICTED, UNRESTRICTED
  }

  public enum Environment {
    AGGREGATE, ASSIGN
  }

  private Level level;

  private List<DataShieldMethod> aggregatingMethods;

  private DataShieldEnvironment assign;

  private DataShieldEnvironment aggregate;

  public Level getLevel() {
    return level != null ? level : Level.RESTRICTED;
  }

  public DataShieldEnvironment getAggregateEnvironment() {
    return aggregate;
  }

  public DataShieldEnvironment getAssignEnvironment() {
    return assign;
  }

  public DataShieldEnvironment getEnvironment(Environment env) {
    Preconditions.checkArgument(env != null, "env cannot be null");
    switch(env) {
    case AGGREGATE:
      return getAggregateEnvironment();
    case ASSIGN:
      return getAssignEnvironment();
    }
    throw new IllegalArgumentException("Unknown environment " + env);
  }
}
