/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.datashield.cfg;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.datashield.core.*;
import org.obiba.datashield.core.impl.DefaultDSEnvironment;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.datashield.core.impl.DefaultDSOption;
import org.obiba.opal.core.domain.HasUniqueProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class DatashieldConfig implements DSConfiguration, HasUniqueProperties {

  private String profile;

  private final Map<DSMethodType, List<DefaultDSMethod>> environments = Maps.newHashMap();

  private final Map<String, String> options = Maps.newHashMap();

  public DatashieldConfig() {
  }

  public DatashieldConfig(String profile) {
    this.profile = profile;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  @Override
  public synchronized DSEnvironment getEnvironment(DSMethodType type) {
    if (!environments.containsKey(type))
      environments.put(type, new ArrayList<>());
    
    return new DefaultDSEnvironment(type, environments.get(type));
  }

  @Override
  public Iterable<DSOption> getOptions() {
    return this.options.keySet().stream().map(k -> new DefaultDSOption(k, this.options.get(k))).collect(Collectors.toList());
  }

  @Override
  public boolean hasOption(String name) {
    return this.options.containsKey(name);
  }

  @Override
  public boolean hasOptions() {
    return !this.options.isEmpty();
  }

  @Override
  public DSOption getOption(String name) {
    if (this.options.containsKey(name)) {
      return new DefaultDSOption(name, this.options.get(name));
    } else {
      throw new NoSuchElementException(name + " option does not exists");
    }
  }

  @Override
  public void addOrUpdateOption(String name, String value) {
    this.addOption(name, value, true);
  }

  @Override
  public void removeOption(String name) {
    if (this.hasOption(name)) {
      this.options.remove(name);
    }
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("profile");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(profile);
  }

  //
  // Private methods
  //

  private void addOption(String name, String value, boolean overwrite) {
    if (overwrite || !this.hasOption(name)) {
      this.options.put(name, value);
    }
  }
}
