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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;
import org.obiba.opal.web.model.client.opal.ResourceTagDto;

import java.util.*;

public class ResourceFactoryChooser extends Chooser {

  private Map<String, ResourceFactoryDto> resourceFactories;

  private List<ResourceTagDto> resourceTags;

  public void initialize(final Map<String, ResourceFactoryDto> resourceFactories, final List<ResourceTagDto> resourceTags, ResourceTagDto filter) {
    this.resourceFactories = resourceFactories;
    this.resourceTags = resourceTags;
    resetOptions(filter);
  }

  private void resetOptions(ResourceTagDto filter) {
    clear();
    Map<String, String> items = Maps.newHashMap();

    for (String key : resourceFactories.keySet()) {
      ResourceFactoryDto factory = resourceFactories.get(key);
      for (String tag : JsArrays.toIterable(factory.getTagsArray())) {
        if (tag.equals(filter.getName())) {
          items.put(factory.getTitle(), key);
        }
      }
    }
    List<String> titles = Lists.newArrayList(items.keySet());
    Collections.sort(titles);
    for (String title : titles) {
      addItem(title, items.get(title));
    }
    setSelectedIndex(0);
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

  public void applyTagFilter(ResourceTagDto selectedTag) {
    resetOptions(selectedTag);
  }
}
