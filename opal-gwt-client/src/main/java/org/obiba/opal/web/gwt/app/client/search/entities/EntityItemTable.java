/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entities;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.model.client.search.ItemResultDto;

public class EntityItemTable extends Table<ItemResultDto> {

  public EntityItemTable() {
    initColumns(null, "Participant");
  }

  public void initialize(PlaceManager placeManager, String entityType) {
    while (getColumnCount()>0) {
      removeColumn(0);
    }
    initColumns(placeManager, entityType);
  }

  private void initColumns(PlaceManager placeManager, final String entityType) {
    setPageSize(Table.DEFAULT_PAGESIZE);
    setEmptyTableWidget(new InlineLabel(translations.noVariablesLabel()));

    if (placeManager != null) {
      addColumn(new EntityItemColumn(new EntityLinkCell(placeManager, entityType)), "ID");
    } else {
      addColumn(new TextColumn<ItemResultDto>() {
        @Override
        public String getValue(ItemResultDto item) {
          return item.getIdentifier();
        }
      }, "ID");
    }
    addColumn(new TextColumn<ItemResultDto>() {
      @Override
      public String getValue(ItemResultDto item) {
        return entityType;
      }
    }, translations.entityTypeLabel());
  }

  private static class EntityItemColumn extends Column<ItemResultDto, ItemResultDto> {
    private EntityItemColumn(Cell<ItemResultDto> cell) {
      super(cell);
    }

    @Override
    public ItemResultDto getValue(ItemResultDto item) {
      return item;
    }
  }

  private static class EntityLinkCell extends PlaceRequestCell<ItemResultDto> {

    private final String entityType;

    private EntityLinkCell(PlaceManager placeManager, String entityType) {
      super(placeManager);
      this.entityType = entityType;
    }

    @Override
    public PlaceRequest getPlaceRequest(ItemResultDto item) {
      return ProjectPlacesHelper.getEntityPlace(entityType, item.getIdentifier());
    }

    @Override
    public String getText(ItemResultDto item) {
      return item.getIdentifier();
    }
  }
}
