/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

import java.util.*;

public class ResourceFactoryChooser extends Chooser {

  private Map<String, ResourceFactoryDto> resourceFactories;

  public void setResourceFactories(final Map<String, ResourceFactoryDto> resourceFactories) {
    this.resourceFactories = resourceFactories;
    clear();
    Map<String, List<String>> resourceFactoriesByGroup = Maps.newHashMap();
    for (String key : resourceFactories.keySet()) {
      ResourceFactoryDto factory = resourceFactories.get(key);
      String group = factory.hasGroup() ? factory.getGroup() : "Other";
      if (!resourceFactoriesByGroup.containsKey(group)) {
        resourceFactoriesByGroup.put(group, new ArrayList<String>());
      }
      resourceFactoriesByGroup.get(group).add(key);
    }

    String firstKey = null;
    List<String> groups = new ArrayList<String>(resourceFactoriesByGroup.keySet());
    Collections.sort(groups);
    for (String group : groups) {
      addGroup(group);
      List<String> factoryKeys = resourceFactoriesByGroup.get(group);
      Collections.sort(factoryKeys, new Comparator<String>() {
        @Override
        public int compare(String k1, String k2) {
          return resourceFactories.get(k1).getTitle().compareToIgnoreCase(resourceFactories.get(k2).getTitle());
        }
      });
      for (String key : factoryKeys) {
        ResourceFactoryDto factory = resourceFactories.get(key);
        if (firstKey == null)
          firstKey = key;
        addItemToGroup(factory.getTitle(), key);
      }
    }
    setSelectedValue(firstKey);
  }

  public Map<String, ResourceFactoryDto> getResourceFactories() {
    return resourceFactories;
  }

  public ResourceFactoryDto getSelectedFactory() {
    return resourceFactories.get(getSelectedValue());
  }

  public void setSelectedFactory(ResourceReferenceDto resource) {
    String key = resource.getProvider() + ":" + resource.getFactory();
    setSelectedValue(key);
  }
}
