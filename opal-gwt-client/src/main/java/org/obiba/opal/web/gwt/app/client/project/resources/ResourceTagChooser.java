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

import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceTagDto;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResourceTagChooser extends Chooser {

  private List<ResourceTagDto> resourceTags;

  public void initialize(final List<ResourceTagDto> resourceTags) {
    this.resourceTags = resourceTags;
    clear();
    Collections.sort(resourceTags, new Comparator<ResourceTagDto>() {
      @Override
      public int compare(ResourceTagDto tag1, ResourceTagDto tag2) {
        return tag1.getTitle().compareTo(tag2.getTitle());
      }
    });
    for (ResourceTagDto tag : resourceTags) {
      addItem(tag.getTitle(), tag.getName());
    }
  }

  public List<ResourceTagDto> getResourceTags() {
    return resourceTags;
  }

  public ResourceTagDto getSelectedTag() {
    String selection = getSelectedValue();
    for (ResourceTagDto tag : resourceTags) {
      if (tag.getName().equals(selection))
        return tag;
    }
    return null;
  }

  public void setSelectedTag(ResourceFactoryDto factory) {
    String key = factory.getTags(0);
    setSelectedValue(key);
  }
}
