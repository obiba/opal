/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.resources;

import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.model.client.opal.ResourceCategoryDto;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResourceCategoryChooser extends Chooser {

  private List<ResourceCategoryDto> resourceCategories;

  public void initialize(final List<ResourceCategoryDto> resourceCategories) {
    this.resourceCategories = resourceCategories;
    clear();
    Collections.sort(resourceCategories, new Comparator<ResourceCategoryDto>() {
      @Override
      public int compare(ResourceCategoryDto tag1, ResourceCategoryDto tag2) {
        return tag1.getTitle().compareTo(tag2.getTitle());
      }
    });
    for (ResourceCategoryDto tag : resourceCategories) {
      addItem(tag.getTitle(), tag.getName());
    }
  }

  public ResourceCategoryDto getSelectedCategory() {
    String selection = getSelectedValue();
    for (ResourceCategoryDto tag : resourceCategories) {
      if (tag.getName().equals(selection))
        return tag;
    }
    return null;
  }

  public void setSelectedCategory(ResourceFactoryDto factory) {
    String key = factory.getTags(0);
    setSelectedValue(key);
  }
}
