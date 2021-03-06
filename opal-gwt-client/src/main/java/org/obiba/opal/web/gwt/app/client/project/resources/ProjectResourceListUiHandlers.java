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

import com.gwtplatform.mvp.client.UiHandlers;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

import java.util.List;

public interface ProjectResourceListUiHandlers extends UiHandlers {

  void onAddResource();

  void onEditResource(ResourceReferenceDto resource);

  void onRemoveResource(ResourceReferenceDto resource);

  void onRemoveResources(List<ResourceReferenceDto> selectedItems);

  void onRefresh();

}
