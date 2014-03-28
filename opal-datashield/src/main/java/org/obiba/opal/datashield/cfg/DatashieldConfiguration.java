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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.datashield.DataShieldEnvironment;
import org.obiba.opal.datashield.DataShieldMethod;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class DatashieldConfiguration implements OpalConfigurationExtension, Serializable {

  public enum Level {
    RESTRICTED, UNRESTRICTED
  }

  public enum Environment {
    AGGREGATE, ASSIGN;

    public String symbol() {
      return "." + toString();
    }
  }

  private Level level;

  @SuppressWarnings("unused")
  @Deprecated
  // Used to allow successful deserialisation
  private List<DataShieldMethod> aggregatingMethods;

  private List<DataShieldEnvironment> environments;

  private Map<String, String> options = new HashMap<>();

  public Level getLevel() {
    return level == null ? Level.RESTRICTED : level;
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  public Iterable<Map.Entry<String, String>> getOptions() {
    return options.entrySet();
  }

  public String getOption(String name) {
    if (options.containsKey(name)) {
      return options.get(name);
    }

    throw new NoSuchElementException(name + " option does not exists");
  }

  public void addOption(String name, String value) {
    options.put(name, value);
  }

  public void updateOption(String name, String value) {
    if (!options.containsKey(name)) {
      throw new NoSuchElementException(name + " option does not exists");
    }

    options.put(name, value);
  }

  public void removeOption(String name) {
    if (!options.containsKey(name)) {
      throw new NoSuchElementException(name + " option does not exists");
    }

    options.remove(name);
  }

  public DataShieldEnvironment getAggregateEnvironment() {
    return getEnvironment(Environment.AGGREGATE);
  }

  public DataShieldEnvironment getAssignEnvironment() {
    return getEnvironment(Environment.ASSIGN);
  }

  public synchronized DataShieldEnvironment getEnvironment(Environment env) {
    Preconditions.checkArgument(env != null, "env cannot be null");
    for(DataShieldEnvironment environment : environments) {
      if(environment.getEnvironment() == env) return environment;
    }
    DataShieldEnvironment e = new DataShieldEnvironment(env, Lists.<DataShieldMethod>newArrayList());
    environments.add(e);
    return e;
  }

  private Object readResolve() throws ObjectStreamException {
    if(aggregatingMethods != null && aggregatingMethods.size() > 0) {
      environments.add(new DataShieldEnvironment(Environment.AGGREGATE, aggregatingMethods));
      aggregatingMethods = null;
    }
    return this;
  }

}
